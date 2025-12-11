package com.example.learning_app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UserDatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "HocTiengAnh.db";
    public static final int DATABASE_VERSION = 1;
    public static final String TABLE_USERS = "users";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_FULL_NAME = "full_name";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_AGE = "age";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_WHY_LEARN = "why_learn";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_JOIN_DATE = "join_date";
    public static final String COLUMN_FRIENDS = "friends";
    public static final String COLUMN_ACHIEVEMENT = "achievement";
    public static final String COLUMN_STREAK = "streak";
    public static final String COLUMN_XP = "xp";
    public static final String COLUMN_AVATAR_PATH = "avatar_path";
    public static final String TABLE_FRIENDSHIPS = "friendships";
    public static final String COLUMN_F_ID = "id";
    public static final String COLUMN_USER_ID_1 = "user_id";
    public static final String COLUMN_USER_ID_2 = "friend_id";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_FULL_NAME + " TEXT, " +
                    COLUMN_USERNAME + " TEXT, " +
                    COLUMN_PASSWORD + " TEXT, " +
                    COLUMN_AGE + " INTEGER, " +
                    COLUMN_EMAIL + " TEXT, " +
                    COLUMN_WHY_LEARN + " TEXT, " +
                    COLUMN_STATUS + " TEXT, " +
                    COLUMN_JOIN_DATE + " TEXT, " +
                    COLUMN_FRIENDS + " INTEGER DEFAULT 0, " +
                    COLUMN_ACHIEVEMENT + " INTEGER DEFAULT 0, " +
                    COLUMN_STREAK + " INTEGER DEFAULT 0, " +
                    COLUMN_XP + " INTEGER DEFAULT 0," +
                    COLUMN_AVATAR_PATH + " TEXT" +
                    ");";


    public UserDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return dateFormat.format(new Date());
    }

    public boolean addUser(String fullName, String username, String password, int age, String email, String whyLearn, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_FULL_NAME, fullName);
        cv.put(COLUMN_USERNAME, username);
        cv.put(COLUMN_PASSWORD, password);
        cv.put(COLUMN_AGE, age);
        cv.put(COLUMN_EMAIL, email);
        cv.put(COLUMN_WHY_LEARN, whyLearn);
        cv.put(COLUMN_STATUS, status);
        cv.put(COLUMN_JOIN_DATE, getCurrentDate());
        long result = db.insert(TABLE_USERS, null, cv);
        return result != -1;
    }

    public boolean checkUserLogin(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {username, password};
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_ID}, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    public boolean checkUsernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_ID}, COLUMN_USERNAME + " = ?", new String[]{username}, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    public boolean checkEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_ID}, COLUMN_EMAIL + " = ?", new String[]{email}, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    public Cursor getUserDetails(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_USERS, null, COLUMN_USERNAME + " = ?", new String[]{username}, null, null, null);
    }

    // Hàm SỬA (Update) thông tin user
    // Chúng ta dùng oldUsername để tìm đúng user
    public boolean updateUser(String oldUsername, String newFullName, String newUsername, String newPassword, int newAge, String newEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_FULL_NAME, newFullName);
        cv.put(COLUMN_USERNAME, newUsername);
        cv.put(COLUMN_PASSWORD, newPassword); // Cần mã hóa trong thực tế
        cv.put(COLUMN_AGE, newAge);
        cv.put(COLUMN_EMAIL, newEmail);

        // Cập nhật hàng (row) có username = oldUsername
        int rowsAffected = db.update(TABLE_USERS, cv, COLUMN_USERNAME + " = ?", new String[]{oldUsername});
        return rowsAffected > 0;
    }

    // Hàm XÓA (Delete) user
    public boolean deleteUser(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_USERS, COLUMN_USERNAME + " = ?", new String[]{username});
        return rowsAffected > 0;
    }
    // Thêm hàm này vào cuối file DatabaseHelper.java
    public boolean updateAvatarPath(String username, String avatarPath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_AVATAR_PATH, avatarPath);

        int rowsAffected = db.update(TABLE_USERS, cv, COLUMN_USERNAME + " = ?", new String[]{username});
        return rowsAffected > 0;
    }
}