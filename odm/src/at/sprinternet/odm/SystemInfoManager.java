package at.sprinternet.odm;

import static at.sprinternet.odm.misc.CommonUtilities.Logd;
import static at.sprinternet.odm.misc.CommonUtilities.getVAR;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.BatteryManager;
import android.telephony.TelephonyManager;
import at.sprinternet.odm.misc.ApiProtocolHandler;

public class SystemInfoManager {
	private static final String TAG = "SystemInfoManager";
	
	private Context context;

	private String osVersion = "";
	private int osApiLevel = 0;
	private String device = "";
	private String model = "";
	private String product = "";
	private int batteryLevel = 0;
	private String deviceID = "";
	private String phoneNr = "";
	private String appVersion = "";
	
	public SystemInfoManager(Context ctx) {
		this.context = ctx;
	}

	// update battery stats if changed
	private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context ctxt, Intent intent) {
			int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
			Logd(TAG, "Received battery level: " + level);
			batteryLevel = level;
		}
	};

	private int getBatteryLevel() {
		Intent batteryIntent = context.registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

		return batteryIntent.getIntExtra("level", -1);
	}

	public void initSystemInfo() {
		osVersion = System.getProperty("os.version");
		osApiLevel = android.os.Build.VERSION.SDK_INT;
		device = android.os.Build.DEVICE;
		model = android.os.Build.MODEL;
		product = android.os.Build.PRODUCT;
		batteryLevel = getBatteryLevel();
		
		try {
			PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			appVersion = pInfo.versionName;
		} catch (NameNotFoundException e) {
			Logd(TAG, "App version exception: " + e.getMessage());
		}
		
		
		// do we need this? if not - comment the
		// android.permission.READ_PHONE_STATE in the manifest
		try {
			TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			deviceID = telephonyManager.getDeviceId();
			phoneNr = telephonyManager.getLine1Number();
		} catch(Exception e) {
			Logd(TAG, "Device ID exception: " + e.getMessage());
			deviceID = "unknown";
			phoneNr = "unknown";
		}
	}

	public void sendSystemInfo(String requestID) {
		Logd(TAG, "Sending system info to server.");
		
		ApiProtocolHandler.apiMessageSysinfo(getVAR("REG_ID"),requestID, osVersion, Integer.toString(osApiLevel), device, model, product, Integer.toString(batteryLevel), deviceID, phoneNr, appVersion);
	}
}
