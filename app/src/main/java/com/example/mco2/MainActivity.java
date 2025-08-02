// src/main/java/com/example/mco2/MainActivity.java
package com.example.mco2;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mco2.data.UserDbHelper;

public class MainActivity extends AppCompatActivity {

    private EditText etLoginUsername;
    private EditText etLoginPassword;
    private UserDbHelper userDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etLoginUsername = findViewById(R.id.activity_login_tv_username);
        etLoginPassword = findViewById(R.id.activity_login_tv_password);
        userDbHelper = new UserDbHelper(this);

        Button loginButton = findViewById(R.id.activity_login_btn_login);
        if (loginButton != null) {
            loginButton.setOnClickListener(v -> {
                String username = etLoginUsername.getText().toString().trim();
                String password = etLoginPassword.getText().toString().trim();

                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                    Toast.makeText(MainActivity.this, "Please enter both username and password.", Toast.LENGTH_SHORT).show();
                } else {
                    if (userDbHelper.checkUser(username, password)) {
                        long currentUserId = userDbHelper.getUserId(username);
                        if (currentUserId != -1) {
                            Toast.makeText(MainActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MainActivity.this, Dashboard.class);
                            intent.putExtra("CURRENT_USER_ID", currentUserId);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(MainActivity.this, "Error: User ID not found.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Invalid username or password.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        TextView registerTextView = findViewById(R.id.activity_login_tv_register);
        if (registerTextView != null) {
            registerTextView.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, Register.class);
                startActivity(intent);
            });
        }
    }
}