// src/main/java/com/example/mco2/Dashboard.java
package com.example.mco2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast; // Make sure Toast is imported if used

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mco2.adapter.JournalEntryAdapter;
import com.example.mco2.data.JournalDbHelper;
import com.example.mco2.model.JournalEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class Dashboard extends AppCompatActivity {

    private RecyclerView recyclerViewEntries;
    private JournalEntryAdapter entryAdapter;
    private JournalDbHelper dbHelper;
    private List<JournalEntry> journalEntries;

    private static final int DETAIL_ACTIVITY_REQUEST_CODE = 1; // For onActivityResult from EntryDetailActivity
    private static final int NEW_ENTRY_ACTIVITY_REQUEST_CODE = 2; // New: For onActivityResult from NewEntryActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        // Handle system insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.dashboard), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new JournalDbHelper(this); // Initialize database helper
        journalEntries = new ArrayList<>(); // Initialize the list

        // Initialize RecyclerView
        recyclerViewEntries = findViewById(R.id.recyclerViewEntries);
        recyclerViewEntries.setLayoutManager(new LinearLayoutManager(this));
        entryAdapter = new JournalEntryAdapter(journalEntries);
        recyclerViewEntries.setAdapter(entryAdapter);

        // Set click listener for RecyclerView items (for View/Edit functionality)
        entryAdapter.setOnItemClickListener(new JournalEntryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(JournalEntry entry) {
                Intent detailIntent = new Intent(Dashboard.this, EntryDetailActivity.class);
                detailIntent.putExtra(EntryDetailActivity.EXTRA_ENTRY_ID, entry.getId());
                // Use startActivityForResult to know when EntryDetailActivity finishes
                startActivityForResult(detailIntent, DETAIL_ACTIVITY_REQUEST_CODE);
            }
        });

        // Initialize FloatingActionButton
        FloatingActionButton fab = findViewById(R.id.fab_add_entry);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(Dashboard.this, NewEntryActivity.class);
            // Change to startActivityForResult to refresh the list after a new entry is saved
            startActivityForResult(intent, NEW_ENTRY_ACTIVITY_REQUEST_CODE);
        });

        // Initial load of entries (optional, onResume will also handle this)
        // loadJournalEntries();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Always refresh entries when the activity comes to the foreground
        loadJournalEntries();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check if the request code matches and if the result was OK
        if (requestCode == DETAIL_ACTIVITY_REQUEST_CODE || requestCode == NEW_ENTRY_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // If either EntryDetailActivity or NewEntryActivity returns RESULT_OK,
                // it means an entry might have been updated, deleted, or a new one added.
                loadJournalEntries(); // Refresh the list of entries
            }
        }
    }

    private void loadJournalEntries() {
        List<JournalEntry> loadedEntries = dbHelper.getAllEntries();
        if (loadedEntries != null) {
            journalEntries.clear(); // Clear existing entries
            journalEntries.addAll(loadedEntries); // Add all loaded entries
            entryAdapter.setEntries(journalEntries); // Update adapter with new data
            Log.d("Dashboard", "Loaded " + journalEntries.size() + " entries."); // For debugging
            if (journalEntries.isEmpty()) {
                Toast.makeText(this, "No entries found. Add a new one!", Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d("Dashboard", "No entries found or error loading.");
            Toast.makeText(this, "Error loading entries or no entries found.", Toast.LENGTH_SHORT).show();
        }
    }
}