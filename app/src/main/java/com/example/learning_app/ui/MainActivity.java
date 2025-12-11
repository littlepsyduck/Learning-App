package com.example.learning_app.ui;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learning_app.R;
import com.example.learning_app.entities.Lesson;
import com.example.learning_app.ui.adapter.LessonAdapter;
import com.example.learning_app.viewmodel.LessonViewModel;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private LessonAdapter adapter;
    private RecyclerView rvLessons;
    private TextView tvSectionTitle, tvLessonName;
    private LessonViewModel mLessonViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rvLessons = findViewById(R.id.rvLessons);
        tvSectionTitle = findViewById(R.id.tvSectionTitle);
        tvLessonName = findViewById(R.id.tvLessonName);

        rvLessons.setLayoutManager(new LinearLayoutManager(this));

        adapter = new LessonAdapter(this, lesson -> {
            updateHeader(lesson);
            showStartDialog(lesson);
        });
        rvLessons.setAdapter(adapter);

        // Get ViewModel instance
        mLessonViewModel = new ViewModelProvider(this).get(LessonViewModel.class);

        // Import data from JSON if database is empty
        mLessonViewModel.importDataFromJson();

        // Observe LiveData from ViewModel
        observeLessons();
    }

    private void showStartDialog(Lesson lesson) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_start_lesson);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView tvTitle = dialog.findViewById(R.id.tvDialogTitle);
        TextView tvSubtitle = dialog.findViewById(R.id.tvDialogSubtitle);
        Button btnStart = dialog.findViewById(R.id.btnStartLesson);

        tvTitle.setText(lesson.name);
        tvSubtitle.setText("Section " + lesson.sectionId);

        btnStart.setOnClickListener(v -> {
            dialog.dismiss();
            // Mở LessonActivity
            Intent intent = new Intent(MainActivity.this, LessonActivity.class);
            intent.putExtra("lessonId", lesson.id);
            startActivity(intent);
        });

        dialog.show();
    }

    // Observe LiveData from ViewModel
    private void observeLessons() {
        LiveData<List<Lesson>> lessonsLiveData = mLessonViewModel.getAllLessons();
        
        // Create observer that updates UI when data changes
        Observer<List<Lesson>> lessonsObserver = new Observer<List<Lesson>>() {
            @Override
            public void onChanged(List<Lesson> lessons) {
                // Check if lessons is not null before updating UI
                if (lessons != null) {
                    // Update the cached copy of the lessons in the adapter
                    adapter.setLessons(lessons);
                    updateHeader(findCurrentLesson(lessons));
                }
            }
        };
        
        // Observe the LiveData, passing in this activity as the LifecycleOwner
        lessonsLiveData.observe(this, lessonsObserver);
    }

    private Lesson findCurrentLesson(List<Lesson> lessons) {
        for (Lesson l : lessons) {
            if (!l.isLocked) return l;
        }
        return !lessons.isEmpty() ? lessons.get(0) : null;
    }

    private void updateHeader(Lesson lesson) {
        if (lesson == null) return;
        tvLessonName.setText(lesson.name);

        String sectionName = "";
        switch (lesson.sectionId) {
            case 1: sectionName = "SECTION 1: EASY STARTER"; break;
            case 2: sectionName = "SECTION 2: MEDIUM BOOST"; break;
            case 3: sectionName = "SECTION 3: HARD MASTER"; break;
            default: sectionName = "SECTION " + lesson.sectionId; break;
        }
        tvSectionTitle.setText(sectionName);
    }

}

