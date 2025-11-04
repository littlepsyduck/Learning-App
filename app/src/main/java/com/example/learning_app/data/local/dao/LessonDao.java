package com.example.learning_app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.learning_app.data.local.entity.LessonEntity;

import java.util.List;

@Dao
public interface LessonDao {
    @Query("SELECT * FROM lessons WHERE skillId = :skillId ORDER BY level ASC")
    LiveData<List<LessonEntity>> bySkill(int skillId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<LessonEntity> list);

    @Query("SELECT COUNT(*) FROM lessons")
    int count();
}
