package lk.codehive.ravishka.fitbro.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import lk.codehive.ravishka.fitbro.models.WeightLog;

@Dao
public interface WeightLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(WeightLog log);

    @Delete
    void delete(WeightLog log);

    @Query("SELECT * FROM weight_logs WHERE userId = :userId ORDER BY timestamp ASC")
    LiveData<List<WeightLog>> getWeightLogs(String userId);

    @Query("DELETE FROM weight_logs")
    void deleteAllWeightLogs();
}
