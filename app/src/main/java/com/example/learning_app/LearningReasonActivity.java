package com.example.learning_app; // (Hãy chắc chắn đây là package của bạn)

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

import java.util.ArrayList;
import java.util.List;

public class LearningReasonActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private Button btnContinue;
    private List<LinearLayout> listItems;
    private LinearLayout selectedItem = null;
    private ImageView ivBack; // Khai báo biến cho nút back

    private int normalBackground;
    private int selectedBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learning_reason);

        progressBar = findViewById(R.id.progressBar);
        btnContinue = findViewById(R.id.btnContinue);
        ivBack = findViewById(R.id.ivBack); // Lấy ID của nút back

        normalBackground = R.drawable.list_item_background;
        selectedBackground = R.drawable.list_item_background_selected;

        // --- SỬA LỖI 2: ĐẶT TIẾN TRÌNH VỀ 0 ---
        // Đặt tiến trình ban đầu là 0
        progressBar.setProgress(0);

        // --- SỬA LỖI 1: THÊM SỰ KIỆN CLICK CHO NÚT BACK ---
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Phương thức finish() sẽ đóng Activity hiện tại
                // và tự động quay lại Activity trước đó (MainActivity)
                finish();
            }
        });

        // (Phần code còn lại giữ nguyên)
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

        // Xử lý nút Continue
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // SỬA Ở ĐÂY:
                // Chuyển sang màn hình chọn "Choose your path"
                Intent intent = new Intent(LearningReasonActivity.this, ChoosePathActivity.class);
                String selectedReason = ((TextView) selectedItem.getChildAt(1)).getText().toString();
                intent.putExtra("WHY_LEARN", selectedReason);
                startActivity(intent);
            }
        });
    }

    private void handleItemClick(LinearLayout clickedItem) {
        // (Giữ nguyên code)
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
        // (Giữ nguyên code)
        btnContinue.setEnabled(true);
        btnContinue.setBackgroundResource(R.drawable.button_background_green);
        btnContinue.setTextColor(Color.WHITE);
    }
}