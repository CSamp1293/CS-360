package com.example.inventoryapp_coreysampson;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SQLiteDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "inventory.db";    // Name of the database file
    private static final int DATABASE_VERSION = 2;     // version number of the database schema

    // Table names used within the database
    private static final String USER_TABLE = "Users";
    private static final String INVENTORY_TABLE = "Inventory";

    private static final int LOW_INVENTORY_THRESHOLD = 5;   // Threshold used for low inventory count
    private static final String PREFS_NAME = "user_prefs";
    private static final String KEY_LOW_INVENTORY_ALERT_SENT = "low_inventory_alert_sent";

    private final Context context;
    private final NotificationHelper notificationHelper;

    // Initializes database helper with the context and sets up NotificationHelper
    public SQLiteDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        this.notificationHelper = new NotificationHelper(context);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create user table
        db.execSQL("CREATE TABLE " + USER_TABLE + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT NOT NULL, " +
                "email TEXT NOT NULL, " +
                "password TEXT NOT NULL)");

        // Create Inventory table
        db.execSQL("CREATE TABLE " + INVENTORY_TABLE + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "date TEXT NOT NULL, " +
                "quantity INTEGER NOT NULL)");
    }

    /**
     * Called when database needs to be upgraded
     * Handles schema changes when version number is incremented
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + INVENTORY_TABLE);
            onCreate(db);
        }
    }

    // Check if a user exists with provided username and password
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + USER_TABLE + " WHERE username=? COLLATE BINARY AND password=? COLLATE BINARY";
        Cursor cursor = db.rawQuery(query, new String[]{username, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // Check if email already exists
    public boolean checkEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Users WHERE email=?", new String[]{email.toLowerCase()});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // Add new user to the database
    public long addUser(String username, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("email", email.toLowerCase());
        values.put("password", password);
        return db.insert(USER_TABLE, null, values);
    }

    // Update the password of an existing user
    public void updateUserPassword(String email, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("password", newPassword);

        db.update(USER_TABLE, values, "email = ?", new String[]{email});
    }

    // Add a new item to the inventory
    public void addItem(String name, String date, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("date", date);
        String formattedQuantity = formatQuantity(quantity).replaceAll(",", "");
        values.put("quantity", Integer.parseInt(formattedQuantity));
        long result = db.insert(INVENTORY_TABLE, null, values);
        Log.d("SQLiteDatabaseHelper", "Item inserted with result: " + result);

        if (result != -1) {
            sendNewItemNotification(name, quantity);
            sendSmsNotification(name, quantity);
            checkLowInventoryForItem(name, quantity);
        }
    }

    // Update an existing item in the inventory
    public void updateItem(int id, String name, String date, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("date", date);

        String formattedQuantity = formatQuantity(quantity).replaceAll(",", "");
        values.put("quantity", Integer.parseInt(formattedQuantity));

        int rowsAffected = db.update(INVENTORY_TABLE, values, "id = ?", new String[]{String.valueOf(id)});
        boolean updated = rowsAffected > 0;

        if (updated) {
            checkLowInventoryForItem(name, quantity);
        }
    }

    // Send a notification for a new item being added
    private void sendNewItemNotification(String itemName, int quantity) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean notificationsEnabled = preferences.getBoolean("notifications_enabled", false);

        if (notificationsEnabled) {

            String formattedQuantity = formatQuantity(quantity);

            String title = "New Item Added";
            String message = "Item: " + itemName + " (Quantity: " + formattedQuantity + ")";
            int notificationId = (int) (System.currentTimeMillis() & 0xfffffff);
            notificationHelper.sendNotification(context, title, message, notificationId);
            Log.d("SQLiteDatabaseHelper", "Notification Sent: " + message);
        } else {
            Log.d("SQLiteDatabaseHelper", "Notifications not enabled, not sending notification.");
        }
    }

    // Send a notification for a low inventory count based on the threshold
    private void sendLowInventoryNotification(Item item) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean notificationsEnabled = preferences.getBoolean("notifications_enabled", false);

        if (notificationsEnabled) {
            String title = "Low Inventory Alert";
            String message = "Item: " + item.getName() + " has low inventory. Quantity: " + item.getQuantity();
            int notificationId = item.getId();
            notificationHelper.sendNotification(context, title, message, notificationId);
            Log.d("SQLiteDatabaseHelper", "Low inventory notification sent: " + message);
        } else {
            Log.d("SQLiteDatabaseHelper", "Notifications not enabled, not sending low inventory notification.");
        }
    }

    // Send an SMS notification for a new item being added
    private void sendSmsNotification(String itemName, int quantity) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            try {

                String formattedQuantity = formatQuantity(quantity);

                String message = "New Item Added: " + itemName + " (Quantity: " + formattedQuantity + ")";
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage("+1234567890", null, message, null, null); // Replace with the desired phone number
                Log.d("SQLiteDatabaseHelper", "SMS Sent: " + message);
            } catch (Exception e) {
                Log.e("SQLiteDatabaseHelper", "Error sending SMS: ", e);
            }
        } else {
            Log.d("SQLiteDatabaseHelper", "SMS Permission not granted, not sending SMS.");
        }
    }

    // Format quantity with comma separators
    private String formatQuantity(int quantity) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
        return numberFormat.format(quantity);
    }

    // Check and handle low inventory for a specific item
    public void checkLowInventoryForItem(String name, int quantity) {
        if (quantity < LOW_INVENTORY_THRESHOLD) {
            SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            boolean alertSent = preferences.getBoolean(KEY_LOW_INVENTORY_ALERT_SENT + "_" + name, false);

            if (!alertSent) {
                Item item = getItemByName(name); // Assume you have this method implemented
                if (item != null) {
                    sendLowInventoryNotification(item);
                    handleLowInventory(item);

                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean(KEY_LOW_INVENTORY_ALERT_SENT + "_" + item.getId(), true);
                    editor.apply();
                }
            }
        }
    }

    // Handle low inventory by sending an SMS or notification
    private void handleLowInventory(Item item) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            try {
                String message = "Low Inventory Alert: " + item.getName() + " (Quantity: " + item.getQuantity() + ")";
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage("+1234567890", null, message, null, null); // Replace with the desired phone number
                Log.d("SQLiteDatabaseHelper", "Low inventory SMS Sent: " + message);
            } catch (Exception e) {
                Log.e("SQLiteDatabaseHelper", "Error sending low inventory SMS: ", e);
            }
        } else {
            Log.d("SQLiteDatabaseHelper", "SMS Permission not granted, not sending low inventory SMS.");
            sendLowInventoryNotification(item);
        }
    }

    // Delete an item from the inventory
    public void deleteItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(INVENTORY_TABLE, "id = ?", new String[]{String.valueOf(id)});

        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(KEY_LOW_INVENTORY_ALERT_SENT + "_" + id);
        editor.apply();
    }

    // Clear all items from the inventory and reset SharedPreferences
    public void clearDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + INVENTORY_TABLE);
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + INVENTORY_TABLE + "'");
        db.close();

        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor =preferences.edit();
        editor.clear();
        editor.apply();
    }

    // Retrieve an item by its name
    public Item getItemByName(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        Item item = null;

        try {
            cursor = db.rawQuery("SELECT * FROM " + INVENTORY_TABLE + " WHERE name=?", new String[]{name});

            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex("id");
                int dateIndex = cursor.getColumnIndex("date");
                int quantityIndex = cursor.getColumnIndex("quantity");

                // Check if column indices are valid
                if (idIndex == -1 || dateIndex == -1 || quantityIndex == -1) {
                    Log.e("SQLiteDatabaseHelper", "One or more column indices are invalid. Ensure column names are correct.");
                    return null;
                }

                int id = cursor.getInt(idIndex);
                String date = cursor.getString(dateIndex);
                int quantity = cursor.getInt(quantityIndex);

                item = new Item(id, name, date, quantity);
            }
        } catch (Exception e) {
            Log.e("SQLiteDatabaseHelper", "Error retrieving item by name: ", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return item;
    }

    // Retrieve all items from the inventory
    public List<Item> getAllItems() {
        List<Item> itemList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + INVENTORY_TABLE, null);

        if (cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex("id");
            int nameIndex = cursor.getColumnIndex("name");
            int dateIndex = cursor.getColumnIndex("date");
            int quantityIndex = cursor.getColumnIndex("quantity");

            // Check if column indices are valid
            if (idIndex == -1 || nameIndex == -1 || dateIndex == -1 || quantityIndex == -1) {
                Log.e("SQLiteDatabaseHelper", "Column index not found. Ensure column names are correct.");
                cursor.close();
                return itemList;
            }

            do {
                int id = cursor.getInt(idIndex);
                String name = cursor.getString(nameIndex);
                String date = cursor.getString(dateIndex);
                int quantity = cursor.getInt(quantityIndex);

                // Format quantity before adding to the list
                String formattedQuantity = formatQuantity(quantity);
                itemList.add(new Item(id, name, date, Integer.parseInt(formattedQuantity.replaceAll(",", ""))));
            } while (cursor.moveToNext());

            cursor.close();
        }
        Log.d("SQLiteDatabaseHelper", "Items retrieved: " + itemList.size());
        return itemList;
    }
}