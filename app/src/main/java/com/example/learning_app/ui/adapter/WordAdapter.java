package com.example.learning_app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learning_app.R;

import java.util.ArrayList;
import java.util.List;

public class WordAdapter extends RecyclerView.Adapter<WordAdapter.WordViewHolder> {

    private List<String> words;
    private OnWordClickListener listener;
    private List<Boolean> wordUsed; // Track which words have been used

    public interface OnWordClickListener {
        void onWordClick(String word, int position);
    }

    public WordAdapter(List<String> words, OnWordClickListener listener) {
        this.words = words != null ? new ArrayList<>(words) : new ArrayList<>();
        this.listener = listener;
        this.wordUsed = new ArrayList<>();
        for (int i = 0; i < this.words.size(); i++) {
            wordUsed.add(false);
        }
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_word, parent, false);
        return new WordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        String word = words.get(position);
        
        // Nếu từ đã được chọn: nền xám, không hiện chữ nhưng giữ nguyên kích thước
        if (wordUsed.get(position)) {
            // Giữ nguyên text để giữ kích thước, nhưng set visibility = INVISIBLE để ẩn chữ
            holder.tvWord.setText(word);
            holder.tvWord.setVisibility(View.INVISIBLE);
            holder.tvWord.setBackgroundResource(R.drawable.bg_word_button_selected);
            holder.itemView.setEnabled(false);
        } else {
            // Chưa chọn: nền trắng, hiện chữ
            holder.tvWord.setText(word);
            holder.tvWord.setVisibility(View.VISIBLE);
            holder.tvWord.setBackgroundResource(R.drawable.bg_word_button);
            holder.itemView.setEnabled(true);
        }

        holder.itemView.setOnClickListener(v -> {
            if (!wordUsed.get(position) && listener != null) {
                listener.onWordClick(word, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return words.size();
    }

    // Mark word as used
    public void markWordAsUsed(int position) {
        if (position >= 0 && position < wordUsed.size()) {
            wordUsed.set(position, true);
            notifyItemChanged(position);
        }
    }

    // Mark word as unused (when user removes it from answer)
    public void markWordAsUnused(int position) {
        if (position >= 0 && position < wordUsed.size()) {
            wordUsed.set(position, false);
            notifyItemChanged(position);
        }
    }

    // Reset all words
    public void resetWords() {
        for (int i = 0; i < wordUsed.size(); i++) {
            wordUsed.set(i, false);
        }
        notifyDataSetChanged();
    }

    // Find position of word in list
    public int findWordPosition(String word) {
        for (int i = 0; i < words.size(); i++) {
            if (words.get(i).equals(word)) {
                return i;
            }
        }
        return -1;
    }

    static class WordViewHolder extends RecyclerView.ViewHolder {
        TextView tvWord;

        WordViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWord = itemView.findViewById(R.id.tvWord);
        }
    }
}

