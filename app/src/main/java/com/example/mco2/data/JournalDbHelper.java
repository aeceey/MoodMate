// src/main/java/com/example/mco2/data/JournalDbHelper.java
package com.example.mco2.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.mco2.model.JournalEntry;

import java.util.ArrayList;
import java.util.List;

public class JournalDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "journal.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_ENTRIES = "journal_entries";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_MOOD = "mood";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_QUOTE = "quote";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_ENTRIES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_DATE + " TEXT NOT NULL," +
                    COLUMN_MOOD + " TEXT," +
                    COLUMN_TITLE + " TEXT," +
                    COLUMN_CONTENT + " TEXT," +
                    COLUMN_QUOTE + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_ENTRIES;

    public JournalDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
        Log.d("JournalDbHelper", "Database created: " + TABLE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
        Log.d("JournalDbHelper", "Database upgraded from version " + oldVersion + " to " + newVersion);
    }

    /**
     * Inserts a new journal entry into the database.
     * @param entry The JournalEntry object to insert.
     * @return The row ID of the newly inserted row, or -1 if an error occurred.
     */
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
        Log.d("JournalDbHelper", "Inserted entry with ID: " + newRowId);
        return newRowId;
    }

    /**
     * Retrieves a journal entry by its ID.
     * @param id The ID of the entry to retrieve.
     * @return The JournalEntry object if found, otherwise null.
     */
    public JournalEntry getEntryById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        JournalEntry entry = null;
        Log.d("JournalDbHelper", "Attempting to retrieve entry with ID: " + id); // New Log

        try {
            cursor = db.query(
                    TABLE_ENTRIES,
                    new String[]{COLUMN_ID, COLUMN_DATE, COLUMN_MOOD, COLUMN_TITLE, COLUMN_CONTENT, COLUMN_QUOTE},
                    COLUMN_ID + "=?",
                    new String[]{String.valueOf(id)},
                    null, null, null, null
            );

            if (cursor != null) {
                Log.d("JournalDbHelper", "Cursor returned with count: " + cursor.getCount()); // New Log
                if (cursor.moveToFirst()) {
                    entry = new JournalEntry();
                    int idIndex = cursor.getColumnIndex(COLUMN_ID);
                    if (idIndex != -1) entry.setId(cursor.getLong(idIndex));
                    else Log.e("JournalDbHelper", "COLUMN_ID not found in cursor."); // New Log

                    int dateIndex = cursor.getColumnIndex(COLUMN_DATE);
                    if (dateIndex != -1) entry.setDate(cursor.getString(dateIndex));
                    else Log.e("JournalDbHelper", "COLUMN_DATE not found in cursor."); // New Log

                    int moodIndex = cursor.getColumnIndex(COLUMN_MOOD);
                    if (moodIndex != -1) entry.setMood(cursor.getString(moodIndex));
                    else Log.e("JournalDbHelper", "COLUMN_MOOD not found in cursor."); // New Log

                    int titleIndex = cursor.getColumnIndex(COLUMN_TITLE);
                    if (titleIndex != -1) entry.setTitle(cursor.getString(titleIndex));
                    else Log.e("JournalDbHelper", "COLUMN_TITLE not found in cursor."); // New Log

                    int contentIndex = cursor.getColumnIndex(COLUMN_CONTENT);
                    if (contentIndex != -1) entry.setContent(cursor.getString(contentIndex));
                    else Log.e("JournalDbHelper", "COLUMN_CONTENT not found in cursor."); // New Log

                    int quoteIndex = cursor.getColumnIndex(COLUMN_QUOTE);
                    if (quoteIndex != -1) entry.setQuote(cursor.getString(quoteIndex));
                    else Log.e("JournalDbHelper", "COLUMN_QUOTE not found in cursor."); // New Log

                    Log.d("JournalDbHelper", "Successfully retrieved entry: " + entry.getTitle() + " (ID: " + entry.getId() + ")"); // New Log
                } else {
                    Log.w("JournalDbHelper", "No entry found for ID: " + id + ". Cursor is empty."); // New Log
                }
            } else {
                Log.e("JournalDbHelper", "Cursor is null for ID: " + id); // New Log
            }
        } catch (Exception e) {
            Log.e("JournalDbHelper", "Error getting entry by ID: " + id, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
            Log.d("JournalDbHelper", "Database connection closed."); // New Log
        }
        return entry;
    }

    /**
     * Retrieves all journal entries from the database.
     * @return A list of all JournalEntry objects.
     */
    public List<JournalEntry> getAllEntries() {
        List<JournalEntry> entryList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        Log.d("JournalDbHelper", "Attempting to retrieve all entries."); // New Log

        try {
            String selectQuery = "SELECT * FROM " + TABLE_ENTRIES + " ORDER BY " + COLUMN_DATE + " DESC";
            cursor = db.rawQuery(selectQuery, null);

            if (cursor != null) {
                Log.d("JournalDbHelper", "All entries cursor count: " + cursor.getCount()); // New Log
                if (cursor.moveToFirst()) {
                    do {
                        JournalEntry entry = new JournalEntry();
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
                } else {
                    Log.w("JournalDbHelper", "No entries found in database."); // New Log
                }
            } else {
                Log.e("JournalDbHelper", "Cursor is null for getAllEntries query."); // New Log
            }
        } catch (Exception e) {
            Log.e("JournalDbHelper", "Error getting all entries: ", e);
            entryList.clear(); // Clear the list on error to avoid partial data
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
            Log.d("JournalDbHelper", "Database connection closed for getAllEntries."); // New Log
        }
        return entryList;
    }

    /**
     * Updates an existing journal entry in the database.
     * @param entry The JournalEntry object with updated data.
     * @return The number of rows affected.
     */
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
        Log.d("JournalDbHelper", "Updated entry with ID: " + entry.getId() + ", Rows affected: " + rowsAffected);
        return rowsAffected;
    }

    /**
     * Deletes a journal entry from the database.
     * @param id The ID of the entry to delete.
     * @return The number of rows affected.
     */
    public int deleteEntry(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_ENTRIES, COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
        Log.d("JournalDbHelper", "Deleted entry with ID: " + id + ", Rows affected: " + rowsAffected);
        return rowsAffected;
    }

    /**
     * Deletes all journal entries from the database.
     */
    public void deleteAllEntries() {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_ENTRIES, null, null);
        db.close();
        Log.d("JournalDbHelper", "Deleted all entries. Rows affected: " + rowsAffected);
    }
}