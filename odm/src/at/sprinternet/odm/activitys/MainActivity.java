package at.sprinternet.odm.activitys;

import static at.sprinternet.odm.misc.CommonUtilities.Logd;
import static at.sprinternet.odm.misc.CommonUtilities.getVAR;

import java.io.IOException;
import java.util.Date;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import at.sprinternet.odm.AlertDialogManager;
import at.sprinternet.odm.ConnectionDetector;
import at.sprinternet.odm.LocationAlarm;
import at.sprinternet.odm.R;
import at.sprinternet.odm.UpdateAlarm;
import at.sprinternet.odm.WakeLocker;
import at.sprinternet.odm.misc.ApiProtocolHandler;
import at.sprinternet.odm.misc.CommonUtilities;

public class MainActivity extends Activity {
	private static final String TAG = "MainActivity";
	// Label to display GCM messages
	TextView lblMessage;
	ImageView imgStatus;
	
	// Alert dialog manager
	AlertDialogManager alert = new AlertDialogManager();
	// Connection detector
	ConnectionDetector cd;
	GoogleCloudMessaging gcm;
	Context context;
	String regId;
	Boolean version_check = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		context = getApplicationContext();
		
		cd = new ConnectionDetector(context);
		// Check if Internet present
		if (!cd.isConnectingToInternet()) {
			// Internet Connection is not present
			alert.showAlertDialog(MainActivity.this, context.getString(R.string.main_internet_required_head), context.getString(R.string.main_internet_required), false);
			// stop executing code by return
			return;
		}
		lblMessage = (TextView) findViewById(R.id.lblMessage);
		imgStatus = (ImageView) findViewById(R.id.imageViewStatus);
		lblMessage.setMovementMethod(LinkMovementMethod.getInstance());
		Logd(TAG, "Starting message receiver.");
		registerReceiver(mHandleMessageReceiver, new IntentFilter("at.sprinternet.odm.DISPLAY_MESSAGE"));
		registerReceiver(mHandleStatusUpdate, new IntentFilter("at.sprinternet.odm.STATUS_CHANGED"));
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
		    version_check = extras.getBoolean("VERSION_CHECK", false);
		}
		Logd(TAG, "Starting registration procedure.");
		new RegisterBackground().execute();
	}

	@Override
	public void onDestroy() {
		Logd(TAG, "Stopping message receiver.");
		unregisterReceiver(mHandleMessageReceiver);
		unregisterReceiver(mHandleStatusUpdate);
		super.onDestroy();
	}
	
	public void editSettings(View view) {
		/*
		Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
		startActivity(i);
		finish();*/
		Intent i = new Intent(getApplicationContext(), LoginActivity.class);
		startActivity(i);
		finish();
	}

	class RegisterBackground extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... arg0) {
			Logd(TAG, "Checking update alarm.");
			UpdateAlarm updateAlarm = new UpdateAlarm();
			updateAlarm.SetAlarmContext(context);
			if (CommonUtilities.getVAR("VERSION").equals("true")) {
				Logd(TAG, "Good to start update alarm.");
				updateAlarm.SetAlarm(context);
			} else {
				Logd(TAG, "Shutting down update alarm.");
				updateAlarm.CancelAlarm(context);
			}
			Logd(TAG, "Checking location alarm.");
			LocationAlarm locationAlarm = new LocationAlarm();
			locationAlarm.SetAlarmContext(context);
			locationAlarm.SetInterval(CommonUtilities.getVAR("LOC_INTERVAL"));
			if (!CommonUtilities.getVAR("LOC_INTERVAL").equals("0")) {
				Logd(TAG, "Good to start location alarm.");
				locationAlarm.SetAlarm(context);
			} else {
				Logd(TAG, "Shutting down location alarm.");
				locationAlarm.CancelAlarm(context);
			}
			try {
				Logd(TAG, "Checking if GCM is null.");
				if (gcm == null) {
					Logd(TAG, "It is, initializing.");
					gcm = GoogleCloudMessaging.getInstance(context);
				}
				Logd(TAG, "Registering.");
				regId = gcm.register(getVAR("SENDER_ID"));
				Logd(TAG, "Device registered, registration ID=" + regId);
				SharedPreferences mPrefs = getSharedPreferences("usersettings", 0);
				SharedPreferences.Editor mEditor = mPrefs.edit();
				mEditor.putString("REG_ID", regId).commit();
				// We are never setting the application REG_ID, but it shouldn't matter, as the service will refresh
				Logd(TAG, "Executing server registration.");
				ApiProtocolHandler.apiRegister(context, regId);
			} catch (IOException ex) {
				Logd(TAG, "Error :" + ex.getMessage());
			}
			return null;
		}
	}

	// Receive screen notifications
	private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String newMessage = intent.getExtras().getString("message");
			if (newMessage != null) {
				// Waking up mobile if it is sleeping
				WakeLocker.acquire(getApplicationContext());
				// Showing received message
				Date date = new Date();
				lblMessage.append(Html.fromHtml(date.toString() + ": " + newMessage + "<br>\n"));
				// Releasing wake lock
				WakeLocker.release();
			} else {
				Logd(TAG, "Got a NULL message");
			}
		}
	};
	
	// Receive screen notifications
		private final BroadcastReceiver mHandleStatusUpdate = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				int newStatus = intent.getExtras().getInt("statusImageID");
				if (newStatus != 0) {
					imgStatus.setImageResource(newStatus);
				} else {
					Logd(TAG, "Got a NULL status");
				}
			}
		};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case 1:
			if (resultCode == Activity.RESULT_OK) {
				Logd(TAG, "Admin enabled!");
			} else {
				Logd(TAG, "Admin enable FAILED!");
			}
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

}
