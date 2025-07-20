// src/main/java/com/example/mco2/EntryDetailActivity.java
package com.example.mco2;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mco2.data.JournalDbHelper;
import com.example.mco2.model.JournalEntry;

public class EntryDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ENTRY_ID = "entry_id"; // Key for passing entry ID
    private long entryId = -1;
    private JournalDbHelper dbHelper;
    private JournalEntry currentEntry;

    private TextView tvDetailDate, tvDetailMood, tvDetailTitle, tvDetailContent, tvDetailQuote;
    private Button btnEditEntry, btnDeleteEntry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_detail);

        dbHelper = new JournalDbHelper(this);

        // Initialize UI elements
        tvDetailDate = findViewById(R.id.tv_detail_date);
        tvDetailMood = findViewById(R.id.tv_detail_mood);
        tvDetailTitle = findViewById(R.id.tv_detail_title);
        tvDetailContent = findViewById(R.id.tv_detail_content);
        tvDetailQuote = findViewById(R.id.tv_detail_quote);
        btnEditEntry = findViewById(R.id.btn_edit_entry);
        btnDeleteEntry = findViewById(R.id.btn_delete_entry);

        // Get entry ID from Intent
        if (getIntent().hasExtra(EXTRA_ENTRY_ID)) {
            entryId = getIntent().getLongExtra(EXTRA_ENTRY_ID, -1);
            if (entryId != -1) {
                loadEntryDetails();
            } else {
                Toast.makeText(this, "Error: Invalid entry ID.", Toast.LENGTH_SHORT).show();
                finish(); // Close activity if ID is invalid
            }
        } else {
            Toast.makeText(this, "Error: No entry ID provided.", Toast.LENGTH_SHORT).show();
            finish(); // Close activity if no ID
        }

        // Set click listeners for buttons
        btnEditEntry.setOnClickListener(v -> {
            // TODO: Implement Edit functionality
            // For now, it will open NewEntryActivity for editing
            if (currentEntry != null) {
                Intent intent = new Intent(EntryDetailActivity.this, NewEntryActivity.class);
                intent.putExtra(NewEntryActivity.EXTRA_EDIT_ENTRY_ID, entryId); // Pass ID for editing
                startActivity(intent);
            }
        });

        btnDeleteEntry.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh entry details in case it was edited in NewEntryActivity
        if (entryId != -1) {
            loadEntryDetails();
        }
    }

    private void loadEntryDetails() {
        // Fetch a single entry by ID (you'll need to add this method to JournalDbHelper)
        currentEntry = dbHelper.getEntryById(entryId); // This method needs to be added to JournalDbHelper

        if (currentEntry != null) {
            tvDetailDate.setText("Date: " + currentEntry.getDate());
            tvDetailMood.setText("Mood: " + currentEntry.getMood());
            tvDetailTitle.setText(currentEntry.getTitle().isEmpty() ? "No Title" : currentEntry.getTitle());
            tvDetailContent.setText(currentEntry.getContent());
            tvDetailQuote.setText("Quote: " + (currentEntry.getQuote().isEmpty() ? "N/A" : currentEntry.getQuote()));
        } else {
            Toast.makeText(this, "Entry not found.", Toast.LENGTH_SHORT).show();
            finish(); // Close activity if entry not found
        }
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Entry")
                .setMessage("Are you sure you want to permanently delete this journal entry?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deleteEntry();
                    }
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteEntry() {
        if (entryId != -1) {
            dbHelper.deleteEntry(entryId);
            Toast.makeText(this, "Entry deleted successfully!", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK); // Indicate that an entry was deleted
            finish(); // Go back to Dashboard
        } else {
            Toast.makeText(this, "Error deleting entry: ID not found.", Toast.LENGTH_SHORT).show();
        }
    }
}