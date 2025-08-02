// src/main/java/com/example/mco2/Dashboard.java
package com.example.mco2;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
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
    private long currentUserId;

    private static final int DETAIL_ACTIVITY_REQUEST_CODE = 1;
    private static final int NEW_ENTRY_ACTIVITY_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.dashboard), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        currentUserId = getIntent().getLongExtra("CURRENT_USER_ID", -1);
        if (currentUserId == -1) {
            Toast.makeText(this, "Error: User not logged in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbHelper = new JournalDbHelper(this);
        journalEntries = new ArrayList<>();

        recyclerViewEntries = findViewById(R.id.recyclerViewEntries);
        if (recyclerViewEntries != null) {
            recyclerViewEntries.setLayoutManager(new LinearLayoutManager(this));
            entryAdapter = new JournalEntryAdapter(journalEntries);
            recyclerViewEntries.setAdapter(entryAdapter);

            entryAdapter.setOnItemClickListener(entry -> {
                Intent detailIntent = new Intent(Dashboard.this, EntryDetailActivity.class);
                detailIntent.putExtra(EntryDetailActivity.EXTRA_ENTRY_ID, entry.getId());
                detailIntent.putExtra("CURRENT_USER_ID", currentUserId);
                startActivityForResult(detailIntent, DETAIL_ACTIVITY_REQUEST_CODE);
            });
        } else {
            Log.e("Dashboard", "RecyclerView with ID 'recyclerViewEntries' not found.");
            Toast.makeText(this, "Application error. Please contact support.", Toast.LENGTH_SHORT).show();
            return;
        }

        FloatingActionButton fab = findViewById(R.id.fab_add_entry);
        if (fab != null) {
            fab.setOnClickListener(view -> {
                Intent intent = new Intent(Dashboard.this, NewEntryActivity.class);
                intent.putExtra("CURRENT_USER_ID", currentUserId);
                startActivityForResult(intent, NEW_ENTRY_ACTIVITY_REQUEST_CODE);
            });
        } else {
            Log.e("Dashboard", "FloatingActionButton with ID 'fab_add_entry' not found.");
            Toast.makeText(this, "Application error. Please contact support.", Toast.LENGTH_SHORT).show();
            return;
        }

        loadJournalEntries();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentUserId != -1) {
            loadJournalEntries();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DETAIL_ACTIVITY_REQUEST_CODE || requestCode == NEW_ENTRY_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                loadJournalEntries();
            }
        }
    }

    private void loadJournalEntries() {
        new LoadJournalEntriesTask().execute(currentUserId);
    }

    private class LoadJournalEntriesTask extends AsyncTask<Long, Void, List<JournalEntry>> {
        @Override
        protected List<JournalEntry> doInBackground(Long... userIds) {
            if (userIds.length > 0) {
                long userId = userIds[0];
                return dbHelper.getAllEntries(userId);
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<JournalEntry> loadedEntries) {
            super.onPostExecute(loadedEntries);
            if (loadedEntries != null) {
                journalEntries.clear();
                journalEntries.addAll(loadedEntries);
                if (entryAdapter != null) {
                    entryAdapter.setEntries(journalEntries);
                }
                Log.d("Dashboard", "Loaded " + journalEntries.size() + " entries for user " + currentUserId);
                if (journalEntries.isEmpty()) {
                    Toast.makeText(Dashboard.this, "No entries found. Add a new one!", Toast.LENGTH_LONG).show();
                }
            } else {
                Log.d("Dashboard", "No entries found or error loading.");
                Toast.makeText(Dashboard.this, "Error loading entries or no entries found.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}