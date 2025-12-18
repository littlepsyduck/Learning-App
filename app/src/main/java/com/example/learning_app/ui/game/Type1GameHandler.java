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

public class Type1GameHandler extends BaseGameHandler {

    private ImageView ivCharacter;
    private LinearLayout answerLine1, answerLine2, answerLine3;
    private View line1, line2, line3;
    private RecyclerView rvWordBank;
    private WordAdapter wordAdapter;
    private List<String> availableWords = new ArrayList<>();
    private String selectedWord = null;
    private String questionTextTemplate;
    private boolean isGameDisabled = false;

    public Type1GameHandler(Context context, Question question, GameCallback callback) {
        super(context, question, callback);
    }

    @Override
    public View createGameView() {
        gameView = LayoutInflater.from(context)
                .inflate(R.layout.layout_question_type1, null);

        ivCharacter = gameView.findViewById(R.id.ivCharacter);
        answerLine1 = gameView.findViewById(R.id.answerLine1);
        answerLine2 = gameView.findViewById(R.id.answerLine2);
        answerLine3 = gameView.findViewById(R.id.answerLine3);
        line1 = gameView.findViewById(R.id.line1);
        line2 = gameView.findViewById(R.id.line2);
        line3 = gameView.findViewById(R.id.line3);
        rvWordBank = gameView.findViewById(R.id.rvWordBank);

        int characterIndex = getNextCharacterImageIndex();
        int characterResId = (characterIndex % 2 == 0)
                ? context.getResources().getIdentifier("character_duo_1", "drawable", context.getPackageName())
                : context.getResources().getIdentifier("character_duo_2", "drawable", context.getPackageName());
        if (characterResId != 0) {
            ivCharacter.setImageResource(characterResId);
        }

        questionTextTemplate = question.questionText;

        if (question.optionsData != null && !question.optionsData.isEmpty()) {
            String[] words = question.optionsData.split("\\|");
            availableWords = new ArrayList<>(Arrays.asList(words));
            Collections.shuffle(availableWords);

            FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(context);
            layoutManager.setFlexDirection(com.google.android.flexbox.FlexDirection.ROW);
            layoutManager.setJustifyContent(com.google.android.flexbox.JustifyContent.FLEX_START);
            rvWordBank.setLayoutManager(layoutManager);

            wordAdapter = new WordAdapter(availableWords, (word, position) -> {
                selectWord(word, position);
            });
            rvWordBank.setAdapter(wordAdapter);
        }

        setupAnswerContainer();

        return gameView;
    }

