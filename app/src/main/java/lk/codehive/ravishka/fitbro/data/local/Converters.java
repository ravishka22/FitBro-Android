package lk.codehive.ravishka.fitbro.data.local;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import lk.codehive.ravishka.fitbro.models.WorkoutDay;

public class Converters {
    @TypeConverter
    public static List<WorkoutDay> fromString(String value) {
        Type listType = new TypeToken<List<WorkoutDay>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromList(List<WorkoutDay> list) {
        Gson gson = new Gson();
        return gson.toJson(list);
    }
}