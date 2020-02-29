package com.tongluyang.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Chapter {
    private String id;
    private String chapterName;
    private List<Paragraph> paragraphs = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChapterName() {
        return chapterName;
    }

    public void setChapterName(String chapterName) {
        this.chapterName = chapterName;
    }

    public List<Paragraph> getParagraphs() {
        return paragraphs;
    }

    public void addParagraph(Paragraph paragraph) {
        this.paragraphs.add(paragraph);
    }

    public void parse(String string) {
        this.paragraphs.addAll(Arrays.stream(string.split("\n|\\u00A0+"))
                .filter(str -> !str.trim().equals(""))
                .map(Paragraph::new).collect(Collectors.toList()));
    }
}
