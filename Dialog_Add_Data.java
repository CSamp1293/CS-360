package com.example.inventoryapp_coreysampson;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Dialog_Add_Data {

    private final Context context;  // Context to show dialogs and toast messages
    private final SQLiteDatabaseHelper dbHelper;    // Database helper to interact with the database
    private final Runnable onDismissCallback;   // Callback for when dialog is dismissed
    private AlertDialog dialog;    // Reference to the AlertDialog
    private Item itemToEdit;    // Item to edit, null if adding a new item

    // Constructor for adding a new item
    public Dialog_Add_Data(@NonNull Context context, SQLiteDatabaseHelper dbHelper, Runnable onDismissCallback) {
        this.context = context;
        this.dbHelper = dbHelper;
        this.onDismissCallback = onDismissCallback;
    }

    // Constructor for editing an existing item
    public Dialog_Add_Data(@NonNull Context context, SQLiteDatabaseHelper dbHelper, Item itemToEdit, Runnable onDismissCallback) {
        this(context, dbHelper, onDismissCallback);
        this.itemToEdit = itemToEdit;
    }

    // Show the dialog for adding or editing an item
    public void show() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.activity_dialog_add_data, null);

        // Initialize views from the dialog layout
        EditText editName = dialogView.findViewById(R.id.edit_name);
        EditText editDate = dialogView.findViewById(R.id.edit_date);
        EditText editQuantity = dialogView.findViewById(R.id.edit_quantity);
        Button saveBtn = dialogView.findViewById(R.id.button_save);
        Button cancelBtn = dialogView.findViewById(R.id.button_cancel);
        Button incrementBtn = dialogView.findViewById(R.id.button_increment);
        Button decrementBtn = dialogView.findViewById(R.id.button_decrement);

        // Check for correctly initialized views
        if (editName == null || editDate == null || editQuantity == null) {
            Log.e("Dialog_Add_Data", "One or more views not found");
        }

        // If editing an item, populate the fields with existing data
        if (itemToEdit != null) {
            assert editName != null;
            editName.setText(itemToEdit.getName());
            assert editDate != null;
            editDate.setText(itemToEdit.getDate());
            assert editQuantity != null;
            editQuantity.setText(formatQuantity(itemToEdit.getQuantity()));
        }

        // Build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(itemToEdit == null ? "Add New Item" : "Edit Item")
                .setView(dialogView)
                .setCancelable(false);

        dialog = builder.create();
        dialog.show();

        // Logic for the increment button
        incrementBtn.setOnClickListener(v -> {
            assert editQuantity != null;
            String quantityStr = editQuantity.getText().toString().trim();
            int quantity = quantityStr.isEmpty() ? 0 : Integer.parseInt(quantityStr);
            quantity++;
            editQuantity.setText(formatQuantity(quantity));
        });

        // Logic for the decrement button
        decrementBtn.setOnClickListener(v -> {
            assert editQuantity != null;
            String quantityStr = editQuantity.getText().toString().trim();
            int quantity = quantityStr.isEmpty() ? 0 : Integer.parseInt(quantityStr);
            if (quantity > 0) {
                quantity--;
                editQuantity.setText(formatQuantity(quantity));
            }
        });

        // Logic for the save button
        saveBtn.setOnClickListener(v -> {
            assert editName != null;
            String name = editName.getText().toString().trim();
            assert editDate != null;
            String date = editDate.getText().toString().trim();
            assert editQuantity != null;
            String quantityStr = editQuantity.getText().toString().trim();

            // Validate input fields
            if (name.isEmpty() || date.isEmpty() || quantityStr.isEmpty()) {
                Toast.makeText(context, "All fields are required.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if the entered date is valid
            if (!isValidDate(date)) {
                Toast.makeText(context, "Invalid date format. Please use MM/DD/YYYY.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Ensures properly parsed string into integer value
            quantityStr = quantityStr.replaceAll(",", "");

            // Try to parse cleaned quantity string into an integer
            try {
                int quantity = Integer.parseInt(quantityStr);

                // Check if parsed quantity is greater than 0
                if (quantity <= 0) {
                    Toast.makeText(context, "Quantity must be greater than 0.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Capitalize the first letter of the item name automatically
                name = capitalizeFirstLetter(name);

                // Add or edit item in the database
                if (itemToEdit == null) {
                    dbHelper.addItem(name, date, quantity);
                    Toast.makeText(context, "Item has been added to the inventory.", Toast.LENGTH_SHORT).show();
                } else {
                    dbHelper.updateItem(itemToEdit.getId(), name, date, quantity);
                    Toast.makeText(context, "Item Updated.", Toast.LENGTH_SHORT).show();
                }

                // Dismiss the dialog and call the callback if provided
                dialog.dismiss();
                if (onDismissCallback != null) {
                    onDismissCallback.run();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(context, "Invalid Quantity.", Toast.LENGTH_SHORT).show();
            }
        });

        // Logic for the cancel button
        cancelBtn.setOnClickListener(v -> dialog.dismiss());
    }

    // Validate the date format and ensure the entered date is a real date
    private boolean isValidDate(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        sdf.setLenient(false);
        try {
            Date parsedDate = sdf.parse(date);
            if (parsedDate == null) {
                return false;
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setLenient(false);
            calendar.setTime(parsedDate);
            return calendar.getTime().equals(parsedDate);
        } catch (ParseException e) {
            return false;
        }
    }

    // Capitalize the first letter of the item automatically
    private String capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase(Locale.US) + text.substring(1).toLowerCase(Locale.US);
    }

    // Format quantity to include commas in numbers greater than 999
    private String formatQuantity(int quantity) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
        return numberFormat.format(quantity);
    }
}