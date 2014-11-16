package com.academy.softwaredrone.fragment;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.academy.softwaredrone.HandleDroneActivity;
import com.academy.softwaredrone.MainActivity;
import com.academy.softwaredrone.R;
import com.academy.softwaredrone.connect.ConnectMotionBase;
import com.academy.softwaredrone.connect.SocketService;

public class Search—onsumersFragment extends Fragment {

	/**
	 * The fragment argument representing the section number for this fragment.
	 */
	private static final String ARG_SECTION_NUMBER = "section_number";
	ConnectMotionBase connectMB;
	SocketService getSocket;
	private ToggleButton toggleConnectContropPoint;
	public static final String TAG = Search—onsumersFragment.class
			.getSimpleName();
	SocketService mBoundService;
	Button bHandle, bStateUsb;
	boolean mIsBound = false;
	private ServiceConnection mConnection;

	/**
	 * Returns a new instance of this fragment for the given section number.
	 */
	public static Search—onsumersFragment newInstance(int sectionNumber) {
		Log.d(TAG, "SearchDroneFragment" + sectionNumber);

		Search—onsumersFragment fragment = new Search—onsumersFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		Log.d(TAG, "SearchDroneFragment setArguments");
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mConnection = new ServiceConnection() {
			// EDITED PART
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				// TODO Auto-generated method stub
				mBoundService = ((SocketService.LocalBinder) service)
						.getService();

			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				// TODO Auto-generated method stub
				mBoundService = null;
			}

		};
		/**
		 * Populate our tab list with tabs. Each item contains a title,
		 * indicator color and divider color, which are used by
		 * {@link SlidingTabLayout}.
		 */

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_search_consumers,
				container, false);

		
		bStateUsb = (Button) view.findViewById(R.id.button_handle);
		bStateUsb.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				connectMB = new ConnectMotionBase(getActivity()
						.getApplicationContext());
				connectMB.onResumeDevice();
				if (connectMB.getStateConnectUSB()) {
					Toast.makeText(
							getActivity().getApplicationContext(),
							"—ÓÂ‰ËÌÂÌËÂ Ò ‰‚Ë„‡ÚÂÎˇÏË ÛÒÚÓÈÒÚ‚‡ ÛÒÔÂ¯ÌÓ ÛÒÚ‡ÌÓ‚ÎÂÌÓ",
							Toast.LENGTH_LONG).show();
					connectMB = null;
				} else {
					Toast.makeText(getActivity().getApplicationContext(),
							"Œ¯Ë·Í‡ ÒÓÂ‰ËÌÂÌËˇ Ò ‰‚Ë„‡ÚÂÎˇÏË ÛÒÚÓÈÒÚ‚‡!",
							Toast.LENGTH_LONG).show();
					connectMB = null;
				}
				connectMB = null;
			}
		});
		toggleConnectContropPoint = (ToggleButton) view
				.findViewById(R.id.toggleConnectControlPoint);
		toggleConnectContropPoint.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (toggleConnectContropPoint.isChecked()) {
					Log.d("isChecked", "try");
					getActivity().startService(
							new Intent(getActivity(), SocketService.class));
					doBindService();
				} else {
					Log.d("isChecked", "false");
					// if (getSocket != null)
					// getSocket.closeConnect();
					SocketService.closeConnect();
					doUnbindService();
					toggleConnectContropPoint.setChecked(false);
				}
			}
		});
		bHandle = (Button) view.findViewById(R.id.button_handle);
		bHandle.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(getActivity(),
						HandleDroneActivity.class);
			startActivity(intent);

			}
		});
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		onAttach(getActivity());
		setRetainInstance(true);

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		((MainActivity) activity).onSectionAttached(getArguments().getInt(
				ARG_SECTION_NUMBER));
	}

	private void doBindService() {
		getActivity().bindService(
				new Intent(getActivity(), SocketService.class), mConnection,
				Context.BIND_AUTO_CREATE);
		mIsBound = true;
		if (mBoundService != null) {
			mBoundService.IsBoundable();
		}
	}

	private void doUnbindService() {
		if (mIsBound) {
			// Detach our existing connection.
			getActivity().unbindService(mConnection);
			mIsBound = false;
		}
	}

}
