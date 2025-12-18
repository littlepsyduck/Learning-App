package com.example.learning_app.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learning_app.R;
import com.example.learning_app.entities.User;
import com.example.learning_app.ui.adapter.CalendarAdapter;
import com.example.learning_app.viewmodel.ProgressViewModel;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StreakActivity extends AppCompatActivity {

    private ProgressViewModel viewModel;

    private Calendar currentCalendar;
    private TextView tvMonthYear;
    private RecyclerView rvCalendar;
    private androidx.lifecycle.Observer<User> userObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streak);

        viewModel = new ViewModelProvider(this).get(ProgressViewModel.class);

        TextView txtStreakBig = findViewById(R.id.tvStreakBig);
        TextView txtStreakSmall = findViewById(R.id.tvStreakSmall);
        TextView txtFreezeCount = findViewById(R.id.tvFreezeCount);

        rvCalendar = findViewById(R.id.rvCalendar);
        tvMonthYear = findViewById(R.id.tvMonthYear);
        ImageView btnPrevMonth = findViewById(R.id.btnPrevMonth);
        ImageView btnNextMonth = findViewById(R.id.btnNextMonth);
        View btnClose = findViewById(R.id.btnClose);

        TextView tabPersonal = findViewById(R.id.tabPersonal);
        TextView tabFriends = findViewById(R.id.tabFriends);
        View contentPersonal = findViewById(R.id.contentPersonal);
        View contentFriends = findViewById(R.id.contentFriends);

        rvCalendar.setLayoutManager(new GridLayoutManager(this, 7));
        currentCalendar = Calendar.getInstance();
        
        loadStudyDatesAndUpdateCalendar();

        if (btnPrevMonth != null) {
            btnPrevMonth.setOnClickListener(v -> {
                currentCalendar.add(Calendar.MONTH, -1);
                loadStudyDatesAndUpdateCalendar();
            });
        }
        if (btnNextMonth != null) {
            btnNextMonth.setOnClickListener(v -> {
                currentCalendar.add(Calendar.MONTH, 1);
                loadStudyDatesAndUpdateCalendar();
            });
        }

        if (tabPersonal != null && tabFriends != null && contentPersonal != null && contentFriends != null) {
            tabPersonal.setOnClickListener(v -> {
                contentPersonal.setVisibility(View.VISIBLE);
                contentFriends.setVisibility(View.GONE);
                tabPersonal.setTextColor(Color.WHITE);
                tabPersonal.setBackgroundResource(R.drawable.bg_tab_indicator);
                tabFriends.setTextColor(Color.parseColor("#CCFFFFFF"));
                tabFriends.setBackground(null);
            });

            tabFriends.setOnClickListener(v -> {
                contentPersonal.setVisibility(View.GONE);
                contentFriends.setVisibility(View.VISIBLE);
                tabFriends.setTextColor(Color.WHITE);
                tabFriends.setBackgroundResource(R.drawable.bg_tab_indicator);
                tabPersonal.setTextColor(Color.parseColor("#CCFFFFFF"));
                tabPersonal.setBackground(null);
            });
        }
        
        AppCompatButton btnFindFriends = findViewById(R.id.btnFindFriends);
        if (btnFindFriends != null) {
            btnFindFriends.setOnClickListener(v -> {
                Intent intent = new Intent(StreakActivity.this, UserDashboardActivity.class);
                intent.putExtra("SELECTED_TAB", "friends");
                startActivity(intent);
                finish();
            });
        }

        viewModel.reloadUserProfile();
        
        userObserver = user -> {
            if (user != null) {
                if (txtStreakBig != null) txtStreakBig.setText(String.valueOf(user.getStreak()));
                if (txtStreakSmall != null) txtStreakSmall.setText(String.valueOf(user.getStreak()));
                if (txtFreezeCount != null) txtFreezeCount.setText(String.valueOf(user.getFreeze()));
                loadStudyDatesAndUpdateCalendar(user);
            }
        };
        viewModel.getCurrentUser().observe(this, userObserver);

        if (btnClose != null) btnClose.setOnClickListener(v -> finish());
    }

    private void loadStudyDatesAndUpdateCalendar() {
        User currentUser = null;
        if (viewModel != null && viewModel.getCurrentUser() != null) {
             currentUser = viewModel.getCurrentUser().getValue();
        }
        
        if (currentUser != null) {
            loadStudyDatesAndUpdateCalendar(currentUser);
        } else {
            updateCalendarUI(new HashMap<>());
        }
    }

    private void loadStudyDatesAndUpdateCalendar(User user) {
        if (user == null || user.getUid() == null) {
            updateCalendarUI(new HashMap<>());
            return;
        }
        
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Map<String, Integer> statusMap = new HashMap<>();
                        try {
                            List<String> studyDates = (List<String>) task.getResult().get("studyDates");
                            if (studyDates != null) {
                                for (String dateStr : studyDates) {
                                    statusMap.put(dateStr, 1);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        updateCalendarUI(statusMap);
                    } else {
                        updateCalendarUI(new HashMap<>());
                    }
                });
    }

    private void updateCalendarUI() {
        loadStudyDatesAndUpdateCalendar();
    }

    private void updateCalendarUI(Map<String, Integer> statusMap) {
        if (tvMonthYear == null || rvCalendar == null) return;

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH);
        tvMonthYear.setText(sdf.format(currentCalendar.getTime()));

        List<String> daysList = new ArrayList<>();
        Calendar tempCal = (Calendar) currentCalendar.clone();

        tempCal.set(Calendar.DAY_OF_MONTH, 1);
        int maxDaysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int dayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK);

        int emptySlots = 0;
        if (dayOfWeek == Calendar.SUNDAY) {
            emptySlots = 6;
        } else {
            emptySlots = dayOfWeek - 2;
        }

        for (int i = 0; i < emptySlots; i++) {
            daysList.add("");
        }

        SimpleDateFormat formatData = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        for (int i = 1; i <= maxDaysInMonth; i++) {
            tempCal.set(Calendar.DAY_OF_MONTH, i);
            daysList.add(formatData.format(tempCal.getTime()));
        }

        if (rvCalendar != null && statusMap != null) {
            CalendarAdapter adapter = new CalendarAdapter(daysList, statusMap);
            rvCalendar.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }
}