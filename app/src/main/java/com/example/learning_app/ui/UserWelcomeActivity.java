package com.example.learning_app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.learning_app.R;
import com.example.learning_app.viewmodel.UserViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserWelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        
        if (currentUser != null && currentUser.isEmailVerified()) {
            Intent intent = new Intent(UserWelcomeActivity.this, UserDashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }
        
        setContentView(R.layout.activity_user_welcome);

        Button btnGetStarted = findViewById(R.id.btnGetStarted);
        Button btnAlreadyHaveAccount = findViewById(R.id.btnAlreadyHaveAccount);

        btnGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserWelcomeActivity.this, UserLearningReasonActivity.class);
                startActivity(intent);
            }
        });
        btnAlreadyHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserWelcomeActivity.this, UserLoginActivity.class);
                startActivity(intent);
            }
        });
    }
}

