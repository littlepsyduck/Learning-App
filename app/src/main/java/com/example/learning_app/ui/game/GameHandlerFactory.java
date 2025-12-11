package com.example.learning_app.ui.game;

import android.content.Context;

import com.example.learning_app.entities.Question;

/**
 * Factory để tạo game handler dựa trên type của question
 */
public class GameHandlerFactory {
    
    public static BaseGameHandler createHandler(Context context, Question question, BaseGameHandler.GameCallback callback) {
        switch (question.type) {
            case 2:
                return new Type2GameHandler(context, question, callback);
            case 4:
                return new Type4GameHandler(context, question, callback);
            default:
                return null;
        }
    }
}


