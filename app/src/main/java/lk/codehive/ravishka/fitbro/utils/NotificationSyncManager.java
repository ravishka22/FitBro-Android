package lk.codehive.ravishka.fitbro.utils;

import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Map;

public class NotificationSyncManager {
    private static final String TAG = "NotificationSync";
    private static NotificationSyncManager instance;
    private ListenerRegistration listenerRegistration;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    public static synchronized NotificationSyncManager getInstance() {
        if (instance == null) {
            instance = new NotificationSyncManager();
        }
        return instance;
    }

    public void startListening(Context context) {
        if (auth.getCurrentUser() == null) return;
        if (listenerRegistration != null) return;

        String uid = auth.getCurrentUser().getUid();
        Log.d(TAG, "Starting sync for: " + uid);

        listenerRegistration = db.collection("notifications")
                .whereEqualTo("targetUserId", uid)
                .whereEqualTo("read", false)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Listen failed.", e);
                        return;
                    }

                    if (snapshots != null) {
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                Map<String, Object> data = dc.getDocument().getData();
                                String title = (String) data.get("title");
                                String body = (String) data.get("body");
                                String id = dc.getDocument().getId();

                                // Show the notification
                                showLocalNotification(context, title, body);

                                // Mark as read/processed so it doesn't show again
                                db.collection("notifications").document(id).update("read", true);
                            }
                        }
                    }
                });
    }

    public void stopListening() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }

    private void showLocalNotification(Context context, String title, String body) {
        // Use the existing logic from our Messaging Service
        lk.codehive.ravishka.fitbro.services.MyFirebaseMessagingService helper = 
                new lk.codehive.ravishka.fitbro.services.MyFirebaseMessagingService();
        
        // We trigger it manually through the context
        helper.showLocalNotificationManually(context, title, body);
    }
}
