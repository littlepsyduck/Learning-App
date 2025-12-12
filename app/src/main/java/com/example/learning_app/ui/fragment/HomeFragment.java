package com.example.learning_app.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import com.example.learning_app.R;
import com.example.learning_app.ui.MainActivity;

public class HomeFragment extends Fragment {
    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Button btnTest = view.findViewById(R.id.btnTestComplete);
        btnTest.setOnClickListener(v -> {
            // Hiển thị màn hình hoàn thành bài học (toàn màn hình)
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).showLessonCompleteScreen(15, 95, "0:30");
            }
        });

        return view;
    }
}