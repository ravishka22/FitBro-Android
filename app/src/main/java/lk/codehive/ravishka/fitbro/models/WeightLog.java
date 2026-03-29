package lk.codehive.ravishka.fitbro.models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

@Entity(tableName = "weight_logs")
@IgnoreExtraProperties
public class WeightLog {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String firestoreId; // To link with Firestore document
    private float weight;
    private long timestamp;
    private String userId;

    @Ignore
    public WeightLog() {}

    public WeightLog(float weight, long timestamp, String userId) {
        this.weight = weight;
        this.timestamp = timestamp;
        this.userId = userId;
    }

    @Exclude
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFirestoreId() { return firestoreId; }
    public void setFirestoreId(String firestoreId) { this.firestoreId = firestoreId; }

    public float getWeight() { return weight; }
    public void setWeight(float weight) { this.weight = weight; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
