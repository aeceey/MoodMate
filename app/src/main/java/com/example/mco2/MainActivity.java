package com.example.mco2;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences; // Import SharedPreferences
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.text.TextUtils;


public class MainActivity extends AppCompatActivity {

    private EditText etLoginUsername;
    private EditText etLoginPassword;

    // Define the same SharedPreferences name and keys as in Register.java
    private static final String PREF_NAME = "UserPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_IS_REGISTERED = "is_registered";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize EditTexts
        etLoginUsername = findViewById(R.id.activity_login_tv_username);
        etLoginPassword = findViewById(R.id.activity_login_tv_password);

        // --- Code for the Login Button ---
        Button loginButton = findViewById(R.id.activity_login_btn_login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etLoginUsername.getText().toString().trim();
                String password = etLoginPassword.getText().toString().trim();

                // Get stored credentials
                SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
                String storedUsername = prefs.getString(KEY_USERNAME, null); // null if not found
                String storedPassword = prefs.getString(KEY_PASSWORD, null); // null if not found
                boolean isRegistered = prefs.getBoolean(KEY_IS_REGISTERED, false); // Check if any user is registered

                // Validation logic
                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                    Toast.makeText(MainActivity.this, "Please enter both username and password.", Toast.LENGTH_SHORT).show();
                } else if (!isRegistered) {
                    Toast.makeText(MainActivity.this, "No account registered. Please register first.", Toast.LENGTH_LONG).show();
                } else if (storedUsername == null || storedPassword == null) {
                    // This case handles if isRegistered is true but keys are null (shouldn't happen if saved correctly)
                    Toast.makeText(MainActivity.this, "Error: Stored credentials not found. Please re-register.", Toast.LENGTH_LONG).show();
                } else if (username.equals(storedUsername) && password.equals(storedPassword)) {
                    Toast.makeText(MainActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, Dashboard.class);
                    startActivity(intent);
                    finish(); // Closes MainActivity
                } else {
                    Toast.makeText(MainActivity.this, "Invalid username or password.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // --- Code for the Register TextView ---
        TextView registerTextView = findViewById(R.id.activity_login_tv_register);
        registerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Register.class);
                startActivity(intent);
            }
        });
    }
}