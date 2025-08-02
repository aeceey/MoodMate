package com.example.mco2.model;

public class JournalEntry {
    private long id;
    private long userId;
    private String date;
    private String mood;
    private String title;
    private String content;
    private String quote;

    // No-argument constructor
    public JournalEntry() {
    }

    // Constructor for creating a new entry
    public JournalEntry(long userId, String date, String mood, String title, String content, String quote) {
        this.userId = userId;
        this.date = date;
        this.mood = mood;
        this.title = title;
        this.content = content;
        this.quote = quote;
    }

    // Constructor for entries retrieved from the database (with ID)
    public JournalEntry(long id, long userId, String date, String mood, String title, String content, String quote) {
        this.id = id;
        this.userId = userId;
        this.date = date;
        this.mood = mood;
        this.title = title;
        this.content = content;
        this.quote = quote;
    }

    // Constructor for entries retrieved from the database (without quote)
    public JournalEntry(long id, long userId, String date, String mood, String title, String content) {
        this.id = id;
        this.userId = userId;
        this.date = date;
        this.mood = mood;
        this.title = title;
        this.content = content;
        this.quote = ""; // Default to an empty string
    }

    // Getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
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