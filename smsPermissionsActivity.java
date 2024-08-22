package com.example.inventoryapp_coreysampson;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class smsPermissionsActivity extends AppCompatActivity {

    private static final int REQUEST_SMS_PERMISSION = 100;
    private static final String PREFS_NAME = "user_prefs";
    private static final String KEY_SMS_NOTIFICATIONS_ENABLED = "sms_notifications_enabled";
    private static final String KEY_SMS_PERMISSION_GRANTED = "smsPermissionGranted";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if SMS permission is already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            proceedToMainActivity();
        } else {
            requestSmsPermission();
        }
    }

    // Request SMS permission if not already granted
    private void requestSmsPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, REQUEST_SMS_PERMISSION);
    }

    // Proceed to the main activity and update preferences (Inventory_List)
    private void proceedToMainActivity() {
        updateSmsPermissionStatus(true);
        Intent intent = new Intent(this, Inventory_List.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    // Handle result of the SMS permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_SMS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                proceedToMainActivity();
                Log.d("smsPermissionsActivity", "SMS Permission granted.");
                updateSmsPermissionStatus(true);
            } else {
                updateSmsPermissionStatus(false);
                Log.d("smsPermissionsActivity", "SMS Permission denied.");
            }
            proceedToMainActivity();
        }
    }

    // Update SMS permission status in SharedPreferences
    private void updateSmsPermissionStatus(boolean isGranted) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_SMS_PERMISSION_GRANTED, isGranted);
        editor.putBoolean(KEY_SMS_NOTIFICATIONS_ENABLED, isGranted);
        editor.apply();
    }
}