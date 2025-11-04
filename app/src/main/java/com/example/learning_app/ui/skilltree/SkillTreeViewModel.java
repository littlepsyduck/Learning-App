package com.example.learning_app.ui.skilltree;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.learning_app.data.local.entity.SkillEntity;
import com.example.learning_app.data.repository.SkillRepository;

import java.util.List;

public class SkillTreeViewModel extends AndroidViewModel {
    private final SkillRepository repo;
    public final LiveData<List<SkillEntity>> skills;

    public SkillTreeViewModel(@NonNull Application app) {
        super(app);
        repo = new SkillRepository(app);
        skills = repo.getAllSkills();
    }

    public void completeSkillAndUnlockNext(int skillId) {
        repo.markCompletedAndUnlockNext(skillId);
    }
}

