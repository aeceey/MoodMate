// src/main/java/com/example/mco2/data/UserDbHelper.java
package com.example.mco2;

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
        Log.d("UserDbHelper", "Users database upgraded from version " + oldVersion + " to " + newVersion);
    }

    /**
     * Registers a new user by inserting their username and hashed password into the database.
     * @param username The username for the new user.
     * @param password The plain text password to be hashed and stored.
     * @return true if registration is successful, false otherwise (e.g., username already exists).
     */
    public boolean registerUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        String hashedPassword = hashPassword(password);

        if (hashedPassword == null) {
            Log.e("UserDbHelper", "Failed to hash password.");
            return false;
        }

        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD_HASH, hashedPassword);

        long newRowId = db.insert(TABLE_USERS, null, values);
        db.close();
        Log.d("UserDbHelper", "Registered user: " + username + " with ID: " + newRowId);
        return newRowId != -1;
    }

    /**
     * Checks if a username already exists in the database.
     * @param username The username to check.
     * @return true if the username is already taken, false otherwise.
     */
    public boolean isUsernameTaken(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        boolean exists = false;
        try {
            cursor = db.query(
                    TABLE_USERS,
                    new String[]{COLUMN_USERNAME},
                    COLUMN_USERNAME + "=?",
                    new String[]{username},
                    null, null, null
            );
            exists = cursor != null && cursor.getCount() > 0;
            Log.d("UserDbHelper", "Checking username '" + username + "'. Exists: " + exists);
        } catch (Exception e) {
            Log.e("UserDbHelper", "Error checking if username exists: " + username, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return exists;
    }

    /**
     * Authenticates a user by checking their username and password against the database.
     * @param username The username to authenticate.
     * @param password The plain text password to authenticate.
     * @return true if credentials are valid, false otherwise.
     */
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        boolean isValid = false;
        String hashedPassword = hashPassword(password);

        if (hashedPassword == null) {
            Log.e("UserDbHelper", "Failed to hash password for login check.");
            return false;
        }

        try {
            cursor = db.query(
                    TABLE_USERS,
                    new String[]{COLUMN_ID}, // We only need to know if a record exists
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

    /**
     * Hashes a plain text password using SHA-256.
     * @param password The plain text password.
     * @return The SHA-256 hash as a hexadecimal string, or null if hashing fails.
     */
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