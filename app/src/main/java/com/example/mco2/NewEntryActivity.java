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
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NewEntryActivity extends AppCompatActivity {
    private static final String BASE_URL = "https://zenquotes.io/api/";
    private String currentQuote = "";
    private boolean isQuoteSavedForEntry = false;
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
    private long entryIdToEdit = -1;
    private JournalEntry existingEntry = null;
    private long currentUserId; // New: field to hold the user's ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_entry);

        dbHelper = new JournalDbHelper(this);

        // Get the current user ID from the intent
        currentUserId = getIntent().getLongExtra("CURRENT_USER_ID", -1);
        if (currentUserId == -1) {
            Toast.makeText(this, "Error: User not logged in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

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

        if (getIntent().hasExtra(EXTRA_EDIT_ENTRY_ID)) {
            entryIdToEdit = getIntent().getLongExtra(EXTRA_EDIT_ENTRY_ID, -1);
            if (entryIdToEdit != -1) {
                loadEntryForEditing(entryIdToEdit);
            }
        }

        if (existingEntry != null && existingEntry.getDate() != null && !existingEntry.getDate().isEmpty()) {
            tvCurrentDate.setText("Today: " + existingEntry.getDate());
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String currentDateAndTime = sdf.format(new Date());
            tvCurrentDate.setText("Today: " + currentDateAndTime);
        }

        View.OnClickListener moodClickListener = v -> {
            resetMoodBackgrounds();
            TextView clickedMood = (TextView) v;
            selectedMood = clickedMood.getText().toString();
            clickedMood.setBackgroundResource(R.drawable.mood_selector_background_selected);
            Toast.makeText(NewEntryActivity.this, "Mood selected: " + selectedMood, Toast.LENGTH_SHORT).show();
        };

        moodAngry.setOnClickListener(moodClickListener);
        moodSad.setOnClickListener(moodClickListener);
        moodNeutral.setOnClickListener(moodClickListener);
        moodHappy.setOnClickListener(moodClickListener);
        moodExcited.setOnClickListener(moodClickListener);

        btnSaveEntry.setOnClickListener(v -> saveOrUpdateJournalEntry());

        if (btnSaveQuote != null) {
            btnSaveQuote.setOnClickListener(v -> {
                if (!currentQuote.isEmpty() && !currentQuote.equals("No quote available") && !currentQuote.equals("Failed to load today's quote") && !currentQuote.equals("No internet connection - quote unavailable")) {
                    isQuoteSavedForEntry = true;
                    Toast.makeText(NewEntryActivity.this, "Quote saved for this entry!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(NewEntryActivity.this, "No valid quote available to save.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        zenQuoteApi = retrofit.create(ZenQuoteApi.class);

        fetchDailyQuote();
    }

    private void loadEntryForEditing(long entryId) {
        // Use the new getEntryById method that requires the user ID
        existingEntry = dbHelper.getEntryById(entryId, currentUserId);
        if (existingEntry != null) {
            etEntryTitle.setText(existingEntry.getTitle());
            etMoodLog.setText(existingEntry.getContent());
            selectedMood = existingEntry.getMood();
            highlightSelectedMood(selectedMood);
        } else {
            Toast.makeText(this, "Failed to load entry for editing.", Toast.LENGTH_SHORT).show();
            entryIdToEdit = -1;
            finish(); // Exit if we can't load the entry
        }
    }

    private void highlightSelectedMood(String mood) {
        resetMoodBackgrounds();
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
        String quoteToSave = isQuoteSavedForEntry ? currentQuote : "";

        // Debug logging
        Log.d("NewEntryActivity", "=== SAVE ENTRY DEBUG ===");
        Log.d("NewEntryActivity", "Title: " + title);
        Log.d("NewEntryActivity", "Content: " + content);
        Log.d("NewEntryActivity", "Selected Mood: " + selectedMood);
        Log.d("NewEntryActivity", "Quote to save: " + quoteToSave);
        Log.d("NewEntryActivity", "Current User ID: " + currentUserId);
        Log.d("NewEntryActivity", "Entry ID to edit: " + entryIdToEdit);

        if (content.isEmpty()) {
            Toast.makeText(this, "Journal entry cannot be empty!", Toast.LENGTH_SHORT).show();
            Log.w("NewEntryActivity", "Validation failed: content is empty");
            return;
        }
        if (selectedMood.isEmpty()) {
            Toast.makeText(this, "Please select a mood!", Toast.LENGTH_SHORT).show();
            Log.w("NewEntryActivity", "Validation failed: mood not selected");
            return;
        }

        String dateToSave;
        if (entryIdToEdit == -1) { // New entry
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            dateToSave = sdf.format(new Date());
            Log.d("NewEntryActivity", "Creating new entry with date: " + dateToSave);
        } else { // Existing entry
            if (existingEntry != null) {
                dateToSave = existingEntry.getDate();
                Log.d("NewEntryActivity", "Updating existing entry with date: " + dateToSave);
            } else {
                Toast.makeText(this, "Error: Existing entry not found.", Toast.LENGTH_SHORT).show();
                Log.e("NewEntryActivity", "existingEntry is null for edit operation");
                return;
            }
        }

        if (entryIdToEdit == -1) { // This is a new entry
            JournalEntry entry = new JournalEntry(currentUserId, dateToSave, selectedMood, title, content, quoteToSave);

            // Additional debug logging for the entry object
            Log.d("NewEntryActivity", "Created entry object:");
            Log.d("NewEntryActivity", "  - User ID: " + entry.getUserId());
            Log.d("NewEntryActivity", "  - Date: " + entry.getDate());
            Log.d("NewEntryActivity", "  - Mood: " + entry.getMood());
            Log.d("NewEntryActivity", "  - Title: " + entry.getTitle());
            Log.d("NewEntryActivity", "  - Content: " + entry.getContent());
            Log.d("NewEntryActivity", "  - Quote: " + entry.getQuote());

            long newRowId = dbHelper.insertEntry(entry);
            Log.d("NewEntryActivity", "Insert result: " + newRowId);

            if (newRowId != -1) {
                Toast.makeText(this, "Entry saved successfully! ID: " + newRowId, Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Error saving entry. Check logs for details.", Toast.LENGTH_LONG).show();
                Log.e("NewEntryActivity", "Failed to save entry - insertEntry returned -1");
            }
        } else { // This is an existing entry to update
            if (existingEntry != null) {
                existingEntry.setDate(dateToSave);
                existingEntry.setMood(selectedMood);
                existingEntry.setTitle(title);
                existingEntry.setContent(content);
                existingEntry.setQuote(quoteToSave);

                Log.d("NewEntryActivity", "Updating entry with ID: " + existingEntry.getId());

                int rowsAffected = dbHelper.updateEntry(existingEntry);
                Log.d("NewEntryActivity", "Update result: " + rowsAffected + " rows affected");

                if (rowsAffected > 0) {
                    Toast.makeText(this, "Entry updated successfully!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(this, "Error updating entry. Check logs for details.", Toast.LENGTH_LONG).show();
                    Log.e("NewEntryActivity", "Failed to update entry - no rows affected");
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
                    isQuoteSavedForEntry = false;
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