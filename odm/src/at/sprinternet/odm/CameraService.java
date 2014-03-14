package at.sprinternet.odm;

import static at.sprinternet.odm.misc.CommonUtilities.Logd;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.view.Display;
import android.view.WindowManager;

public class CameraService extends Service {
	private static final String TAG = "CameraService";

	int cameraInt = 0;
	Context context;
	Boolean max = false;
	int volume = 0;
	AudioManager am;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Logd(TAG, "Starting camera service...");
		String message = intent.getStringExtra("message");
		final String requestid = intent.getStringExtra("requestid");
		context = getApplicationContext();
		if (message.equals("Command:FrontPhoto") || message.equals("Command:FrontPhotoMAX"))
			cameraInt = 1;
		if (message.equals("Command:RearPhotoMAX") || message.equals("Command:FrontPhotoMAX"))
			max = true;
		
		Handler handler = new Handler();
		
		class CustomStarterRunnable implements Runnable {
			String messageid;
			
			public CustomStarterRunnable(String messageid) {
				this.messageid = messageid;
			}
			
			@Override
			public void run() {
				captureImage(this.messageid);
				
			}
		}
		
		handler.postDelayed(new CustomStarterRunnable(requestid), 2000);
		am = (AudioManager) getApplicationContext().getSystemService("audio");
		volume = am.getStreamVolume(1);
		Logd(TAG, "Current volume: " + volume);
		am.setStreamVolume(1, 0, 0); // set volume to zero
		
		return START_STICKY;
	}

	private void captureImage(String requestid) {
		Logd(TAG, "About to capture image...");
		final CameraCapture cc = new CameraCapture();
		cc.setMax(max);
		Logd(TAG, "Setting camera...");
		cc.setCamera(cameraInt);
	    WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE); 
	    Display display = window.getDefaultDisplay();
		Logd(TAG, "Setting display...");
		cc.setDisplay(display);
		Logd(TAG, "Creating container...");
		cc.setServiceContainer(this);
		Logd(TAG, "Starting window manager...");
		cc.startWindowManager();
		Handler handler = new Handler();
		
		class CustomCaptureRunnable implements Runnable {
			String messageid;
			
			public CustomCaptureRunnable(String messageid) {
				this.messageid = messageid;
			}
			
			@Override
			public void run() {
				Logd(TAG, "Capturing image...");
				cc.captureImage(this.messageid);
			}
		}
		
		handler.postDelayed(new CustomCaptureRunnable(requestid), 2000);
	}
	
	public void stopCamera() {
		Logd(TAG, "Stopping camera service.");
		am.setStreamVolume(1, volume, 0); // set volume back to previous value
		this.stopSelf();
	}
}
