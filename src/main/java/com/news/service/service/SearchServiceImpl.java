package com.news.service.service;

import com.news.service.config.AppConfig;
import com.news.service.model.SearchRequest;
import com.news.service.model.SearchResult;
import com.news.service.model.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Comparator;

/**
 * 搜索服务实现类
 */
@Slf4j
@Service
public class SearchServiceImpl implements SearchService {
    
    private JdbcTemplate jdbcTemplate;
    private final AppConfig appConfig;
    
    @Autowired
    public SearchServiceImpl(AppConfig appConfig) {
        this.appConfig = appConfig;
    }
    
    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        log.info("JdbcTemplate {}已注入", jdbcTemplate != null ? "" : "未");
    }
    
    @Override
    public PageResult<SearchResult> search(SearchRequest request) {
        log.info("====> 开始执行搜索方法: keywords={}, page={}, size={}, sortField={}, sortDirection={}", 
                request.getKeywords(), request.getPage(), request.getSize(),
                request.getSortField(), request.getSortDirection());
        
        if (!isAvailable()) {
            log.warn("搜索服务不可用: {}", getUnavailableReason());
            return PageResult.of(new ArrayList<>(), request.getPage(), request.getSize(), 0);
        }
        
        try {
            // 首先按逗号分隔不同的搜索条件组
            log.debug("解析关键词: {}", request.getKeywords());
            String[] keywordGroups = request.getKeywords().split(",");
            
            if (keywordGroups.length == 0 || (keywordGroups.length == 1 && keywordGroups[0].trim().isEmpty())) {
                log.warn("关键词为空，返回空结果");
                return PageResult.of(new ArrayList<>(), request.getPage(), request.getSize(), 0);
            }
            
            // 构建复杂的搜索条件
            List<String> conditionGroups = new ArrayList<>();
            List<Object> params = new ArrayList<>();
            
            // 收集所有用于权重计算和高亮显示的关键词
            List<String> allKeywords = new ArrayList<>();
            
            for (String group : keywordGroups) {
                String trimmedGroup = group.trim();
                if (trimmedGroup.isEmpty()) {
                    continue;
                }
                
                // 检查组内是否有OR关系
                String[] orTerms = trimmedGroup.split("(?i)\\s+or\\s+");
                
                if (orTerms.length > 1) {
                    // OR关系的关键词组
                    List<String> orConditions = new ArrayList<>();
                    
                    for (String orTerm : orTerms) {
                        String trimmedTerm = orTerm.trim();
                        if (!trimmedTerm.isEmpty()) {
                            processKeywordTerm(trimmedTerm, orConditions, params, allKeywords);
                        }
                    }
                    
                    if (!orConditions.isEmpty()) {
                        conditionGroups.add("(" + String.join(" OR ", orConditions) + ")");
                    }
                } else {
                    // 普通AND关系的关键词组
                    processKeywordTerm(trimmedGroup, conditionGroups, params, allKeywords);
                }
            }
            
            log.debug("构建的条件组: {}", conditionGroups);
            log.debug("构建的参数列表: {}", params);
            log.debug("所有关键词: {}", allKeywords);
            
            if (conditionGroups.isEmpty()) {
                log.warn("无有效搜索条件，返回空结果");
                return PageResult.of(new ArrayList<>(), request.getPage(), request.getSize(), 0);
            }
            
            // 构建WHERE子句
            String whereClause = String.join(" AND ", conditionGroups);
            
            // 构建COUNT查询 - 修正表名
            String countSql = "SELECT COUNT(*) FROM cob_program p JOIN com_basicinfo b ON p.objectid = b.id WHERE " + whereClause;
            
            // 获取总记录数
            log.debug("执行记录数计数查询: {}", countSql);
            Long totalCount = jdbcTemplate.queryForObject(countSql, Long.class, params.toArray());
            long total = (totalCount != null) ? totalCount : 0;
            log.debug("总记录数: {}", total);
            
            // 构建完整SQL查询 - 修正表名和字段名
            String sql = "SELECT b.ID, b.NAME, p.FIELD1079 as xml_content, b.CREATED FROM cob_program p JOIN com_basicinfo b ON p.objectid = b.id WHERE " + whereClause;
            
            // 执行查询
            log.info("执行数据库查询...");
            List<Object> queryParams = new ArrayList<>(params);
            List<SearchResult> results = jdbcTemplate.query(sql, rs -> {
                List<SearchResult> queryResults = new ArrayList<>();
                while (rs.next()) {
                    SearchResult result = mapRowToSearchResult(rs, rs.getRow());
                    
                    // 计算权重分数 - 确保每条结果都计算分数
                    double score = calculateScore(result, allKeywords);
                    result.setScore(score);
                    log.debug("为结果 [{}] 计算分数: {}", result.getId(), score);
                    
                    // 高亮显示命中的关键词
                    String highlightedContent = highlightKeywords(result.getContent(), allKeywords);
                    result.setHighlightedContent(highlightedContent);
                    
                    queryResults.add(result);
                }
                return queryResults;
            }, queryParams.toArray());
            
            log.info("查询完成，获取到 {} 条结果", results.size());
            
            // 根据排序字段和排序方向进行排序
            if (results.isEmpty()) {
                log.warn("没有匹配的搜索结果");
            } else {
                // 检查每个结果是否有分数
                for (SearchResult result : results) {
                    if (result.getScore() == 0.0) {
                        log.warn("结果 [{}] 没有计算分数，应用默认分数0.1", result.getId());
                        result.setScore(0.1); // 设置最小分数，确保前端显示
                    }
                }
                
                // 根据排序参数排序
                String sortField = request.getSortField();
                String sortDirection = request.getSortDirection();
                
                log.debug("排序字段: {}, 排序方向: {}", sortField, sortDirection);
                
                if ("NAME".equalsIgnoreCase(sortField)) {
                    // 按名称排序
                    if ("asc".equalsIgnoreCase(sortDirection)) {
                        results.sort(Comparator.comparing(SearchResult::getName, Comparator.nullsLast(String::compareTo)));
                    } else {
                        results.sort(Comparator.comparing(SearchResult::getName, Comparator.nullsLast(String::compareTo)).reversed());
                    }
                    log.debug("按名称{}排序完成", sortDirection);
                } else if ("CREATED".equalsIgnoreCase(sortField)) {
                    // 按创建时间排序
                    if ("asc".equalsIgnoreCase(sortDirection)) {
                        results.sort(Comparator.comparing(SearchResult::getCreated, Comparator.nullsLast(Date::compareTo)));
                    } else {
                        results.sort(Comparator.comparing(SearchResult::getCreated, Comparator.nullsLast(Date::compareTo)).reversed());
                    }
                    log.debug("按创建时间{}排序完成", sortDirection);
                } else {
                    // 默认按分数排序
                    if ("asc".equalsIgnoreCase(sortDirection)) {
                        results.sort(Comparator.comparing(SearchResult::getScore));
                    } else {
                        results.sort(Comparator.comparing(SearchResult::getScore).reversed());
                    }
                    log.debug("按分数{}排序完成", sortDirection);
                }
            }
            
            // 应用分页逻辑
            int from = request.getPage() * request.getSize();
            int to = Math.min(from + request.getSize(), results.size());
            
            if (from >= results.size()) {
                log.warn("请求的页码超出范围，返回空结果");
                return PageResult.of(new ArrayList<>(), request.getPage(), request.getSize(), total);
            }
            
            List<SearchResult> pagedResults = results.subList(from, to);
            
            // 记录结果
            if (pagedResults.isEmpty()) {
                log.warn("搜索未找到匹配结果");
            } else {
                // 修改日志输出，避免输出完整结果内容
                if (!pagedResults.isEmpty()) {
                    SearchResult firstResult = pagedResults.get(0);
                    log.debug("搜索结果第一条: ID={}, 标题={}, 分数={}, 内容长度={}", 
                             firstResult.getId(), 
                             firstResult.getName(),
                             firstResult.getScore(),
                             firstResult.getContent() != null ? firstResult.getContent().length() : 0);
                }
                log.debug("搜索结果ID和分数列表: {}", 
                         pagedResults.stream()
                            .map(r -> String.format("%s:%.2f", r.getId(), r.getScore()))
                            .collect(Collectors.toList()));
            }
            
            log.info("<==== 结束执行搜索方法，共返回 {} 条结果", pagedResults.size());
            return PageResult.of(pagedResults, request.getPage(), request.getSize(), total);
        } catch (Exception e) {
            log.error("搜索失败: {}", e.getMessage(), e);
            return PageResult.of(new ArrayList<>(), request.getPage(), request.getSize(), 0);
        }
    }
    
    @Override
    public long count(String keywords) {
        log.info("====> 开始执行计数方法: keywords={}", keywords);
        
        if (!isAvailable()) {
            log.warn("搜索服务不可用: {}", getUnavailableReason());
            return 0;
        }
        
        try {
            // 首先按逗号分隔不同的搜索条件组
            log.debug("解析关键词: {}", keywords);
            String[] keywordGroups = keywords.split(",");
            
            if (keywordGroups.length == 0 || (keywordGroups.length == 1 && keywordGroups[0].trim().isEmpty())) {
                log.warn("关键词为空，返回计数0");
                return 0;
            }
            
            // 构建复杂的搜索条件
            List<String> conditionGroups = new ArrayList<>();
            List<Object> params = new ArrayList<>();
            
            for (String group : keywordGroups) {
                group = group.trim();
                if (group.isEmpty()) continue;
                
                if (group.toLowerCase().contains(" or ")) {
                    // 处理OR关系的关键词
                    String[] orKeywords = group.split("(?i)\\s+or\\s+");
                    List<String> orConditions = new ArrayList<>();
                    
                    for (String keyword : orKeywords) {
                        keyword = keyword.trim();
                        if (!keyword.isEmpty()) {
                            orConditions.add("(UPPER(b.NAME) LIKE UPPER(?) OR UPPER(p.FIELD1079) LIKE UPPER(?))");
                            params.add("%" + keyword + "%");
                            params.add("%" + keyword + "%");
                        }
                    }
                    
                    if (!orConditions.isEmpty()) {
                        conditionGroups.add("(" + String.join(" OR ", orConditions) + ")");
                    }
                } else if (group.contains(" ")) {
                    // 处理AND关系的关键词(空格分隔)
                    String[] andKeywords = group.split("\\s+");
                    List<String> andConditions = new ArrayList<>();
                    
                    for (String keyword : andKeywords) {
                        keyword = keyword.trim();
                        if (!keyword.isEmpty()) {
                            andConditions.add("(UPPER(b.NAME) LIKE UPPER(?) OR UPPER(p.FIELD1079) LIKE UPPER(?))");
                            params.add("%" + keyword + "%");
                            params.add("%" + keyword + "%");
                        }
                    }
                    
                    if (!andConditions.isEmpty()) {
                        conditionGroups.add("(" + String.join(" AND ", andConditions) + ")");
                    }
                } else {
                    // 处理单个关键词
                    conditionGroups.add("(UPPER(b.NAME) LIKE UPPER(?) OR UPPER(p.FIELD1079) LIKE UPPER(?))");
                    params.add("%" + group + "%");
                    params.add("%" + group + "%");
                }
            }
            
            // 如果没有有效的条件，返回0
            if (conditionGroups.isEmpty()) {
                log.warn("处理后无有效关键词，返回计数0");
                return 0;
            }
            
            // 将所有条件组以AND连接（保持与search方法一致）
            String whereClause = String.join(" AND ", conditionGroups);
            log.debug("构建的完整搜索条件: {}", whereClause);
            
            // 构建SQL查询，确保与search方法使用相同的表结构
            String sql = "SELECT COUNT(*) FROM cob_program p JOIN com_basicinfo b ON p.objectid = b.id WHERE " + whereClause;
            
            log.debug("构建的计数SQL查询: {}", sql);
            log.debug("计数查询参数: {}", params);
            
            // 执行查询
            log.info("执行计数查询...");
            Long count = jdbcTemplate.queryForObject(sql, Long.class, params.toArray());
            log.info("计数查询完成，总共匹配 {} 条结果", count);
            
            log.info("<==== 结束执行计数方法，计数结果: {}", count);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("获取搜索结果数量失败: {}", e.getMessage(), e);
            return 0;
        }
    }
    
    @Override
    public boolean isAvailable() {
        boolean available = appConfig.isDataSourceEnabled();
        log.debug("检查搜索服务是否可用: {}", available);
        return available;
    }

    /**
     * 获取服务不可用的原因
     */
    private String getUnavailableReason() {
        if (!appConfig.isDataSourceEnabled()) {
            return "数据库未启用，请在配置页面启用并配置数据库";
        }
        return "未知原因";
    }
    
    /**
     * 将数据库行映射为搜索结果对象，并解析XML提取content字段
     */
    private SearchResult mapRowToSearchResult(ResultSet rs, int rowNum) throws SQLException {
        String id = rs.getString("ID");
        log.debug("开始映射结果行: rowNum={}, id={}", rowNum, id);
        
        String xmlContent = rs.getString("xml_content");
        String name = rs.getString("NAME");
        Date created = new Date(rs.getTimestamp("CREATED").getTime());
        
        // 不输出XML内容，只输出长度
        log.debug("行数据: id={}, name={}, created={}, xmlContent长度={}", 
                 id, name, created, xmlContent != null ? xmlContent.length() : 0);
        
        String extractedContent = extractContentFromXml(xmlContent);
        
        // 修改为只在日志中显示内容的前50个字符
        log.debug("从XML中提取的内容，长度: {}, 前50字符: {}", 
                extractedContent.length(),
                extractedContent.length() > 50 ? extractedContent.substring(0, 50) + "..." : extractedContent);
        
        SearchResult result = new SearchResult();
        result.setId(id);
        result.setName(name);
        result.setCreated(created);
        result.setContent(extractedContent);
        return result;
    }
    
    /**
     * 从XML中提取Content字段内容
     * 
     * @param xmlContent XML内容
     * @return 提取的Content内容，如果解析失败则返回提示信息
     */
    private String extractContentFromXml(String xmlContent) {
        if (xmlContent == null || xmlContent.trim().isEmpty()) {
            log.debug("XML内容为空");
            return "没有提取到内容";
        }
        
        log.debug("开始解析XML内容，长度: {}", xmlContent.length());
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xmlContent)));
            
            NodeList nodes = document.getElementsByTagName("Content");
            int nodesCount = nodes.getLength();
            
            if (nodesCount > 0) {
                log.debug("找到 <Content> 标签 {} 个", nodesCount);
                
                StringBuilder contentBuilder = new StringBuilder();
                int nonEmptyCount = 0;
                
                for (int i = 0; i < nodesCount; i++) {
                    Element element = (Element) nodes.item(i);
                    String content = element.getTextContent();
                    
                    if (content != null && !content.trim().isEmpty()) {
                        if (contentBuilder.length() > 0) {
                            contentBuilder.append("\n");
                        }
                        contentBuilder.append(content.trim());
                        nonEmptyCount++;
                    }
                }
                
                String result = contentBuilder.toString();
                if (!result.isEmpty()) {
                    log.debug("成功提取内容，有效内容标签数: {}/{}, 总长度: {}", nonEmptyCount, nodesCount, result.length());
                    
                    if (result.length() > 500) {
                        return result.substring(0, 500) + "...";
                    }
                    return result;
                } else {
                    log.warn("所有 <Content> 元素都为空");
                    return "没有提取到内容";
                }
            } else {
                log.warn("XML中未找到 <Content> 标签");
                return "没有提取到内容";
            }
            
        } catch (Exception e) {
            log.error("解析XML内容失败: {}", e.getMessage(), e);
            
            try {
                log.debug("尝试正则表达式提取Content标签内容");
                Pattern pattern = Pattern.compile("<Content>(.*?)</Content>", Pattern.DOTALL);
                Matcher matcher = pattern.matcher(xmlContent);
                
                StringBuilder directBuilder = new StringBuilder();
                int count = 0;
                int nonEmptyCount = 0;
                
                while (matcher.find()) {
                    count++;
                    String content = matcher.group(1).trim();
                    
                    if (!content.isEmpty()) {
                        if (directBuilder.length() > 0) {
                            directBuilder.append("\n");
                        }
                        directBuilder.append(content);
                        nonEmptyCount++;
                    }
                }
                
                if (count > 0) {
                    String result = directBuilder.toString();
                    log.debug("正则表达式匹配完成，有效内容标签数: {}/{}, 总长度: {}, 内容前50字符: {}", 
                            nonEmptyCount, count, result.length(),
                            result.length() > 50 ? result.substring(0, 50) + "..." : result);
                    
                    if (result.length() > 500) {
                        return result.substring(0, 500) + "...";
                    }
                    return result.isEmpty() ? "没有提取到内容" : result;
                }
            } catch (Exception ex) {
                log.error("正则表达式提取失败: {}", ex.getMessage());
            }
            
            return "解析内容失败";
        }
    }
    
    /**
     * 计算搜索结果的权重分数
     */
    private double calculateScore(SearchResult result, List<String> keywords) {
        if (result.getContent() == null || result.getContent().isEmpty() || keywords.isEmpty()) {
            return 0.0;
        }
        
        String content = result.getContent().toLowerCase();
        String name = result.getName() != null ? result.getName().toLowerCase() : "";
        
        // 日志中只显示内容前50个字符
        log.debug("计算权重分数，标题: {}, 内容前50字符: {}", 
                name.length() > 50 ? name.substring(0, 50) + "..." : name,
                content.length() > 50 ? content.substring(0, 50) + "..." : content);
        
        double score = 0.0;
        
        // 基础权重因子
        final double TITLE_WEIGHT = 3.0;      // 标题中出现关键词的权重
        final double CONTENT_WEIGHT = 1.0;    // 内容中出现关键词的权重
        final double FREQUENCY_FACTOR = 0.5;  // 频率因子，每多出现一次增加的权重
        
        for (String keyword : keywords) {
            if (keyword == null || keyword.isEmpty()) continue;
            
            keyword = keyword.toLowerCase();
            
            // 标题匹配权重（标题中关键词权重更高）
            if (name.contains(keyword)) {
                score += TITLE_WEIGHT;
            }
            
            // 计算内容中关键词出现频率
            int contentOccurrences = countOccurrences(content, keyword);
            if (contentOccurrences > 0) {
                // 基础匹配分数 + 频率奖励
                score += CONTENT_WEIGHT + (contentOccurrences - 1) * FREQUENCY_FACTOR;
            }
        }
        
        // 对长内容稍微降权，避免冗长但相关性不高的内容排名过高
        double lengthNormalization = 1.0 / (1.0 + Math.log(1 + content.length() / 500.0));
        score *= lengthNormalization;
        
        log.debug("结果 [{}] 的计算权重: {}", result.getId(), score);
        return score;
    }
    
    /**
     * 计算文本中关键词出现的次数
     */
    private int countOccurrences(String text, String keyword) {
        if (text == null || keyword == null || text.isEmpty() || keyword.isEmpty()) {
            return 0;
        }
        
        String lowerText = text.toLowerCase();
        String lowerKeyword = keyword.toLowerCase();
        
        int count = 0;
        int index = 0;
        
        while ((index = lowerText.indexOf(lowerKeyword, index)) != -1) {
            count++;
            index += lowerKeyword.length();
        }
        
        return count;
    }
    
    /**
     * 高亮显示文本中的关键词
     */
    private String highlightKeywords(String text, List<String> keywords) {
        if (text == null || text.isEmpty() || keywords == null || keywords.isEmpty()) {
            return text;
        }
        
        // 日志中只显示内容前50个字符
        log.debug("高亮处理文本，长度: {}, 前50字符: {}", 
                text.length(), 
                text.length() > 50 ? text.substring(0, 50) + "..." : text);
        
        String result = text;
        
        // 按照关键词长度降序排序，避免短关键词替换影响长关键词
        List<String> sortedKeywords = new ArrayList<>(keywords);
        sortedKeywords.sort((a, b) -> Integer.compare(b.length(), a.length()));
        
        for (String keyword : sortedKeywords) {
            if (keyword == null || keyword.isEmpty()) continue;
            
            // 不区分大小写的替换，使用正则表达式
            String pattern = "(?i)" + Pattern.quote(keyword);
            result = result.replaceAll(pattern, "<span class=\"highlight\">$0</span>");
        }
        
        // 添加日志，只显示高亮处理后结果的长度
        log.debug("高亮处理完成，结果长度: {}", result.length());
        
        return result;
    }
    
    /**
     * 处理单个关键词项，构建SQL条件
     */
    private void processKeywordTerm(String term, List<String> conditions, List<Object> params, List<String> allKeywords) {
        // 将空格分隔的多个关键词视为AND关系
        String[] andTerms = term.trim().split("\\s+");
        
        for (String keyword : andTerms) {
            String trimmedKeyword = keyword.trim();
            if (!trimmedKeyword.isEmpty()) {
                // 修正字段名，使用正确的表别名
                conditions.add("(UPPER(b.NAME) LIKE UPPER(?) OR UPPER(p.FIELD1079) LIKE UPPER(?))");
                params.add("%" + trimmedKeyword + "%");
                params.add("%" + trimmedKeyword + "%");
                
                // 收集到关键词列表中，用于后续计算权重
                allKeywords.add(trimmedKeyword);
            }
        }
    }
}