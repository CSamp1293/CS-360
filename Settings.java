package com.example.inventoryapp_coreysampson;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class Settings extends AppCompatActivity {

    // UI components
    private SwitchCompat notifSwitch, smsNotifSwitch;

    // Request codes for permissions
    private static final int REQUEST_CODE_NOTIFICATION_PERMISSION = 123;
    private static final int REQUEST_CODE_SMS_PERMISSION = 124;

    // SharedPreference keys
    private static final String PREFS_NAME = "user_prefs";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";
    private static final String KEY_SMS_NOTIFICATIONS_ENABLED = "sms_notifications_enabled";
    private static final String KEY_SMS_PERMISSION_GRANTED = "sms_permission_granted";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize UI components
        notifSwitch = findViewById(R.id.switch_notifications);
        smsNotifSwitch = findViewById(R.id.switch_sms_notifications);
        Button changePwdBtn = findViewById(R.id.button_change_password);
        Button clearDataBtn = findViewById(R.id.button_clear_data);
        Button aboutAppBtn = findViewById(R.id.button_about_app);
        Button helpBtn = findViewById(R.id.button_help);

        // Load preferences for switch states
        loadPreferences();

        notifSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> handleNotificationSwitch(isChecked));
        smsNotifSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> handleSmsNotificationSwitch(isChecked));

        changePwdBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.this, ResetPassword.class);
            startActivity(intent);
        });

        clearDataBtn.setOnClickListener(v -> clearAppData());

        aboutAppBtn.setOnClickListener(v -> Toast.makeText(this, "About the App", Toast.LENGTH_SHORT).show());

        helpBtn.setOnClickListener(v -> Toast.makeText(this, "Help & Support", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateSwitchStates();   // Ensure switches reflect current settings
    }

    /**
     * Updates the state of the switches based on saved preferences and permissions.
     */
    private void updateSwitchStates() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean notificationsEnabled = preferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, false);
        boolean smsNotificationsEnabled = preferences.getBoolean(KEY_SMS_NOTIFICATIONS_ENABLED, false);

        notifSwitch.setChecked(notificationsEnabled);

        boolean smsPermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
        smsNotifSwitch.setChecked(smsPermissionGranted && smsNotificationsEnabled);
    }

    /**
     * Saves the user's preference for notifications and SMS notifications.
     */
    private void savePreferences(String key, boolean enabled) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, enabled);
        editor.apply();
    }

    /**
     * Loads the saved preferences for notifications and SMS notifications
     */
    private void loadPreferences() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean notificationsEnabled = preferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, false);
        boolean smsNotificationsEnabled = preferences.getBoolean(KEY_SMS_NOTIFICATIONS_ENABLED, false);

        notifSwitch.setChecked(notificationsEnabled);
        smsNotifSwitch.setChecked(smsNotificationsEnabled);
    }

    private void handleNotificationSwitch(boolean isChecked) {
        if (isChecked) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_NOTIFICATION_PERMISSION);
            } else {
                savePreferences(KEY_NOTIFICATIONS_ENABLED, true);
                Toast.makeText(Settings.this, "Notifications Enabled", Toast.LENGTH_SHORT).show();
            }
        } else {
            savePreferences(KEY_NOTIFICATIONS_ENABLED, false);
            Toast.makeText(Settings.this, "Notifications Disabled", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handles logic when SMS notifications switch is toggled
     */
    private void handleSmsNotificationSwitch(boolean isChecked) {
        if (isChecked) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, REQUEST_CODE_SMS_PERMISSION);
            } else {
                savePreferences(KEY_SMS_NOTIFICATIONS_ENABLED, true);
                Toast.makeText(Settings.this, "SMS Notifications Enabled.", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                showPermissionRevokeDialog();
            } else {
                savePreferences(KEY_SMS_NOTIFICATIONS_ENABLED, false);
                Toast.makeText(Settings.this, "SMS Notifications Disabled.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Shows dialog asking user if they want to revoke SMS permissions
     */
    private void showPermissionRevokeDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Revoke SMS Permissions")
                .setMessage("To disable SMS Notifications, you must revoke the SMS Permissions. Navigate to app settings?")
                .setPositiveButton("Yes", (dialog, which) -> openAppSettings())
                .setNegativeButton("No", (dialog, which) -> {
                    smsNotifSwitch.setChecked(true);
                    savePreferences(KEY_SMS_NOTIFICATIONS_ENABLED, true);
                    dialog.dismiss();
                })
                .show();
    }

    /**
     * Opens the app settings to allow user to manage app permissions
     */
    private void openAppSettings() {
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    /**
     * Clears the app data including preferences and database
     */
    private void clearAppData() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(KEY_SMS_NOTIFICATIONS_ENABLED);
        editor.remove(KEY_NOTIFICATIONS_ENABLED);
        editor.remove(KEY_SMS_PERMISSION_GRANTED);
        editor.apply();

        SQLiteDatabaseHelper dbHelper = new SQLiteDatabaseHelper(this);
        dbHelper.clearDatabase();

        Toast.makeText(this, "Inventory data has been cleared,", Toast.LENGTH_SHORT).show();
    }

    /**
     * Handles the result of permission requests
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                savePreferences(KEY_NOTIFICATIONS_ENABLED, true);
                Toast.makeText(this, "Notifications Enabled", Toast.LENGTH_SHORT).show();
            } else {
                notifSwitch.setChecked(false);
                savePreferences(KEY_NOTIFICATIONS_ENABLED, false);
                Toast.makeText(this, "Notifications Permission Denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CODE_SMS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                savePreferences(KEY_SMS_NOTIFICATIONS_ENABLED, true);
                savePreferences(KEY_SMS_PERMISSION_GRANTED, true);
                smsNotifSwitch.setChecked(true);
                Toast.makeText(this, "SMS Notifications Enabled.", Toast.LENGTH_SHORT).show();
            } else {
                savePreferences(KEY_SMS_NOTIFICATIONS_ENABLED, false);
                savePreferences(KEY_SMS_PERMISSION_GRANTED, false);
                smsNotifSwitch.setChecked(false);
                Toast.makeText(this, "SMS Notifications Disabled.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}