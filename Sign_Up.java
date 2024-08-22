package com.example.inventoryapp_coreysampson;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Sign_Up extends AppCompatActivity {

    private EditText editTextUsername, editTextEmail, editTextPassword, editTextConfirmPassword;
    private SQLiteDatabaseHelper dbHelper;

    private static final int MIN_USERNAME_LENGTH = 5;
    private static final int MAX_USERNAME_LENGTH = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initial database helper
        dbHelper = new SQLiteDatabaseHelper(this);

        // Initialize UI components
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        Button buttonSignUp = findViewById(R.id.buttonSignUp);

        // Set up the sign in button's click event
        buttonSignUp.setOnClickListener(v -> handleSignUp());
    }

    private void handleSignUp() {
        // Get user input from UI
        String username = editTextUsername.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim().toLowerCase();
        String password = editTextPassword.getText().toString();
        String confirmPassword = editTextConfirmPassword.getText().toString();

        // Validate inputs
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(Sign_Up.this, "All fields are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (username.length() < MIN_USERNAME_LENGTH || username.length() > MAX_USERNAME_LENGTH) {
            Toast.makeText(Sign_Up.this, "Username must be between " + MIN_USERNAME_LENGTH + " and " + MAX_USERNAME_LENGTH + " characters.", Toast.LENGTH_SHORT).show();
        }

        if (password.length() < 6) {
            Toast.makeText(Sign_Up.this, "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(Sign_Up.this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if username or email already exists
        if (dbHelper.checkUser(username, password)) {
            Toast.makeText(Sign_Up.this, "Username already exists.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dbHelper.checkEmailExists(email)) {
            Toast.makeText(Sign_Up.this, "Email already registered.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Register user in the database
        long userId = dbHelper.addUser(username, email, password);
        if (userId > 0) {
            Toast.makeText(Sign_Up.this, "User registered successfully.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Sign_Up.this, Login.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(Sign_Up.this, "Registration failed. Please Try Again.", Toast.LENGTH_SHORT).show();
        }
    }
}