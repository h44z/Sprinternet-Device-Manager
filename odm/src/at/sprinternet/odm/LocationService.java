package at.sprinternet.odm;

import static at.sprinternet.odm.misc.CommonUtilities.Logd;
import static at.sprinternet.odm.misc.CommonUtilities.getVAR;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import at.sprinternet.odm.GetLocation.LocationResult;
import at.sprinternet.odm.misc.ApiProtocolHandler;

public class LocationService extends Service {
	private static final String TAG = "LocationService";
	Context context;
	LocationType locationType = LocationType.ALL;
	Boolean isHistory = false;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		context = getApplicationContext();
		String message = intent.getStringExtra("message");
		String requestId = intent.getStringExtra("requestid");
		isHistory = intent.getBooleanExtra("ishistory", false);
		
		context = getApplicationContext();
		if (message != null) {
			Logd(TAG, "Location Message: " + message);
			if (message.equals("Command:GetLocationGPS"))
				locationType = LocationType.GPSONLY;
			if (message.equals("Command:GetLocationNetwork"))
				locationType = LocationType.NETWORKONLY;
		}
		if (isHistory) {
 			// To overcome a context reference bug, we need to recheck if it's an alarm.
 			if (getVAR("HISTNOGPS").equals("true")) {
 				locationType = LocationType.NETWORKONLY;
 			} else {
 				locationType = LocationType.ALL;
 			}
 		}

		LocationResult locationResult = new CustomLocationResult(requestId, isHistory);
		GetLocation myLocation = new GetLocation();
		myLocation.getLocation(context, locationResult, locationType);
		return START_STICKY;
	}

	public void stopLocation() {
		this.stopSelf();
	}
	
	class CustomLocationResult extends LocationResult {
		String requestID;
		Boolean isHistory = false;
		
		public CustomLocationResult(String requestID, Boolean isHistory) {
			super();
			this.requestID = requestID;
			this.isHistory = isHistory;
		}
		
		
		@Override
		public void gotLocation(Location location) {
			//Got the location! Send to server
			if (location != null) {
				String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMAN).format(Calendar.getInstance().getTime());
				Logd(TAG, "Got location for reqID: " + this.requestID);
				if(this.isHistory) {
					ApiProtocolHandler.apiLocation(getVAR("REG_ID"), String.valueOf(location.getLongitude()), String.valueOf(location.getLatitude()), location.getProvider(), timestamp, String.valueOf(location.getAltitude()), String.valueOf(location.getAccuracy()));
				} else {
					ApiProtocolHandler.apiMessageLocation(getVAR("REG_ID"), this.requestID, String.valueOf(location.getLongitude()), String.valueOf(location.getLatitude()), location.getProvider(), timestamp, String.valueOf(location.getAltitude()), String.valueOf(location.getAccuracy()));
				}
			} else {
				Logd(TAG, "No location provided by device.");
			}
			stopLocation();
		}
	}
}
