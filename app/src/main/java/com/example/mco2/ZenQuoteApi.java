package com.example.mco2;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ZenQuoteApi {
    @GET("today")
    Call<List<Quote>> getTodayQuote();
}
