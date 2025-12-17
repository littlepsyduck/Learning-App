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

public class Type3GameHandler extends BaseGameHandler {
    
    private ImageView ivCharacter;
    private TextView tvEnglishWord;
    private RecyclerView rvWordBank;
    private WordAdapter wordAdapter;
    private List<String> availableWords = new ArrayList<>();
    private String correctAnswer;
    private String selectedAnswer = null;
    private int selectedPosition = -1;
    private boolean isGameDisabled = false;
    
    public Type3GameHandler(Context context, Question question, GameCallback callback) {
        super(context, question, callback);
    }
    
    @Override
    public View createGameView() {
        gameView = LayoutInflater.from(context)
                .inflate(R.layout.layout_question_type3, null);
        
        ivCharacter = gameView.findViewById(R.id.ivCharacter);
        tvEnglishWord = gameView.findViewById(R.id.tvEnglishWord);
        rvWordBank = gameView.findViewById(R.id.rvWordBank);
        
        int characterIndex = getNextCharacterImageIndex();
        int characterResId = (characterIndex % 2 == 0) 
                ? context.getResources().getIdentifier("character_duo_1", "drawable", context.getPackageName())
                : context.getResources().getIdentifier("character_duo_2", "drawable", context.getPackageName());
        if (characterResId != 0) {
            ivCharacter.setImageResource(characterResId);
        }
        
        tvEnglishWord.setText(question.questionText);
        correctAnswer = question.correctAnswer.trim();
        
        if (question.optionsData != null && !question.optionsData.isEmpty()) {
            String[] words = question.optionsData.split("\\|");
            availableWords = new ArrayList<>(Arrays.asList(words));
            Collections.shuffle(availableWords);
            
            FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(context);
            layoutManager.setFlexDirection(com.google.android.flexbox.FlexDirection.ROW);
            layoutManager.setJustifyContent(com.google.android.flexbox.JustifyContent.FLEX_START);
            rvWordBank.setLayoutManager(layoutManager);
            
            wordAdapter = new WordAdapter(availableWords, (word, position) -> {
                selectAnswer(word, position);
            });
            rvWordBank.setAdapter(wordAdapter);
        }
        
        return gameView;
    }
    
    private void selectAnswer(String word, int position) {
        if (isGameDisabled) return;
        
        if (selectedPosition == position && selectedAnswer != null) {
            deselectAnswer();
            return;
        }
        
        if (selectedPosition >= 0 && wordAdapter != null) {
            wordAdapter.unselectWord(selectedPosition);
        }
        
        selectedAnswer = word;
        selectedPosition = position;
        
        if (wordAdapter != null) {
            wordAdapter.setWordSelected(position);
            wordAdapter.enableUnselectedWords();
        }
        
        if (callback != null) {
            callback.enableContinueButton(true);
        }
    }
    
    private void deselectAnswer() {
        if (selectedPosition >= 0 && wordAdapter != null) {
            wordAdapter.unselectWord(selectedPosition);
        }
        selectedAnswer = null;
        selectedPosition = -1;
        
        if (wordAdapter != null) {
            wordAdapter.enableUnselectedWords();
        }
        
        if (callback != null) {
            callback.enableContinueButton(false);
        }
    }
    
    private void disableAllWords() {
        isGameDisabled = true;
        if (rvWordBank != null) {
            rvWordBank.setEnabled(false);
            rvWordBank.setClickable(false);
        }
        // Disable tất cả các word buttons trong adapter
        if (wordAdapter != null) {
            wordAdapter.disableAllWords();
        }
    }
    
    @Override
    public boolean checkAnswer() {
        if (selectedAnswer != null && selectedPosition >= 0) {
            boolean isCorrect = selectedAnswer.trim().equalsIgnoreCase(correctAnswer);
            
            if (wordAdapter != null) {
                wordAdapter.updateWordState(selectedPosition, isCorrect);
            }
            
            if (callback != null) {
                callback.showFeedback(isCorrect ? "Nice!" : "Incorrect!", isCorrect);
            }
            
            return isCorrect;
        }
        return false;
    }
    
    @Override
    public ContinueButtonResult handleContinueButton(boolean isRetryMode, boolean alreadyChecked) {
        if (alreadyChecked && !isRetryMode) {
            return new ContinueButtonResult(false, true);
        } else if (isRetryMode) {
            return new ContinueButtonResult(true, true);
        } else {
            return new ContinueButtonResult(true, false);
        }
    }
    
    @Override
    public void disableGameInteraction() {
        disableAllWords();
    }
}
