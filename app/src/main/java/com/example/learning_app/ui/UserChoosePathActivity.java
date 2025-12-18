package com.example.learning_app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.learning_app.R;

public class UserChoosePathActivity extends AppCompatActivity {

    private ImageView ivBack;
    private LinearLayout cardNewLearner;
    private LinearLayout cardExistingLearner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_choose_path);

        ivBack = findViewById(R.id.ivBack);
        cardNewLearner = findViewById(R.id.card_new_learner);
        cardExistingLearner = findViewById(R.id.card_existing_learner);

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        String whyLearn = getIntent().getStringExtra("WHY_LEARN");
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserChoosePathActivity.this, UserRegisterActivity.class);
                intent.putExtra("WHY_LEARN", whyLearn);
                if (v.getId() == R.id.card_new_learner) {
                    intent.putExtra("STATUS", "newbie");
                } else if (v.getId() == R.id.card_existing_learner) {
                    intent.putExtra("STATUS", "alreadyknow");
                }
                startActivity(intent);
            }
        };

        cardNewLearner.setOnClickListener(listener);
        cardExistingLearner.setOnClickListener(listener);
    }
}



