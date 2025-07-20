// src/main/java/com/example/mco2/adapter/JournalEntryAdapter.java
package com.example.mco2.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mco2.R; // Make sure this R is correctly pointing to your project's R file
import com.example.mco2.model.JournalEntry;

import java.util.List;

public class JournalEntryAdapter extends RecyclerView.Adapter<JournalEntryAdapter.JournalEntryViewHolder> {

    private List<JournalEntry> journalEntries;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(JournalEntry entry);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public JournalEntryAdapter(List<JournalEntry> journalEntries) {
        this.journalEntries = journalEntries;
    }

    // Method to update the data in the adapter
    public void setEntries(List<JournalEntry> newEntries) {
        this.journalEntries = newEntries;
        notifyDataSetChanged(); // Notifies the RecyclerView to refresh
    }

    @NonNull
    @Override
    public JournalEntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_journal_entry, parent, false);
        return new JournalEntryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JournalEntryViewHolder holder, int position) {
        JournalEntry currentEntry = journalEntries.get(position);
        holder.tvDate.setText("Date: " + currentEntry.getDate());
        holder.tvMood.setText("Mood: " + currentEntry.getMood());
        holder.tvTitle.setText(currentEntry.getTitle().isEmpty() ? "No Title" : currentEntry.getTitle());
        holder.tvContentPreview.setText(currentEntry.getContent());
        holder.tvQuote.setText("Quote: " + (currentEntry.getQuote().isEmpty() ? "N/A" : currentEntry.getQuote()));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(currentEntry);
            }
        });
    }

    @Override
    public int getItemCount() {
        return journalEntries.size();
    }

    public static class JournalEntryViewHolder extends RecyclerView.ViewHolder {
        public TextView tvDate;
        public TextView tvMood;
        public TextView tvTitle;
        public TextView tvContentPreview;
        public TextView tvQuote;

        public JournalEntryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_item_date);
            tvMood = itemView.findViewById(R.id.tv_item_mood);
            tvTitle = itemView.findViewById(R.id.tv_item_title);
            tvContentPreview = itemView.findViewById(R.id.tv_item_content_preview);
            tvQuote = itemView.findViewById(R.id.tv_item_quote);
        }
    }
}