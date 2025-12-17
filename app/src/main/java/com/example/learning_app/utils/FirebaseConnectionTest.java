package com.example.learning_app.utils;

import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FirebaseConnectionTest {

    private static final String TAG = "FirebaseTest";

    public static void testAuthConnection() {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        Log.d(TAG, "Testing Firebase Auth connection...");
        Log.d(TAG, "Current user: " + (auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : "null"));

        auth.signInAnonymously()
                .addOnCompleteListener(new OnCompleteListener<com.google.firebase.auth.AuthResult>() {
                    @Override
                    public void onComplete(Task<com.google.firebase.auth.AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = task.getResult().getUser();
                            Log.d(TAG, "Firebase Auth connected successfully");
                            Log.d(TAG, "Anonymous user ID: " + user.getUid());
                        } else {
                            Log.e(TAG, "Firebase Auth connection failed", task.getException());
                        }
                    }
                });
    }

    public static void testFirestoreConnection() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Log.d(TAG, "Testing Firestore connection...");

        Map<String, Object> testData = new HashMap<>();
        testData.put("timestamp", System.currentTimeMillis());
        testData.put("test", true);

        db.collection("connection_test")
                .document("test_doc")
                .set(testData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Firestore write successful");

                            db.collection("connection_test")
                                    .document("test_doc")
                                    .get()
                                    .addOnCompleteListener(
                                            new OnCompleteListener<com.google.firebase.firestore.DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(
                                                        Task<com.google.firebase.firestore.DocumentSnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        Log.d(TAG, "Firestore read successful");
                                                        Log.d(TAG, "Data: " + task.getResult().getData());
                                                    } else {
                                                        Log.e(TAG, "Firestore read failed", task.getException());
                                                    }
                                                }
                                            });
                        } else {
                            Log.e(TAG, "Firestore write failed", task.getException());
                            Log.e(TAG, "Error: "
                                    + (task.getException() != null ? task.getException().getMessage() : "unknown"));
                        }
                    }
                });
    }

    public static void testAllConnections() {
        Log.d(TAG, "Starting Firebase Connection Tests...");
        testAuthConnection();
        testFirestoreConnection();
        Log.d(TAG, "Check Logcat for results");
    }
}
