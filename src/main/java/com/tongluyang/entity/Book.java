package com.tongluyang.entity;

import java.util.LinkedHashMap;
import java.util.Map;

public class Book {
    private String name;
    private String language = "zh";
    private String creator = "";
    private String copyrights = "";
    private String publisher = "";
    private byte[] cover = null;

    private Map<String, Chapter> chapters = new LinkedHashMap<>();

    public Book(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCopyrights() {
        return copyrights;
    }

    public void setCopyrights(String copyrights) {
        this.copyrights = copyrights;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public byte[] getCover() {
        return cover;
    }

    public void setCover(byte[] cover) {
        this.cover = cover;
    }

    public Map<String, Chapter> getChapters() {
        return chapters;
    }

    public void addChapter(String key, Chapter chapter) {
        this.chapters.put(key, chapter);
    }

}
