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

import androidx.appcompat.app.AlertDialog;
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
    private List<Question> questions;
    private List<Question> wrongQuestions = new ArrayList<>();
    private java.util.Map<Integer, Integer> questionRetryCount = new java.util.HashMap<>();
    private java.util.Map<Integer, Boolean> questionRetryCompleted = new java.util.HashMap<>();
    private java.util.Map<Integer, Boolean> questionIsCorrect = new java.util.HashMap<>();
    private Set<Integer> answeredQuestions = new HashSet<>();
    private int currentQuestionIndex = 0;
    private int correctAnswers = 0;
    private int totalCorrectAnswers = 0;
    private int completedQuestions = 0;
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
    
    // Observer for lessons
    private Observer<List<Lesson>> lessonsObserver;

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

        btnClose.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        btnContinue.setOnClickListener(v -> {
            String buttonText = btnContinue.getText().toString();
            if (buttonText.equals("CONTINUE")) {
                Question currentQ = getCurrentQuestion();
                if (currentQ == null || currentGameHandler == null) {
                    return;
                }
                
                boolean alreadyChecked = tvFeedback.getVisibility() == View.VISIBLE && answeredQuestions.contains(currentQ.id);
                BaseGameHandler.ContinueButtonResult result = currentGameHandler.handleContinueButton(isRetryMode, alreadyChecked);
                
                if (result.shouldCheckAnswer) {
                    checkAnswer();
                }
                if (result.shouldMoveToNext) {
                    moveToNextQuestion();
                }
            } else if (buttonText.equals("CLAIM XP")) {
                finish();
            }
        });
    }

    private void hideHearts() {
        if (heartsContainer != null) {
            heartsContainer.setVisibility(View.GONE);
        }
    }

    private Question getCurrentQuestion() {
        if (isRetryMode && currentQuestionIndex < wrongQuestions.size()) {
            return wrongQuestions.get(currentQuestionIndex);
        } else if (!isRetryMode && currentQuestionIndex < questions.size()) {
            return questions.get(currentQuestionIndex);
        }
        return null;
    }

    private boolean isQuestionCompleted(int questionId) {
        Boolean completed = questionRetryCompleted.get(questionId);
        return completed != null && completed;
    }

    private void loadQuestions() {
        viewModel.getQuestionsByLesson(lessonId).observe(this, new Observer<List<Question>>() {
            @Override
            public void onChanged(List<Question> questionList) {
                if (questionList != null && !questionList.isEmpty()) {
                    questions = questionList;
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
                Question q = wrongQuestions.get(index);
                if (isQuestionCompleted(q.id)) {
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
        
        currentGameHandler = GameHandlerFactory.createHandler(this, question, new BaseGameHandler.GameCallback() {
            @Override
            public void onGameCompleted() {
                Question currentQ = getCurrentQuestion();
                if (currentQ != null && currentQ.type == 4) {
                    boolean alreadyChecked = isRetryMode && answeredQuestions.contains(currentQ.id);
                    if (!alreadyChecked) {
                        if (!answeredQuestions.contains(currentQ.id)) {
                            answeredQuestions.add(currentQ.id);
                        }
                        questionIsCorrect.put(currentQ.id, true);
                        totalCorrectAnswers++;
                        if (!isRetryMode) {
                            correctAnswers++;
                        } else {
                            questionRetryCompleted.put(currentQ.id, true);
                        }
                        streak++;
                        tvStreak.setText(streak + " IN A ROW");
                        showFeedback("Nice!", true);
                        btnContinue.setEnabled(true);
                    }
                } else {
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
            View gameView = currentGameHandler.createGameView();
            questionContainer.addView(gameView);
            
            // Reset continue button
            btnContinue.setEnabled(false);
            btnContinue.setText("CONTINUE");
            tvFeedback.setVisibility(View.GONE);
        } else {
            if (!answeredQuestions.contains(question.id)) {
                answeredQuestions.add(question.id);
                questionIsCorrect.put(question.id, true);
                totalCorrectAnswers++;
                if (!isRetryMode) {
                    correctAnswers++;
                    completedQuestions++;
                }
            }
            Toast.makeText(this, "Đang bỏ qua câu hỏi type " + question.type + "...", Toast.LENGTH_SHORT).show();
            new Handler(Looper.getMainLooper()).postDelayed(() -> moveToNextQuestion(), 500);
        }
    }

    private void checkAnswer() {
        Question question = getCurrentQuestion();
        if (question == null) return;

        boolean alreadyChecked = tvFeedback.getVisibility() == View.VISIBLE && answeredQuestions.contains(question.id);
        if (alreadyChecked && !isRetryMode && question.type != 3) {
            return;
        }
        if (!answeredQuestions.contains(question.id)) {
            answeredQuestions.add(question.id);
        }

        boolean isCorrect = currentGameHandler != null && currentGameHandler.checkAnswer();

        if (isCorrect) {
            totalCorrectAnswers++;
            questionIsCorrect.put(question.id, true);
            
            if (!isRetryMode) {
                correctAnswers++;
            } else {
                questionRetryCompleted.put(question.id, true);
            }
            streak++;
        } else {
            streak = 0;
            if (question.type != 4) {
                if (!isRetryMode) {
                    if (!wrongQuestions.contains(question)) {
                        wrongQuestions.add(question);
                        questionRetryCount.put(question.id, 0);
                        questionIsCorrect.put(question.id, false);
                    }
                } else {
                    int retryCount = questionRetryCount.getOrDefault(question.id, 0) + 1;
                    questionRetryCount.put(question.id, retryCount);
                    if (retryCount >= 3) {
                        questionRetryCompleted.put(question.id, true);
                    }
                }
            }
        }

        tvStreak.setText(streak + " IN A ROW");
        
        if (currentGameHandler != null) {
            currentGameHandler.disableGameInteraction();
        }
        
        btnContinue.setEnabled(true);
    }

    private void moveToNextQuestion() {
        if (!isRetryMode) {
            completedQuestions++;
        }
        
        if (isRetryMode) {
            Question currentQ = getCurrentQuestion();
            if (currentQ != null && !isQuestionCompleted(currentQ.id)) {
                questionRetryCompleted.put(currentQ.id, true);
            }
        }
        
        updateProgress();
        
        if (isRetryMode) {
            int nextIndex = findNextUncompletedRetryQuestion(currentQuestionIndex + 1);
            if (nextIndex == -1) {
                showLessonComplete();
                return;
            }
            if (nextIndex == currentQuestionIndex) {
                if (countUncompletedRetryQuestions() == 0) {
                    showLessonComplete();
                    return;
                }
            }
            currentQuestionIndex = nextIndex;
            showQuestion(currentQuestionIndex);
        } else {
            currentQuestionIndex++;
            showQuestion(currentQuestionIndex);
        }
    }
    
    private int findNextUncompletedRetryQuestion(int startIndex) {
        for (int i = startIndex; i < wrongQuestions.size(); i++) {
            if (!isQuestionCompleted(wrongQuestions.get(i).id)) {
                return i;
            }
        }
        for (int i = 0; i < startIndex && i < wrongQuestions.size(); i++) {
            if (!isQuestionCompleted(wrongQuestions.get(i).id)) {
                return i;
            }
        }
        return -1;
    }
    
    private void updateProgress() {
        int progress = questions != null && !questions.isEmpty()
                ? (int) ((completedQuestions * 100.0) / questions.size())
                : 0;
        progressBar.setProgress(Math.min(progress, 100));
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
    
    private int countUncompletedRetryQuestions() {
        int count = 0;
        for (Question q : wrongQuestions) {
            if (!isQuestionCompleted(q.id)) {
                count++;
            }
        }
        return count;
    }

    private void showLessonComplete() {
        long timeSpent = System.currentTimeMillis() - startTime;
        int totalXP = totalCorrectAnswers * 10;
        
        int correctCount = 0;
        for (Question q : questions) {
            Boolean isCorrect = questionIsCorrect.get(q.id);
            if (isCorrect != null && isCorrect) {
                correctCount++;
            }
        }
        int accuracy = questions != null && !questions.isEmpty()
                ? (int) ((correctCount * 100.0) / questions.size())
                : 0;
        
        unlockNextLesson();

        questionContainer.removeAllViews();
        LessonCompleteFragment fragment = LessonCompleteFragment.newInstance(totalXP, accuracy, formatTime(timeSpent));
        fragment.setOnClaimClickListener(() -> {
            // Set result để MainActivity biết lesson đã hoàn thành
            setResult(RESULT_OK);
            // Override transition trước khi finish
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            // Đảm bảo quay về MainActivity
            finish();
        });
        fragment.show(getSupportFragmentManager(), "LessonCompleteFragment");

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
        viewModel.updateLessonCompleted(lessonId, true);
        
        // Remove observer cũ nếu có để tránh duplicate
        if (lessonsObserver != null) {
            viewModel.getAllLessons().removeObserver(lessonsObserver);
        }
        
        // Tạo observer mới
        lessonsObserver = new Observer<List<Lesson>>() {
            @Override
            public void onChanged(List<Lesson> lessons) {
                if (lessons != null && !lessons.isEmpty()) {
                    int currentIndex = -1;
                    Lesson currentLesson = null;
                    for (int i = 0; i < lessons.size(); i++) {
                        if (lessons.get(i).id == lessonId) {
                            currentIndex = i;
                            currentLesson = lessons.get(i);
                            break;
                        }
                    }
                    
                    if (currentLesson == null) {
                        // Remove observer nếu không tìm thấy lesson
                        if (lessonsObserver != null) {
                            viewModel.getAllLessons().removeObserver(lessonsObserver);
                            lessonsObserver = null;
                        }
                        return;
                    }
                    
                    // Unlock lesson tiếp theo trong cùng section
                    if (currentIndex >= 0 && currentIndex < lessons.size() - 1) {
                        Lesson nextLesson = lessons.get(currentIndex + 1);
                        if (nextLesson.sectionId == currentLesson.sectionId && nextLesson.isLocked) {
                            viewModel.updateLessonLocked(nextLesson.id, false);
                        }
                    }
                    
                    // Kiểm tra xem đã hoàn thành tất cả lesson trong section hiện tại chưa
                    boolean allLessonsInSectionCompleted = true;
                    Lesson firstLessonInNextSection = null;
                    
                    for (Lesson lesson : lessons) {
                        if (lesson.sectionId == currentLesson.sectionId) {
                            if (!lesson.isCompleted) {
                                allLessonsInSectionCompleted = false;
                                break;
                            }
                        } else if (lesson.sectionId == currentLesson.sectionId + 1 && firstLessonInNextSection == null) {
                            firstLessonInNextSection = lesson;
                        }
                    }
                    
                    // Nếu đã hoàn thành tất cả lesson trong section, unlock lesson đầu tiên của section tiếp theo
                    if (allLessonsInSectionCompleted && firstLessonInNextSection != null && firstLessonInNextSection.isLocked) {
                        viewModel.updateLessonLocked(firstLessonInNextSection.id, false);
                    }
                    
                    // Remove observer sau khi xử lý xong để tránh trigger nhiều lần
                    if (lessonsObserver != null) {
                        viewModel.getAllLessons().removeObserver(lessonsObserver);
                        lessonsObserver = null;
                    }
                }
            }
        };
        
        // Observe LiveData
        viewModel.getAllLessons().observe(this, lessonsObserver);
    }

    @Override
    public void onBackPressed() {
        // Show confirmation dialog before exiting lesson
        new AlertDialog.Builder(this)
                .setTitle("Thoát bài học?")
                .setMessage("Tiến trình của bạn sẽ không được lưu. Bạn có chắc muốn thoát?")
                .setPositiveButton("Thoát", (dialog, which) -> {
                    setResult(RESULT_CANCELED);
                    super.onBackPressed(); // Call parent implementation to finish activity
                })
                .setNegativeButton("Ở lại", null) // Cancel dialog, stay in lesson
                .setCancelable(true)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cleanup observer để tránh memory leak
        if (lessonsObserver != null && viewModel != null) {
            viewModel.getAllLessons().removeObserver(lessonsObserver);
            lessonsObserver = null;
        }
    }

}

