// src/main/java/com/example/mco2/model/JournalEntry.java
package com.example.mco2.model;

public class JournalEntry {
    private long id;
    private String date;
    private String mood;
    private String title;
    private String content;
    private String quote;

    public JournalEntry() {
        // Default constructor
    }

    public JournalEntry(String date, String mood, String title, String content, String quote) {
        this.date = date;
        this.mood = mood;
        this.title = title;
        this.content = content;
        this.quote = quote;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }
}