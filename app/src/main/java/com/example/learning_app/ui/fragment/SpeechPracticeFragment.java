package com.example.learning_app.ui.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.learning_app.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class SpeechPracticeFragment extends Fragment {

    private static final int REQUEST_CODE_SPEECH_INPUT = 100;
    private static final int REQUEST_CODE_AUDIO_PERMISSION = 200;

    private TextView tvPracticeText;
    private TextView tvSpeechResult;
    private TextView tvMatchResult;
    private TextView tvStatus;
    private Button btnSpeak;
    private Button btnRecord;
    private Button btnRefresh;
    private CardView cardUserSpeech;
    private CardView cardMatchResult;
    private ProgressBar progressBar;
    private ImageView ivCharacter;

    private TextToSpeech textToSpeech;
    private String currentSentence;
    private boolean isTTSInitialized = false;
    private int characterIndex = 0;

    private final List<String> practiceSentences = new ArrayList<>(Arrays.asList(
            "Hello, how are you?",
            "My name is Duolingo",
            "I love learning languages",
            "Vietnam is the best country",
            "Japan is an island",
            "This is my flag",
            "I love my country",
            "Paris is in France",
            "The capital of France is Paris",
            "I speak English",
            "Learning is fun",
            "Practice makes perfect"));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_speech_practice, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initializeTTS();
        setupClickListeners();
        loadNewSentence();
    }

    private void initViews(View view) {
        tvPracticeText = view.findViewById(R.id.tvPracticeText);
        tvSpeechResult = view.findViewById(R.id.tvSpeechResult);
        tvMatchResult = view.findViewById(R.id.tvMatchResult);
        tvStatus = view.findViewById(R.id.tvStatus);
        btnSpeak = view.findViewById(R.id.btnSpeak);
        btnRecord = view.findViewById(R.id.btnRecord);
        btnRefresh = view.findViewById(R.id.btnRefresh);
        cardUserSpeech = view.findViewById(R.id.cardUserSpeech);
        cardMatchResult = view.findViewById(R.id.cardMatchResult);
        progressBar = view.findViewById(R.id.progressBar);
        ivCharacter = view.findViewById(R.id.ivCharacter);
    }

    private void initializeTTS() {
        textToSpeech = new TextToSpeech(getContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tvStatus.setText("TTS language not supported");
                    isTTSInitialized = false;
                } else {
                    isTTSInitialized = true;
                    tvStatus.setText("Ready to practice");
                }
            } else {
                tvStatus.setText("TTS initialization failed");
                isTTSInitialized = false;
            }
        });
    }

    private void setupClickListeners() {
        btnSpeak.setOnClickListener(v -> speakText());

        btnRecord.setOnClickListener(v -> {
            if (checkAudioPermission()) {
                startSpeechRecognition();
            } else {
                requestAudioPermission();
            }
        });

        btnRefresh.setOnClickListener(v -> loadNewSentence());
    }

    private void speakText() {
        if (!isTTSInitialized) {
            Toast.makeText(getContext(), "TTS not ready. Please wait...", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentSentence != null && !currentSentence.isEmpty()) {
            textToSpeech.speak(currentSentence, TextToSpeech.QUEUE_FLUSH, null, null);
            tvStatus.setText("Playing audio...");
        }
    }

    private boolean checkAudioPermission() {
        return ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestAudioPermission() {
        ActivityCompat.requestPermissions(
                requireActivity(),
                new String[] { Manifest.permission.RECORD_AUDIO },
                REQUEST_CODE_AUDIO_PERMISSION);
    }

    private void startSpeechRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...");

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
            progressBar.setVisibility(View.VISIBLE);
            tvStatus.setText("Listening...");
        } catch (Exception e) {
            Toast.makeText(getContext(), "Speech recognition not available: " + e.getMessage(), Toast.LENGTH_SHORT)
                    .show();
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        progressBar.setVisibility(View.GONE);

        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == -1 && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (result != null && !result.isEmpty()) {
                    String userSpeech = result.get(0);
                    processSpeechResult(userSpeech);
                } else {
                    tvStatus.setText("No speech detected");
                }
            } else {
                tvStatus.setText("Speech recognition cancelled or failed");
            }
        }
    }

    private void processSpeechResult(String userSpeech) {
        cardUserSpeech.setVisibility(View.VISIBLE);
        tvSpeechResult.setText(userSpeech);

        if (currentSentence == null || currentSentence.isEmpty()) {
            cardMatchResult.setVisibility(View.VISIBLE);
            tvMatchResult.setText("Error: No sentence to compare");
            tvMatchResult.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
            tvStatus.setText("Error: Please refresh and try again");
            return;
        }

        String trimmedUserSpeech = userSpeech.trim();
        boolean isMatch = currentSentence.equalsIgnoreCase(trimmedUserSpeech);

        cardMatchResult.setVisibility(View.VISIBLE);

        if (isMatch) {
            tvMatchResult.setText("✓ Perfect match!");
            tvMatchResult.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark));
            tvStatus.setText("Excellent! You got it right!");
        } else {
            tvMatchResult.setText("✗ Not matching");
            tvMatchResult.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
            tvStatus.setText("Try again. Listen to the sentence and repeat.");
        }
    }

    private void loadNewSentence() {
        if (practiceSentences.isEmpty()) {
            tvStatus.setText("No sentences available");
            return;
        }

        Random random = new Random();
        currentSentence = practiceSentences.get(random.nextInt(practiceSentences.size()));

        characterIndex++;
        int characterResId = (characterIndex % 2 == 0)
                ? getResources().getIdentifier("character_duo_1", "drawable", requireContext().getPackageName())
                : getResources().getIdentifier("character_duo_2", "drawable", requireContext().getPackageName());
        if (characterResId != 0 && ivCharacter != null) {
            ivCharacter.setImageResource(characterResId);
        }

        tvPracticeText.setText(currentSentence);
        cardUserSpeech.setVisibility(View.GONE);
        cardMatchResult.setVisibility(View.GONE);
        tvStatus.setText("Ready to practice");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startSpeechRecognition();
            } else {
                Toast.makeText(getContext(), "Microphone permission is required for speech practice", Toast.LENGTH_LONG)
                        .show();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}
