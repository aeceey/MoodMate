// src/main/java/com/example/mco2/NewEntryActivity.java
package com.example.mco2;

import android.content.Intent;
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

    public static final String EXTRA_EDIT_ENTRY_ID = "edit_entry_id"; // Key for editing

    private EditText etEntryTitle;
    private EditText etMoodLog;
    private TextView tvCurrentDate;
    private TextView moodAngry, moodSad, moodNeutral, moodHappy, moodExcited;
    private Button btnSaveEntry;
    private Button btnSaveQuote; // Assuming this button is for saving the daily quote

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
        tvCurrentDate = findViewById(R.id.textView);
        btnSaveEntry = findViewById(R.id.activity_new_entry_btn_saveentry);
        btnSaveQuote = findViewById(R.id.activity_new_entry_btn_savequote);

        moodAngry = findViewById(R.id.mood_angry);
        moodSad = findViewById(R.id.mood_sad);
        moodNeutral = findViewById(R.id.mood_neutral);
        moodHappy = findViewById(R.id.mood_happy);
        moodExcited = findViewById(R.id.mood_excited);

        // Check if we are editing an existing entry
        if (getIntent().hasExtra(EXTRA_EDIT_ENTRY_ID)) {
            entryIdToEdit = getIntent().getLongExtra(EXTRA_EDIT_ENTRY_ID, -1);
            if (entryIdToEdit != -1) {
                loadEntryForEditing(entryIdToEdit);
            }
        }

        // Display current date if not editing or if no date is set for existing entry
        if (existingEntry == null || existingEntry.getDate() == null || existingEntry.getDate().isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String currentDateAndTime = sdf.format(new Date());
            tvCurrentDate.setText("Today: " + currentDateAndTime);
        } else {
            tvCurrentDate.setText("Today: " + existingEntry.getDate());
        }


        // Set click listeners for mood emojis
        View.OnClickListener moodClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetMoodBackgrounds();
                TextView clickedMood = (TextView) v;
                selectedMood = clickedMood.getText().toString();
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
    }

    private void loadEntryForEditing(long entryId) {
        existingEntry = dbHelper.getEntryById(entryId);
        if (existingEntry != null) {
            etEntryTitle.setText(existingEntry.getTitle());
            etMoodLog.setText(existingEntry.getContent());
            tvCurrentDate.setText("Today: " + existingEntry.getDate());
            selectedMood = existingEntry.getMood();
            highlightSelectedMood(selectedMood); // Highlight the mood if set
            // You might want to display the quote for editing too
        } else {
            Toast.makeText(this, "Failed to load entry for editing.", Toast.LENGTH_SHORT).show();
            entryIdToEdit = -1; // Reset to ensure it's treated as a new entry if loading fails
        }
    }

    private void highlightSelectedMood(String mood) {
        resetMoodBackgrounds();
        if (moodAngry.getText().toString().equals(mood)) moodAngry.setBackgroundResource(R.drawable.mood_selector_background_selected);
        else if (moodSad.getText().toString().equals(mood)) moodSad.setBackgroundResource(R.drawable.mood_selector_background_selected);
        else if (moodNeutral.getText().toString().equals(mood)) moodNeutral.setBackgroundResource(R.drawable.mood_selector_background_selected);
        else if (moodHappy.getText().toString().equals(mood)) moodHappy.setBackgroundResource(R.drawable.mood_selector_background_selected);
        else if (moodExcited.getText().toString().equals(mood)) moodExcited.setBackgroundResource(R.drawable.mood_selector_background_selected);
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
        String quote = "Placeholder Quote"; // Still a placeholder

        if (content.isEmpty()) {
            Toast.makeText(this, "Journal entry cannot be empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (entryIdToEdit == -1) { // New entry
            JournalEntry entry = new JournalEntry(date, selectedMood, title, content, quote);
            long newRowId = dbHelper.insertEntry(entry);
            if (newRowId != -1) {
                Toast.makeText(this, "Entry saved successfully!", Toast.LENGTH_SHORT).show();
                finish(); // Go back to Dashboard
            } else {
                Toast.makeText(this, "Error saving entry.", Toast.LENGTH_SHORT).show();
            }
        } else { // Update existing entry
            if (existingEntry != null) {
                existingEntry.setDate(date);
                existingEntry.setMood(selectedMood);
                existingEntry.setTitle(title);
                existingEntry.setContent(content);
                existingEntry.setQuote(quote); // Update quote if it can be changed

                int rowsAffected = dbHelper.updateEntry(existingEntry);
                if (rowsAffected > 0) {
                    Toast.makeText(this, "Entry updated successfully!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK); // Indicate that the entry was updated
                    finish(); // Go back to EntryDetailActivity or Dashboard
                } else {
                    Toast.makeText(this, "Error updating entry.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}