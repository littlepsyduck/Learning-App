package com.example.learning_app.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learning_app.R;
import com.example.learning_app.ui.adapter.CalendarAdapter;
import com.example.learning_app.viewmodel.MainViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StreakActivity extends AppCompatActivity {

    private MainViewModel viewModel;

    // Biến quản lý Lịch
    private Calendar currentCalendar;
    private TextView tvMonthYear;
    private RecyclerView rvCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streak);

        // 1. ÁNH XẠ VIEW
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

        // 2. SETUP LỊCH
        rvCalendar.setLayoutManager(new GridLayoutManager(this, 7));
        currentCalendar = Calendar.getInstance();

        updateCalendarUI(); // Vẽ lịch lần đầu

        // Sự kiện chuyển tháng
        if (btnPrevMonth != null) {
            btnPrevMonth.setOnClickListener(v -> {
                currentCalendar.add(Calendar.MONTH, -1);
                updateCalendarUI();
            });
        }
        if (btnNextMonth != null) {
            btnNextMonth.setOnClickListener(v -> {
                currentCalendar.add(Calendar.MONTH, 1);
                updateCalendarUI();
            });
        }

        // ==========================================
        // 3. LOGIC TABS (ĐÃ KHÔI PHỤC LOGIC CLICK)
        // ==========================================
        if (tabPersonal != null && tabFriends != null && contentPersonal != null && contentFriends != null) {

            // Xử lý bấm Tab CÁ NHÂN
            tabPersonal.setOnClickListener(v -> {
                contentPersonal.setVisibility(View.VISIBLE);
                contentFriends.setVisibility(View.GONE);
                tabPersonal.setTextColor(Color.WHITE);
                tabPersonal.setBackgroundResource(R.drawable.bg_tab_indicator);
                tabFriends.setTextColor(Color.parseColor("#CCFFFFFF"));
                tabFriends.setBackground(null);
            });

            // Xử lý bấm Tab BẠN BÈ
            tabFriends.setOnClickListener(v -> {
                contentPersonal.setVisibility(View.GONE);
                contentFriends.setVisibility(View.VISIBLE);
                tabFriends.setTextColor(Color.WHITE);
                tabFriends.setBackgroundResource(R.drawable.bg_tab_indicator);
                tabPersonal.setTextColor(Color.parseColor("#CCFFFFFF"));
                tabPersonal.setBackground(null);
            });
        }

        // 4. VIEWMODEL
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        viewModel.getUserStats().observe(this, user -> {
            if (user != null) {
                if (txtStreakBig != null) txtStreakBig.setText(String.valueOf(user.streak));
                if (txtStreakSmall != null) txtStreakSmall.setText(String.valueOf(user.streak));
                if (txtFreezeCount != null) txtFreezeCount.setText(String.valueOf(user.streakFreezes));
            }
        });

        if (btnClose != null) btnClose.setOnClickListener(v -> finish());
    }

    // --- HÀM CẬP NHẬT GIAO DIỆN LỊCH (Giữ nguyên logic chuẩn) ---
    private void updateCalendarUI() {
        if (tvMonthYear == null || rvCalendar == null) return;

        // 1. Cập nhật tiêu đề tháng
        SimpleDateFormat sdf = new SimpleDateFormat("'tháng' MM 'năm' yyyy", new Locale("vi", "VN"));
        tvMonthYear.setText(sdf.format(currentCalendar.getTime()));

        List<String> daysList = new ArrayList<>();
        Calendar tempCal = (Calendar) currentCalendar.clone();

        tempCal.set(Calendar.DAY_OF_MONTH, 1);
        int maxDaysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int dayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK);

        // Tính số ô trống
        int emptySlots = 0;
        if (dayOfWeek == Calendar.SUNDAY) {
            emptySlots = 6;
        } else {
            emptySlots = dayOfWeek - 2;
        }

        // Chèn ô trống
        for (int i = 0; i < emptySlots; i++) {
            daysList.add("");
        }

        // Chèn ngày thật
        SimpleDateFormat formatData = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        for (int i = 1; i <= maxDaysInMonth; i++) {
            tempCal.set(Calendar.DAY_OF_MONTH, i);
            daysList.add(formatData.format(tempCal.getTime()));
        }

        // Tạo dữ liệu giả lập (Mock Data)
        Map<String, Integer> statusMap = new HashMap<>();
        statusMap.put("2025-12-01", 1);
        statusMap.put("2025-12-02", 1);
        statusMap.put("2025-12-03", 1);
        statusMap.put("2025-12-04", 2);
        statusMap.put("2025-12-11", 1);

        // ĐƯA VÀO ADAPTER
        if (rvCalendar != null) {
            CalendarAdapter adapter = new CalendarAdapter(daysList, statusMap);
            rvCalendar.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }
}