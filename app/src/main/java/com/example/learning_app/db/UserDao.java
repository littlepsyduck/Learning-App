package com.example.learning_app.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface UserDao {
    @Insert
    void insert(UserEntity user);

    @Update
    void update(UserEntity user);

    @Query("SELECT * FROM user_stats LIMIT 1")
    UserEntity getUser();

    @Query("SELECT * FROM user_stats LIMIT 1")
    LiveData<UserEntity> getUserStats();

    @Query("SELECT * FROM user_stats LIMIT 1")
    UserEntity getUserStatsSync();
}