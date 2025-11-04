package com.example.learning_app.ui.skilltree;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.learning_app.R;
import com.example.learning_app.data.local.entity.SkillEntity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class SkillInfoSheet extends BottomSheetDialogFragment {

    SkillEntity skill;
    public SkillInfoSheet(SkillEntity s) { skill = s; }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sheet_skill_info, container, false);

        TextView tvName = v.findViewById(R.id.tvName);
        Button btnStart = v.findViewById(R.id.btnStart);

        tvName.setText(skill.title);

        btnStart.setOnClickListener(view -> {
            // TODO: start lesson
            dismiss();
        });
        return v;
    }
}
