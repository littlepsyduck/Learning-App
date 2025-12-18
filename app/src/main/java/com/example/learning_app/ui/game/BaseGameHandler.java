package com.example.learning_app.ui.game;

import android.content.Context;
import android.view.View;

import com.example.learning_app.entities.Question;

public abstract class BaseGameHandler {
    protected Context context;
    protected Question question;
    protected GameCallback callback;
    protected View gameView;
    
    private static int sharedCharacterImageIndex = 0;
    
    public interface GameCallback {
        void onGameCompleted();
        void enableContinueButton(boolean enable);
        void showFeedback(String message, boolean isCorrect);
    }
    
    public BaseGameHandler(Context context, Question question, GameCallback callback) {
        this.context = context;
        this.question = question;
        this.callback = callback;
    }
    
    public abstract View createGameView();
    
    public abstract boolean checkAnswer();
    
    public void disableGameInteraction() {
    }
    
    protected int getNextCharacterImageIndex() {
        return sharedCharacterImageIndex++;
    }
    
    public ContinueButtonResult handleContinueButton(boolean isRetryMode, boolean alreadyChecked) {
        if (alreadyChecked && !isRetryMode) {
            return new ContinueButtonResult(false, true);
        } else {
            return new ContinueButtonResult(true, false);
        }
    }
    
    public static class ContinueButtonResult {
        public final boolean shouldCheckAnswer;
        public final boolean shouldMoveToNext;
        
        public ContinueButtonResult(boolean shouldCheckAnswer, boolean shouldMoveToNext) {
            this.shouldCheckAnswer = shouldCheckAnswer;
            this.shouldMoveToNext = shouldMoveToNext;
        }
    }
}

