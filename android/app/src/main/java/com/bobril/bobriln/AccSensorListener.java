package com.bobril.bobriln;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.Arrays;

public class AccSensorListener implements SensorEventListener {

    public interface Listener {
        void onShake();
    }

    private long[] queue = new long[8];
    private int queueSize = 0;
    private final Listener listener;

    private SensorManager sensorManager;
    private Sensor accelerometer;

    public AccSensorListener(Listener listener) {
        this.listener = listener;
    }

    public boolean start(SensorManager sensorManager) {
        if (accelerometer != null) {
            return true;
        }

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (accelerometer != null) {
            this.sensorManager = sensorManager;
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        }
        return accelerometer != null;
    }

    public void stop() {
        if (accelerometer != null) {
            sensorManager.unregisterListener(this, accelerometer);
            sensorManager = null;
            accelerometer = null;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        final float ax = event.values[0];
        final float ay = event.values[1];
        final float az = event.values[2];
        final double magnitudeSquared = ax * ax + ay * ay + az * az;
        final boolean accelerating = magnitudeSquared > 11 * 11;
        long timestamp = event.timestamp;
        final long tooOld = timestamp - 500000000; // more than 0.5s is too old
        int pos = 0;
        while (pos < queueSize - 4) { // minimum size of queue is 4
            if (queue[pos] > tooOld) break;
            pos++;
        }
        if (pos > 0) {
            System.arraycopy(queue, pos, queue, 0, queueSize - pos);
            queueSize -= pos;
        }
        if (queueSize == queue.length) {
            queue = Arrays.copyOf(queue, queueSize * 2);
        }
        queue[queueSize++] = (timestamp & ~1) | (accelerating ? 1 : 0);
        int accCount = 0;
        for (pos = 0; pos < queueSize; pos++) {
            if ((queue[pos] & 1) != 0) accCount++;
        }
        if (queueSize > 1 && accCount * 4 >= queueSize * 3 && queue[queueSize - 1] - queue[0] > 250000000) {
            // 3/4 of events in last 0.25s there accelerating => shake detected
            queueSize = 0;
            listener.onShake();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
