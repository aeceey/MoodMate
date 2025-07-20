// src/main/java/com/example/mco2/Dashboard.java
package com.example.mco2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast; // Added import for Toast

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

    // Request code for starting EntryDetailActivity if you want to get a result back (e.g., if deleted)
    private static final int DETAIL_ACTIVITY_REQUEST_CODE = 1;

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

        dbHelper = new JournalDbHelper(this);
        journalEntries = new ArrayList<>();

        // Initialize RecyclerView
        recyclerViewEntries = findViewById(R.id.recyclerViewEntries);
        recyclerViewEntries.setLayoutManager(new LinearLayoutManager(this));
        entryAdapter = new JournalEntryAdapter(journalEntries);
        recyclerViewEntries.setAdapter(entryAdapter);

        // Set click listener for RecyclerView items
        entryAdapter.setOnItemClickListener(new JournalEntryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(JournalEntry entry) {
                Intent detailIntent = new Intent(Dashboard.this, EntryDetailActivity.class);
                detailIntent.putExtra(EntryDetailActivity.EXTRA_ENTRY_ID, entry.getId());
                // Use startActivityForResult if you need to know if an entry was deleted
                startActivityForResult(detailIntent, DETAIL_ACTIVITY_REQUEST_CODE);
            }
        });

        // Initialize FloatingActionButton
        FloatingActionButton fab = findViewById(R.id.fab_add_entry);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(Dashboard.this, NewEntryActivity.class);
            startActivity(intent); // No need for result for adding a new entry unless you want immediate refresh
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadJournalEntries(); // Always refresh entries when the activity resumes
    }

    // Handle results from other activities (e.g., EntryDetailActivity after deletion)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DETAIL_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // If EntryDetailActivity returns RESULT_OK, it means an entry might have been deleted
                loadJournalEntries(); // Refresh the list
            }
        }
    }

    private void loadJournalEntries() {
        List<JournalEntry> loadedEntries = dbHelper.getAllEntries();
        if (loadedEntries != null) {
            journalEntries.clear();
            journalEntries.addAll(loadedEntries);
            entryAdapter.setEntries(journalEntries); // Notify adapter of data change
            Log.d("Dashboard", "Loaded " + journalEntries.size() + " entries.");
        } else {
            Log.d("Dashboard", "No entries found.");
        }
    }
}