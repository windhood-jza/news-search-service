package com.news.service.service;

import com.news.service.config.AppConfig;
import com.news.service.model.PageResult;
import com.news.service.model.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
public class SearchServiceImpl implements SearchService {

    private static final Logger logger = LoggerFactory.getLogger(SearchServiceImpl.class);

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public PageResult<SearchResult> search(String keywords, int page, int size) {
        logger.info("开始搜索，关键词：{}，页码：{}，每页大小：{}", keywords, page, size);

        if (!appConfig.isDataSourceEnabled()) {
            logger.warn("数据源未启用，无法执行搜索");
            return null;
        }

        List<String> keywordList = parseKeywords(keywords);
        if (keywordList.isEmpty()) {
            logger.warn("关键词为空，返回空结果");
            return new PageResult<>(new ArrayList<>(), 0, page, size);
        }

        List<Object> params = new ArrayList<>();
        String conditions = buildSearchConditions(keywordList, params);

        String countSql = "SELECT COUNT(*) FROM cob_program p JOIN com_basicinfo b ON p.objectid = b.id WHERE " + conditions;
        int total = jdbcTemplate.queryForObject(countSql, params.toArray(), Integer.class);

        if (total == 0) {
            logger.info("未找到匹配的结果");
            return new PageResult<>(new ArrayList<>(), 0, page, size);
        }

        int offset = (page - 1) * size;
        String sql = "SELECT b.ID, b.NAME, p.FIELD1079 as xml_content, b.CREATED FROM cob_program p JOIN com_basicinfo b ON p.objectid = b.id WHERE " +
                conditions + " ORDER BY b.CREATED DESC LIMIT " + size + " OFFSET " + offset;

        List<SearchResult> results = jdbcTemplate.query(sql, params.toArray(), new SearchResultRowMapper());
        logger.info("搜索完成，共找到 {} 条结果", total);

        return new PageResult<>(results, total, page, size);
    }

    private List<String> parseKeywords(String keywords) {
        List<String> keywordList = new ArrayList<>();
        if (keywords == null || keywords.trim().isEmpty()) {
            return keywordList;
        }

        String[] terms = keywords.split(",");
        for (String term : terms) {
            if (!term.trim().isEmpty()) {
                keywordList.add(term.trim());
            }
        }
        return keywordList;
    }

    private String buildSearchConditions(List<String> keywords, List<Object> params) {
        List<String> conditions = new ArrayList<>();

        for (String keyword : keywords) {
            if (keyword.contains("OR")) {
                String[] orTerms = keyword.split("OR");
                List<String> orConditions = new ArrayList<>();
                for (String term : orTerms) {
                    if (!term.trim().isEmpty()) {
                        orConditions.add(processKeywordTerm(term.trim(), params));
                    }
                }
                if (!orConditions.isEmpty()) {
                    conditions.add("(" + String.join(" OR ", orConditions) + ")");
                }
            } else {
                conditions.add(processKeywordTerm(keyword, params));
            }
        }

        return String.join(" AND ", conditions);
    }

    private String processKeywordTerm(String term, List<Object> params) {
        String likePattern = "%" + term + "%";
        params.add(likePattern);
        params.add(likePattern);
        return "(UPPER(b.NAME) LIKE UPPER(?) OR UPPER(p.FIELD1079) LIKE UPPER(?))"; 
    }

    private static class SearchResultRowMapper implements RowMapper<SearchResult> {
        @Override
        public SearchResult mapRow(ResultSet rs, int rowNum) throws SQLException {
            SearchResult result = new SearchResult();
            result.setId(rs.getString("ID"));
            String xmlContent = rs.getString("xml_content");
            result.setContent(xmlContent != null ? xmlContent : "");
            result.setTitle(rs.getString("NAME"));
            result.setCreateTime(rs.getTimestamp("CREATED"));
            return result;
        }
    }

    @Override
    public int count(String keywords) {
        logger.info("开始统计搜索结果数量，关键词：{}", keywords);

        if (!appConfig.isDataSourceEnabled()) {
            logger.warn("数据源未启用，无法执行统计");
            return 0;
        }

        List<String> keywordList = parseKeywords(keywords);
        if (keywordList.isEmpty()) {
            logger.warn("关键词为空，返回0");
            return 0;
        }

        List<Object> params = new ArrayList<>();
        String conditions = buildSearchConditions(keywordList, params);

        String sql = "SELECT COUNT(*) FROM cob_program p JOIN com_basicinfo b ON p.objectid = b.id WHERE " + conditions;
        int count = jdbcTemplate.queryForObject(sql, params.toArray(), Integer.class);

        logger.info("统计完成，共找到 {} 条结果", count);
        return count;
    }
}
