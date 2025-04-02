package com.news.service.model;

public class SearchRequest {
    private String keywords;
    private int page = 1;
    private int size = 10;

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "SearchRequest{" +
                "keywords='" + keywords + '\'' +
                ", page=" + page +
                ", size=" + size +
                '}';
    }
}
