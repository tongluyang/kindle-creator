package com.tongluyang.entity;

public class Paragraph {
    private String htmlTag = "p";
    private String text;

    public Paragraph() {
    }

    public Paragraph(String text) {
        this.text = text;
    }

    public String getHtmlTag() {
        return htmlTag;
    }

    public void setHtmlTag(String htmlTag) {
        this.htmlTag = htmlTag;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
