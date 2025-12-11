package com.example.learning_app.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.learning_app.R;

/**
 * Fragment hiển thị màn hình hoàn thành bài học
 */
public class LessonCompleteFragment extends Fragment {
    
    private int totalXP;
    private int accuracy;
    private String timeSpent;
    private OnClaimClickListener listener;
    
    public interface OnClaimClickListener {
        void onClaimClick();
    }
    
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            totalXP = getArguments().getInt("totalXP", 0);
            accuracy = getArguments().getInt("accuracy", 0);
            timeSpent = getArguments().getString("timeSpent", "0:00");
        }
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_lesson_complete, container, false);
        
        // Set character image
        ImageView ivCharacterFinish = view.findViewById(R.id.ivCharacterFinish);
        int characterFinishResId = getResources().getIdentifier("character_duo_1", "drawable", requireContext().getPackageName());
        if (characterFinishResId != 0) {
            ivCharacterFinish.setImageResource(characterFinishResId);
        }
        
        // Set stats
        TextView tvTotalXP = view.findViewById(R.id.tvTotalXP);
        TextView tvAccuracy = view.findViewById(R.id.tvAccuracy);
        TextView tvTime = view.findViewById(R.id.tvTime);
        Button btnClaimXP = view.findViewById(R.id.btnClaimXP);
        
        tvTotalXP.setText(String.valueOf(totalXP));
        tvAccuracy.setText(accuracy + "%");
        tvTime.setText(timeSpent);
        
        btnClaimXP.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClaimClick();
            }
        });
        
        return view;
    }
    
    public void setOnClaimClickListener(OnClaimClickListener listener) {
        this.listener = listener;
    }
}


