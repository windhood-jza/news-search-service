package com.news.service.service;

import com.news.service.config.AppConfig;
import com.news.service.model.SearchRequest;
import com.news.service.model.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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

/**
 * 搜索服务实现类
 */
@Slf4j
@Service
public class SearchServiceImpl implements SearchService {
    
    private final JdbcTemplate jdbcTemplate;
    private final AppConfig appConfig;
    
    public SearchServiceImpl(JdbcTemplate jdbcTemplate, AppConfig appConfig) {
        this.jdbcTemplate = jdbcTemplate;
        this.appConfig = appConfig;
    }
    
    @Override
    public List<SearchResult> search(SearchRequest request) {
        if (!isAvailable()) {
            log.warn("搜索服务不可用，数据库连接未配置");
            return new ArrayList<>();
        }
        
        try {
            String[] keywordArray = request.getKeywords().split(",");
            List<String> keywords = Arrays.stream(keywordArray)
                    .map(String::trim)
                    .filter(k -> !k.isEmpty())
                    .collect(Collectors.toList());
            
            if (keywords.isEmpty()) {
                return new ArrayList<>();
            }
            
            // 构建全文搜索条件
            String matchCondition = keywords.stream()
                    .map(keyword -> "FIELD1079 LIKE ?")
                    .collect(Collectors.joining(" OR "));
            
            // 排序方向
            String direction = "asc".equalsIgnoreCase(request.getSortDirection()) ? "ASC" : "DESC";
            
            // 构建SQL查询
            String sql = "SELECT b.ID, b.NAME, b.CREATED, p.FIELD1079 " +
                    "FROM cob_program p " +
                    "JOIN com_basicinfo b ON p.OBJECTID = b.ID " +
                    "WHERE " + matchCondition + " " +
                    "ORDER BY b." + request.getSortField() + " " + direction + " " +
                    "LIMIT ? OFFSET ?";
            
            // 设置查询参数
            List<Object> params = new ArrayList<>();
            for (String keyword : keywords) {
                params.add("%" + keyword + "%");
            }
            params.add(request.getSize());
            params.add(request.getPage() * request.getSize());
            
            // 执行查询
            return jdbcTemplate.query(sql, this::mapRowToSearchResult, params.toArray());
        } catch (Exception e) {
            log.error("搜索失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public long count(String keywords) {
        if (!isAvailable()) {
            log.warn("搜索服务不可用，数据库连接未配置");
            return 0;
        }
        
        try {
            String[] keywordArray = keywords.split(",");
            List<String> keywordList = Arrays.stream(keywordArray)
                    .map(String::trim)
                    .filter(k -> !k.isEmpty())
                    .collect(Collectors.toList());
            
            if (keywordList.isEmpty()) {
                return 0;
            }
            
            // 构建全文搜索条件
            String matchCondition = keywordList.stream()
                    .map(keyword -> "FIELD1079 LIKE ?")
                    .collect(Collectors.joining(" OR "));
            
            // 构建SQL查询
            String sql = "SELECT COUNT(*) " +
                    "FROM cob_program p " +
                    "JOIN com_basicinfo b ON p.OBJECTID = b.ID " +
                    "WHERE " + matchCondition;
            
            // 设置查询参数
            List<Object> params = new ArrayList<>();
            for (String keyword : keywordList) {
                params.add("%" + keyword + "%");
            }
            
            // 执行查询
            return jdbcTemplate.queryForObject(sql, Long.class, params.toArray());
        } catch (Exception e) {
            log.error("获取搜索结果数量失败: {}", e.getMessage(), e);
            return 0;
        }
    }
    
    @Override
    public boolean isAvailable() {
        return appConfig.isDataSourceEnabled() && jdbcTemplate != null;
    }
    
    /**
     * 将数据库行映射为搜索结果对象，并解析XML提取content字段
     */
    private SearchResult mapRowToSearchResult(ResultSet rs, int rowNum) throws SQLException {
        String xmlContent = rs.getString("FIELD1079");
        Long id = rs.getLong("ID");
        String name = rs.getString("NAME");
        Date created = new Date(rs.getTimestamp("CREATED").getTime());
        
        String extractedContent = extractContentFromXml(xmlContent);
        
        SearchResult result = new SearchResult();
        result.setId(id);
        result.setName(name);
        result.setCreated(created);
        result.setContent(extractedContent);
        return result;
    }
    
    /**
     * 从XML中提取content字段内容
     * 
     * @param xmlContent XML内容
     * @return 提取的content内容，如果解析失败则返回提示信息
     */
    private String extractContentFromXml(String xmlContent) {
        if (xmlContent == null || xmlContent.trim().isEmpty()) {
            return "没有提取到内容";
        }
        
        // 尝试解析XML并提取content
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xmlContent)));
            
            // 提取所有content元素
            NodeList contentNodes = document.getElementsByTagName("content");
            if (contentNodes.getLength() == 0) {
                log.debug("XML中未找到content元素");
                return "没有提取到内容"; // 如果没有找到content元素，返回提示信息
            }
            
            StringBuilder contentBuilder = new StringBuilder();
            for (int i = 0; i < contentNodes.getLength(); i++) {
                Element contentElement = (Element) contentNodes.item(i);
                String content = contentElement.getTextContent();
                if (content != null && !content.trim().isEmpty()) {
                    if (contentBuilder.length() > 0) {
                        contentBuilder.append("\n");
                    }
                    contentBuilder.append(content.trim());
                }
            }
            
            // 如果所有content标签都为空，返回提示信息
            if (contentBuilder.length() == 0) {
                return "没有提取到内容";
            }
            
            return contentBuilder.toString();
        } catch (Exception e) {
            log.warn("XML解析失败: {}", e.getMessage());
            return "没有提取到内容"; // 解析失败时返回提示信息
        }
    }
} 