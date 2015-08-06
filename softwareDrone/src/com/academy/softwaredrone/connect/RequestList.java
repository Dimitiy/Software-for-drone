package com.academy.softwaredrone.connect;

import java.io.IOException;
import java.util.Arrays;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.location.Location;

import com.android.util.AppConstants;

public class RequestList {
	public static void sendLocationRequest(Location location, Context ctx) {
		String sendStr;
		JSONObject object = new JSONObject();

		try {
			object.put("type", AppConstants.TYPE_LOCATION_TRACKER_REQUEST);
			object.put(
					"location",
					location.getLongitude() + " " + location.getLatitude()
							+ " " + location.getAltitude() + " "
							+ location.getAccuracy() + " "
							+ location.getSpeed());

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			sendStr = RequestBuilder.buildComandRequest(object.toString(), ctx);
			SocketService.sendToServerComand(sendStr);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void sendOrientationRequest(float[] aValues, float[] mValues, Context ctx) {

		String sendStr;
		JSONObject object = new JSONObject();
		String objectAsString = Arrays.toString(aValues);
		objectAsString += "," + Arrays.toString(mValues);
		try {
			object.put("orientation", objectAsString);
			object.put("type", AppConstants.TYPE_ORIENTATION_REQUEST);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			sendStr = RequestBuilder.buildComandRequest(object.toString(), ctx);
			SocketService.sendToServerComand(sendStr);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
