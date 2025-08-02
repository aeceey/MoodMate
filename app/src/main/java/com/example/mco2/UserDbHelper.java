// src/main/java/com/example/mco2/data/UserDbHelper.java
package com.example.mco2.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UserDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "users.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD_HASH = "password_hash";

    private static final String SQL_CREATE_USERS =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_USERNAME + " TEXT UNIQUE NOT NULL," +
                    COLUMN_PASSWORD_HASH + " TEXT NOT NULL)";

    public UserDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_USERS);
        Log.d("UserDbHelper", "Users database created: " + TABLE_USERS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
        Log.d("UserDbHelper", "Users database upgraded from " + oldVersion + " to " + newVersion);
    }

    public boolean addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD_HASH, hashPassword(password));
        long newRowId = db.insert(TABLE_USERS, null, values);
        db.close();
        Log.d("UserDbHelper", "New user added with ID: " + newRowId);
        return newRowId != -1;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        boolean isValid = false;
        try {
            String hashedPassword = hashPassword(password);
            cursor = db.query(
                    TABLE_USERS,
                    new String[]{COLUMN_ID},
                    COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD_HASH + "=?",
                    new String[]{username, hashedPassword},
                    null, null, null
            );
            isValid = cursor != null && cursor.getCount() > 0;
            Log.d("UserDbHelper", "Login attempt for '" + username + "'. Valid: " + isValid);
        } catch (Exception e) {
            Log.e("UserDbHelper", "Error checking user credentials: " + username, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return isValid;
    }

    public long getUserId(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        long userId = -1;
        Cursor cursor = null;
        try {
            cursor = db.query(
                    TABLE_USERS,
                    new String[]{COLUMN_ID},
                    COLUMN_USERNAME + "=?",
                    new String[]{username},
                    null, null, null
            );
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(COLUMN_ID);
                userId = cursor.getLong(columnIndex);
            }
        } catch (Exception e) {
            Log.e("UserDbHelper", "Error getting user ID for username: " + username, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return userId;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e("UserDbHelper", "SHA-256 algorithm not found.", e);
            return null;
        }
    }
}