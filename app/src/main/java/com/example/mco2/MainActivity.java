package com.example.mco2;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView; // Import TextView


public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // --- Code for the Login Button ---
        Button loginButton = findViewById(R.id.activity_login_btn_login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This is the line that opens the Dashboard Activity
                Intent intent = new Intent(MainActivity.this, Dashboard.class);
                startActivity(intent);
                // You can add finish(); here if you don't want the user to go back to login
                // after they've successfully logged in.
                // finish();
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