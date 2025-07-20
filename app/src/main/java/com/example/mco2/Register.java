package com.example.mco2;

import android.content.Intent;
import android.content.SharedPreferences; // Import SharedPreferences
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.text.TextUtils;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Register extends AppCompatActivity {

    private EditText etRegisterUsername;
    private EditText etRegisterEmail; // Although not saved, it's good for validation
    private EditText etRegisterPassword;
    private EditText etRegisterConfirmPassword; // Add this if you intend to use it in XML

    // Define a name for your SharedPreferences file
    private static final String PREF_NAME = "UserPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_IS_REGISTERED = "is_registered"; // To check if any user is registered

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

        // Initialize EditTexts
        etRegisterUsername = findViewById(R.id.activity_register_username);
        etRegisterEmail = findViewById(R.id.activity_register_email);
        etRegisterPassword = findViewById(R.id.activity_register_password);
        // If you have a confirm password field in activity_register.xml, uncomment and initialize:
        // etRegisterConfirmPassword = findViewById(R.id.activity_register_confirm_password);


        Button btnRegister = findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etRegisterUsername.getText().toString().trim();
                String email = etRegisterEmail.getText().toString().trim();
                String password = etRegisterPassword.getText().toString().trim();
                // String confirmPassword = etRegisterConfirmPassword.getText().toString().trim(); // If you use confirm password

                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(Register.this, "Please fill in all registration details.", Toast.LENGTH_SHORT).show();
                }
                // Add more specific validation if needed
                // For example, checking email format, password strength
                // Or if you have confirm password:
                // else if (!password.equals(confirmPassword)) {
                //    Toast.makeText(Register.this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
                // }
                else {
                    // Save credentials to SharedPreferences
                    SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(KEY_USERNAME, username);
                    editor.putString(KEY_PASSWORD, password);
                    editor.putBoolean(KEY_IS_REGISTERED, true); // Mark that a user has registered
                    editor.apply(); // Apply changes asynchronously

                    Toast.makeText(Register.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Register.this, MainActivity.class);
                    startActivity(intent);
                    finish(); // Close Register activity
                }
            }
        });
    }
}