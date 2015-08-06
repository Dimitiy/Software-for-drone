package com.academy.softwaredrone.fragment;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.academy.softwaredrone.MainActivity;
import com.academy.softwaredrone.R;
import com.android.util.Logging;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class LocationFragment extends Fragment implements InfoWindowAdapter {

	/**
	 * The fragment argument representing the section number for this fragment.
	 */
	private static final String ARG_SECTION_NUMBER = "section_number";
	private final String TAG = LocationFragment.class.getSimpleName()
			.toString();
	final int RQS_GooglePlayServices = 1;

	MapView mMapView;
	GoogleMap map;

	/**
	 * Returns a new instance of this fragment for the given section number.
	 */
	public static LocationFragment newInstance(int sectionNumber) {
		LocationFragment fragment = new LocationFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/**
		 * Populate our tab list with tabs. Each item contains a title,
		 * indicator color and divider color, which are used by
		 * {@link SlidingTabLayout}.
		 */

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		onAttach(getActivity());
		setRetainInstance(true);
	}

	public LocationFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.location_layout, container,
				false);
		map = ((SupportMapFragment) getChildFragmentManager().findFragmentById(
				R.id.map)).getMap();

		map.setMyLocationEnabled(true);
		map.getUiSettings().setCompassEnabled(true);
		map.getUiSettings().setRotateGesturesEnabled(false);
		map.getUiSettings().setTiltGesturesEnabled(true);
		map.getUiSettings().setZoomControlsEnabled(true);
		map.getUiSettings().setMyLocationButtonEnabled(true);
		
		Location location = map.getMyLocation();
		if (location != null) {
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
					location.getLatitude(), location.getLongitude()), 3));

		}
		map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		return rootView;
	}

	@Override
	public void onResume() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(getActivity());

		if (resultCode == ConnectionResult.SUCCESS) {
			Toast.makeText(getActivity(), "Google Play Services доступны",
					Toast.LENGTH_LONG).show();
		} else {
			GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(),
					RQS_GooglePlayServices);
		}
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		mMapView.onLowMemory();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		((MainActivity) activity).onSectionAttached(getArguments().getInt(
				ARG_SECTION_NUMBER));
	}

	@Override
	public View getInfoContents(Marker arg0) {
		// TODO Auto-generated method stub
		Logging.doLog(TAG, "getInfoContents" + arg0.toString());
		return null;
	}

	@Override
	public View getInfoWindow(Marker arg0) {
		// TODO Auto-generated method stub
		Logging.doLog(TAG, "getInfoWindow" + arg0.toString());

		return null;
	}
}
