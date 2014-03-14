package at.sprinternet.odm;

import static at.sprinternet.odm.misc.CommonUtilities.Logd;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import at.sprinternet.odm.activitys.StartupActivity;
import at.sprinternet.odm.misc.CommonUtilities;

public class LaunchReceiver extends BroadcastReceiver {
	private static final String TAG = "LaunchReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		String pin = CommonUtilities.getVAR("PIN");
		String code = getResultData();
		if (code == null)
			code = intent.getStringExtra("android.intent.extra.PHONE_NUMBER");
		if (code.equals("#*##" + pin)) {
			Logd(TAG, "Launching.");
			setResultData(null);
			Intent i = new Intent(context, StartupActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i);
		}
	}
}