package lk.codehive.ravishka.fitbro.utils;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class StepCounterManager implements SensorEventListener {

    public interface StepListener {
        void onStepCountChanged(int steps);
    }

    private final SensorManager sensorManager;
    private final Sensor stepSensor;
    private StepListener listener;
    private int initialSteps = -1;

    public StepCounterManager(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
    }

    public void startListening(StepListener listener) {
        this.listener = listener;
        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void stopListening() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            int totalStepsSinceBoot = (int) event.values[0];
            if (initialSteps == -1) {
                initialSteps = totalStepsSinceBoot;
            }
            int currentSessionSteps = totalStepsSinceBoot - initialSteps;
            if (listener != null) {
                listener.onStepCountChanged(currentSessionSteps);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
