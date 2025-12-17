package com.example.learning_app.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.learning_app.entities.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UserRepository {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private MutableLiveData<User> currentUserLiveData;
    private MutableLiveData<Boolean> loginResult;
    private MutableLiveData<Boolean> registerResult;
    private MutableLiveData<String> errorMessage;

    public UserRepository(Application application) {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserLiveData = new MutableLiveData<>();
        loginResult = new MutableLiveData<>();
        registerResult = new MutableLiveData<>();
        errorMessage = new MutableLiveData<>();
    }

    public LiveData<User> getCurrentUser() {
        return currentUserLiveData;
    }

    public LiveData<Boolean> getLoginResult() {
        return loginResult;
    }

    public LiveData<Boolean> getRegisterResult() {
        return registerResult;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public FirebaseUser getCurrentFirebaseUser() {
        return mAuth.getCurrentUser();
    }

    public void login(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null && firebaseUser.isEmailVerified()) {
                                updateLastDate(firebaseUser.getUid());
                                loadUserProfile(firebaseUser.getUid());
                                loginResult.setValue(true);
                            } else {
                                if (firebaseUser != null) {
                                    firebaseUser.sendEmailVerification();
                                }
                                mAuth.signOut();
                                errorMessage.setValue("Vui lòng kiểm tra Email để xác thực tài khoản trước!");
                                loginResult.setValue(false);
                            }
                        } else {
                            errorMessage.setValue("Sai email hoặc mật khẩu!");
                            loginResult.setValue(false);
                        }
                    }
                });
    }

    public void register(String email, String password, String fullName, String username, int age,
                        String whyLearn, String status) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                firebaseUser.sendEmailVerification();
                                saveUserToFirestore(firebaseUser.getUid(), fullName, username, email, password, age, whyLearn, status);
                                registerResult.setValue(true);
                            }
                        } else {
                            errorMessage.setValue(task.getException() != null ? task.getException().getMessage() : "Lỗi đăng ký");
                            registerResult.setValue(false);
                        }
                    }
                });
    }

    private void saveUserToFirestore(String uid, String fullName, String username, String email,
                                     String password, int age, String whyLearn, String status) {
        String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        User newUser = new User(uid, fullName, username, email, password, age, whyLearn, status, currentDate, currentDate);

        db.collection("users").document(uid)
                .set(newUser)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        if (!task.isSuccessful()) {
                            errorMessage.setValue("Failed to save data: " + (task.getException() != null ? task.getException().getMessage() : ""));
                        }
                    }
                });
    }

    public void loadUserProfile(String uid) {
        db.collection("users").document(uid).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                User user = document.toObject(User.class);
                                currentUserLiveData.setValue(user);
                            }
                        }
                    }
                });
    }

    private void updateLastDate(String uid) {
        String today = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        db.collection("users").document(uid).update("lastDate", today);
    }

    public void updateUserProfile(String uid, String fullName, String username, int age, String avatarUrl, String password) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", fullName);
        updates.put("age", age);
        updates.put("username", username);
        if (avatarUrl != null) {
            updates.put("avatarUrl", avatarUrl);
        }
        if (password != null && !password.isEmpty()) {
            updates.put("password", password);
        }

        db.collection("users").document(uid).update(updates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        if (task.isSuccessful()) {
                            loadUserProfile(uid);
                        } else {
                            errorMessage.setValue("Failed to save: " + (task.getException() != null ? task.getException().getMessage() : ""));
                        }
                    }
                });
    }

    public void deleteUserAccount(String uid) {
        db.collection("users").document(uid).delete();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.delete();
        }
    }

    public void logout() {
        mAuth.signOut();
        currentUserLiveData.setValue(null);
    }

    public void sendPasswordResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email);
    }

    public void claimLessonRewards(String uid, int xpGained) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat sdfCalendar = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(new Date());
        String todayCalendar = sdfCalendar.format(new Date());

        db.collection("users").document(uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot doc = task.getResult();
                        if (doc.exists()) {
                            User user = doc.toObject(User.class);
                            if (user != null) {
                                Map<String, Object> updates = new HashMap<>();
                                updates.put("xp", FieldValue.increment(xpGained));

                                boolean isFirstLessonToday = user.getLastLessonDate() == null || !user.getLastLessonDate().equals(today);
                                
                                if (isFirstLessonToday) {
                                    updates.put("streak", FieldValue.increment(1));
                                    updates.put("lastLessonDate", today);
                                    
                                    List<String> studyDates = (List<String>) doc.get("studyDates");
                                    if (studyDates == null) {
                                        studyDates = new ArrayList<>();
                                    }
                                    if (!studyDates.contains(todayCalendar)) {
                                        studyDates.add(todayCalendar);
                                        updates.put("studyDates", studyDates);
                                    }
                                }

                                db.collection("users").document(uid).update(updates)
                                        .addOnCompleteListener(updateTask -> {
                                            if (updateTask.isSuccessful()) {
                                                loadUserProfile(uid);
                                            }
                                        });
                            }
                        }
                    }
                });
    }
    

    public void decrementHeart(String uid) {
        db.collection("users").document(uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot doc = task.getResult();
                        if (doc.exists()) {
                            User user = doc.toObject(User.class);
                            if (user != null && user.getHearts() > 0) {
                                Map<String, Object> updates = new HashMap<>();
                                updates.put("hearts", FieldValue.increment(-1));

                                db.collection("users").document(uid).update(updates)
                                        .addOnCompleteListener(updateTask -> {
                                            if (updateTask.isSuccessful()) {
                                                loadUserProfile(uid);
                                            }
                                        });
                            }
                        }
                    }
                });
    }

    public void addFreeze(String uid) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("freeze", FieldValue.increment(1));

        db.collection("users").document(uid).update(updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        loadUserProfile(uid);
                    }
                });
    }

    public void checkStreakStatus(String uid) {
        db.collection("users").document(uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot doc = task.getResult();
                        if (doc.exists()) {
                            User user = doc.toObject(User.class);
                            if (user != null && user.getLastLessonDate() != null && !user.getLastLessonDate().isEmpty()) {
                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                String today = sdf.format(new Date());
                                String lastDate = user.getLastLessonDate();

                                try {
                                    Date lastLessonDate = sdf.parse(lastDate);
                                    Date todayDate = sdf.parse(today);
                                    if (lastLessonDate != null && todayDate != null) {
                                        long diffInMillis = todayDate.getTime() - lastLessonDate.getTime();
                                        long diffInDays = diffInMillis / (1000 * 60 * 60 * 24);

                                        if (diffInDays > 1) {
                                            if (user.getFreeze() > 0) {
                                                Map<String, Object> updates = new HashMap<>();
                                                updates.put("freeze", FieldValue.increment(-1));
                                                db.collection("users").document(uid).update(updates);
                                            } else {
                                                Map<String, Object> updates = new HashMap<>();
                                                updates.put("streak", 0);
                                                db.collection("users").document(uid).update(updates);
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });
    }
}

