package com.example.learning_app.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LessonProgressRepository {
    private static final String TAG = "LessonProgressRepo";
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private MutableLiveData<String> errorMessage;

    public LessonProgressRepository(Application application) {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        errorMessage = new MutableLiveData<>();
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Save lesson completed status to Firestore using Transaction for atomic updates
     */
    public void saveLessonCompleted(String userId, int lessonId, boolean isCompleted) {
        com.google.firebase.firestore.DocumentReference docRef = db.collection("users")
                .document(userId)
                .collection("lessonProgress")
                .document("status");
        
        db.runTransaction((com.google.firebase.firestore.Transaction transaction) -> {
            DocumentSnapshot doc = transaction.get(docRef);
            Map<String, Object> data;
            
            if (doc.exists() && doc.getData() != null) {
                data = new HashMap<>(doc.getData());
            } else {
                data = new HashMap<>();
            }
            
            // Get existing completedLessons
            Map<String, Object> completedLessons = new HashMap<>();
            Object existingCompleted = data.get("completedLessons");
            if (existingCompleted instanceof Map) {
                completedLessons.putAll((Map<String, Object>) existingCompleted);
            }
            
            // Handle legacy flat fields for migration
            for (String key : data.keySet()) {
                if (key.startsWith("completedLessons.")) {
                    String lessonIdFromKey = key.substring("completedLessons.".length());
                    if (!completedLessons.containsKey(lessonIdFromKey)) {
                        completedLessons.put(lessonIdFromKey, data.get(key));
                    }
                }
            }
            
            // Update the specific lesson
            String lessonIdStr = String.valueOf(lessonId);
            if (isCompleted) {
                completedLessons.put(lessonIdStr, true);
            } else {
                completedLessons.remove(lessonIdStr);
            }
            
            data.put("completedLessons", completedLessons);
            
            // Preserve unlockedLessons if exists
            if (!data.containsKey("unlockedLessons")) {
                Object existingUnlocked = data.get("unlockedLessons");
                if (existingUnlocked instanceof Map) {
                    data.put("unlockedLessons", existingUnlocked);
                } else {
                    data.put("unlockedLessons", new HashMap<String, Object>());
                }
            }
            
            transaction.set(docRef, data, com.google.firebase.firestore.SetOptions.merge());
            return null;
        }).addOnSuccessListener(aVoid -> {
            Log.d(TAG, "Saved lesson completed status (transaction): lessonId=" + lessonId + ", completed=" + isCompleted);
        }).addOnFailureListener(e -> {
            errorMessage.setValue("Failed to save lesson progress: " + e.getMessage());
            Log.e(TAG, "Error saving lesson completed (transaction)", e);
        });
    }

    /**
     * Save lesson locked/unlocked status to Firestore using Transaction for atomic updates
     */
    public void saveLessonLocked(String userId, int lessonId, boolean isLocked) {
        com.google.firebase.firestore.DocumentReference docRef = db.collection("users")
                .document(userId)
                .collection("lessonProgress")
                .document("status");
        
        db.runTransaction((com.google.firebase.firestore.Transaction transaction) -> {
            DocumentSnapshot doc = transaction.get(docRef);
            Map<String, Object> data;
            
            if (doc.exists() && doc.getData() != null) {
                data = new HashMap<>(doc.getData());
            } else {
                data = new HashMap<>();
            }
            
            // Get existing unlockedLessons
            Map<String, Object> unlockedLessons = new HashMap<>();
            Object existingUnlocked = data.get("unlockedLessons");
            if (existingUnlocked instanceof Map) {
                unlockedLessons.putAll((Map<String, Object>) existingUnlocked);
            }
            
            // Handle legacy flat fields for migration
            for (String key : data.keySet()) {
                if (key.startsWith("unlockedLessons.")) {
                    String lessonIdFromKey = key.substring("unlockedLessons.".length());
                    if (!unlockedLessons.containsKey(lessonIdFromKey)) {
                        unlockedLessons.put(lessonIdFromKey, data.get(key));
                    }
                }
            }
            
            // Update the specific lesson
            String lessonIdStr = String.valueOf(lessonId);
            if (!isLocked) { // If unlocked, save it
                unlockedLessons.put(lessonIdStr, true);
            } else {
                unlockedLessons.remove(lessonIdStr);
            }
            
            data.put("unlockedLessons", unlockedLessons);
            
            // Preserve completedLessons if exists
            if (!data.containsKey("completedLessons")) {
                Object existingCompleted = data.get("completedLessons");
                if (existingCompleted instanceof Map) {
                    data.put("completedLessons", existingCompleted);
                } else {
                    data.put("completedLessons", new HashMap<String, Object>());
                }
            }
            
            transaction.set(docRef, data, com.google.firebase.firestore.SetOptions.merge());
            return null;
        }).addOnSuccessListener(aVoid -> {
            Log.d(TAG, "Saved lesson locked status (transaction): lessonId=" + lessonId + ", locked=" + isLocked);
        }).addOnFailureListener(e -> {
            errorMessage.setValue("Failed to save lesson progress: " + e.getMessage());
            Log.e(TAG, "Error saving lesson locked (transaction)", e);
        });
    }

    /**
     * Load lesson progress from Firestore
     */
    public void loadLessonProgress(String userId, OnProgressLoadedListener listener) {
        db.collection("users").document(userId).collection("lessonProgress").document("status")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot doc = task.getResult();
                        Map<String, Object> data = doc.getData();
                        
                        List<Integer> completedLessons = new ArrayList<>();
                        List<Integer> unlockedLessons = new ArrayList<>();
                        
                        if (data != null) {
                            Log.d(TAG, "Raw Firestore data: " + data.toString());
                            
                            Set<Integer> processedCompleted = new HashSet<>();
                            Set<Integer> processedUnlocked = new HashSet<>();
                            
                            // Extract completed lessons - handle both nested map and flat fields
                            Object completedObj = data.get("completedLessons");
                            if (completedObj instanceof Map) {
                                Map<String, Object> completedMap = (Map<String, Object>) completedObj;
                                Log.d(TAG, "completedLessons map: " + completedMap.toString());
                                for (String key : completedMap.keySet()) {
                                    try {
                                        int lessonId = Integer.parseInt(key);
                                        if (!processedCompleted.contains(lessonId)) {
                                            completedLessons.add(lessonId);
                                            processedCompleted.add(lessonId);
                                            Log.d(TAG, "Added completed lesson from map: " + key);
                                        }
                                    } catch (NumberFormatException e) {
                                        Log.w(TAG, "Invalid lesson ID in completedLessons: " + key);
                                    }
                                }
                            }
                            
                            // Also check for flat fields like "completedLessons.1"
                            for (String key : data.keySet()) {
                                if (key.startsWith("completedLessons.")) {
                                    String lessonIdStr = key.substring("completedLessons.".length());
                                    try {
                                        int lessonId = Integer.parseInt(lessonIdStr);
                                        if (!processedCompleted.contains(lessonId)) {
                                            completedLessons.add(lessonId);
                                            processedCompleted.add(lessonId);
                                            Log.d(TAG, "Added completed lesson from flat field: " + lessonIdStr);
                                        }
                                    } catch (NumberFormatException e) {
                                        Log.w(TAG, "Invalid lesson ID in completedLessons flat field: " + lessonIdStr);
                                    }
                                }
                            }
                            
                            // Extract unlocked lessons - handle both nested map and flat fields
                            Object unlockedObj = data.get("unlockedLessons");
                            if (unlockedObj instanceof Map) {
                                Map<String, Object> unlockedMap = (Map<String, Object>) unlockedObj;
                                Log.d(TAG, "unlockedLessons map: " + unlockedMap.toString());
                                for (String key : unlockedMap.keySet()) {
                                    try {
                                        int lessonId = Integer.parseInt(key);
                                        if (!processedUnlocked.contains(lessonId)) {
                                            unlockedLessons.add(lessonId);
                                            processedUnlocked.add(lessonId);
                                            Log.d(TAG, "Added unlocked lesson from map: " + key);
                                        }
                                    } catch (NumberFormatException e) {
                                        Log.w(TAG, "Invalid lesson ID in unlockedLessons: " + key);
                                    }
                                }
                            }
                            
                            // Also check for flat fields like "unlockedLessons.1"
                            for (String key : data.keySet()) {
                                if (key.startsWith("unlockedLessons.")) {
                                    String lessonIdStr = key.substring("unlockedLessons.".length());
                                    try {
                                        int lessonId = Integer.parseInt(lessonIdStr);
                                        if (!processedUnlocked.contains(lessonId)) {
                                            unlockedLessons.add(lessonId);
                                            processedUnlocked.add(lessonId);
                                            Log.d(TAG, "Added unlocked lesson from flat field: " + lessonIdStr);
                                        }
                                    } catch (NumberFormatException e) {
                                        Log.w(TAG, "Invalid lesson ID in unlockedLessons flat field: " + lessonIdStr);
                                    }
                                }
                            }
                        } else {
                            Log.w(TAG, "Document data is null");
                        }
                        
                        Log.d(TAG, "Loaded progress: " + completedLessons.size() + " completed, " + 
                                unlockedLessons.size() + " unlocked");
                        
                        if (listener != null) {
                            listener.onProgressLoaded(completedLessons, unlockedLessons);
                        }
                    } else {
                        Log.w(TAG, "No lesson progress found or error loading: " + 
                                (task.getException() != null ? task.getException().getMessage() : "Unknown"));
                        if (listener != null) {
                            listener.onProgressLoaded(new ArrayList<>(), new ArrayList<>());
                        }
                    }
                });
    }

    public interface OnProgressLoadedListener {
        void onProgressLoaded(List<Integer> completedLessonIds, List<Integer> unlockedLessonIds);
    }
}

