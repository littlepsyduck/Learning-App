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
    private List<Integer> wordStates; // 0 = normal, 1 = correct (green), 2 = incorrect (red), 3 = used (gray), 4 = selected (blue)

    public interface OnWordClickListener {
        void onWordClick(String word, int position);
    }

    public WordAdapter(List<String> words, OnWordClickListener listener) {
        this.words = words != null ? new ArrayList<>(words) : new ArrayList<>();
        this.listener = listener;
        this.wordUsed = new ArrayList<>();
        this.wordStates = new ArrayList<>();
        for (int i = 0; i < this.words.size(); i++) {
            wordUsed.add(false);
            wordStates.add(0); // 0 = normal
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
        int state = wordStates.get(position);
        
        holder.tvWord.setText(word);
        holder.tvWord.setVisibility(View.VISIBLE);
        
        switch (state) {
            case 1:
                holder.tvWord.setBackgroundResource(R.drawable.bg_matching_card_correct);
                holder.itemView.setEnabled(false);
                break;
            case 2:
                holder.tvWord.setBackgroundResource(R.drawable.bg_matching_card_incorrect);
                holder.itemView.setEnabled(false);
                break;
            case 3:
                holder.tvWord.setVisibility(View.INVISIBLE);
                holder.tvWord.setBackgroundResource(R.drawable.bg_word_button_selected);
                holder.itemView.setEnabled(false);
                break;
            case 4:
                holder.tvWord.setBackgroundResource(R.drawable.bg_matching_card_selected_blue);
                holder.itemView.setEnabled(true);
                break;
            default:
                holder.tvWord.setBackgroundResource(R.drawable.bg_word_button);
                holder.itemView.setEnabled(!wordUsed.get(position));
                break;
        }

        holder.itemView.setOnClickListener(v -> {
            if ((state == 0 && !wordUsed.get(position)) || state == 4) {
                if (listener != null) {
                    listener.onWordClick(word, position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return words.size();
    }

    public void markWordAsUsed(int position) {
        if (position >= 0 && position < wordUsed.size() && position < wordStates.size()) {
            wordUsed.set(position, true);
            wordStates.set(position, 3);
            notifyItemChanged(position);
        }
    }

    public void markWordAsUnused(int position) {
        if (position >= 0 && position < wordUsed.size() && position < wordStates.size()) {
            wordUsed.set(position, false);
            wordStates.set(position, 0);
            notifyItemChanged(position);
        }
    }

    public void setWordSelected(int position) {
        if (position >= 0 && position < wordStates.size()) {
            wordStates.set(position, 4);
            wordUsed.set(position, true);
            notifyItemChanged(position);
        }
    }
    
    public void unselectWord(int position) {
        if (position >= 0 && position < wordStates.size()) {
            wordStates.set(position, 0);
            wordUsed.set(position, false);
            notifyItemChanged(position);
        }
    }
    
    public int getSelectedWordPosition() {
        for (int i = 0; i < wordStates.size(); i++) {
            if (wordStates.get(i) == 4) {
                return i;
            }
        }
        return -1;
    }
    
    public void updateWordState(int position, boolean isCorrect) {
        if (position >= 0 && position < wordStates.size()) {
            wordStates.set(position, isCorrect ? 1 : 2);
            wordUsed.set(position, true);
            notifyItemChanged(position);
        }
    }
    
    public void disableAllWords() {
        for (int i = 0; i < wordUsed.size(); i++) {
            if (wordStates.get(i) == 0 || wordStates.get(i) == 4) {
                wordUsed.set(i, true);
            }
        }
        notifyDataSetChanged();
    }
    
    public void enableUnselectedWords() {
        for (int i = 0; i < wordUsed.size(); i++) {
            if (wordStates.get(i) == 0) {
                wordUsed.set(i, false);
            }
        }
        notifyDataSetChanged();
    }
    
    public void resetWords() {
        for (int i = 0; i < wordUsed.size(); i++) {
            wordUsed.set(i, false);
            wordStates.set(i, 0);
        }
        notifyDataSetChanged();
    }

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

