package com.example.learning_app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.learning_app.R;

public class UserAllDoneActivity extends AppCompatActivity {

    private Button btnLogin;
    private ImageView ivBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_all_done);

        btnLogin = findViewById(R.id.btnLogin);
        ivBack = findViewById(R.id.ivBack);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserAllDoneActivity.this, UserLoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserAllDoneActivity.this, UserRegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}



