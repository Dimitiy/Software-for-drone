package com.academy.softwaredrone;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.academy.softwaredrone.connect.ConnectMotionBase;

public class HandleDroneActivity extends ActionBarActivity implements
		SensorEventListener {
	private final String TAG = HandleDroneActivity.class.getSimpleName();
	private SensorManager mSensorManager;
	private Sensor mAccel;
	private ToggleButton lightButton;

	private int xAxis = 0;
	private int yAxis = 0;

	private boolean show_Debug = true; // отображение отладочной информации
	private int xMax; // предел по оси X, максимальное значение для ШИМ (0-10),
						// чем больше, тем больше нужно наклонять
						// Android-устройство
	private int yMax; // предел по оси Y, максимальное значение для ШИМ (0-10)
	private int zMax = 5; // предел по оси Y, максимальное значение для ШИМ
							// (0-10)

	private int yThreshold; // минимальное значение ШИМ (порог ниже которого не
							// вращается двигатель)
	private int pwmMax; // максимальное значение ШИМ
	private int xR; // точка разворота
	private String commandLeft; // символ команды левого двигателя
	private String commandRight; // символ команды правого двигателя
	private String commandHorn; // символ команды для доп. канала (звуковой
								// сигнал)
	ConnectMotionBase usbConnect;
	float[] history = new float[2];
	String[] direction = { "NONE", "NONE" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

		setContentView(R.layout.handle_layout);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setTitle("");
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		xMax = Integer.parseInt((String) getResources().getText(
				R.string.default_xMax));
		xR = Integer.parseInt((String) getResources().getText(
				R.string.default_xR));
		yMax = Integer.parseInt((String) getResources().getText(
				R.string.default_yMax));
		yThreshold = Integer.parseInt((String) getResources().getText(
				R.string.default_yThreshold));
		pwmMax = Integer.parseInt((String) getResources().getText(
				R.string.default_pwmMax));
		commandLeft = (String) getResources().getText(
				R.string.default_commandLeft);
		commandRight = (String) getResources().getText(
				R.string.default_commandRight);
		commandHorn = (String) getResources().getText(
				R.string.default_commandHorn);

		loadPref();
		lightButton = (ToggleButton) findViewById(R.id.LightButton);
		lightButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (lightButton.isChecked()) {
					Log.d("isChecked", "try");
					mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
					mAccel = mSensorManager
							.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
					mSensorManager.registerListener(HandleDroneActivity.this,
							mAccel, SensorManager.SENSOR_DELAY_NORMAL);
					usbConnect = new ConnectMotionBase(getApplicationContext());
					usbConnect.onResumeDevice();
				} else {
					Log.d("isChecked", "false");
					mSensorManager.unregisterListener(HandleDroneActivity.this);
				}
			}
		});
		// mSensorManager = (SensorManager)
		// getSystemService(Context.SENSOR_SERVICE);
		// mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		// usbConnect = new ConnectMotionBase(getApplicationContext());
		// usbConnect.onResumeDevice();

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// app icon in action bar clicked; goto parent activity.
			this.finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void onSensorChanged(SensorEvent e) {

		String log = null;
		if ((getApplicationContext().getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE) {
			xAxis = Math.round(e.values[0] * pwmMax / xR);
			yAxis = Math.round(e.values[1] * pwmMax / yMax);

		} else {
			yAxis = Math.round(e.values[0] * pwmMax / yMax);
			xAxis = Math.round(-e.values[1] * pwmMax / xR);

		}
		if (xAxis < -50 && yAxis >= -220) {
			usbConnect.motionRight();
			log = "right";
		} else if (xAxis > 50 && yAxis >= -140) {
			usbConnect.motionLeft();
			log = "left";
		} else if (xAxis > -50 && yAxis > 140) {
			usbConnect.motionForward();
			log = "forward";
		} else if (xAxis > -50 && yAxis < -27) {
			usbConnect.motionBackward();
			log = "backward";
		} else {
			usbConnect.stopMotion();
			log = "stop";
		}

		TextView textX = (TextView) findViewById(R.id.textViewX);
		TextView textY = (TextView) findViewById(R.id.textViewY);
		TextView textCmdSend = (TextView) findViewById(R.id.textViewCmdSend);

		if (show_Debug) {
			textX.setText(String.valueOf("X:"
					+ String.format("%.1f", e.values[0]) + "; xPWM:" + xAxis));
			textY.setText(String.valueOf("Y:"
					+ String.format("%.1f", e.values[1]) + "; yPWM:" + yAxis));

			textCmdSend.setText(log);
		} else {
			textX.setText("");
			textY.setText("");
			textCmdSend.setText("");
		}

	}

	private void loadPref() {
		SharedPreferences mySharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);

		xMax = Integer.parseInt(mySharedPreferences.getString("pref_xMax",
				String.valueOf(xMax)));
		xR = Integer.parseInt(mySharedPreferences.getString("pref_xR",
				String.valueOf(xR)));
		yMax = Integer.parseInt(mySharedPreferences.getString("pref_yMax",
				String.valueOf(yMax)));
		yThreshold = Integer.parseInt(mySharedPreferences.getString(
				"pref_yThreshold", String.valueOf(yThreshold)));
		pwmMax = Integer.parseInt(mySharedPreferences.getString("pref_pwmMax",
				String.valueOf(pwmMax)));
		// show_Debug = mySharedPreferences.getBoolean("pref_Debug", false);
		commandLeft = mySharedPreferences.getString("pref_commandLeft",
				commandLeft);
		commandRight = mySharedPreferences.getString("pref_commandRight",
				commandRight);
		commandHorn = mySharedPreferences.getString("pref_commandHorn",
				commandHorn);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// mAccel = mSensorManager
		// .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		//
		// mSensorManager.registerListener(this, mAccel,
		// SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mSensorManager != null)
			mSensorManager.unregisterListener(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		loadPref();
	}

	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
	}
	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// // Only show items in the action bar relevant to this screen
	// // if the drawer is not showing. Otherwise, let the drawer
	// // decide what to show in the action bar.
	// Log.d(TAG, "onCreateOptionsMenu");
	// MenuInflater inflater = new MenuInflater(this);
	// inflater.inflate(R.menu.toolbar_menu, menu);
	//
	// return super.onCreateOptionsMenu(menu);
	// }
}
