package com.example.learning_app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.learning_app.R;

import java.util.Calendar;

public class DailyStreakActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_streak);

        TextView tvStreakNumber = findViewById(R.id.tvStreakNumber);
        LinearLayout weekDaysLayout = findViewById(R.id.weekDaysLayout);
        Button btnCommitted = findViewById(R.id.btnCommitted);

        // Get streak from Intent and display it
        int currentStreak = getIntent().getIntExtra("currentStreak", 0);
        tvStreakNumber.setText(String.valueOf(currentStreak));

        // Setup the weekly progress view
        setupWeeklyProgressView(weekDaysLayout, currentStreak);

        // Set listener to finish the activity
        btnCommitted.setOnClickListener(v -> finish());
    }

    private void setupWeeklyProgressView(LinearLayout weekDaysLayout, int currentStreak) {
        weekDaysLayout.removeAllViews();

        String[] dayAbbreviations = {"Mo", "Tu", "We", "Th", "Fr", "Sa", "Su"};
        LayoutInflater inflater = LayoutInflater.from(this);

        Calendar calendar = Calendar.getInstance();
        int todayIndex = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7; // Mo=0, Tu=1, ... Su=6

        for (int i = 0; i < 7; i++) {
            View dayView = inflater.inflate(R.layout.item_streak_day, weekDaysLayout, false);
            TextView tvDayName = dayView.findViewById(R.id.tvDayName);
            ImageView ivStatus = dayView.findViewById(R.id.ivStatus);

            tvDayName.setText(dayAbbreviations[i]);

            if (i <= todayIndex) {
                if ((todayIndex - i) < currentStreak) {
                    ivStatus.setImageResource(R.drawable.ic_check_circle);
                    ivStatus.setColorFilter(ContextCompat.getColor(this, R.color.orange));
                } else {
                    ivStatus.setImageResource(R.drawable.ic_check_circle_outline);
                }
            } else {
                ivStatus.setImageResource(R.drawable.ic_check_circle_outline);
            }
            weekDaysLayout.addView(dayView);
        }
    }
}