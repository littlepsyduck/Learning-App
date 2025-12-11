package com.example.learning_app.ui.game;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.learning_app.R;
import com.example.learning_app.entities.Question;
import com.example.learning_app.ui.adapter.WordAdapter;
import com.google.android.flexbox.FlexboxLayoutManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Handler cho game Type 2: Sắp xếp câu
 */
public class Type2GameHandler extends BaseGameHandler {
    
    private ImageView ivCharacter;
    private LinearLayout answerLine1, answerLine2, answerLine3;
    private View line1, line2, line3;
    private TextView tvAnswerPlaceholder;
    private RecyclerView rvWordBank;
    private WordAdapter wordAdapter;
    private List<String> selectedWords = new ArrayList<>();
    private List<String> availableWords = new ArrayList<>();
    
    public Type2GameHandler(Context context, Question question, GameCallback callback) {
        super(context, question, callback);
    }
    
    @Override
    public View createGameView() {
        gameView = LayoutInflater.from(context)
                .inflate(R.layout.layout_question_type2, null);
        
        // Get views
        ivCharacter = gameView.findViewById(R.id.ivCharacter);
        answerLine1 = gameView.findViewById(R.id.answerLine1);
        answerLine2 = gameView.findViewById(R.id.answerLine2);
        answerLine3 = gameView.findViewById(R.id.answerLine3);
        line1 = gameView.findViewById(R.id.line1);
        line2 = gameView.findViewById(R.id.line2);
        line3 = gameView.findViewById(R.id.line3);
        tvAnswerPlaceholder = gameView.findViewById(R.id.tvAnswerPlaceholder);
        rvWordBank = gameView.findViewById(R.id.rvWordBank);
        
        // Set character image - xen kẽ 2 ảnh
        int characterIndex = getNextCharacterImageIndex();
        int characterResId = (characterIndex % 2 == 0) 
                ? context.getResources().getIdentifier("character_duo_1", "drawable", context.getPackageName())
                : context.getResources().getIdentifier("character_duo_2", "drawable", context.getPackageName());
        if (characterResId != 0) {
            ivCharacter.setImageResource(characterResId);
        }
        
        // Setup word bank
        if (question.optionsData != null && !question.optionsData.isEmpty()) {
            String[] words = question.optionsData.split("\\|");
            availableWords = new ArrayList<>(Arrays.asList(words));
            Collections.shuffle(availableWords);
            
            // Setup RecyclerView with FlexboxLayoutManager
            FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(context);
            layoutManager.setFlexDirection(com.google.android.flexbox.FlexDirection.ROW);
            layoutManager.setJustifyContent(com.google.android.flexbox.JustifyContent.FLEX_START);
            rvWordBank.setLayoutManager(layoutManager);
            
            wordAdapter = new WordAdapter(availableWords, (word, position) -> {
                addWordToAnswer(word, position);
            });
            rvWordBank.setAdapter(wordAdapter);
        }
        
        // Setup answer container
        setupAnswerContainer();
        
        return gameView;
    }
    
    @Override
    public boolean checkAnswer() {
        String userAnswer = String.join(" ", selectedWords).trim();
        String correctAnswer = question.correctAnswer.trim();
        return userAnswer.equalsIgnoreCase(correctAnswer);
    }
    
    @Override
    public void resetGame() {
        selectedWords.clear();
        if (wordAdapter != null) {
            wordAdapter.resetWords();
        }
        setupAnswerContainer();
    }
    
    private void setupAnswerContainer() {
        // Clear all answer lines và ẩn các line không cần
        answerLine1.removeAllViews();
        answerLine2.removeAllViews();
        answerLine3.removeAllViews();
        answerLine2.setVisibility(View.GONE);
        answerLine3.setVisibility(View.GONE);
        line1.setVisibility(View.GONE);
        line2.setVisibility(View.GONE);
        line3.setVisibility(View.GONE);
        
        if (selectedWords.isEmpty()) {
            // Show placeholder on first line
            tvAnswerPlaceholder.setVisibility(View.VISIBLE);
            tvAnswerPlaceholder.setText("Tap words to form a sentence");
            answerLine1.addView(tvAnswerPlaceholder);
        } else {
            tvAnswerPlaceholder.setVisibility(View.GONE);
            
            // Phân bổ từ vào các dòng tự động
            answerLine1.post(() -> {
                distributeWordsToLines();
            });
        }
        
        // Enable continue button if answer is not empty
        if (callback != null) {
            callback.enableContinueButton(!selectedWords.isEmpty());
        }
    }
    