    @Override
    public boolean checkAnswer() {
        if (selectedWord == null) {
            return false;
        }
        String userAnswer = selectedWord.trim();
        String correctAnswer = question.correctAnswer.trim();
        boolean isCorrect = userAnswer.equalsIgnoreCase(correctAnswer);

        if (callback != null) {
            callback.showFeedback(isCorrect ? "Nice!" : "Incorrect!", isCorrect);
        }

        return isCorrect;
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

    private void setupAnswerContainer() {
        answerLine1.removeAllViews();
        answerLine2.removeAllViews();
        answerLine3.removeAllViews();
        answerLine2.setVisibility(View.GONE);
        answerLine3.setVisibility(View.GONE);
        line2.setVisibility(View.GONE);
        line3.setVisibility(View.GONE);
        line1.setVisibility(View.VISIBLE);

        if (selectedWord == null) {
            displayQuestionWithPlaceholder();
        } else {
            displayQuestionWithAnswer();
        }

        if (callback != null) {
            callback.enableContinueButton(selectedWord != null);
        }
    }

    private void displayQuestionWithPlaceholder() {
        answerLine1.removeAllViews();

        String pattern = getBlankPattern();
        if (pattern != null) {
            int firstIndex = questionTextTemplate.indexOf(pattern);
            String before = questionTextTemplate.substring(0, firstIndex);
            String after = questionTextTemplate.substring(firstIndex + pattern.length());

            if (!before.trim().isEmpty()) {
                answerLine1.addView(createTextPart(before.trim()));
            }
            answerLine1.addView(createPlaceholderView());
            if (!after.trim().isEmpty()) {
                answerLine1.addView(createTextPart(after.trim()));
            }
        } else {
            answerLine1.addView(createTextPart(questionTextTemplate));
        }
    }

    private void displayQuestionWithAnswer() {
        answerLine1.removeAllViews();

        String pattern = getBlankPattern();
        if (pattern != null && selectedWord != null) {
            int firstIndex = questionTextTemplate.indexOf(pattern);
            String before = questionTextTemplate.substring(0, firstIndex);
            String after = questionTextTemplate.substring(firstIndex + pattern.length());

            if (!before.trim().isEmpty()) {
                answerLine1.addView(createTextPart(before.trim()));
            }
            answerLine1.addView(createAnswerWordView(selectedWord));
            if (!after.trim().isEmpty()) {
                answerLine1.addView(createTextPart(after.trim()));
            }
        } else {
            answerLine1.addView(createTextPart(questionTextTemplate));
        }
    }

    private String getBlankPattern() {
        if (questionTextTemplate.contains("____")) {
            return "____";
        } else if (questionTextTemplate.contains("___")) {
            return "___";
        } else if (questionTextTemplate.contains("...")) {
            return "...";
        }
        return null;
    }

    private TextView createTextPart(String text) {
        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setTextSize(18);
        textView.setTextColor(context.getResources().getColor(android.R.color.black));
        textView.setPadding(4, 8, 4, 8);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(2, 4, 2, 4);
        textView.setLayoutParams(params);

        return textView;
    }

    private TextView createPlaceholderView() {
        TextView placeholderView = new TextView(context);
        placeholderView.setText("");
        placeholderView.setTextSize(16);
        placeholderView.setTextColor(context.getResources().getColor(android.R.color.transparent));
        placeholderView.setPadding(20, 16, 20, 16);
        placeholderView.setBackgroundResource(R.drawable.bg_word_button);
        placeholderView.setMinWidth(80);
        placeholderView.setMinHeight(56);
        placeholderView.setGravity(android.view.Gravity.CENTER);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(4, 4, 4, 4);
        placeholderView.setLayoutParams(params);

        return placeholderView;
    }

    private TextView createAnswerWordView(String word) {
        TextView wordView = new TextView(context);
        wordView.setText(word);
        wordView.setPadding(20, 16, 20, 16);
        wordView.setBackgroundResource(R.drawable.bg_word_button);
        wordView.setTextColor(context.getResources().getColor(android.R.color.black));
        wordView.setTextSize(16);
        wordView.setMinWidth(80);
        wordView.setMinHeight(56);
        wordView.setGravity(android.view.Gravity.CENTER);
        wordView.setClickable(true);
        wordView.setFocusable(true);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(4, 4, 4, 4);
        wordView.setLayoutParams(params);

        wordView.setOnClickListener(v -> {
            removeSelectedWord();
        });

        return wordView;
    }

    private void selectWord(String word, int position) {
        if (isGameDisabled)
            return;

        selectedWord = word;
        if (wordAdapter != null) {
            wordAdapter.markWordAsUsed(position);
        }
        setupAnswerContainer();
    }

    private void removeSelectedWord() {
        if (isGameDisabled)
            return;

        if (selectedWord != null) {
            String wordToRemove = selectedWord;
            selectedWord = null;
            if (wordAdapter != null) {
                int wordPosition = wordAdapter.findWordPosition(wordToRemove);
                if (wordPosition >= 0) {
                    wordAdapter.markWordAsUnused(wordPosition);
                }
            }
            setupAnswerContainer();
        }
    }

    @Override
    public void disableGameInteraction() {
        isGameDisabled = true;
        if (rvWordBank != null) {
            rvWordBank.setEnabled(false);
            rvWordBank.setClickable(false);
        }
        if (answerLine1 != null) {
            disableLineInteraction(answerLine1);
        }
        if (answerLine2 != null) {
            disableLineInteraction(answerLine2);
        }
        if (answerLine3 != null) {
            disableLineInteraction(answerLine3);
        }
    }

    private void disableLineInteraction(LinearLayout line) {
        for (int i = 0; i < line.getChildCount(); i++) {
            View child = line.getChildAt(i);
            if (child instanceof TextView && child.isClickable()) {
                child.setClickable(false);
                child.setEnabled(false);
            }
        }
    }

}
