package com.example.learning_app.ui.game;

import android.content.Context;

import com.example.learning_app.entities.Question;

public class GameHandlerFactory {
    
    public static BaseGameHandler createHandler(Context context, Question question, BaseGameHandler.GameCallback callback) {
        switch (question.type) {
            case 1:
                return new Type1GameHandler(context, question, callback);
            case 2:
                return new Type2GameHandler(context, question, callback);
            case 3:
                return new Type3GameHandler(context, question, callback);
            case 4:
                return new Type4GameHandler(context, question, callback);
            default:
                return null;
        }
    }
}


