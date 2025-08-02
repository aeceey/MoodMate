// src/main/java/com/example/mco2/Register.java
package com.example.mco2;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.mco2.data.UserDbHelper;

public class Register extends AppCompatActivity {

    private EditText etRegisterUsername;
    private EditText etRegisterEmail;
    private EditText etRegisterPassword;
    private UserDbHelper userDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etRegisterUsername = findViewById(R.id.activity_register_username);
        etRegisterEmail = findViewById(R.id.activity_register_email);
        etRegisterPassword = findViewById(R.id.activity_register_password);

        userDbHelper = new UserDbHelper(this);

        Button registerButton = findViewById(R.id.btn_register);
        if (registerButton != null) {
            registerButton.setOnClickListener(v -> {
                String username = etRegisterUsername.getText().toString().trim();
                String email = etRegisterEmail.getText().toString().trim();
                String password = etRegisterPassword.getText().toString().trim();

                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(Register.this, "Please fill in all registration details.", Toast.LENGTH_SHORT).show();
                } else {
                    if (userDbHelper.addUser(username, password)) {
                        Toast.makeText(Register.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Register.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(Register.this, "Registration failed. Username may already exist.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}