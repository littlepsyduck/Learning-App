package com.example.learning_app.data.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.learning_app.data.local.dao.LessonDao;
import com.example.learning_app.data.local.dao.SkillDao;
import com.example.learning_app.data.local.entity.LessonEntity;
import com.example.learning_app.data.local.entity.SkillEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = { SkillEntity.class, LessonEntity.class }, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;
    public abstract SkillDao skillDao();
    public abstract LessonDao lessonDao();

    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(4);

    public static AppDatabase getInstance(Context ctx) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(ctx.getApplicationContext(),
                                    AppDatabase.class, "learning_app.db")
                            .addCallback(SEED)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final Callback SEED = new Callback() {
        @Override public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            databaseWriteExecutor.execute(() -> {
                AppDatabase d = INSTANCE;
                SkillDao skillDao = d.skillDao();
                LessonDao lessonDao = d.lessonDao();

                // SECTION 1, UNIT 1: "Use basic phrases" → unlocked = true
                SkillEntity s1 = new SkillEntity("Use basic phrases", 1, 1, true, false, 0);
                int s1Id = (int) skillDao.insert(s1);

                // SECTION 1, UNIT 2: "Greetings" → locked
                SkillEntity s2 = new SkillEntity("Greetings", 1, 2, false, false, 1);
                int s2Id = (int) skillDao.insert(s2);

                List<LessonEntity> l1 = new ArrayList<>();
                l1.add(new LessonEntity(s1Id, "Basics 1", 1, false));
                l1.add(new LessonEntity(s1Id, "Basics 2", 2, false));
                l1.add(new LessonEntity(s1Id, "Basics 3", 3, false));
                lessonDao.insertAll(l1);

                List<LessonEntity> l2 = new ArrayList<>();
                l2.add(new LessonEntity(s2Id, "Greet 1", 1, false));
                l2.add(new LessonEntity(s2Id, "Greet 2", 2, false));
                lessonDao.insertAll(l2);
            });
        }
    };
}
