package lk.codehive.ravishka.fitbro.utils;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NotificationHelper {
    private static final String TAG = "NotificationHelper";

    /**
     * Triggers a notification by writing to a central 'notifications' collection.
     * This is designed to be picked up by a Firebase Cloud Function.
     */
    public static void triggerNotification(String targetUserId, String title, String body, String type) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("id", UUID.randomUUID().toString());
        notification.put("targetUserId", targetUserId);
        notification.put("title", title);
        notification.put("body", body);
        notification.put("type", type);
        notification.put("timestamp", System.currentTimeMillis());
        notification.put("read", false);

        db.collection("notifications").add(notification)
                .addOnSuccessListener(documentReference -> Log.d(TAG, "Notification trigger sent: " + title))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to trigger notification", e));
    }

    /**
     * Notify all admins about a platform event (e.g., a new payment)
     */
    public static void notifyAdmins(String title, String body, String type) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").whereEqualTo("role", "admin").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (var doc : queryDocumentSnapshots) {
                        triggerNotification(doc.getId(), title, body, type);
                    }
                });
    }
}
