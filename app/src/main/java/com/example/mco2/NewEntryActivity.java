// src/main/java/com/example/mco2/NewEntryActivity.java
package com.example.mco2;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mco2.data.JournalDbHelper;
import com.example.mco2.model.JournalEntry;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NewEntryActivity extends AppCompatActivity {
    private static final String BASE_URL = "https://zenquotes.io/api/";
    private String currentQuote = "";
    private Retrofit retrofit;
    private ZenQuoteApi zenQuoteApi;

    public static final String EXTRA_EDIT_ENTRY_ID = "edit_entry_id";

    private EditText etEntryTitle;
    private EditText etMoodLog;
    private TextView tvCurrentDate;
    private TextView moodAngry, moodSad, moodNeutral, moodHappy, moodExcited;
    private TextView quoteTextView;
    private Button btnSaveEntry;
    private Button btnSaveQuote;

    private String selectedMood = "";
    private JournalDbHelper dbHelper;
    private long entryIdToEdit = -1; // To store the ID of the entry being edited
    private JournalEntry existingEntry = null; // To hold the existing entry data

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_entry);

        dbHelper = new JournalDbHelper(this);

        // Initialize UI elements
        etEntryTitle = findViewById(R.id.et_entry_title);
        etMoodLog = findViewById(R.id.et_mood_log2);
        tvCurrentDate = findViewById(R.id.tvCurrentDate);
        btnSaveEntry = findViewById(R.id.activity_new_entry_btn_saveentry);
        btnSaveQuote = findViewById(R.id.activity_new_entry_btn_savequote);

        moodAngry = findViewById(R.id.mood_angry);
        moodSad = findViewById(R.id.mood_sad);
        moodNeutral = findViewById(R.id.mood_neutral);
        moodHappy = findViewById(R.id.mood_happy);
        moodExcited = findViewById(R.id.mood_excited);

        quoteTextView = findViewById(R.id.tvQuote);

        // Check if we are editing an existing entry
        if (getIntent().hasExtra(EXTRA_EDIT_ENTRY_ID)) {
            entryIdToEdit = getIntent().getLongExtra(EXTRA_EDIT_ENTRY_ID, -1);
            if (entryIdToEdit != -1) {
                loadEntryForEditing(entryIdToEdit);
            }
        }

        // Display current date if not editing or if no date is set for existing entry

        if (existingEntry != null && existingEntry.getDate() != null && !existingEntry.getDate().isEmpty()) {
            // If editing an existing entry, display its date
            tvCurrentDate.setText("Today: " + existingEntry.getDate());
        } else {
            // For a new entry, display the current date and time
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String currentDateAndTime = sdf.format(new Date());
            tvCurrentDate.setText("Today: " + currentDateAndTime);
        }


        // Set click listeners for mood emojis
        View.OnClickListener moodClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetMoodBackgrounds();
                TextView clickedMood = (TextView) v;
                selectedMood = clickedMood.getText().toString();
                // Apply the selected background, assuming mood_selector_background_selected is defined
                clickedMood.setBackgroundResource(R.drawable.mood_selector_background_selected);
                Toast.makeText(NewEntryActivity.this, "Mood selected: " + selectedMood, Toast.LENGTH_SHORT).show();
            }
        };

        moodAngry.setOnClickListener(moodClickListener);
        moodSad.setOnClickListener(moodClickListener);
        moodNeutral.setOnClickListener(moodClickListener);
        moodHappy.setOnClickListener(moodClickListener);
        moodExcited.setOnClickListener(moodClickListener);

        // Set click listener for Save Entry button
        btnSaveEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveOrUpdateJournalEntry();
            }
        });

        // Set click listener for Save Quote button
        if (btnSaveQuote != null) {
            btnSaveQuote.setOnClickListener(v -> {
                if (!currentQuote.isEmpty()) {
                    // Update the current quote for this entry
                    Toast.makeText(NewEntryActivity.this, "Quote saved for this entry!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(NewEntryActivity.this, "No quote available to save. Please wait for quote to load.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Initialize Retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        zenQuoteApi = retrofit.create(ZenQuoteApi.class);

        // Fetches quote automatically when activity starts
        fetchDailyQuote();
    }

    private void loadEntryForEditing(long entryId) {
        existingEntry = dbHelper.getEntryById(entryId);
        if (existingEntry != null) {
            etEntryTitle.setText(existingEntry.getTitle());
            etMoodLog.setText(existingEntry.getContent());
            // Date is handled in onCreate based on existingEntry status
            selectedMood = existingEntry.getMood();
            highlightSelectedMood(selectedMood); // Highlight the mood if set
        } else {
            Toast.makeText(this, "Failed to load entry for editing.", Toast.LENGTH_SHORT).show();
            entryIdToEdit = -1; // Reset to ensure it's treated as a new entry if loading fails
        }
    }

    private void highlightSelectedMood(String mood) {
        resetMoodBackgrounds(); // First, reset all backgrounds to default
        // Then, apply the selected background to the appropriate TextView
        if (moodAngry.getText().toString().equals(mood)) {
            moodAngry.setBackgroundResource(R.drawable.mood_selector_background_selected);
        } else if (moodSad.getText().toString().equals(mood)) {
            moodSad.setBackgroundResource(R.drawable.mood_selector_background_selected);
        } else if (moodNeutral.getText().toString().equals(mood)) {
            moodNeutral.setBackgroundResource(R.drawable.mood_selector_background_selected);
        } else if (moodHappy.getText().toString().equals(mood)) {
            moodHappy.setBackgroundResource(R.drawable.mood_selector_background_selected);
        } else if (moodExcited.getText().toString().equals(mood)) {
            moodExcited.setBackgroundResource(R.drawable.mood_selector_background_selected);
        }
    }

    private void resetMoodBackgrounds() {
        moodAngry.setBackgroundResource(R.drawable.mood_selector_background);
        moodSad.setBackgroundResource(R.drawable.mood_selector_background);
        moodNeutral.setBackgroundResource(R.drawable.mood_selector_background);
        moodHappy.setBackgroundResource(R.drawable.mood_selector_background);
        moodExcited.setBackgroundResource(R.drawable.mood_selector_background);
    }

    private void saveOrUpdateJournalEntry() {
        String title = etEntryTitle.getText().toString().trim();
        String content = etMoodLog.getText().toString().trim();
        String date = tvCurrentDate.getText().toString().replace("Today: ", "").trim();
        String quote = currentQuote.isEmpty() ? "No quote available" : currentQuote; // Use fetched quote

        if (content.isEmpty()) {
            Toast.makeText(this, "Journal entry cannot be empty!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedMood.isEmpty()) {
            Toast.makeText(this, "Please select a mood!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (entryIdToEdit == -1) { // This is a new entry
            JournalEntry entry = new JournalEntry(date, selectedMood, title, content, quote);
            long newRowId = dbHelper.insertEntry(entry);
            if (newRowId != -1) {
                Toast.makeText(this, "Entry saved successfully!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Error saving entry.", Toast.LENGTH_SHORT).show();
            }
        } else { // This is an existing entry to update
            if (existingEntry != null) {
                existingEntry.setDate(date);
                existingEntry.setMood(selectedMood);
                existingEntry.setTitle(title);
                existingEntry.setContent(content);
                // Only update quote if user explicitly saved a new one
                if (!currentQuote.isEmpty() && !currentQuote.equals("No quote available")) {
                    existingEntry.setQuote(currentQuote);
                }

                int rowsAffected = dbHelper.updateEntry(existingEntry);
                if (rowsAffected > 0) {
                    Toast.makeText(this, "Entry updated successfully!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(this, "Error updating entry.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void fetchDailyQuote() {
        Call<List<Quote>> call = zenQuoteApi.getTodayQuote();

        call.enqueue(new Callback<List<Quote>>() {
            @Override
            public void onResponse(Call<List<Quote>> call, Response<List<Quote>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Quote quote = response.body().get(0);
                    currentQuote = quote.getFormattedQuote();

                    // Update the UI with the fetched quote
                    if (quoteTextView != null) {
                        quoteTextView.setText(currentQuote);
                    }
                } else {
                    currentQuote = "Failed to load today's quote";
                    if (quoteTextView != null) {
                        quoteTextView.setText(currentQuote);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Quote>> call, Throwable t) {
                currentQuote = "No internet connection - quote unavailable";
                if (quoteTextView != null) {
                    quoteTextView.setText(currentQuote);
                }
                Toast.makeText(NewEntryActivity.this, "Failed to fetch daily quote", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}