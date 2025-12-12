package com.example.learning_app.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learning_app.R;
import com.example.learning_app.db.UserEntity;
import com.example.learning_app.ui.adapter.CalendarAdapter;
import com.example.learning_app.viewmodel.MainViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PersonalStreakFragment extends Fragment {

    private MainViewModel viewModel;
    private Calendar currentCalendar;
    private TextView tvMonthYear;
    private RecyclerView rvCalendar;
    private UserEntity currentUser;
    private TextView tvStreakBig, tvStreakSmall, tvFreezeCount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_personal_streak, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvStreakBig = view.findViewById(R.id.tvStreakBig);
        tvStreakSmall = view.findViewById(R.id.tvStreakSmall);
        tvFreezeCount = view.findViewById(R.id.tvFreezeCount);
        rvCalendar = view.findViewById(R.id.rvCalendar);
        tvMonthYear = view.findViewById(R.id.tvMonthYear);
        ImageView btnPrevMonth = view.findViewById(R.id.btnPrevMonth);
        ImageView btnNextMonth = view.findViewById(R.id.btnNextMonth);

        rvCalendar.setLayoutManager(new GridLayoutManager(getContext(), 7));
        currentCalendar = Calendar.getInstance();

        btnPrevMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            updateCalendarUI();
        });

        btnNextMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            updateCalendarUI();
        });

        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        viewModel.getUserStats().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                currentUser = user;
                tvStreakBig.setText(String.valueOf(user.streak));
                tvStreakSmall.setText(String.valueOf(user.streak));
                int remainingSlots = 2 - user.streakFreezes;
                tvFreezeCount.setText(String.valueOf(remainingSlots));
                updateCalendarUI();
            }
        });
    }

    private void updateCalendarUI() {
        if (tvMonthYear == null || rvCalendar == null) return;

        SimpleDateFormat sdf = new SimpleDateFormat("'tháng' MM 'năm' yyyy", new Locale("vi", "VN"));
        tvMonthYear.setText(sdf.format(currentCalendar.getTime()));

        List<String> daysList = new ArrayList<>();
        Calendar tempCal = (Calendar) currentCalendar.clone();
        tempCal.set(Calendar.DAY_OF_MONTH, 1);
        int maxDaysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int dayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK);

        int emptySlots = (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - 2;

        for (int i = 0; i < emptySlots; i++) {
            daysList.add("");
        }

        SimpleDateFormat formatData = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        for (int i = 1; i <= maxDaysInMonth; i++) {
            tempCal.set(Calendar.DAY_OF_MONTH, i);
            daysList.add(formatData.format(tempCal.getTime()));
        }

        Map<String, Integer> statusMap = generateStatusMap();

        CalendarAdapter adapter = new CalendarAdapter(daysList, statusMap);
        rvCalendar.setAdapter(adapter);
    }

    private Map<String, Integer> generateStatusMap() {
        Map<String, Integer> statusMap = new HashMap<>();
        if (currentUser == null) {
            return statusMap;
        }

        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        if (currentUser.lastFreezeDate != null && !currentUser.lastFreezeDate.isEmpty()) {
            try {
                Date freezeDate = formatter.parse(currentUser.lastFreezeDate);
                if (freezeDate != null) {
                     statusMap.put(parser.format(freezeDate), 2);
                }
            } catch (ParseException e) { e.printStackTrace(); }
        }

        try {
            if (currentUser.lastLessonDate != null && !currentUser.lastLessonDate.isEmpty()){
                Date lastLesson = formatter.parse(currentUser.lastLessonDate);
                Calendar cal = Calendar.getInstance();
                if (lastLesson != null) {
                    cal.setTime(lastLesson);
                    for (int i = 0; i < currentUser.streak; i++) {
                        statusMap.put(parser.format(cal.getTime()), 1);
                        cal.add(Calendar.DATE, -1);
                    }
                }
            }
        } catch (ParseException e) { e.printStackTrace(); }
        
        String todayStr = parser.format(new Date());
        if (!statusMap.containsKey(todayStr)) {
            statusMap.put(todayStr, 0);
        }

        return statusMap;
    }
}