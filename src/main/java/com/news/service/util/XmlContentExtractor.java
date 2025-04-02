package com.news.service.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * XML内容提取工具类
 * 
 * 从 XML 文本中提取 Content 标签中的内容
 */
@Slf4j
@Component
public class XmlContentExtractor {
    
    /**
     * 提取XML内容
     *
     * @param xml XML文本
     * @return 提取的内容，如果提取失败则返回空字符串
     */
    public String extract(String xml) {
        if (xml == null || xml.trim().isEmpty()) {
            return "没有提取到内容";
        }
        
        try {
            // 使用DOM解析XML
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xml)));
            
            // 查找 Content 标签
            NodeList contentNodes = doc.getElementsByTagName("Content");
            log.debug("找到 {} 个 Content 标签", contentNodes.getLength());
            
            if (contentNodes.getLength() > 0) {
                String content = contentNodes.item(0).getTextContent();
                if (content != null && !content.trim().isEmpty()) {
                    return content.trim();
                }
            }
            
            // 如果DOM解析失败，尝试使用正则表达式
            Pattern pattern = Pattern.compile("<Content>(.*?)</Content>", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(xml);
            
            if (matcher.find()) {
                String content = matcher.group(1);
                if (content != null && !content.trim().isEmpty()) {
                    return content.trim();
                }
            }
            
            log.warn("未找到内容: {}", xml);
            return "没有提取到内容";
            
        } catch (Exception e) {
            log.error("提取XML内容失败: {}", e.getMessage(), e);
            return "没有提取到内容";
        }
    }
}