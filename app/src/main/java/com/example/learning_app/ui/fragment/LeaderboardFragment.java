package com.example.learning_app.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learning_app.R;
import com.example.learning_app.entities.User;
import com.example.learning_app.ui.adapter.LeaderboardAdapter;
import com.example.learning_app.viewmodel.LeaderboardViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class LeaderboardFragment extends Fragment {

    private RecyclerView rvLeaderboard;
    private LeaderboardAdapter adapter;
    private List<User> userList;
    private LeaderboardViewModel leaderboardViewModel;
    private TextView tvTimeRemaining, tvLeagueTitle;
    private ImageView ivBronze, ivSilver, ivGold, ivPlatinum;
    private LinearLayout trophyContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_leaderboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvLeaderboard = view.findViewById(R.id.rvLeaderboard);
        tvTimeRemaining = view.findViewById(R.id.tvTimeRemaining);
        tvLeagueTitle = view.findViewById(R.id.tvLeagueTitle);
        trophyContainer = view.findViewById(R.id.trophyContainer);

        ivBronze = view.findViewById(R.id.ivBronze);
        ivSilver = view.findViewById(R.id.ivSilver);
        ivGold = view.findViewById(R.id.ivGold);
        ivPlatinum = view.findViewById(R.id.ivPlatinum);

        rvLeaderboard.setLayoutManager(new LinearLayoutManager(getContext()));
        userList = new ArrayList<>();
        adapter = new LeaderboardAdapter(getContext(), userList);
        rvLeaderboard.setAdapter(adapter);

        leaderboardViewModel = new ViewModelProvider(this).get(LeaderboardViewModel.class);

        updateTimeRemaining();
        setupObservers();

        leaderboardViewModel.loadCurrentUser();
        leaderboardViewModel.loadLeaderboard();
    }

    private void setupObservers() {
        leaderboardViewModel.getLeaderboard().observe(getViewLifecycleOwner(), new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                if (users != null) {
                    userList.clear();
                    userList.addAll(users);
                    adapter.notifyDataSetChanged();
                }
            }
        });

        leaderboardViewModel.getCurrentUser().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(User user) {
                if (user != null) {
                    updateLeagueUI(user.getXp());
                }
            }
        });
    }

    private void updateTimeRemaining() {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        int daysUntilSunday = (Calendar.SUNDAY - dayOfWeek + 7) % 7;
        if (dayOfWeek == Calendar.SUNDAY) {
            daysUntilSunday = 0;
        }

        String timeText;
        if (daysUntilSunday == 0) {
            timeText = "ENDS TODAY";
        } else if (daysUntilSunday == 1) {
            timeText = "1 DAY";
        } else {
            timeText = daysUntilSunday + " DAYS";
        }

        tvTimeRemaining.setText(timeText);
    }

    private void updateLeagueUI(int xp) {
        float defaultScale = 1.0f;
        float enlargedScale = 1.3f;

        ivBronze.setScaleX(defaultScale); ivBronze.setScaleY(defaultScale);
        ivSilver.setScaleX(defaultScale); ivSilver.setScaleY(defaultScale);
        ivGold.setScaleX(defaultScale);   ivGold.setScaleY(defaultScale);
        ivPlatinum.setScaleX(defaultScale); ivPlatinum.setScaleY(defaultScale);

        String leagueName = "Bronze";
        float translationX = 0f;

        float density = getResources().getDisplayMetrics().density;

        if (xp < 200) {
            leagueName = "Bronze";
            ivBronze.setScaleX(enlargedScale);
            ivBronze.setScaleY(enlargedScale);
            translationX = (152 - 38) * density;
        } else if (xp < 500) {
            leagueName = "Silver";
            ivSilver.setScaleX(enlargedScale);
            ivSilver.setScaleY(enlargedScale);
            translationX = (152 - 114) * density;
        } else if (xp < 1000) {
            leagueName = "Gold";
            ivGold.setScaleX(enlargedScale);
            ivGold.setScaleY(enlargedScale);
            translationX = (152 - 190) * density;
        } else {
            leagueName = "Platinum";
            ivPlatinum.setScaleX(enlargedScale);
            ivPlatinum.setScaleY(enlargedScale);
            translationX = (152 - 266) * density;
        }

        tvLeagueTitle.setText(leagueName + " League");
        trophyContainer.setTranslationX(translationX);
    }
}

