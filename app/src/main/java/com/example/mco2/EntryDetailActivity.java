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

    public static final String EXTRA_ENTRY_ID = "entry_id";
    private long entryId = -1;
    private long currentUserId; // New: field to hold the user's ID
    private JournalDbHelper dbHelper;
    private JournalEntry currentEntry;

    private TextView tvDetailDate, tvDetailMood, tvDetailTitle, tvDetailContent, tvDetailQuote;
    private Button btnEditEntry, btnDeleteEntry;

    private static final int EDIT_ENTRY_REQUEST_CODE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_detail);

        dbHelper = new JournalDbHelper(this);

        // Get the user ID from the intent
        currentUserId = getIntent().getLongExtra("CURRENT_USER_ID", -1);
        if (currentUserId == -1) {
            Toast.makeText(this, "Error: User not logged in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvDetailDate = findViewById(R.id.tv_detail_date);
        if (tvDetailDate == null) Log.e("EntryDetailActivity", "tvDetailDate is null. Check activity_entry_detail.xml for @id/tv_detail_date");
        tvDetailMood = findViewById(R.id.tv_detail_mood);
        if (tvDetailMood == null) Log.e("EntryDetailActivity", "tvDetailMood is null. Check activity_entry_detail.xml for @id/tv_detail_mood");
        tvDetailTitle = findViewById(R.id.tv_detail_title);
        if (tvDetailTitle == null) Log.e("EntryDetailActivity", "tvDetailTitle is null. Check activity_entry_detail.xml for @id/tv_detail_title");
        tvDetailContent = findViewById(R.id.tv_detail_content);
        if (tvDetailContent == null) Log.e("EntryDetailActivity", "tvDetailContent is null. Check activity_entry_detail.xml for @id/tv_detail_content");
        tvDetailQuote = findViewById(R.id.tv_detail_quote);
        if (tvDetailQuote == null) Log.e("EntryDetailActivity", "tvDetailQuote is null. Check activity_entry_detail.xml for @id/tv_detail_quote");

        btnEditEntry = findViewById(R.id.btn_edit_entry);
        if (btnEditEntry == null) Log.e("EntryDetailActivity", "btnEditEntry is null. Check activity_entry_detail.xml for @id/btn_edit_entry");
        btnDeleteEntry = findViewById(R.id.btn_delete_entry);
        if (btnDeleteEntry == null) Log.e("EntryDetailActivity", "btnDeleteEntry is null. Check activity_entry_detail.xml for @id/btn_delete_entry");

        if (getIntent().hasExtra(EXTRA_ENTRY_ID)) {
            entryId = getIntent().getLongExtra(EXTRA_ENTRY_ID, -1);
            Log.d("EntryDetailActivity", "Received entry ID: " + entryId);
            if (entryId != -1) {
                loadEntryDetails();
            } else {
                Toast.makeText(this, "Error: Invalid entry ID provided.", Toast.LENGTH_SHORT).show();
                Log.e("EntryDetailActivity", "Invalid entry ID: " + entryId);
                finish();
            }
        } else {
            Toast.makeText(this, "Error: No entry ID provided.", Toast.LENGTH_SHORT).show();
            Log.e("EntryDetailActivity", "No entry ID provided in Intent.");
            finish();
        }

        if (btnEditEntry != null) {
            btnEditEntry.setOnClickListener(v -> {
                if (currentEntry != null && entryId != -1) {
                    Intent intent = new Intent(EntryDetailActivity.this, NewEntryActivity.class);
                    intent.putExtra(NewEntryActivity.EXTRA_EDIT_ENTRY_ID, entryId);
                    intent.putExtra("CURRENT_USER_ID", currentUserId); // Pass user ID
                    startActivityForResult(intent, EDIT_ENTRY_REQUEST_CODE);
                    Log.d("EntryDetailActivity", "Launching NewEntryActivity for editing ID: " + entryId);
                } else {
                    Toast.makeText(EntryDetailActivity.this, "Cannot edit, entry data not available.", Toast.LENGTH_SHORT).show();
                    Log.w("EntryDetailActivity", "Attempted to edit null entry or invalid ID.");
                }
            });
        }

        if (btnDeleteEntry != null) {
            btnDeleteEntry.setOnClickListener(v -> showDeleteConfirmationDialog());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (entryId != -1 && currentUserId != -1) {
            loadEntryDetails();
        } else {
            Log.w("EntryDetailActivity", "onResume: entryId or userId is invalid, finishing activity.");
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_ENTRY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Log.d("EntryDetailActivity", "Returned from NewEntryActivity with RESULT_OK. Refreshing details.");
                loadEntryDetails();
                setResult(RESULT_OK);
            } else {
                Log.d("EntryDetailActivity", "Returned from NewEntryActivity with non-OK result.");
            }
        }
    }

    private void loadEntryDetails() {
        // Use the new getEntryById method with the user ID
        currentEntry = dbHelper.getEntryById(entryId, currentUserId);

        if (currentEntry != null) {
            if (tvDetailDate != null) tvDetailDate.setText("Date: " + currentEntry.getDate());
            if (tvDetailMood != null) tvDetailMood.setText("Mood: " + currentEntry.getMood());
            if (tvDetailTitle != null) tvDetailTitle.setText(currentEntry.getTitle().isEmpty() ? "No Title" : currentEntry.getTitle());
            if (tvDetailContent != null) tvDetailContent.setText(currentEntry.getContent());
            if (tvDetailQuote != null) tvDetailQuote.setText("Quote: " + (currentEntry.getQuote().isEmpty() ? "N/A" : currentEntry.getQuote()));
            Log.d("EntryDetailActivity", "Entry details loaded for ID: " + entryId + ", Title: " + currentEntry.getTitle());
        } else {
            Toast.makeText(this, "Entry not found in database.", Toast.LENGTH_SHORT).show();
            Log.e("EntryDetailActivity", "Entry with ID " + entryId + " for user " + currentUserId + " not found.");
            setResult(RESULT_OK);
            finish();
        }
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Entry")
                .setMessage("Are you sure you want to permanently delete this journal entry?")
                .setPositiveButton("Delete", (dialog, which) -> deleteEntry())
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteEntry() {
        if (entryId != -1) {
            // Use the new deleteEntry method with the user ID
            int rowsAffected = dbHelper.deleteEntry(entryId, currentUserId);
            if (rowsAffected > 0) {
                Toast.makeText(this, "Entry deleted successfully!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
                Log.d("EntryDetailActivity", "Entry with ID " + entryId + " deleted successfully.");
            } else {
                Toast.makeText(this, "Error deleting entry or entry not found.", Toast.LENGTH_SHORT).show();
                Log.e("EntryDetailActivity", "Failed to delete entry with ID: " + entryId + " for user " + currentUserId);
            }
        } else {
            Toast.makeText(this, "Error deleting entry: ID not found.", Toast.LENGTH_SHORT).show();
            Log.e("EntryDetailActivity", "Attempted to delete entry with invalid ID: " + entryId);
        }
    }
}