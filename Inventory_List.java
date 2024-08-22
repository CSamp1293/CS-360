package com.example.inventoryapp_coreysampson;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class Inventory_List extends AppCompatActivity {

    private SQLiteDatabaseHelper dbHelper;  // Database helper to interact with the database
    private InventoryAdapter inventoryAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_list);

        // Initialize the database helper
        dbHelper = new SQLiteDatabaseHelper(this);

        // Set up RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Initialize adapter with items from the database
        inventoryAdapter = new InventoryAdapter(this, dbHelper.getAllItems(), dbHelper, this::onItemClick);
        recyclerView.setAdapter(inventoryAdapter);

        // Set up FloatingActionButton for adding new items
        FloatingActionButton fabAdd = findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(v -> {
            // Show dialog for adding a new item
            Dialog_Add_Data dialog = new Dialog_Add_Data(this, dbHelper, this::refreshItems);
            dialog.show();
        });
    }

    // Refresh the item list and update the adapter
    private void refreshItems() {
        List<Item> updatedItems = dbHelper.getAllItems();
        inventoryAdapter.updateItems(updatedItems);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh items on activity resume
        refreshItems();
    }

    // Logs for onStart, onPause, onStop, and onDestroy
    @Override
    protected void onStart() {
        super.onStart();
        Log.d("Inventory_List", "onStart called");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("Inventory_List", "onPause called");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("Inventory_List", "onStop called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Inventory_List", "onDestroy called");
    }

    // Handle item click event
    private void onItemClick(Item item) {
        // Show dialog for editing an existing item
        Dialog_Add_Data editDialog = new Dialog_Add_Data(this, dbHelper, item, this::refreshItems);
        editDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_inventory_list, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                //Navigate to settings screen
                Intent intent = new Intent(Inventory_List.this, Settings.class);
                startActivity(intent);
                return true;
            case R.id.menu_logout:
                // Perform logout operation
                performLogout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Handle user logout
    private void performLogout() {
        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        // Retain specific settings and clear other preferences
        boolean smsPermissionGranted = preferences.getBoolean("smsPermissionGranted", false);
        boolean notificationsEnabled = preferences.getBoolean("notifications_enabled", false);

        editor.clear(); // Clear all preferences
        editor.putBoolean("smsPermissionGranted", smsPermissionGranted);
        editor.putBoolean("notifications_enabled", notificationsEnabled);
        editor.apply();

        // Navigate back to the login screen
        Intent intent = new Intent(this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        Toast.makeText(this, "Logged out successfully.", Toast.LENGTH_SHORT).show();
    }
}