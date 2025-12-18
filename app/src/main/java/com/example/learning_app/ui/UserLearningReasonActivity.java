package com.example.learning_app.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.learning_app.R;

import java.util.ArrayList;
import java.util.List;

public class UserLearningReasonActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private Button btnContinue;
    private List<LinearLayout> listItems;
    private LinearLayout selectedItem = null;
    private ImageView ivBack;

    private int normalBackground;
    private int selectedBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_learning_reason);

        progressBar = findViewById(R.id.progressBar);
        btnContinue = findViewById(R.id.btnContinue);
        ivBack = findViewById(R.id.ivBack);

        normalBackground = R.drawable.list_item_background;
        selectedBackground = R.drawable.list_item_background_selected;

        progressBar.setProgress(0);

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        listItems = new ArrayList<>();
        listItems.add(findViewById(R.id.item_job));
        listItems.add(findViewById(R.id.item_culture));
        listItems.add(findViewById(R.id.item_brain));
        listItems.add(findViewById(R.id.item_family));
        listItems.add(findViewById(R.id.item_travel));
        listItems.add(findViewById(R.id.item_school));
        listItems.add(findViewById(R.id.item_other));

        View.OnClickListener itemClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleItemClick((LinearLayout) v);
            }
        };

        for (LinearLayout item : listItems) {
            item.setOnClickListener(itemClickListener);
        }

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserLearningReasonActivity.this, UserChoosePathActivity.class);
                String selectedReason = ((TextView) selectedItem.getChildAt(1)).getText().toString();
                intent.putExtra("WHY_LEARN", selectedReason);
                startActivity(intent);
            }
        });
    }

    private void handleItemClick(LinearLayout clickedItem) {
        if (selectedItem != null) {
            selectedItem.setBackgroundResource(normalBackground);
            ((TextView) selectedItem.getChildAt(1)).setTextColor(ContextCompat.getColor(this, R.color.duo_grey));
        }
        selectedItem = clickedItem;
        selectedItem.setBackgroundResource(selectedBackground);
        ((TextView) selectedItem.getChildAt(1)).setTextColor(ContextCompat.getColor(this, R.color.duo_green));
        activateContinueButton();
    }

    private void activateContinueButton() {
        btnContinue.setEnabled(true);
        btnContinue.setBackgroundResource(R.drawable.button_background_green);
        btnContinue.setTextColor(Color.WHITE);
    }
}



