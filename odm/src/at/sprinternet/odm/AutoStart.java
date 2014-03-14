package at.sprinternet.odm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import at.sprinternet.odm.misc.CommonUtilities;

public class AutoStart extends BroadcastReceiver {
	private static final String TAG= "AutoStart";
	UpdateAlarm updateAlarm = new UpdateAlarm();
	LocationAlarm locationAlarm = new LocationAlarm();

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
			String interval = CommonUtilities.getVAR("LOC_INTERVAL");
			String version = CommonUtilities.getVAR("VERSION");
			if (!interval.equals("0")) {
				CommonUtilities.Logd(TAG, "Good to start location alarm.");
				locationAlarm.SetInterval(interval);
				locationAlarm.SetAlarm(context);
			}
			if (version.equals("true")) {
				CommonUtilities.Logd(TAG, "Good to start update alarm.");
				updateAlarm.SetAlarm(context);
			}
		}
	}
}
