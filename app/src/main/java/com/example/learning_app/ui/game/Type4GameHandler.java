package com.example.learning_app.ui.game;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.learning_app.R;
import com.example.learning_app.entities.Question;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Type4GameHandler extends BaseGameHandler {
    
    private ImageView ivCharacter;
    private TextView tvInstruction;
    private LinearLayout leftColumn, rightColumn;
    private List<MatchingPair> matchingPairs = new ArrayList<>();
    private TextView selectedLeftCard = null;
    private TextView selectedRightCard = null;
    private int matchedCount = 0;
    private boolean isGameDisabled = false;
    
    private static class MatchingPair {
        String english;
        String vietnamese;
        TextView leftCard;
        TextView rightCard;
        boolean isMatched = false;
        
        MatchingPair(String en, String vn) {
            this.english = en;
            this.vietnamese = vn;
        }
    }
    
    public Type4GameHandler(Context context, Question question, GameCallback callback) {
        super(context, question, callback);
    }
    
    @Override
    public View createGameView() {
        gameView = LayoutInflater.from(context)
                .inflate(R.layout.layout_question_type4, null);
        
        ivCharacter = gameView.findViewById(R.id.ivCharacter);
        tvInstruction = gameView.findViewById(R.id.tvInstruction);
        leftColumn = gameView.findViewById(R.id.leftColumn);
        rightColumn = gameView.findViewById(R.id.rightColumn);
        
        int characterIndex = getNextCharacterImageIndex();
        int characterResId = (characterIndex % 2 == 0) 
                ? context.getResources().getIdentifier("character_duo_1", "drawable", context.getPackageName())
                : context.getResources().getIdentifier("character_duo_2", "drawable", context.getPackageName());
        if (characterResId != 0) {
            ivCharacter.setImageResource(characterResId);
        }
        
        tvInstruction.setText(question.questionText);
        
        matchingPairs.clear();
        matchedCount = 0;
        selectedLeftCard = null;
        selectedRightCard = null;
        
        try {
            JSONArray pairsArray = new JSONArray(question.correctAnswer);
            for (int i = 0; i < pairsArray.length(); i++) {
                JSONObject pair = pairsArray.getJSONObject(i);
                String en = pair.getString("en");
                String vn = pair.getString("vn");
                matchingPairs.add(new MatchingPair(en, vn));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return gameView;
        }
        
        List<String> leftWords = new ArrayList<>();
        List<String> rightWords = new ArrayList<>();
        for (MatchingPair pair : matchingPairs) {
            leftWords.add(pair.english);
            rightWords.add(pair.vietnamese);
        }
        Collections.shuffle(leftWords);
        Collections.shuffle(rightWords);
        
        leftColumn.removeAllViews();
        for (String word : leftWords) {
            TextView card = createMatchingCard(word, true);
            leftColumn.addView(card);
            for (MatchingPair pair : matchingPairs) {
                if (pair.english.equals(word)) {
                    pair.leftCard = card;
                    break;
                }
            }
        }
        
        rightColumn.removeAllViews();
        for (String word : rightWords) {
            TextView card = createMatchingCard(word, false);
            rightColumn.addView(card);
            for (MatchingPair pair : matchingPairs) {
                if (pair.vietnamese.equals(word)) {
                    pair.rightCard = card;
                    break;
                }
            }
        }
        
        return gameView;
    }
    
    @Override
    public boolean checkAnswer() {
        return matchedCount >= matchingPairs.size();
    }
    
    @Override
    public ContinueButtonResult handleContinueButton(boolean isRetryMode, boolean alreadyChecked) {
        return new ContinueButtonResult(false, true);
    }
    
    @Override
    public void disableGameInteraction() {
        isGameDisabled = true;
        if (leftColumn != null) {
            disableColumnInteraction(leftColumn);
        }
        if (rightColumn != null) {
            disableColumnInteraction(rightColumn);
        }
    }
    
    private void disableColumnInteraction(LinearLayout column) {
        for (int i = 0; i < column.getChildCount(); i++) {
            View child = column.getChildAt(i);
            if (child instanceof TextView) {
                child.setClickable(false);
                child.setEnabled(false);
            }
        }
    }
    
    private TextView createMatchingCard(String text, boolean isLeft) {
        TextView card = new TextView(context);
        card.setText(text);
        card.setPadding(20, 16, 20, 16);
        card.setBackgroundResource(R.drawable.bg_word_button);
        card.setTextColor(context.getResources().getColor(android.R.color.black));
        card.setTextSize(16);
        card.setMinHeight(56);
        card.setGravity(android.view.Gravity.CENTER);
        card.setClickable(true);
        card.setFocusable(true);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 8, 0, 8);
        card.setLayoutParams(params);
        
        card.setOnClickListener(v -> onMatchingCardClick(card, isLeft));
        
        return card;
    }
    
    private void onMatchingCardClick(TextView card, boolean isLeft) {
        if (isGameDisabled) return;
        
        for (MatchingPair pair : matchingPairs) {
            if ((isLeft && pair.leftCard == card) || (!isLeft && pair.rightCard == card)) {
                if (pair.isMatched) return;
            }
        }
        
        if (isLeft) {
            if (selectedLeftCard != null && selectedLeftCard != card) {
                selectedLeftCard.setBackgroundResource(R.drawable.bg_word_button);
            }
            selectedLeftCard = card;
            card.setBackgroundResource(R.drawable.bg_matching_card_selected_blue);
            
            if (selectedRightCard != null) {
                checkMatchingPair();
            }
        } else {
            if (selectedRightCard != null && selectedRightCard != card) {
                selectedRightCard.setBackgroundResource(R.drawable.bg_word_button);
            }
            selectedRightCard = card;
            card.setBackgroundResource(R.drawable.bg_matching_card_selected_blue);
            
            if (selectedLeftCard != null) {
                checkMatchingPair();
            }
        }
    }
    
    private void checkMatchingPair() {
        if (selectedLeftCard == null || selectedRightCard == null) return;
        
        MatchingPair matchedPair = null;
        for (MatchingPair pair : matchingPairs) {
            if (pair.leftCard == selectedLeftCard && pair.rightCard == selectedRightCard) {
                matchedPair = pair;
                break;
            }
        }
        
        if (matchedPair != null) {
            selectedLeftCard.setBackgroundResource(R.drawable.bg_matching_card_correct);
            selectedRightCard.setBackgroundResource(R.drawable.bg_matching_card_correct);
            selectedLeftCard.setEnabled(false);
            selectedRightCard.setEnabled(false);
            matchedPair.isMatched = true;
            matchedCount++;
            
            selectedLeftCard = null;
            selectedRightCard = null;
            
            if (matchedCount >= matchingPairs.size() && callback != null) {
                callback.showFeedback("Nice!", true);
                callback.enableContinueButton(true);
                callback.onGameCompleted();
            }
        } else {
            selectedLeftCard.setBackgroundResource(R.drawable.bg_matching_card_incorrect);
            selectedRightCard.setBackgroundResource(R.drawable.bg_matching_card_incorrect);
            
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (selectedLeftCard != null) {
                    selectedLeftCard.setBackgroundResource(R.drawable.bg_word_button);
                    selectedLeftCard = null;
                }
                if (selectedRightCard != null) {
                    selectedRightCard.setBackgroundResource(R.drawable.bg_word_button);
                    selectedRightCard = null;
                }
            }, 500);
        }
    }
}

