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

import com.example.learning_app.R;

public class StreakDialogFragment extends DialogFragment {

    private int streak;
    private OnContinueClickListener listener;

    public interface OnContinueClickListener {
        void onContinueClick();
    }

    public static StreakDialogFragment newInstance(int streak) {
        StreakDialogFragment fragment = new StreakDialogFragment();
        Bundle args = new Bundle();
        args.putInt("streak", streak);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            streak = getArguments().getInt("streak", 0);
        }
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_streak_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView ivFire = view.findViewById(R.id.ivFire);
        TextView tvStreakNumber = view.findViewById(R.id.tvStreakNumber);
        TextView tvStreakMessage = view.findViewById(R.id.tvStreakMessage);
        Button btnContinue = view.findViewById(R.id.btnContinue);

        tvStreakNumber.setText(String.valueOf(streak));
        
        if (streak == 1) {
            tvStreakMessage.setText("Bạn đã bắt đầu chuỗi ngày học!");
        } else {
            tvStreakMessage.setText("Bạn đã học " + streak + " ngày liên tiếp!");
        }

        btnContinue.setOnClickListener(v -> {
            if (listener != null) {
                listener.onContinueClick();
            }
            dismiss();
        });
    }

    public void setOnContinueClickListener(OnContinueClickListener listener) {
        this.listener = listener;
    }
}