    private void distributeWordsToLines() {
        if (selectedWords.isEmpty()) return;
        
        // Clear lại các dòng trước khi phân bổ
        answerLine1.removeAllViews();
        answerLine2.removeAllViews();
        answerLine3.removeAllViews();
        
        List<LinearLayout> lines = new ArrayList<>();
        lines.add(answerLine1);
        lines.add(answerLine2);
        lines.add(answerLine3);
        
        int currentLineIndex = 0;
        LinearLayout currentLine = lines.get(currentLineIndex);
        currentLine.setVisibility(View.VISIBLE);
        
        int availableWidth = answerLine1.getWidth() > 0 
                ? answerLine1.getWidth() - 16
                : context.getResources().getDisplayMetrics().widthPixels - 64;
        int currentLineWidth = 0;
        
        for (int i = 0; i < selectedWords.size(); i++) {
            String word = selectedWords.get(i);
            TextView wordView = createAnswerWordView(word, i);
            
            int widthSpec = View.MeasureSpec.makeMeasureSpec(availableWidth, View.MeasureSpec.AT_MOST);
            int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            wordView.measure(widthSpec, heightSpec);
            int wordWidth = wordView.getMeasuredWidth() + 8;
            
            if (currentLineWidth + wordWidth > availableWidth && currentLine.getChildCount() > 0) {
                if (currentLineIndex < lines.size() - 1) {
                    View lineView = currentLineIndex == 0 ? line1 : (currentLineIndex == 1 ? line2 : line3);
                    if (lineView != null) {
                        lineView.setVisibility(View.VISIBLE);
                    }
                    currentLineIndex++;
                    currentLine = lines.get(currentLineIndex);
                    currentLine.setVisibility(View.VISIBLE);
                    currentLineWidth = 0;
                }
            }
            
            currentLine.addView(wordView);
            currentLineWidth += wordWidth;
        }
        
        if (currentLine.getChildCount() > 0) {
            View lastLine = currentLineIndex == 0 ? line1 : (currentLineIndex == 1 ? line2 : line3);
            if (lastLine != null) {
                lastLine.setVisibility(View.VISIBLE);
            }
        }
    }
    
    private TextView createAnswerWordView(String word, int position) {
        TextView wordView = new TextView(context);
        wordView.setText(word);
        wordView.setPadding(16, 12, 16, 12);
        wordView.setBackgroundResource(R.drawable.bg_word_button);
        wordView.setTextColor(context.getResources().getColor(android.R.color.black));
        wordView.setTextSize(16);
        wordView.setMinWidth(80);
        wordView.setGravity(android.view.Gravity.CENTER);
        wordView.setClickable(true);
        wordView.setFocusable(true);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(4, 4, 4, 4);
        wordView.setLayoutParams(params);
        
        wordView.setOnClickListener(v -> {
            removeWordFromAnswer(position);
        });
        
        return wordView;
    }
    
    private void addWordToAnswer(String word, int position) {
        selectedWords.add(word);
        if (wordAdapter != null) {
            wordAdapter.markWordAsUsed(position);
        }
        setupAnswerContainer();
    }
    
    private void removeWordFromAnswer(int position) {
        if (position >= 0 && position < selectedWords.size()) {
            String word = selectedWords.remove(position);
            if (wordAdapter != null) {
                int wordPosition = wordAdapter.findWordPosition(word);
                if (wordPosition >= 0) {
                    wordAdapter.markWordAsUnused(wordPosition);
                }
            }
            setupAnswerContainer();
        }
    }
}

