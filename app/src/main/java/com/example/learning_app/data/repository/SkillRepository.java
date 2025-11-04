package com.example.learning_app.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.learning_app.data.local.AppDatabase;
import com.example.learning_app.data.local.dao.SkillDao;
import com.example.learning_app.data.local.entity.SkillEntity;

import java.util.List;

public class SkillRepository {
    private final SkillDao skillDao;

    public SkillRepository(Application app) {
        this.skillDao = AppDatabase.getInstance(app).skillDao();
    }

    public LiveData<List<SkillEntity>> getAllSkills() {
        return skillDao.getAll();
    }

    public void markCompletedAndUnlockNext(int completedSkillId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            SkillEntity done = skillDao.getSkillSync(completedSkillId);
            if (done == null) return;
            skillDao.setCompleted(done.id, true);

            SkillEntity next = skillDao.getNextSkillSync(done.positionIndex);
            if (next != null && !next.unlocked) {
                skillDao.setUnlocked(next.id, true);
            }
        });
    }

    public void setUnlocked(int id, boolean unlocked) {
        AppDatabase.databaseWriteExecutor.execute(() -> skillDao.setUnlocked(id, unlocked));
    }
}