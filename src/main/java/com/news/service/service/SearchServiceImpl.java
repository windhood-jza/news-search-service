package com.news.service.service;

import com.news.service.config.AppConfig;
import com.news.service.model.SearchRequest;
import com.news.service.model.SearchResult;
import com.news.service.model.PageResult;
import com.news.service.util.XmlContentExtractor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 搜索服务实现类
 */
@Slf4j
@Service
public class SearchServiceImpl implements SearchService {
    
    private final AppConfig appConfig;
    private final JdbcTemplate jdbcTemplate;
    private final XmlContentExtractor xmlContentExtractor;
    
    @Autowired
    public SearchServiceImpl(AppConfig appConfig, JdbcTemplate jdbcTemplate, XmlContentExtractor xmlContentExtractor) {
        this.appConfig = appConfig;
        this.jdbcTemplate = jdbcTemplate;
        this.xmlContentExtractor = xmlContentExtractor;
    }
    
    @Override
    public boolean isAvailable() {
        return appConfig.isDataSourceEnabled();
    }
    
    @Override
    public PageResult<SearchResult> search(SearchRequest request) {
        log.info("开始搜索: keywords={}, page={}, pageSize={}", 
                request.getKeywords(), request.getPage(), request.getPageSize());
        
        // 检查服务是否可用
        if (!isAvailable()) {
            throw new IllegalStateException("搜索服务不可用");
        }
        
        // 解析关键词
        String keywords = request.getKeywords().trim();
        if (keywords.isEmpty()) {
            throw new IllegalArgumentException("请输入搜索关键词");
        }
        
        // 构建搜索条件
        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        
        // 处理关键词
        if (keywords.contains("OR")) {
            // OR查询
            String[] terms = keywords.split("\\s*OR\\s*");
            List<String> termConditions = new ArrayList<>();
            for (String term : terms) {
                processKeywordTerm(term.trim(), termConditions, params);
            }
            if (!termConditions.isEmpty()) {
                conditions.add("(" + String.join(" OR ", termConditions) + ")");
            }
        } else {
            // AND查询
            String[] terms = keywords.split("\\s+");
            for (String term : terms) {
                processKeywordTerm(term.trim(), conditions, params);
            }
        }
        
        // 构建 WHERE 子句
        String whereClause = conditions.isEmpty() ? "" : "WHERE " + String.join(" AND ", conditions);
        
        // 构建分页参数
        int offset = (request.getPage() - 1) * request.getPageSize();
        int limit = request.getPageSize();
        
        // 构建查询SQL
        String countSql = String.format(
                "SELECT COUNT(*) FROM cob_program p JOIN com_basicinfo b ON p.objectid = b.id %s",
                whereClause
        );
        
        String searchSql = String.format(
                "SELECT b.ID, b.NAME, p.FIELD1079 as xml_content, b.CREATED " +
                "FROM cob_program p JOIN com_basicinfo b ON p.objectid = b.id %s " +
                "ORDER BY b.CREATED DESC LIMIT ? OFFSET ?",
                whereClause
        );
        
        // 添加分页参数
        params.add(limit);
        params.add(offset);
        
        try {
            // 查询总数
            long total = jdbcTemplate.queryForObject(countSql, params.toArray(), Long.class);
            log.info("查询到总数: {}", total);
            
            if (total == 0) {
                return PageResult.<SearchResult>builder()
                        .page(request.getPage())
                        .pageSize(request.getPageSize())
                        .total(0)
                        .items(new ArrayList<>())
                        .build();
            }
            
            // 查询数据
            List<SearchResult> results = jdbcTemplate.query(
                    searchSql,
                    params.toArray(),
                    (rs, rowNum) -> mapRowToSearchResult(rs, keywords)
            );
            
            // 构建分页结果
            return PageResult.<SearchResult>builder()
                    .page(request.getPage())
                    .pageSize(request.getPageSize())
                    .total(total)
                    .items(results)
                    .build();
            
        } catch (Exception e) {
            log.error("执行搜索查询失败: {}", e.getMessage(), e);
            throw new RuntimeException("执行搜索查询失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public long count(String keywords) {
        log.info("开始计数: keywords={}", keywords);
        
        // 检查服务是否可用
        if (!isAvailable()) {
            throw new IllegalStateException("搜索服务不可用");
        }
        
        // 解析关键词
        keywords = keywords.trim();
        if (keywords.isEmpty()) {
            throw new IllegalArgumentException("请输入搜索关键词");
        }
        
        // 构建搜索条件
        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        
        // 处理关键词
        if (keywords.contains("OR")) {
            // OR查询
            String[] terms = keywords.split("\\s*OR\\s*");
            List<String> termConditions = new ArrayList<>();
            for (String term : terms) {
                processKeywordTerm(term.trim(), termConditions, params);
            }
            if (!termConditions.isEmpty()) {
                conditions.add("(" + String.join(" OR ", termConditions) + ")");
            }
        } else {
            // AND查询
            String[] terms = keywords.split("\\s+");
            for (String term : terms) {
                processKeywordTerm(term.trim(), conditions, params);
            }
        }
        
        // 构建 WHERE 子句
        String whereClause = conditions.isEmpty() ? "" : "WHERE " + String.join(" AND ", conditions);
        
        // 构建查询SQL
        String countSql = String.format(
                "SELECT COUNT(*) FROM cob_program p JOIN com_basicinfo b ON p.objectid = b.id %s",
                whereClause
        );
        
        try {
            long count = jdbcTemplate.queryForObject(countSql, params.toArray(), Long.class);
            log.info("计数完成: {}", count);
            return count;
            
        } catch (Exception e) {
            log.error("执行计数查询失败: {}", e.getMessage(), e);
            throw new RuntimeException("执行计数查询失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 处理关键词条件
     */
    private void processKeywordTerm(String term, List<String> conditions, List<Object> params) {
        if (!term.isEmpty()) {
            conditions.add("(UPPER(b.NAME) LIKE UPPER(?) OR UPPER(p.FIELD1079) LIKE UPPER(?))");
            String pattern = "%" + term + "%";
            params.add(pattern);
            params.add(pattern);
        }
    }
    
    /**
     * 将数据库行映射为搜索结果对象
     */
    private SearchResult mapRowToSearchResult(ResultSet rs, String keywords) throws SQLException {
        // 提取数据
        String id = rs.getString("ID");
        String xmlContent = rs.getString("xml_content");
        String title = rs.getString("NAME");
        java.sql.Timestamp created = rs.getTimestamp("CREATED");
        
        log.debug("处理搜索结果行: id={}, title={}, xmlLength={}",
                id, title, xmlContent != null ? xmlContent.length() : 0);
        
        // 提取XML内容
        String content = xmlContentExtractor.extract(xmlContent);
        if (content == null || content.isEmpty()) {
            content = "没有提取到内容";
        }
        
        // 计算匹配分数
        double score = calculateScore(title, content, keywords);
        
        // 高亮关键词
        String highlightedTitle = highlightKeywords(title, keywords);
        String highlightedContent = highlightKeywords(content, keywords);
        
        // 构建搜索结果
        return SearchResult.builder()
                .id(id)
                .title(highlightedTitle)
                .content(highlightedContent)
                .created(created)
                .score(score)
                .build();
    }
    
    /**
     * 计算搜索结果匹配分数
     */
    private double calculateScore(String title, String content, String keywords) {
        double score = 0.0;
        
        // 分解关键词
        List<String> terms;
        if (keywords.contains("OR")) {
            terms = Arrays.asList(keywords.split("\\s*OR\\s*"));
        } else {
            terms = Arrays.asList(keywords.split("\\s+"));
        }
        
        // 计算每个关键词的匹配分数
        for (String term : terms) {
            term = term.trim().toLowerCase();
            if (!term.isEmpty()) {
                // 标题匹配分数（权重更高）
                int titleMatches = countMatches(title.toLowerCase(), term);
                score += titleMatches * 2.0;
                
                // 内容匹配分数
                int contentMatches = countMatches(content.toLowerCase(), term);
                score += contentMatches * 1.0;
            }
        }
        
        return score;
    }
    
    /**
     * 计算字符串中关键词的出现次数
     */
    private int countMatches(String text, String term) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(term, index)) != -1) {
            count++;
            index += term.length();
        }
        return count;
    }
    
    /**
     * 高亮显示关键词
     */
    private String highlightKeywords(String text, String keywords) {
        if (text == null || text.isEmpty() || keywords == null || keywords.isEmpty()) {
            return text;
        }
        
        // 分解关键词
        List<String> terms;
        if (keywords.contains("OR")) {
            terms = Arrays.asList(keywords.split("\\s*OR\\s*"));
        } else {
            terms = Arrays.asList(keywords.split("\\s+"));
        }
        
        // 高亮每个关键词
        String result = text;
        for (String term : terms) {
            term = term.trim();
            if (!term.isEmpty()) {
                // 使用正则表达式进行大小写不敏感的替换
                Pattern pattern = Pattern.compile(Pattern.quote(term), Pattern.CASE_INSENSITIVE);
                result = pattern.matcher(result).replaceAll(match -> 
                        "<span class=\"highlight\">" + match.group() + "</span>"
                );
            }
        }
        
        return result;
    }
}