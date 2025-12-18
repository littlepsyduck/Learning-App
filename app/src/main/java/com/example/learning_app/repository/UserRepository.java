package com.example.learning_app.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.learning_app.entities.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
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
import java.util.Calendar;

public class UserRepository {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private MutableLiveData<User> currentUserLiveData;
    private MutableLiveData<Boolean> loginResult;
    private MutableLiveData<Boolean> registerResult;
    private MutableLiveData<Boolean> passwordUpdateResult;
    private MutableLiveData<String> errorMessage;

    public UserRepository(Application application) {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserLiveData = new MutableLiveData<>();
        loginResult = new MutableLiveData<>();
        registerResult = new MutableLiveData<>();
        passwordUpdateResult = new MutableLiveData<>();
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

    public LiveData<Boolean> getPasswordUpdateResult() {
        return passwordUpdateResult;
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
                                saveUserToFirestore(firebaseUser.getUid(), fullName, username, email, age, whyLearn,
                                        status);
                                registerResult.setValue(true);
                            }
                        } else {
                            errorMessage.setValue(
                                    task.getException() != null ? task.getException().getMessage() : "Lỗi đăng ký");
                            registerResult.setValue(false);
                        }
                    }
                });
    }

    private void saveUserToFirestore(String uid, String fullName, String username, String email,
            int age, String whyLearn, String status) {
        String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        User newUser = new User(uid, fullName, username, email, age, whyLearn, status, currentDate, currentDate);

        db.collection("users").document(uid)
                .set(newUser)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        if (!task.isSuccessful()) {
                            errorMessage.setValue("Failed to save data: "
                                    + (task.getException() != null ? task.getException().getMessage() : ""));
                        }
                    }
                });
    }

    public void loadUserProfile(String uid) {
        db.collection("users").document(uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            User user = document.toObject(User.class);

                            String today = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
                            String resetDate = user.getDailyChallengeResetDate();

                            // Check for new month to reset monthly freezes
                            SimpleDateFormat monthFormat = new SimpleDateFormat("MM/yyyy", Locale.getDefault());
                            String currentMonth = monthFormat.format(new Date());
                            String lastLessonDate = user.getLastLessonDate();
                            boolean isNewMonth = false;

                            if (lastLessonDate != null && !lastLessonDate.isEmpty()) {
                                try {
                                    Date lastDateObj = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                            .parse(lastLessonDate);
                                    if (lastDateObj != null) {
                                        String lastMonth = monthFormat.format(lastDateObj);
                                        if (!currentMonth.equals(lastMonth)) {
                                            isNewMonth = true;
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            Map<String, Object> updates = new HashMap<>();
                            boolean needsUpdate = false;

                            if (resetDate == null || !today.equals(resetDate)) {
                                updates.put("hearts", 5);
                                updates.put("perfectLessonCount", 0);
                                updates.put("dailyChallengeResetDate", today);
                                needsUpdate = true;
                            }

                            if (isNewMonth) {
                                updates.put("monthlyFreezesUsed", 0);
                                needsUpdate = true;
                            }

                            if (needsUpdate) {
                                db.collection("users").document(uid).update(updates)
                                        .addOnCompleteListener(updateTask -> {
                                            if (updateTask.isSuccessful()) {
                                                loadUserProfile(uid, null);
                                            } else {
                                                currentUserLiveData.postValue(user);
                                            }
                                        });
                            } else {
                                currentUserLiveData.postValue(user);
                            }
                        }
                    }
                });
    }

    public void loadUserProfile(String uid, Runnable onFinished) {
        db.collection("users").document(uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            User user = document.toObject(User.class);
                            currentUserLiveData.postValue(user);
                        }
                    }
                    if (onFinished != null) {
                        onFinished.run();
                    }
                });
    }

    public void updateUserProfile(String uid, String fullName, String username, int age, String avatarUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", fullName);
        updates.put("age", age);
        updates.put("username", username);
        if (avatarUrl != null) {
            updates.put("avatarUrl", avatarUrl);
        }

        db.collection("users").document(uid).update(updates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        if (task.isSuccessful()) {
                            loadUserProfile(uid);
                        } else {
                            errorMessage.setValue("Failed to save: "
                                    + (task.getException() != null ? task.getException().getMessage() : ""));
                        }
                    }
                });
    }

    public void updatePassword(String email, String currentPassword, String newPassword) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && email != null) {
            // Create credential with current password for re-authentication
            AuthCredential credential = EmailAuthProvider.getCredential(email, currentPassword);

            // Re-authenticate user first
            user.reauthenticate(credential)
                    .addOnCompleteListener(reAuthTask -> {
                        if (reAuthTask.isSuccessful()) {
                            // Re-authentication successful, now update password
                            user.updatePassword(newPassword)
                                    .addOnCompleteListener(updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            errorMessage.setValue(null);
                                            passwordUpdateResult.setValue(true);
                                        } else {
                                            errorMessage.setValue("Failed to update password: " +
                                                    (updateTask.getException() != null
                                                            ? updateTask.getException().getMessage()
                                                            : "Unknown error"));
                                            passwordUpdateResult.setValue(false);
                                        }
                                    });
                        } else {
                            // Re-authentication failed
                            errorMessage.setValue("Mật khẩu hiện tại không đúng. Vui lòng thử lại!");
                            passwordUpdateResult.setValue(false);
                        }
                    });
        } else {
            errorMessage.setValue("No user logged in");
            passwordUpdateResult.setValue(false);
        }
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

    public void claimLessonRewards(String uid, int xpGained, int accuracy, Runnable onFinished) {
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

                                int currentHearts = user.getHearts();
                                if (currentHearts == 0 && !doc.contains("hearts")) {
                                    currentHearts = 5;
                                }

                                if (currentHearts > 0) {
                                    updates.put("hearts", currentHearts - 1);
                                }

                                String lastLessonDate = user.getLastLessonDate();
                                boolean isFirstLessonToday = lastLessonDate == null || !lastLessonDate.equals(today);

                                if (isFirstLessonToday) {
                                    updates.put("lastLessonDate", today);

                                    // Streak Logic Update
                                    if (lastLessonDate == null || lastLessonDate.isEmpty()) {
                                        // First lesson ever
                                        updates.put("streak", 1);
                                    } else {
                                        try {
                                            Date lastDate = sdf.parse(lastLessonDate);
                                            Date todayDate = sdf.parse(today);
                                            long diffInMillis = todayDate.getTime() - lastDate.getTime();
                                            long diffInDays = diffInMillis / (1000 * 60 * 60 * 24);

                                            if (diffInDays == 1) {
                                                // Consecutive day
                                                updates.put("streak", FieldValue.increment(1));
                                            } else if (diffInDays == 2) {
                                                // Missed one day - Check freeze
                                                int freezesUsed = user.getMonthlyFreezesUsed();
                                                int currentFreezes = user.getFreeze();

                                                if (currentFreezes > 0 && freezesUsed < 2) {
                                                    // Use freeze
                                                    updates.put("freeze", FieldValue.increment(-1));
                                                    updates.put("monthlyFreezesUsed", FieldValue.increment(1));
                                                    updates.put("streak", FieldValue.increment(1)); // Continue streak

                                                    // Add missed day to frozenDates
                                                    Calendar cal = Calendar.getInstance();
                                                    cal.setTime(todayDate);
                                                    cal.add(Calendar.DAY_OF_MONTH, -1); // Yesterday was missed
                                                    String missedDateCalendar = sdfCalendar.format(cal.getTime());

                                                    List<String> frozenDates = user.getFrozenDates();
                                                    if (frozenDates == null)
                                                        frozenDates = new ArrayList<>();
                                                    if (!frozenDates.contains(missedDateCalendar)) {
                                                        updates.put("frozenDates",
                                                                FieldValue.arrayUnion(missedDateCalendar));
                                                    }
                                                } else {
                                                    // No freezes left or limit reached - Reset streak
                                                    updates.put("streak", 1);
                                                }
                                            } else {
                                                // Missed more than 1 day - Reset streak
                                                updates.put("streak", 1);
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            updates.put("streak", 1);
                                        }
                                    }

                                    List<String> studyDates = (List<String>) doc.get("studyDates");
                                    if (studyDates == null) {
                                        studyDates = new ArrayList<>();
                                    }
                                    if (!studyDates.contains(todayCalendar)) {
                                        updates.put("studyDates", FieldValue.arrayUnion(todayCalendar));
                                    }
                                }

                                if (accuracy > 90) {
                                    updates.put("perfectLessonCount", FieldValue.increment(1));
                                }

                                db.collection("users").document(uid).update(updates)
                                        .addOnCompleteListener(updateTask -> {
                                            if (updateTask.isSuccessful()) {
                                                loadUserProfile(uid, onFinished);
                                            } else if (onFinished != null) {
                                                onFinished.run();
                                            }
                                        });
                            }
                        }
                    } else if (onFinished != null) {
                        onFinished.run();
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
        // Legacy logic commented out as streak status is now handled in
        // claimLessonRewards
        /*
         * db.collection("users").document(uid).get()
         * .addOnCompleteListener(task -> {
         * if (task.isSuccessful() && task.getResult() != null) {
         * DocumentSnapshot doc = task.getResult();
         * if (doc.exists()) {
         * User user = doc.toObject(User.class);
         * if (user != null && user.getLastLessonDate() != null &&
         * !user.getLastLessonDate().isEmpty()) {
         * SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy",
         * Locale.getDefault());
         * String today = sdf.format(new Date());
         * String lastDate = user.getLastLessonDate();
         * 
         * try {
         * Date lastLessonDate = sdf.parse(lastDate);
         * Date todayDate = sdf.parse(today);
         * if (lastLessonDate != null && todayDate != null) {
         * long diffInMillis = todayDate.getTime() - lastLessonDate.getTime();
         * long diffInDays = diffInMillis / (1000 * 60 * 60 * 24);
         * 
         * if (diffInDays > 1) {
         * if (user.getFreeze() > 0) {
         * Map<String, Object> updates = new HashMap<>();
         * updates.put("freeze", FieldValue.increment(-1));
         * db.collection("users").document(uid).update(updates);
         * } else {
         * Map<String, Object> updates = new HashMap<>();
         * updates.put("streak", 0);
         * db.collection("users").document(uid).update(updates);
         * }
         * }
         * }
         * } catch (Exception e) {
         * e.printStackTrace();
         * }
         * }
         * }
         * }
         * });
         */
    }
}