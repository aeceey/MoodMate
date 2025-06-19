package com.example.mco2;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class NewEntryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_entry);

        // Optional: Set up toolbar with back arrow if you want
        Toolbar toolbar = findViewById(R.id.new_entry_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("New Entry");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    // Handle the back arrow
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
