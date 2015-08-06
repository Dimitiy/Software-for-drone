package com.academy.softwaredrone.data;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.academy.softwaredrone.MainActivity;
import com.android.util.Logging;

public class OrientationService extends Service implements SensorEventListener {
	static String TAG = OrientationService.class.getSimpleName().toString();
	SensorManager sensorManager;
	ToggleButton changeCoordinateSystem;
	public float[] aValues = new float[3];
	public float[] mValues = new float[3];
	private DroneValue dValue;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		if (MainActivity.isApplication == false) {
			Logging.doLog(TAG, "onStartCommand = false");
			this.stopSelf();
			return 0;
		}
		Logging.doLog(TAG, "onStartCommand");

		sensorManager = (SensorManager) getApplicationContext()
				.getSystemService(SENSOR_SERVICE);
		Sensor accelerometer = sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		Sensor magField = sensorManager
				.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		dValue = new DroneValue();

		sensorManager.registerListener(this, accelerometer,
				SensorManager.SENSOR_DELAY_NORMAL*5);
		sensorManager.registerListener(this, magField,
				SensorManager.SENSOR_DELAY_NORMAL*5);
		return START_STICKY;
	}

	

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		Toast.makeText(getApplicationContext(), "Started", Toast.LENGTH_SHORT)
				.show();
		super.onCreate();
	}


	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
			aValues = event.values;
		if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
			mValues = event.values;
	   	dValue.setOrientation(aValues, mValues);
	}}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

}
