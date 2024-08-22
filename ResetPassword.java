package com.example.inventoryapp_coreysampson;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ResetPassword extends AppCompatActivity {

    private SQLiteDatabaseHelper dbHelper;  // Database helper
    private EditText newPasswordText, confirmPasswordText;  // UI components for password input
    private String userEmail;   // User email for password reset

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // Initialize database helper and UI components
        dbHelper = new SQLiteDatabaseHelper(this);
        newPasswordText = findViewById(R.id.new_password);
        confirmPasswordText = findViewById(R.id.confirm_password);

        // Retrieve user email from intent
        userEmail = getIntent().getStringExtra("email");

        // Set up the reset button click listener
        Button resetBtn = findViewById(R.id.button_reset);
        resetBtn.setOnClickListener(v -> resetPassword());
    }

    /**
     * Handle password reset
     * Validates input and updates the user's password in the database
     */
    private void resetPassword() {
        String newPassword = newPasswordText.getText().toString().trim();
        String confirmPassword = confirmPasswordText.getText().toString().trim();

        // Validate input fields
        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Both fields are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update user password in the database
        dbHelper.updateUserPassword(userEmail, newPassword);
        Toast.makeText(this, "Password has been reset.", Toast.LENGTH_SHORT).show();

        // Redirect to login screen
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
        finish();
    }
}