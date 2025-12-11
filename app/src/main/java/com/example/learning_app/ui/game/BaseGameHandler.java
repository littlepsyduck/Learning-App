package com.example.learning_app.ui.game;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.learning_app.entities.Question;

/**
 * Base class cho tất cả các game handler
 * Mỗi loại game sẽ extend class này và implement các method cần thiết
 */
public abstract class BaseGameHandler {
    protected Context context;
    protected Question question;
    protected GameCallback callback;
    protected View gameView;
    
    // UI components chung
    protected Button btnContinue;
    protected TextView tvFeedback;
    
    // Shared character image index để xen kẽ ảnh nhân vật
    private static int sharedCharacterImageIndex = 0;
    
    public interface GameCallback {
        void onAnswerCorrect();
        void onAnswerIncorrect();
        void onGameCompleted();
        void enableContinueButton(boolean enable);
        void showFeedback(String message, boolean isCorrect);
    }
    
    public BaseGameHandler(Context context, Question question, GameCallback callback) {
        this.context = context;
        this.question = question;
        this.callback = callback;
    }
    
    /**
     * Tạo và trả về view của game
     */
    public abstract View createGameView();
    
    /**
     * Kiểm tra đáp án
     * @return true nếu đúng, false nếu sai
     */
    public abstract boolean checkAnswer();
    
    /**
     * Reset game về trạng thái ban đầu
     */
    public abstract void resetGame();
    
    /**
     * Cleanup khi game kết thúc
     */
    public void cleanup() {
        // Override nếu cần cleanup
    }
    
    /**
     * Set UI components chung
     */
    public void setUIComponents(Button btnContinue, TextView tvFeedback) {
        this.btnContinue = btnContinue;
        this.tvFeedback = tvFeedback;
    }
    
    /**
     * Lấy và tăng character image index để xen kẽ ảnh nhân vật
     */
    protected int getNextCharacterImageIndex() {
        return sharedCharacterImageIndex++;
    }
}

