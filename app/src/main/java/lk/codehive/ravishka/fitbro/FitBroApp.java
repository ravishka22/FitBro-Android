package lk.codehive.ravishka.fitbro;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
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

        // Initialize Stripe
        PaymentConfiguration.init(getApplicationContext(), STRIPE_PUBLISHABLE_KEY);
        Log.d(TAG, "Stripe initialized");

        // Initialize Cloudinary (Alternative to Firebase Storage)
        initCloudinary();
        
        // Log SHA-1 for easy registration
        logHashKey();
        
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        
        if (BuildConfig.DEBUG) {
            firebaseAppCheck.installAppCheckProviderFactory(
                    DebugAppCheckProviderFactory.getInstance());
            Log.d(TAG, "App Check: Debug Provider installed");
        } else {
            firebaseAppCheck.installAppCheckProviderFactory(
                    PlayIntegrityAppCheckProviderFactory.getInstance());
            Log.d(TAG, "App Check: Play Integrity Provider installed");
        }

        firebaseAppCheck.addAppCheckListener(token -> {
            Log.d(TAG, "App Check Token Status: " + (token != null ? "Successfully Obtained" : "Failed to obtain"));
        });
    }

    private void initCloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dbhgxc4dj");
        config.put("api_key", "684774453656553");
        config.put("api_secret", "ZwXdaKllcVuooiQKar5_l2Yy1vE");
        MediaManager.init(this, config);
        Log.d(TAG, "Cloudinary initialized");
    }

    public void logHashKey() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String hashKey = Base64.encodeToString(md.digest(), Base64.NO_WRAP);
                Log.d(TAG, "Your SHA-1 Hash Key for Firebase: " + hashKey);
                
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
