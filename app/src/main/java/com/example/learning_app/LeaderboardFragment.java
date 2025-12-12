package com.example.learning_app;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class LeaderboardFragment extends Fragment {

    private RecyclerView rvLeaderboard;
    private LeaderboardAdapter adapter;
    private List<UserModel> userList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private TextView tvTimeRemaining, tvLeagueTitle;
    private ImageView ivBronze, ivSilver, ivGold, ivPlatinum;
    private android.widget.LinearLayout trophyContainer;

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

        // Setup RecyclerView
        rvLeaderboard.setLayoutManager(new LinearLayoutManager(getContext()));
        userList = new ArrayList<>();
        adapter = new LeaderboardAdapter(getContext(), userList);
        rvLeaderboard.setAdapter(adapter);

        // Setup Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Calculate Time Remaining (until Sunday)
        updateTimeRemaining();

        // Load Data
        loadCurrentUserAndLeague();
        loadLeaderboardData();
    }

    private void updateTimeRemaining() {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        
        // Calendar.SUNDAY = 1, MONDAY = 2, ... SATURDAY = 7
        // We want days remaining until the END of the week (assuming Sunday resets)
        // If today is Thursday (5), remaining = 3 (Fri, Sat, Sun).
        // Formula: 8 - dayOfWeek if we count today as passed, or include today? 
        // Let's use standard logic: Days until next Sunday.
        
        int daysRemaining = Calendar.SATURDAY - dayOfWeek + 1; // +1 to include Sunday as end day

        // Or if we want exact days until Sunday (exclusive):
        // int days = (Calendar.SUNDAY - dayOfWeek + 7) % 7; 
        
        // Let's stick to a simple countdown.
        // If today is Thursday (5), days until Sunday (1) is 3 days.
        int daysUntilSunday = (Calendar.SUNDAY - dayOfWeek + 7) % 7;
        if (dayOfWeek == Calendar.SUNDAY) {
             daysUntilSunday = 0; // Ends today
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

    private void loadCurrentUserAndLeague() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("users").document(currentUser.getUid()).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    UserModel user = documentSnapshot.toObject(UserModel.class);
                    if (user != null) {
                        updateLeagueUI(user.getXp());
                    }
                }
            });
    }

    private void updateLeagueUI(int xp) {
        // Reset all icons to default size
        float defaultScale = 1.0f;
        float enlargedScale = 1.3f; // Enlarge factor

        ivBronze.setScaleX(defaultScale); ivBronze.setScaleY(defaultScale);
        ivSilver.setScaleX(defaultScale); ivSilver.setScaleY(defaultScale);
        ivGold.setScaleX(defaultScale);   ivGold.setScaleY(defaultScale);
        ivPlatinum.setScaleX(defaultScale); ivPlatinum.setScaleY(defaultScale);
        
        String leagueName = "Bronze";
        float translationX = 0f;
        
        // Calculation for centering:
        // Item width (60dp) + margin (16dp) = 76dp.
        // Container mid point relative to start (4 items) = (4 * 76) / 2 = 152dp.
        // Item centers: 38 (B), 114 (S), 190 (G), 266 (P).
        // Shift needed: MidPoint - ItemCenter.
        // Note: Positive translation shifts right.
        
        float density = getResources().getDisplayMetrics().density;
        
        if (xp < 200) {
            // Bronze
            leagueName = "Bronze";
            ivBronze.setScaleX(enlargedScale);
            ivBronze.setScaleY(enlargedScale);
            translationX = (152 - 38) * density; // +114dp
        } else if (xp < 500) {
            // Silver
            leagueName = "Silver";
            ivSilver.setScaleX(enlargedScale);
            ivSilver.setScaleY(enlargedScale);
            translationX = (152 - 114) * density; // +38dp
        } else if (xp < 1000) {
            // Gold
            leagueName = "Gold";
            ivGold.setScaleX(enlargedScale);
            ivGold.setScaleY(enlargedScale);
            translationX = (152 - 190) * density; // -38dp
        } else {
            // Platinum
            leagueName = "Platinum";
            ivPlatinum.setScaleX(enlargedScale);
            ivPlatinum.setScaleY(enlargedScale);
            translationX = (152 - 266) * density; // -114dp
        }

        tvLeagueTitle.setText(leagueName + " League");
        
        // Apply translation to container
        trophyContainer.setTranslationX(translationX);
    }

    private void loadLeaderboardData() {
        // Fetch top 50 users by XP, Descending
        db.collection("users")
                .orderBy("xp", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        UserModel user = document.toObject(UserModel.class);
                        if (user != null) {
                            userList.add(user);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Lỗi tải bảng xếp hạng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}