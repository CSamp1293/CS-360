package com.example.inventoryapp_coreysampson;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class Login extends AppCompatActivity {

    // UI components
    private EditText usernameInput, passwordInput;
    private TextView forgotPassText;
    private SQLiteDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize UI components
        usernameInput = findViewById(R.id.username);
        passwordInput = findViewById(R.id.password);
        Button signInBtn = findViewById(R.id.sign_in);
        forgotPassText = findViewById(R.id.forgot_pass);
        TextView signUpText = findViewById(R.id.sign_up);

        // Initialize database helper
        dbHelper = new SQLiteDatabaseHelper(this);

        // Set up click listeners for buttons/textview
        signInBtn.setOnClickListener(v -> handleSignIn());
        forgotPassText.setOnClickListener(v -> handleForgotPassword());
        signUpText.setOnClickListener(v -> handleSignUp());
    }

    // Handle user sign in
    private void handleSignIn() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        // Validate input fields
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Both fields must be filled in.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check user credentials
        if (dbHelper.checkUser(username, password)) {
            SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
            boolean isFirstLogin = preferences.getBoolean("isFirstLogin", true);
            boolean smsPermissionGranted = preferences.getBoolean("smsPermissionGranted", false);

            // Navigate based on login status and permissions
            if (isFirstLogin) {
                if (!smsPermissionGranted) {
                    Intent intent = new Intent(Login.this, smsPermissionsActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    setFirstLoginFlag();
                    Intent intent = new Intent(Login.this, Inventory_List.class);
                    startActivity(intent);
                    finish();
                }
            } else {
                Intent intent = new Intent(Login.this, Inventory_List.class);
                startActivity(intent);
                finish();
            }
        } else {
            Toast.makeText(this, "Invalid Username or Password.", Toast.LENGTH_SHORT).show();
        }
    }

    // Set flag indicating a user logging in for the first time
    private void setFirstLoginFlag() {
        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isFirstLogin", false);
        editor.apply();
    }

    // Handle forgot password textview being clicked
    private void handleForgotPassword() {
        forgotPassText.setOnClickListener(v -> showForgotPasswordDialog());
    }

    // Show forgot password dialog
    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset Password");

        final EditText input = new EditText(this);
        input.setHint("Enter your email address");
        builder.setView(input);

        builder.setPositiveButton("Submit", (dialog, which) -> {
            String email = input.getText().toString().trim();
            if (isValidEmail(email)) {
                sendResetEmail(email);
            } else {
                Toast.makeText(this, "Invalid Email Address.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    // Validate email address format
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Handle password reset email (Does not actually send an email and instead redirects to forgot password screen)
    private void sendResetEmail(String email) {
        if (dbHelper.checkEmailExists(email)) {
            Toast.makeText(this, "Redirecting to password reset...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, ResetPassword.class);
            intent.putExtra("email", email);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Email Address is not registered.", Toast.LENGTH_SHORT).show();
        }
    }

    // Handles click on Sign Up, navigates to sign up screen
    private void handleSignUp() {
        Intent intent = new Intent(Login.this, Sign_Up.class);
        startActivity(intent);
    }
}