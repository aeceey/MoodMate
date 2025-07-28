package com.example.mco2;

import com.google.gson.annotations.SerializedName;

public class Quote {
    @SerializedName("q") // Maps JSON key 'q' to quoteText
    private String quoteText;

    @SerializedName("a") // Maps JSON key 'a' to author
    private String author;

    public String getFormattedQuote() {
        return "\"" + quoteText + "\"\n- " + author;
    }
}

