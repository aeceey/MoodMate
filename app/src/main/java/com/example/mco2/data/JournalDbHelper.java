// src/main/java/com/example/mco2/data/JournalDbHelper.java
package com.example.mco2.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log; // Import Log for debugging

import com.example.mco2.model.JournalEntry;

import java.util.ArrayList;
import java.util.List;

public class JournalDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "journal.db";
    private static final int DATABASE_VERSION = 1;

    // Table and column names
    public static final String TABLE_ENTRIES = "journal_entries";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_MOOD = "mood";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_QUOTE = "quote";

    // SQL statement to create the entries table
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_ENTRIES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_DATE + " TEXT NOT NULL," +
                    COLUMN_MOOD + " TEXT," +
                    COLUMN_TITLE + " TEXT," +
                    COLUMN_CONTENT + " TEXT NOT NULL," +
                    COLUMN_QUOTE + " TEXT);";

    public JournalDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
        Log.d("JournalDbHelper", "Database created: " + SQL_CREATE_ENTRIES); // For debugging
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This simple upgrade policy discards all data and recreates the table.
        // For a real app, you'd implement a proper migration strategy.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ENTRIES);
        onCreate(db);
        Log.d("JournalDbHelper", "Database upgraded from version " + oldVersion + " to " + newVersion); // For debugging
    }

    // --- CRUD Operations ---

    // Insert a new journal entry
    public long insertEntry(JournalEntry entry) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, entry.getDate());
        values.put(COLUMN_MOOD, entry.getMood());
        values.put(COLUMN_TITLE, entry.getTitle());
        values.put(COLUMN_CONTENT, entry.getContent());
        values.put(COLUMN_QUOTE, entry.getQuote());
        long newRowId = db.insert(TABLE_ENTRIES, null, values);
        db.close();
        Log.d("JournalDbHelper", "Inserted new entry with ID: " + newRowId); // For debugging
        return newRowId;
    }

    // New method: Get a single journal entry by ID
    public JournalEntry getEntryById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_ENTRIES,
                new String[]{COLUMN_ID, COLUMN_DATE, COLUMN_MOOD, COLUMN_TITLE, COLUMN_CONTENT, COLUMN_QUOTE},
                COLUMN_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null, null
        );

        JournalEntry entry = null;
        if (cursor != null && cursor.moveToFirst()) {
            entry = new JournalEntry();
            int idIndex = cursor.getColumnIndex(COLUMN_ID);
            if (idIndex != -1) entry.setId(cursor.getLong(idIndex));

            int dateIndex = cursor.getColumnIndex(COLUMN_DATE);
            if (dateIndex != -1) entry.setDate(cursor.getString(dateIndex));

            int moodIndex = cursor.getColumnIndex(COLUMN_MOOD);
            if (moodIndex != -1) entry.setMood(cursor.getString(moodIndex));

            int titleIndex = cursor.getColumnIndex(COLUMN_TITLE);
            if (titleIndex != -1) entry.setTitle(cursor.getString(titleIndex));

            int contentIndex = cursor.getColumnIndex(COLUMN_CONTENT);
            if (contentIndex != -1) entry.setContent(cursor.getString(contentIndex));

            int quoteIndex = cursor.getColumnIndex(COLUMN_QUOTE);
            if (quoteIndex != -1) entry.setQuote(cursor.getString(quoteIndex));

            cursor.close();
        }
        db.close();
        return entry;
    }

    // Get all journal entries
    public List<JournalEntry> getAllEntries() {
        List<JournalEntry> entryList = new ArrayList<>();
        // Order by date in descending order for timeline view
        String selectQuery = "SELECT * FROM " + TABLE_ENTRIES + " ORDER BY " + COLUMN_DATE + " DESC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                JournalEntry entry = new JournalEntry();
                // It's safer to get column index first, then retrieve value
                int idIndex = cursor.getColumnIndex(COLUMN_ID);
                if (idIndex != -1) entry.setId(cursor.getLong(idIndex));

                int dateIndex = cursor.getColumnIndex(COLUMN_DATE);
                if (dateIndex != -1) entry.setDate(cursor.getString(dateIndex));

                int moodIndex = cursor.getColumnIndex(COLUMN_MOOD);
                if (moodIndex != -1) entry.setMood(cursor.getString(moodIndex));

                int titleIndex = cursor.getColumnIndex(COLUMN_TITLE);
                if (titleIndex != -1) entry.setTitle(cursor.getString(titleIndex));

                int contentIndex = cursor.getColumnIndex(COLUMN_CONTENT);
                if (contentIndex != -1) entry.setContent(cursor.getString(contentIndex));

                int quoteIndex = cursor.getColumnIndex(COLUMN_QUOTE);
                if (quoteIndex != -1) entry.setQuote(cursor.getString(quoteIndex));

                entryList.add(entry);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        Log.d("JournalDbHelper", "Fetched " + entryList.size() + " entries."); // For debugging
        return entryList;
    }

    // Update an existing journal entry
    public int updateEntry(JournalEntry entry) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, entry.getDate());
        values.put(COLUMN_MOOD, entry.getMood());
        values.put(COLUMN_TITLE, entry.getTitle());
        values.put(COLUMN_CONTENT, entry.getContent());
        values.put(COLUMN_QUOTE, entry.getQuote());
        int rowsAffected = db.update(TABLE_ENTRIES, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(entry.getId())});
        db.close();
        Log.d("JournalDbHelper", "Updated entry with ID: " + entry.getId() + ", Rows Affected: " + rowsAffected); // For debugging
        return rowsAffected;
    }

    // Delete a journal entry by ID
    public void deleteEntry(long entryId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ENTRIES, COLUMN_ID + " = ?",
                new String[]{String.valueOf(entryId)});
        db.close();
        Log.d("JournalDbHelper", "Deleted entry with ID: " + entryId); // For debugging
    }

    // Delete all journal entries
    public void deleteAllEntries() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ENTRIES, null, null);
        db.close();
        Log.d("JournalDbHelper", "Deleted all entries."); // For debugging
    }
}