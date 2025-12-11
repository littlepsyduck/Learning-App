package com.example.learning_app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.example.learning_app.R;
import com.example.learning_app.entities.Lesson;
import com.example.learning_app.entities.Question;
import com.example.learning_app.viewmodel.LessonViewModel;
import com.example.learning_app.ui.game.BaseGameHandler;
import com.example.learning_app.ui.game.GameHandlerFactory;
import com.example.learning_app.ui.fragment.LessonCompleteFragment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LessonActivity extends AppCompatActivity {

    private LessonViewModel viewModel;
    private int lessonId;
    private Lesson currentLesson;
    private List<Question> questions;
    private List<Question> wrongQuestions = new ArrayList<>(); // Danh sách câu hỏi làm sai
    private List<Boolean> isRetryQuestion = new ArrayList<>(); // Đánh dấu câu hỏi là làm lại
    private java.util.Map<Integer, Integer> questionRetryCount = new java.util.HashMap<>(); // Track số lần làm lại của mỗi câu (key: questionId, value: retryCount)
    private java.util.Map<Integer, Boolean> questionRetryCompleted = new java.util.HashMap<>(); // Track câu đã hoàn thành trong retry mode (key: questionId, value: isCompleted)
    private java.util.Map<Integer, Boolean> questionIsCorrect = new java.util.HashMap<>(); // Track câu đã làm đúng (key: questionId, value: isCorrect) - để tính accuracy
    private Set<Integer> answeredQuestions = new HashSet<>(); // Track các câu đã được check answer để tránh double counting
    private int currentQuestionIndex = 0;
    private int correctAnswers = 0; // Số câu đúng ở lần làm đầu (để tính progress)
    private int totalCorrectAnswers = 0; // Tổng số câu đúng (cả lần làm đầu và làm lại) - để tính XP và accuracy
    private int totalQuestionsAnswered = 0; // Tổng số câu đã làm (bao gồm cả làm lại) - để tính accuracy
    private int completedQuestions = 0; // Số câu đã hoàn thành ở lượt 1 (đã ấn Continue) - để tính progress
    private long startTime;
    private int streak = 0; // Số câu trả lời đúng liên tiếp
    private boolean isRetryMode = false; // Đang ở chế độ làm lại câu sai

    // UI Components
    private ProgressBar progressBar;
    private TextView tvStreak;
    private LinearLayout heartsContainer;
    private FrameLayout questionContainer;
    private TextView tvFeedback;
    private Button btnContinue;
    private ImageView btnClose;

    // Current game handler
    private BaseGameHandler currentGameHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);

        // Get lesson ID from intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("lessonId")) {
            lessonId = intent.getIntExtra("lessonId", -1);
            if (lessonId == -1) {
                Toast.makeText(this, "Không tìm thấy bài học", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } else {
            Toast.makeText(this, "Không tìm thấy bài học", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(LessonViewModel.class);

        // Initialize UI
        initViews();

        // Start timer
        startTime = System.currentTimeMillis();

        // Load questions
        loadQuestions();
    }

    private void initViews() {
        progressBar = findViewById(R.id.progressBar);
        tvStreak = findViewById(R.id.tvStreak);
        heartsContainer = findViewById(R.id.heartsContainer);
        questionContainer = findViewById(R.id.questionContainer);
        tvFeedback = findViewById(R.id.tvFeedback);
        btnContinue = findViewById(R.id.btnContinue);
        btnClose = findViewById(R.id.btnClose);

        btnClose.setOnClickListener(v -> finish());

        btnContinue.setOnClickListener(v -> {
            String buttonText = btnContinue.getText().toString();
            if (buttonText.equals("CONTINUE")) {
                // Với matching pair (type 4), nếu button được enable thì đã match hết và đã check answer rồi
                // Chỉ cần move to next question
                Question currentQ = isRetryMode && currentQuestionIndex < wrongQuestions.size()
                        ? wrongQuestions.get(currentQuestionIndex)
                        : (currentQuestionIndex < questions.size() ? questions.get(currentQuestionIndex) : null);
                
                if (currentQ != null && currentQ.type == 4) {
                    // Type 4: matching pair - đã check answer khi match hết, chỉ cần move to next
                    moveToNextQuestion();
                } else {
                    // Type khác: check if answer has been checked
                    if (tvFeedback.getVisibility() == View.VISIBLE) {
                        // Answer already checked, move to next question
                        moveToNextQuestion();
                    } else {
                        // Check answer first
                        checkAnswer();
                    }
                }
            } else if (buttonText.equals("CLAIM XP")) {
                finish();
            }
        });
    }

    // Loại bỏ hệ thống máu - ẩn hearts container
    private void hideHearts() {
        if (heartsContainer != null) {
            heartsContainer.setVisibility(View.GONE);
        }
    }

    private void loadQuestions() {
        viewModel.getQuestionsByLesson(lessonId).observe(this, new Observer<List<Question>>() {
            @Override
            public void onChanged(List<Question> questionList) {
                if (questionList != null && !questionList.isEmpty()) {
                    // Lấy tất cả các câu hỏi (hoặc có thể filter theo type nếu cần)
                    questions = questionList;
                    
                    // Khởi tạo danh sách đánh dấu câu hỏi làm lại
                    for (int i = 0; i < questions.size(); i++) {
                        isRetryQuestion.add(false);
                    }
                    hideHearts();
                    showQuestion(0);
                } else {
                    Toast.makeText(LessonActivity.this, "Không tìm thấy câu hỏi trong bài học này", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

    private void showQuestion(int index) {
        // Kiểm tra nếu đã làm xong tất cả câu hỏi
        if (!isRetryMode && index >= questions.size()) {
            // Nếu có câu hỏi sai, chuyển sang chế độ làm lại
            if (!wrongQuestions.isEmpty()) {
                startRetryMode();
                return;
            } else {
                // Không có câu sai, hoàn thành bài học
                showLessonComplete();
                return;
            }
        }
        
        // Nếu đang ở chế độ làm lại
        if (isRetryMode) {
            if (index >= wrongQuestions.size()) {
                // Đã vượt quá danh sách, tìm câu tiếp theo chưa hoàn thành
                int nextIndex = findNextUncompletedRetryQuestion(0);
                if (nextIndex == -1) {
                    showLessonComplete();
                    return;
                }
                index = nextIndex;
            } else {
                // Kiểm tra xem câu này đã hoàn thành chưa
                Question q = wrongQuestions.get(index);
                Boolean completed = questionRetryCompleted.get(q.id);
                if (completed != null && completed) {
                    // Câu này đã hoàn thành, tìm câu tiếp theo
                    int nextIndex = findNextUncompletedRetryQuestion(index + 1);
                    if (nextIndex == -1) {
                        showLessonComplete();
                        return;
                    }
                    index = nextIndex;
                }
            }
        }

        Question question;
        if (isRetryMode) {
            question = wrongQuestions.get(index);
        } else {
            if (index >= questions.size()) {
                // Đã làm xong tất cả câu hỏi
                if (!wrongQuestions.isEmpty()) {
                    startRetryMode();
                    return;
                } else {
                    showLessonComplete();
                    return;
                }
            }
            question = questions.get(index);
        }
        currentQuestionIndex = index;

        // Update progress - tính dựa trên số câu đã hoàn thành (đã ấn Continue)
        updateProgress();

        // Update streak
        tvStreak.setText(streak + " IN A ROW");

        // Clear previous question
        questionContainer.removeAllViews();
        
        // Cleanup previous handler
        if (currentGameHandler != null) {
            currentGameHandler.cleanup();
        }
        
        // Reset answered questions set khi chuyển câu mới (chỉ reset khi không phải retry mode)
        // Trong retry mode, giữ lại để tránh double counting
        if (!isRetryMode) {
            answeredQuestions.clear();
        }
        
        // Create game handler
        currentGameHandler = GameHandlerFactory.createHandler(this, question, new BaseGameHandler.GameCallback() {
            @Override
            public void onAnswerCorrect() {
                // Handled in checkAnswer()
            }
            
            @Override
            public void onAnswerIncorrect() {
                // Handled in checkAnswer()
            }
            
            @Override
            public void onGameCompleted() {
                // Game tự động completed (như matching pair khi match hết)
                // Với matching pair (type 4), nếu match hết thì coi như đúng
                // Đảm bảo chỉ tính 1 lần đúng cho 1 câu hỏi
                Question currentQ = isRetryMode && currentQuestionIndex < wrongQuestions.size()
                        ? wrongQuestions.get(currentQuestionIndex)
                        : (currentQuestionIndex < questions.size() ? questions.get(currentQuestionIndex) : null);
                
                if (currentQ != null && currentQ.type == 4) {
                    // Với type 4, đánh dấu đúng ngay lập tức và không cho check lại
                    if (!answeredQuestions.contains(currentQ.id)) {
                        answeredQuestions.add(currentQ.id);
                        questionIsCorrect.put(currentQ.id, true);
                        totalCorrectAnswers++;
                        if (!isRetryMode) {
                            correctAnswers++;
                        } else {
                            questionRetryCompleted.put(currentQ.id, true);
                        }
                        streak++;
                        tvFeedback.setText("Nice!");
                        tvFeedback.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                        tvFeedback.setVisibility(View.VISIBLE);
                        tvStreak.setText(streak + " IN A ROW");
                    }
                } else {
                    // Type khác, gọi checkAnswer bình thường
                    checkAnswer();
                }
            }
            
            @Override
            public void enableContinueButton(boolean enable) {
                btnContinue.setEnabled(enable);
            }
            
            @Override
            public void showFeedback(String message, boolean isCorrect) {
                tvFeedback.setText(message);
                tvFeedback.setTextColor(getResources().getColor(
                    isCorrect ? android.R.color.holo_green_dark : android.R.color.holo_red_dark));
                tvFeedback.setVisibility(View.VISIBLE);
            }
        });
        
        if (currentGameHandler != null) {
            currentGameHandler.setUIComponents(btnContinue, tvFeedback);
            View gameView = currentGameHandler.createGameView();
            questionContainer.addView(gameView);
            
            // Reset continue button
            btnContinue.setEnabled(false);
            btnContinue.setText("CONTINUE");
            tvFeedback.setVisibility(View.GONE);
        } else {
            // Không hỗ trợ loại game này
            Toast.makeText(this, "Đang bỏ qua câu hỏi type " + question.type + "...", Toast.LENGTH_SHORT).show();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                moveToNextQuestion();
            }, 500);
        }
    }

    // Old methods removed - now using game handlers
    // Type 2 and Type 4 logic moved to Type2GameHandler and Type4GameHandler

    private void checkAnswer() {
        Question question;
        if (isRetryMode) {
            if (currentQuestionIndex >= wrongQuestions.size()) return;
            question = wrongQuestions.get(currentQuestionIndex);
        } else {
            if (currentQuestionIndex >= questions.size()) return;
            question = questions.get(currentQuestionIndex);
        }

        // Tránh double counting - nếu câu này đã được check rồi thì bỏ qua
        if (answeredQuestions.contains(question.id)) {
            return;
        }
        answeredQuestions.add(question.id);

        // Sử dụng game handler để check answer
        boolean isCorrect = false;
        if (currentGameHandler != null) {
            isCorrect = currentGameHandler.checkAnswer();
        }

        if (isCorrect) {
            // Tính vào totalCorrectAnswers (để tính XP) - cả lần làm đầu và làm lại
            totalCorrectAnswers++;
            // Đánh dấu câu này đã làm đúng
            questionIsCorrect.put(question.id, true);
            
            if (!isRetryMode) {
                // Tính vào correctAnswers (để tính progress) - chỉ lần làm đầu
                correctAnswers++;
            } else {
                // Nếu đang làm lại và đúng, đánh dấu câu này đã hoàn thành
                questionRetryCompleted.put(question.id, true);
            }
            streak++;
            tvFeedback.setText("Nice!");
            tvFeedback.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            // Với matching pair (type 4), không thể có trường hợp sai vì phải match hết mới được continue
            // Nếu đến đây mà sai thì có lỗi logic, nhưng vẫn xử lý để tránh crash
            streak = 0;
            if (question.type == 4) {
                // Matching pair không được tính sai, chỉ có "chưa xong" hoặc "xong và đúng"
                // Không thêm vào wrongQuestions
                tvFeedback.setText("Please complete all pairs");
                tvFeedback.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            } else {
                tvFeedback.setText("Incorrect!");
                tvFeedback.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                
                if (!isRetryMode) {
                    // Nếu không phải chế độ làm lại, thêm vào danh sách câu sai
                    if (!wrongQuestions.contains(question)) {
                        wrongQuestions.add(question);
                        questionRetryCount.put(question.id, 0); // Khởi tạo số lần làm lại = 0
                        questionIsCorrect.put(question.id, false); // Đánh dấu câu này làm sai
                    }
                    } else {
                    // Nếu đang làm lại và vẫn sai, tăng số lần làm lại
                    int retryCount = questionRetryCount.getOrDefault(question.id, 0);
                    retryCount++;
                    questionRetryCount.put(question.id, retryCount);
                    
                    // Nếu đã làm lại quá 3 lần, đánh dấu là đã hoàn thành (dù sai)
                    if (retryCount >= 3) {
                        questionRetryCompleted.put(question.id, true);
                        // Vẫn giữ questionIsCorrect = false vì chưa làm đúng
                    }
                }
            }
        }

        totalQuestionsAnswered++;
        tvFeedback.setVisibility(View.VISIBLE);
        tvStreak.setText(streak + " IN A ROW");
        
        // Luôn enable continue button sau khi check answer (kể cả sai)
        btnContinue.setEnabled(true);
        
        // Note: Disable word selection logic is now handled in game handlers
    }

    private void moveToNextQuestion() {
        // Chỉ tăng completedQuestions khi không phải retry mode (để tính progress)
        if (!isRetryMode) {
            completedQuestions++;
        }
        // Cập nhật progress
        updateProgress();
        
        if (isRetryMode) {
            // Tìm câu tiếp theo chưa hoàn thành
            int nextIndex = findNextUncompletedRetryQuestion(currentQuestionIndex + 1);
            
            if (nextIndex == -1) {
                // Không còn câu nào chưa hoàn thành, hoàn thành bài học
                showLessonComplete();
                return;
            }
            
            currentQuestionIndex = nextIndex;
            showQuestion(currentQuestionIndex);
        } else {
            // Không phải retry mode, tăng index bình thường
            currentQuestionIndex++;
            showQuestion(currentQuestionIndex);
        }
    }
    
    // Tìm câu tiếp theo chưa hoàn thành trong retry mode
    private int findNextUncompletedRetryQuestion(int startIndex) {
        for (int i = startIndex; i < wrongQuestions.size(); i++) {
            Question q = wrongQuestions.get(i);
            Boolean completed = questionRetryCompleted.get(q.id);
            if (completed == null || !completed) {
                return i;
            }
        }
        // Nếu không tìm thấy từ startIndex, tìm từ đầu
        for (int i = 0; i < startIndex && i < wrongQuestions.size(); i++) {
            Question q = wrongQuestions.get(i);
            Boolean completed = questionRetryCompleted.get(q.id);
            if (completed == null || !completed) {
                return i;
            }
        }
        return -1; // Không còn câu nào chưa hoàn thành
    }
    
    private void updateProgress() {
        // Thanh trạng thái chỉ nhảy theo các câu ban đầu, max khi xong hết lượt trả lời 1
        int progress = questions != null && !questions.isEmpty()
                ? (int) ((completedQuestions * 100.0) / questions.size())
                : 0;
        // Đảm bảo progress không vượt quá 100
        if (progress > 100) progress = 100;
        progressBar.setProgress(progress);
    }
    
    private void startRetryMode() {
        isRetryMode = true;
        // Tìm câu đầu tiên chưa hoàn thành
        int firstUncompletedIndex = findNextUncompletedRetryQuestion(0);
        if (firstUncompletedIndex == -1) {
            // Tất cả câu đã hoàn thành, chuyển sang finish
            showLessonComplete();
            return;
        }
        currentQuestionIndex = firstUncompletedIndex;
        int remainingCount = countUncompletedRetryQuestions();
        Toast.makeText(this, "Làm lại " + remainingCount + " câu hỏi đã sai (tối đa 3 lần mỗi câu)", Toast.LENGTH_SHORT).show();
        showQuestion(currentQuestionIndex);
    }
    
    // Đếm số câu chưa hoàn thành trong retry mode
    private int countUncompletedRetryQuestions() {
        int count = 0;
        for (Question q : wrongQuestions) {
            Boolean completed = questionRetryCompleted.get(q.id);
            if (completed == null || !completed) {
                count++;
            }
        }
        return count;
    }

    private void showLessonComplete() {
        // Calculate stats
        long timeSpent = System.currentTimeMillis() - startTime;
        // XP: +10 XP mỗi câu trả lời đúng (cả lần làm lại cũng tính)
        int totalXP = totalCorrectAnswers * 10;
        // Accuracy: tính trên số câu ban đầu trong lesson
        // Đếm số câu đã làm đúng (chỉ tính câu thực sự làm đúng, không tính câu hết lượt làm lại)
        int correctCount = 0;
        for (Question q : questions) {
            // Kiểm tra xem câu này đã làm đúng chưa
            Boolean isCorrect = questionIsCorrect.get(q.id);
            if (isCorrect != null && isCorrect) {
                // Câu này đã làm đúng (ở lần đầu hoặc lần làm lại)
                correctCount++;
            }
        }
        // Accuracy = số câu đã làm đúng / số câu ban đầu
        int accuracy = questions != null && !questions.isEmpty()
                ? (int) ((correctCount * 100.0) / questions.size())
                : 0;
        
        // Mở khóa bài tiếp theo
        unlockNextLesson();

        // Show complete screen using Fragment
        questionContainer.removeAllViews();
        LessonCompleteFragment fragment = LessonCompleteFragment.newInstance(totalXP, accuracy, formatTime(timeSpent));
        fragment.setOnClaimClickListener(() -> {
            // TODO: Save progress to database
            finish();
        });
        
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.questionContainer, fragment)
                .commit();

        // Ẩn bottom bar và feedback khi hiển thị màn hình hoàn thành
        findViewById(R.id.bottomBar).setVisibility(View.GONE);
        tvFeedback.setVisibility(View.GONE);
        btnContinue.setVisibility(View.GONE);
    }

    private String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    private void unlockNextLesson() {
        // Đánh dấu bài học hiện tại là đã hoàn thành
        viewModel.updateLessonCompleted(lessonId, true);
        
        // Load danh sách lessons để tìm bài tiếp theo
        viewModel.getAllLessons().observe(this, new Observer<List<Lesson>>() {
            @Override
            public void onChanged(List<Lesson> lessons) {
                if (lessons != null) {
                    // Tìm bài học hiện tại
                    int currentIndex = -1;
                    for (int i = 0; i < lessons.size(); i++) {
                        if (lessons.get(i).id == lessonId) {
                            currentIndex = i;
                            break;
                        }
                    }
                    
                    // Mở khóa bài tiếp theo
                    if (currentIndex >= 0 && currentIndex < lessons.size() - 1) {
                        Lesson nextLesson = lessons.get(currentIndex + 1);
                        if (nextLesson.isLocked) {
                            viewModel.updateLessonLocked(nextLesson.id, false);
                        }
                    }
                }
            }
        });
    }

}

