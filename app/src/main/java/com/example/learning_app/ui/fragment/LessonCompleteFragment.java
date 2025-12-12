package com.example.learning_app.ui.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.learning_app.R;
import com.example.learning_app.viewmodel.MainViewModel;

public class LessonCompleteFragment extends DialogFragment {

    private MainViewModel viewModel;
    private int totalXP;

    public static LessonCompleteFragment newInstance(int totalXP, int accuracy, String timeSpent) {
        LessonCompleteFragment fragment = new LessonCompleteFragment();
        Bundle args = new Bundle();
        args.putInt("totalXP", totalXP);
        args.putInt("accuracy", accuracy);
        args.putString("timeSpent", timeSpent);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            totalXP = getArguments().getInt("totalXP");
        }
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
            // Tùy chọn: làm cho nền trong suốt nếu layout của bạn có góc bo tròn
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_lesson_complete, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() == null) return;

        int accuracy = getArguments().getInt("accuracy");
        String timeSpent = getArguments().getString("timeSpent");

        ImageView ivCharacterFinish = view.findViewById(R.id.ivCharacterFinish);
        TextView tvTotalXP = view.findViewById(R.id.tvTotalXP);
        TextView tvAccuracy = view.findViewById(R.id.tvAccuracy);
        TextView tvTime = view.findViewById(R.id.tvTime);
        Button btnClaimXP = view.findViewById(R.id.btnClaimXP);

        int characterFinishResId = getResources().getIdentifier("character_duo_1", "drawable", requireContext().getPackageName());
        if (characterFinishResId != 0) {
            ivCharacterFinish.setImageResource(characterFinishResId);
        }

        tvTotalXP.setText(String.valueOf(totalXP));
        tvAccuracy.setText(accuracy + "%");
        tvTime.setText(timeSpent);

        btnClaimXP.setOnClickListener(v -> {
            viewModel.claimLessonRewards(totalXP);
            viewModel.decrementHeart();
            dismiss();
        });
    }
}