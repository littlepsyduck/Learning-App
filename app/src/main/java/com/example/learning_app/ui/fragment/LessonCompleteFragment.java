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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.learning_app.R;
import com.example.learning_app.entities.User;
import com.example.learning_app.viewmodel.ProgressViewModel;

public class LessonCompleteFragment extends DialogFragment {

    private ProgressViewModel viewModel;
    private int totalXP;
    private int accuracy;
    private OnClaimClickListener listener;
    private String lastLessonDateBeforeClaim;

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            totalXP = getArguments().getInt("totalXP", 0);
            accuracy = getArguments().getInt("accuracy", 0);
        }
        viewModel = new ViewModelProvider(requireActivity()).get(ProgressViewModel.class);
        
        viewModel.getCurrentUser().observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                if (user != null && lastLessonDateBeforeClaim == null) {
                    lastLessonDateBeforeClaim = user.getLastLessonDate();
                }
            }
        });
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
        return inflater.inflate(R.layout.layout_lesson_complete, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() == null) return;

        String timeSpent = getArguments().getString("timeSpent", "0:00");

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
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
            String today = sdf.format(new java.util.Date());
            
            boolean isFirstLessonToday = lastLessonDateBeforeClaim == null || !lastLessonDateBeforeClaim.equals(today);
            
            viewModel.claimLessonRewards(totalXP, accuracy);
            
            if (isFirstLessonToday) {
                // Dismiss fragment trước
                dismiss();
                
                // Sau đó check streak và show dialog nếu cần
                android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
                handler.postDelayed(() -> {
                    if (getActivity() != null && !getActivity().isFinishing()) {
                        Observer<User> userObserver = new Observer<User>() {
                            @Override
                            public void onChanged(User user) {
                                if (user != null) {
                                    viewModel.getCurrentUser().removeObserver(this);
                                    
                                    if (user.getStreak() > 0) {
                                        // Show streak dialog
                                        StreakDialogFragment streakDialog = StreakDialogFragment.newInstance(user.getStreak());
                                        streakDialog.setOnContinueClickListener(() -> {
                                            if (listener != null) {
                                                listener.onClaimClick();
                                            }
                                        });
                                        if (getActivity() != null && !getActivity().isFinishing()) {
                                            streakDialog.show(getActivity().getSupportFragmentManager(), "StreakDialogFragment");
                                        } else {
                                            // Activity đã finish, gọi listener trực tiếp
                                            if (listener != null) {
                                                listener.onClaimClick();
                                            }
                                        }
                                    } else {
                                        // Không có streak, finish activity ngay
                                        if (listener != null) {
                                            listener.onClaimClick();
                                        }
                                    }
                                } else {
                                    viewModel.getCurrentUser().removeObserver(this);
                                    // User null, finish activity
                                    if (listener != null) {
                                        listener.onClaimClick();
                                    }
                                }
                            }
                        };
                        viewModel.getCurrentUser().observe(getActivity(), userObserver);
                        
                        // Timeout sau 2 giây nếu không có response
                        handler.postDelayed(() -> {
                            viewModel.getCurrentUser().removeObserver(userObserver);
                            if (listener != null) {
                                listener.onClaimClick();
                            }
                        }, 2000);
                    } else {
                        // Activity đã finish, gọi listener trực tiếp
                        if (listener != null) {
                            listener.onClaimClick();
                        }
                    }
                }, 500);
            } else {
                // Không phải bài học đầu tiên, finish ngay
                dismiss();
                if (listener != null) {
                    listener.onClaimClick();
                }
            }
        });
    }

    public void setOnClaimClickListener(OnClaimClickListener listener) {
        this.listener = listener;
    }
}
