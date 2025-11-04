package com.example.learning_app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;


import com.example.learning_app.data.local.entity.SkillEntity;

import java.util.List;

@Dao
public interface SkillDao {
    @Query("SELECT * FROM skills ORDER BY positionIndex ASC")
    LiveData<List<SkillEntity>> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(SkillEntity e);

    @Update
    void update(SkillEntity e);

    @Query("UPDATE skills SET unlocked = :unlocked WHERE id = :id")
    void setUnlocked(int id, boolean unlocked);

    @Query("UPDATE skills SET completed = :completed WHERE id = :id")
    void setCompleted(int id, boolean completed);

    @Query("SELECT COUNT(*) FROM skills")
    int count();

    @Query("SELECT * FROM skills WHERE id = :id LIMIT 1")
    SkillEntity getSkillSync(int id);

    @Query("SELECT * FROM skills WHERE positionIndex > :pos ORDER BY positionIndex ASC LIMIT 1")
    SkillEntity getNextSkillSync(int pos);

}
