package com.academy.softwaredrone.connect;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.academy.softwaredrone.MainActivity;
import com.academy.softwaredrone.data.DroneValue;
import com.academy.softwaredrone.data.DroneValueListener;
import com.academy.softwaredrone.data.LocationControllerService;
import com.academy.softwaredrone.data.OrientationService;
import com.android.util.AppConstants;
import com.android.util.Logging;

public class SocketService extends Service implements DroneValueListener {
	String address = "0.0.0.0";
	static Socket socket;
	private static Context mContext;
	public final static String TAG = SocketService.class.getSimpleName()
			.toString();
	private SharedPreferences sPref;
	private ResponseParser respParse;
	private DataInputStream in;
	private static DataOutputStream out;
	private DroneValue dValue;
	private Timer t;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		System.out.println("I am in on start");
		// Toast.makeText(this,"Service created ...", Toast.LENGTH_LONG).show();
		mContext = getApplicationContext();
		if (MainActivity.isApplication == false) {
			this.stopSelf();
			return 0;
		}
		dValue = new DroneValue();
		startService(new Intent(this, LocationControllerService.class));
		startService(new Intent(this, OrientationService.class));

		Connect connect = new Connect();
		connect.execute();
		t = new Timer();
		return START_STICKY;
	}

	private void establishingConnection() {
		sPref = mContext.getSharedPreferences("Settings", Context.MODE_PRIVATE);

		Logging.doLog(TAG, "connect", "connect");
		int serverPort = Integer.parseInt(sPref.getString("port", "10082")); // здесь
																				// обязательно
																				// нужно
																				// указать
																				// порт
																				// к
		// которому привязывается сервер.
		this.address = sPref.getString("ip", "192.168.1.235");
		Logging.doLog(TAG, "address" + address);
		// это IP-адрес компьютера, где исполняется наша
		// серверная программа.
		// Здесь указан адрес того самого компьютера где
		// будет исполняться и клиент.

		try {
			InetAddress ipAddress = InetAddress.getByName(address); // создаем
																	// объект
																	// который
																	// отображает
																	// вышеописанный
																	// IP-адрес.
			Log.d("ConnectServ",
					"Any of you heard of a socket with IP address " + address
							+ " and port " + serverPort + "?");
			socket = new Socket(ipAddress, serverPort); // создаем сокет
														// используя
														// IP-адрес и
														// порт сервера.
			Log.d("ConnectServ1", "Yes! I just got hold of the program.");
			Handler handler = new Handler(Looper.getMainLooper());
			handler.post(new Runnable() {
				public void run() {
					Toast.makeText(mContext, "Соединение с ПУ установлено!",
							Toast.LENGTH_LONG).show();

				}
			});
			dValue.addListener(this);
			t.schedule(new TimerForSendData(), 500, 500);
			// Берем входной и выходной потоки сокета, теперь можем получать и
			// отсылать данные клиентом.
			InputStream sin = socket.getInputStream();
			OutputStream sout = socket.getOutputStream();

			// Конвертируем потоки в другой тип, чтоб легче обрабатывать
			// текстовые сообщения.
			in = new DataInputStream(sin);
			out = new DataOutputStream(sout);

			// // Создаем поток для чтения с клавиатуры.
			// BufferedReader keyboard = new BufferedReader(new
			// InputStreamReader(
			// System.in));
			String line = null;
			respParse = new ResponseParser();
			if (!ConnectMotionBase.stateUSB)
				ConnectMotionBase.onResumeDevice(mContext);

			while (true) {
				line = in.readUTF(); // ждем пока сервер отошлет строку текста.
				if (line != null) {
					respParse.parser(String.valueOf(line));

					if (respParse.getType() == 1 && ConnectMotionBase.stateUSB) {
						Logging.doLog(TAG,
								"getType() == 1 " + respParse.getData(),
								"etType() == 1" + respParse.getData());
						switch (respParse.getData()) {
						case "1":
							ConnectMotionBase.motionForward();
							break;
						case "2":
							ConnectMotionBase.motionLeft();
							break;
						case "3":
							ConnectMotionBase.motionRight();
							break;
						case "4":
							ConnectMotionBase.motionBackward();
							break;
						case "5":
							ConnectMotionBase.testConnect(1);
							break;
						case "6":
							ConnectMotionBase.testConnect(0);
						case "0":
							ConnectMotionBase.stopMotion();
							break;
						default:
							break;
						}
					}
				}
			}
		} catch (Exception x) {
			if (dValue != null)
				dValue.delListener(this);

			Handler handler = new Handler(Looper.getMainLooper());
			handler.post(new Runnable() {
				public void run() {
					Toast.makeText(mContext, "Соединение закрыто!",
							Toast.LENGTH_LONG).show();

				}
			});
			x.printStackTrace();
			t.cancel();
			stopService(new Intent(this, LocationControllerService.class));
			stopService(new Intent(this, OrientationService.class));
			stopSelf();
		}
	}

	public static void closeConnect() {
		sendToServerComand(AppConstants.END_CONNECTIONS);

	}

	public static void sendToServerComand(String request) {
		try {
			if (out != null && socket != null) {

				out.writeUTF(request); // отсылаем клиенту конфигурацию
				out.flush(); // заставляем поток закончить передачу
								// данных.System.out.println("Waiting for the next line...");
								// status = in.readUTF(); // ожидаем пока клиент
								// пришлет строку
			}

			if (String.valueOf(request).equals("END")) {
				if (socket != null) {
					socket.close();
					socket = null;
				}

			}
		} catch (IOException e) {
			// TODO Автоматически созданный блок catch
			e.printStackTrace();
		}
	}

	private class Connect extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			// TODO Автоматически созданная заглушка метода
			establishingConnection();
			return null;
		}

	}

	private final IBinder myBinder = new LocalBinder();
	public float[] mValues;
	public float[] aValues;
	public Location location;

	public class LocalBinder extends Binder {
		public SocketService getService() {
			System.out.println("I am in Localbinder ");
			return SocketService.this;

		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return myBinder;
	}

	public void IsBoundable() {
		Toast.makeText(this, "I bind like butter", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			socket.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		socket = null;
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		this.location = location;

	}

	@Override
	public void onOrientationChanged(float[] aValues, float[] mValues) {
		// TODO Auto-generated method stub
		this.aValues = aValues;
		this.mValues = mValues;

	}

	private class TimerForSendData extends TimerTask {

		/**
		 * Ничего нового, тот же интерфейс Runnable. (не пишите такие
		 * комментарии в реальном коде, ну пожалуйста)
		 */
		@Override
		public void run() {
			if (aValues != null && mValues != null)
				RequestList.sendOrientationRequest(aValues, mValues, mContext);
			if (location != null)
				RequestList.sendLocationRequest(location, mContext);

		}
	}
}