package lk.codehive.ravishka.fitbro;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.util.Base64;
import android.util.Log;
import com.cloudinary.android.MediaManager;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.stripe.android.PaymentConfiguration;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

public class FitBroApp extends Application {
    private static final String TAG = "FitBroApp";
    private static final String STRIPE_PUBLISHABLE_KEY = "pk_test_51TGBaqE6iHrDOdq4NRQ824kqEqlGxmnVuQzZiW1WgesUt3ZPplHAX7N5iJLvWGRVIizSMntmUmEP5MEdc1lioIcs00WA4PtmuK";

    @Override
    public void onCreate() {
        super.onCreate();
        
        FirebaseApp.initializeApp(this);
        Log.d(TAG, "Firebase initialized");

        // 1. Initialize Notification Channels
        createNotificationChannel();

        // 2. Initialize Stripe
        PaymentConfiguration.init(getApplicationContext(), STRIPE_PUBLISHABLE_KEY);
        
        // 3. Initialize Cloudinary
        initCloudinary();
        
        // 4. Log SHA-1 for registration
        logHashKey();
        
        // 5. Initialize App Check
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        if (BuildConfig.DEBUG) {
            firebaseAppCheck.installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance());
        } else {
            firebaseAppCheck.installAppCheckProviderFactory(PlayIntegrityAppCheckProviderFactory.getInstance());
        }

        firebaseAppCheck.addAppCheckListener(token -> {
            Log.d(TAG, "App Check Token Status: " + (token != null ? "Successfully Obtained" : "Failed to obtain"));
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "fitbro_notifications_channel",
                    "FitBro Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Workout and payment alerts");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void initCloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dbhgxc4dj");
        config.put("api_key", "684774453656553");
        config.put("api_secret", "ZwXdaKllcVuooiQKar5_l2Yy1vE");
        MediaManager.init(this, config);
    }

    public void logHashKey() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest mdSha1 = MessageDigest.getInstance("SHA1");
                byte[] digest = mdSha1.digest(signature.toByteArray());
                StringBuilder hexString = new StringBuilder();
                for (byte b : digest) {
                    hexString.append(String.format("%02X:", b));
                }
                Log.d(TAG, "SHA-1 Fingerprint (Hex): " + hexString.toString().replaceAll(":$", ""));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error logging Hash Key", e);
        }
    }
}
