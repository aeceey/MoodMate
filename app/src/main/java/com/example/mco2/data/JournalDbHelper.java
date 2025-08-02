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

import com.example.mco2.data.UserDbHelper;

public class JournalDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "journal.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_JOURNAL = "journal_entries";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_MOOD = "mood";
    public static final String COLUMN_QUOTE = "quote";
    public static final String COLUMN_USER_ID = "user_id";

    private static final String SQL_CREATE_JOURNAL =
            "CREATE TABLE " + TABLE_JOURNAL + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_TITLE + " TEXT," +
                    COLUMN_CONTENT + " TEXT," +
                    COLUMN_DATE + " TEXT NOT NULL," +
                    COLUMN_MOOD + " TEXT," +
                    COLUMN_QUOTE + " TEXT," +
                    COLUMN_USER_ID + " INTEGER NOT NULL" +
                    ")"; // Removed foreign key constraint for now

    public JournalDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Enable foreign key constraints
        db.execSQL("PRAGMA foreign_keys=ON");

        // Create the journal table
        db.execSQL(SQL_CREATE_JOURNAL);
        Log.d("JournalDbHelper", "Journal database created: " + TABLE_JOURNAL);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        // Enable foreign key constraints every time database is opened
        db.execSQL("PRAGMA foreign_keys=ON");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_JOURNAL);
        onCreate(db);
        Log.d("JournalDbHelper", "Journal database upgraded from " + oldVersion + " to " + newVersion);
    }

    public long insertEntry(JournalEntry entry) {
        SQLiteDatabase db = null;
        long newRowId = -1;

        try {
            db = this.getWritableDatabase();

            // Validate that all required fields are present
            if (entry.getUserId() <= 0) {
                Log.e("JournalDbHelper", "Invalid user ID: " + entry.getUserId());
                return -1;
            }

            if (entry.getDate() == null || entry.getDate().trim().isEmpty()) {
                Log.e("JournalDbHelper", "Date is required but not provided");
                return -1;
            }

            ContentValues values = new ContentValues();
            values.put(COLUMN_USER_ID, entry.getUserId());
            values.put(COLUMN_TITLE, entry.getTitle() != null ? entry.getTitle() : "");
            values.put(COLUMN_CONTENT, entry.getContent() != null ? entry.getContent() : "");
            values.put(COLUMN_DATE, entry.getDate());
            values.put(COLUMN_MOOD, entry.getMood() != null ? entry.getMood() : "");
            values.put(COLUMN_QUOTE, entry.getQuote() != null ? entry.getQuote() : "");

            newRowId = db.insert(TABLE_JOURNAL, null, values);

            if (newRowId == -1) {
                Log.e("JournalDbHelper", "Failed to insert journal entry");
            } else {
                Log.d("JournalDbHelper", "Successfully inserted journal entry with ID: " + newRowId);
            }

        } catch (Exception e) {
            Log.e("JournalDbHelper", "Error inserting journal entry", e);
            newRowId = -1;
        } finally {
            if (db != null) {
                db.close();
            }
        }

        return newRowId;
    }

    public List<JournalEntry> getAllEntries(long userId) {
        List<JournalEntry> entries = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();
            cursor = db.query(
                    TABLE_JOURNAL,
                    null,
                    COLUMN_USER_ID + " = ?",
                    new String[]{String.valueOf(userId)},
                    null, null, COLUMN_DATE + " DESC"
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int idIndex = cursor.getColumnIndexOrThrow(COLUMN_ID);
                    int titleIndex = cursor.getColumnIndexOrThrow(COLUMN_TITLE);
                    int contentIndex = cursor.getColumnIndexOrThrow(COLUMN_CONTENT);
                    int dateIndex = cursor.getColumnIndexOrThrow(COLUMN_DATE);
                    int moodIndex = cursor.getColumnIndexOrThrow(COLUMN_MOOD);
                    int quoteIndex = cursor.getColumnIndexOrThrow(COLUMN_QUOTE);
                    int userIdIndex = cursor.getColumnIndexOrThrow(COLUMN_USER_ID);

                    JournalEntry entry = new JournalEntry();
                    entry.setId(cursor.getLong(idIndex));
                    entry.setTitle(cursor.getString(titleIndex));
                    entry.setContent(cursor.getString(contentIndex));
                    entry.setDate(cursor.getString(dateIndex));
                    entry.setMood(cursor.getString(moodIndex));
                    entry.setQuote(cursor.getString(quoteIndex));
                    entry.setUserId(cursor.getLong(userIdIndex));

                    entries.add(entry);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("JournalDbHelper", "Error getting all entries for user: " + userId, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return entries;
    }

    public JournalEntry getEntryById(long entryId, long userId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        JournalEntry entry = null;

        try {
            db = this.getReadableDatabase();

            String[] projection = {
                    COLUMN_ID,
                    COLUMN_USER_ID,
                    COLUMN_TITLE,
                    COLUMN_CONTENT,
                    COLUMN_DATE,
                    COLUMN_MOOD,
                    COLUMN_QUOTE
            };

            String selection = COLUMN_ID + " = ? AND " + COLUMN_USER_ID + " = ?";
            String[] selectionArgs = {String.valueOf(entryId), String.valueOf(userId)};

            cursor = db.query(
                    TABLE_JOURNAL,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
                long entryUserId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                String content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));
                String mood = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MOOD));
                String quote = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_QUOTE));

                entry = new JournalEntry(id, entryUserId, date, mood, title, content, quote);
            }
        } catch (Exception e) {
            Log.e("JournalDbHelper", "Error getting entry by ID: " + entryId, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return entry;
    }

    public int updateEntry(JournalEntry entry) {
        SQLiteDatabase db = null;
        int count = 0;

        try {
            db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(COLUMN_TITLE, entry.getTitle() != null ? entry.getTitle() : "");
            values.put(COLUMN_CONTENT, entry.getContent() != null ? entry.getContent() : "");
            values.put(COLUMN_DATE, entry.getDate());
            values.put(COLUMN_MOOD, entry.getMood() != null ? entry.getMood() : "");
            values.put(COLUMN_QUOTE, entry.getQuote() != null ? entry.getQuote() : "");

            String selection = COLUMN_ID + " = ? AND " + COLUMN_USER_ID + " = ?";
            String[] selectionArgs = {String.valueOf(entry.getId()), String.valueOf(entry.getUserId())};

            count = db.update(TABLE_JOURNAL, values, selection, selectionArgs);

            if (count > 0) {
                Log.d("JournalDbHelper", "Successfully updated entry with ID: " + entry.getId());
            } else {
                Log.w("JournalDbHelper", "No rows updated for entry ID: " + entry.getId());
            }

        } catch (Exception e) {
            Log.e("JournalDbHelper", "Error updating entry with ID: " + entry.getId(), e);
        } finally {
            if (db != null) {
                db.close();
            }
        }

        return count;
    }

    public int deleteEntry(long entryId, long userId) {
        SQLiteDatabase db = null;
        int rowsAffected = 0;

        try {
            db = this.getWritableDatabase();
            rowsAffected = db.delete(
                    TABLE_JOURNAL,
                    COLUMN_ID + "=? AND " + COLUMN_USER_ID + "=?",
                    new String[]{String.valueOf(entryId), String.valueOf(userId)}
            );

            if (rowsAffected > 0) {
                Log.d("JournalDbHelper", "Successfully deleted entry with ID: " + entryId);
            } else {
                Log.w("JournalDbHelper", "No rows deleted for entry ID: " + entryId);
            }

        } catch (Exception e) {
            Log.e("JournalDbHelper", "Error deleting entry with ID: " + entryId, e);
        } finally {
            if (db != null) {
                db.close();
            }
        }

        return rowsAffected;
    }
}