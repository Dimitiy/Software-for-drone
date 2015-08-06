package com.academy.softwaredrone;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.academy.softwaredrone.connect.ConnectMotionBase;
import com.academy.softwaredrone.data.CompassView;
import com.academy.softwaredrone.data.DroneValue;
import com.academy.softwaredrone.data.DroneValueListener;
import com.android.util.Logging;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class HandleDroneActivity extends ActionBarActivity implements
		SensorEventListener, InfoWindowAdapter, DroneValueListener {
	private final static String TAG = HandleDroneActivity.class.getSimpleName();
	private SensorManager mSensorManager;
	private Sensor mAccel;
	private ToggleButton lightButton;
	private GoogleMap map;

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
	float[] history = new float[2];
	String[] direction = { "NONE", "NONE" };
	private static CompassView compassView;
	public static boolean isInFront = false;
	private TextView textX, textY, textCmdSend;
	private DroneValue dValue;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		// supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		setContentView(R.layout.handle_layout);
		initial();
		loadPref();
		textX = (TextView) findViewById(R.id.textViewX);
		textY = (TextView) findViewById(R.id.textViewY);
		textCmdSend = (TextView) findViewById(R.id.textViewCmdSend);
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
					if (!ConnectMotionBase.stateUSB)
						ConnectMotionBase
								.onResumeDevice(getApplicationContext());
				} else {
					Log.d("isChecked", "false");
					textX.setVisibility(View.INVISIBLE);
					textY.setVisibility(View.INVISIBLE);
					textCmdSend.setVisibility(View.INVISIBLE);
					mSensorManager.unregisterListener(HandleDroneActivity.this);
				}
			}
		});
	}

	private void initial() {
		LayoutInflater ltInflater = getLayoutInflater();

		map = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map)).getMap();
		if (map == null) {
			Toast.makeText(getApplicationContext(),
					"Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
		}
		(findViewById(R.id.map))
				.getViewTreeObserver()
				.addOnGlobalLayoutListener(
						new android.view.ViewTreeObserver.OnGlobalLayoutListener() {

							@Override
							public void onGlobalLayout() {
								if (android.os.Build.VERSION.SDK_INT >= 16) {
									(findViewById(R.id.map))
											.getViewTreeObserver()
											.removeOnGlobalLayoutListener(this);
								} else {
									(findViewById(R.id.map))
											.getViewTreeObserver()
											.removeGlobalOnLayoutListener(this);
								}

							}
						});
		map.setPadding(0, 250, 0, 0);
		map.setMyLocationEnabled(true);
		map.getUiSettings().setCompassEnabled(true);
		map.getUiSettings().setMyLocationButtonEnabled(false);
		map.setTrafficEnabled(true);
		map.getUiSettings().setRotateGesturesEnabled(false);
		map.getUiSettings().setTiltGesturesEnabled(true);
		map.getUiSettings().setZoomControlsEnabled(true);
		map.setIndoorEnabled(true);
		map.setBuildingsEnabled(false);

		Location location = map.getMyLocation();
		if (location != null) {
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
					location.getLatitude(), location.getLongitude()), 3));

		}
		map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		LinearLayout linLayout = (LinearLayout) findViewById(R.id.layout_compass);
		View view = ltInflater
				.inflate(R.layout.compass_layout, linLayout, true);

		compassView = (CompassView) view.findViewById(R.id.compassView);
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
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setTitle("");
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

	public static void refreshDisplay(float[] values) {

		if (compassView != null) {
			compassView.setBearing(values[0]);
			compassView.setPitch(values[1]);
			compassView.setRoll(-values[2]);
			compassView.invalidate();
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
			ConnectMotionBase.motionRight();
			log = "right";
		} else if (xAxis > 50 && yAxis >= -140) {
			ConnectMotionBase.motionLeft();
			log = "left";
		} else if (xAxis > -50 && yAxis > 140) {
			ConnectMotionBase.motionForward();
			log = "forward";
		} else if (xAxis > -50 && yAxis < -27) {
			ConnectMotionBase.motionBackward();
			log = "backward";
		} else {
			ConnectMotionBase.stopMotion();
			log = "stop";
		}

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
		isInFront = true;
		dValue = new DroneValue();
		dValue.addListener(this);

	}

	@Override
	protected void onPause() {
		super.onPause();
		isInFront = false;
		if (dValue != null)
			dValue.delListener(this);

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

	@Override
	public View getInfoContents(Marker arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public View getInfoWindow(Marker arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onLocationChanged(Location location) {
		Logging.doLog(TAG, "onLocationChanged" + location.toString());
		CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(new LatLng(location.getLatitude(), location
						.getLongitude())).zoom(16).build();

		map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

	}

	@Override
	public void onOrientationChanged(float[] aValues, float[] mValues) {
		// TODO Auto-generated method stub
		refreshDisplay(calculateOrientation(aValues, mValues));
	}

	private float[] calculateOrientation(float[] aValues, float[] mValues) {
		float[] values = new float[3];
		float[] R = new float[9];
		float[] outR = new float[9];
		int axisX = 0, axisY = 0;
		WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		Display display = window.getDefaultDisplay();
		int mScreenRotation = display.getRotation();
		SensorManager.getRotationMatrix(R, null, aValues, mValues);
		switch (mScreenRotation) {
		case Surface.ROTATION_0:
			axisX = SensorManager.AXIS_X;
			axisY = SensorManager.AXIS_Z;
			break;

		case Surface.ROTATION_90:
			axisX = SensorManager.AXIS_Z;
			axisY = SensorManager.AXIS_MINUS_X;
			break;

		case Surface.ROTATION_180:
			axisX = SensorManager.AXIS_MINUS_X;
			axisY = SensorManager.AXIS_MINUS_Y;
			break;

		case Surface.ROTATION_270:
			axisX = SensorManager.AXIS_MINUS_Z;
			axisY = SensorManager.AXIS_X;
			break;

		default:
			break;
		}
		SensorManager.remapCoordinateSystem(R, axisX, axisY, outR);

		SensorManager.getOrientation(outR, values);

		// Convert from Radians to Degrees.
		values[0] = (float) Math.toDegrees(values[0]);
		values[1] = (float) Math.toDegrees(values[1]);
		values[2] = (float) Math.toDegrees(values[2]);

		return values;
	}
}
