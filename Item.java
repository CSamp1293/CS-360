package com.example.inventoryapp_coreysampson;

import java.text.NumberFormat;
import java.util.Locale;

public class Item {
    private int id;    // Unique identifier for each item
    private String name;    // Name of the item
    private String date;    // Date associated with the item
    private int quantity;   // Quantity of the item

    // Constructor to initialize the item
    public Item(int id, String name, String date, int quantity) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.quantity = quantity;
    }

    // Get the formatted quantity with commas for anything over 999
    public String getFormattedQuantity() {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
        return numberFormat.format(quantity);
    }

    // Getters and setters for each field
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}