package com.example.inventoryapp_coreysampson;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ViewHolder> {

    private final Context context;  // Context for inflating layouts and accessing resources
    private List<Item> itemList;    // List of items to display in RecyclerView
    private final SQLiteDatabaseHelper dbHelper;    // Database helper
    private final OnItemClickListener onItemClickListener;  // Listener for item clicks

    // Constructor to initialize fields
    public InventoryAdapter(Context context, List<Item> itemList, SQLiteDatabaseHelper dbHelper, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.itemList = itemList;
        this.dbHelper = dbHelper;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate item layout and create ViewHolder instance
        View view = LayoutInflater.from(context).inflate(R.layout.activity_inventory_list_data, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get current item from list
        Item item = itemList.get(position);

        // Bind item data to ViewHolder view
        holder.textId.setText(String.valueOf(item.getId()));
        holder.textName.setText(item.getName());
        holder.textDate.setText(item.getDate());
        holder.textQuantity.setText(item.getFormattedQuantity());

        // Functionality for delete button
        holder.deleteBtn.setOnClickListener(v -> {
            dbHelper.deleteItem(item.getId());  // Remove item from the database
            itemList.remove(position);  // Remove item from the list
            notifyItemRemoved(position);    // Notify the adapter of removal
        });

        // Item click functionality
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }   // Return size of item list

    @SuppressLint("NotifyDataSetChanged")
    public void updateItems(List<Item> newItemList) {
        this.itemList = newItemList;
        notifyDataSetChanged(); // Notify adapter of data changed
    }

    // Interface for handling item click events
    public interface OnItemClickListener {
        void onItemClick(Item item);
    }

    // ViewHolder class for caching item views
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textId;
        TextView textName;
        TextView textDate;
        TextView textQuantity;
        ImageButton deleteBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize views
            textId = itemView.findViewById(R.id.text_id);
            textName = itemView.findViewById(R.id.text_name);
            textDate = itemView.findViewById(R.id.text_date);
            textQuantity = itemView.findViewById(R.id.text_quantity);
            deleteBtn = itemView.findViewById(R.id.button_delete);
        }
    }
}