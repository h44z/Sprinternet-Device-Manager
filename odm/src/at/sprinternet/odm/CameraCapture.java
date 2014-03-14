package at.sprinternet.odm;

import static at.sprinternet.odm.misc.CommonUtilities.Logd;
import static at.sprinternet.odm.misc.CommonUtilities.getVAR;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import at.sprinternet.odm.R;
import at.sprinternet.odm.misc.ApiProtocolHandler;

import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Size;
import android.os.Handler;
import android.view.Display;
import android.view.Gravity;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

public class CameraCapture implements SurfaceHolder.Callback {
	public CameraService cs;
	public View v;
	SurfaceHolder sh;
	SurfaceHolder sh_created;
	SurfaceView sv;
	int cameraInt = 0;
	Camera c;
	Camera.PictureCallback mCall;
	Camera.Parameters params;
	Camera.PictureCallback callback;
	Display display;
	Boolean max = false;
	Boolean focused = false;
	int focusTimeout = 10; // in seconds
	long focusStart = 0;

	private static final String TAG = "CameraCapture";

	public CameraCapture() {
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		if (c != null) {
			params = c.getParameters();
			params.set("orientation", "portrait");
			c.setParameters(params);
			c.startPreview();
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		sh_created = holder;
		startPreview();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (this.c != null) {
			this.c.stopPreview();
			this.c.release();
			this.c = null;
		}
	}
	
	public void setMax(Boolean m) {
		max = m;
	}

	public void setCamera(int inCameraInt) {
		cameraInt = inCameraInt;
	}

	public void setDisplay(Display inDisplay) {
		display = inDisplay;
	}

	public void setServiceContainer(CameraService inCameraService) {
		cs = inCameraService;
	}

	public void startWindowManager() {
		Logd(TAG, "Starting up the window manager...");
		if (v != null) {
			((WindowManager) cs.getSystemService("window")).removeView(v);
			v = null;
		}
		WindowManager wm = (WindowManager) cs.getSystemService("window");
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams(1, 1, WindowManager.LayoutParams.TYPE_PRIORITY_PHONE, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT); //width, height, _type (2007), _flags (32), _format (-3)
		lp.y = 0;
		lp.x = 0;
		lp.gravity = Gravity.LEFT;
		try {
			v = ((LayoutInflater) cs.getSystemService("layout_inflater")).inflate(R.layout.camera, null);
		} catch (InflateException e) {
			Logd(TAG, "Error: " + e.getLocalizedMessage());
		}
		v.setVisibility(0);
		wm.addView(v, lp);
		sv = ((SurfaceView) v.findViewById(R.id.surfaceView));
		sh = sv.getHolder();
		sh.addCallback(this);
	}

	public void stopCapture() {
		Logd(TAG, "Stopping capture.");
		if (c != null) {
			c.stopPreview();
			c.release();
			c = null;
		}
		if (v != null) {
			((WindowManager) this.cs.getSystemService("window")).removeView(v);
			v = null;
		}
	}

	void startPreview() {
		Logd(TAG, "Starting CC preview...");
		if (c != null) {
			c.stopPreview();
			c.release();
			c = null;
		}
		try {
			c = Camera.open(cameraInt);
			if (c == null) {
				Logd(TAG, "Error opening camera.");
				return;
			}
		} catch (RuntimeException e) {
			Logd(TAG, "Error: " + e.getLocalizedMessage());
		}
		while (true) {
			try {
				if (max) {
					params = c.getParameters();
					List<Size> sl = params.getSupportedPictureSizes();
					int w = 0;
					int h = 0;
					for (Size s : sl) {
						if (s.width > w) {
							w = s.width;
							h = s.height;
						}
					}
					params.setPictureSize(w, h);
					c.setParameters(params);
				}
				c.setPreviewDisplay(sh_created);
				c.startPreview();
				return;
			} catch (Exception localException) {
				c.release();
				c = null;
				return;
			}
		}
	}
	
	public void waitForFocus() {
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				long curTime = System.currentTimeMillis();
				if (focused == true || (curTime - focusStart >= (focusTimeout*1000))) { 
					try {
						Logd(TAG, "Taking picture...");
						c.takePicture(null, null, callback);
					} catch (RuntimeException e) {
						Logd(TAG, e.getMessage());
					}
				} else {
					waitForFocus();
				}
			}
		}, 2000);
	}

	public void captureImage(final String messageId) {
		try {
			Logd(TAG, "Starting captureImage...");
			params = c.getParameters();
			c.setParameters(params);
			Logd(TAG, "Starting preview...");
			c.startPreview();
			callback = new CustomPictureCallback(messageId);
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					try {						
						Logd(TAG, "Focusing...");
						//c.takePicture(null, null, callback);
						focusStart = System.currentTimeMillis();
						c.autoFocus(afc);
						waitForFocus();
					} catch (RuntimeException e) {
						Logd(TAG, e.getMessage());
					}
				}
			}, 2000);
		} catch (Exception e) {
			Logd(TAG, "Error: " + e.getMessage());
			if (c != null) {
				c.stopPreview();
				c.release();
				c = null;
			}
		}
	}
	
	class CustomPictureCallback implements Camera.PictureCallback {
		
		String requestID = null;
		
		public CustomPictureCallback(String messageID) {
			this.requestID = messageID;
		}

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			Logd(TAG, "Image captured.");
			// TODO: is this correctly implemented?
			String cam = "rear";
			if(cameraInt == 1)
				cam = "front";
			
			Logd(TAG, "Trying to send pic for requestId: " + this.requestID);
			Logd(TAG, "Image size: " + data.length + " bytes");
			
			String imagePath = "";
			File outputDir = OdmApplication.getAppContext().getCacheDir(); // context being the Activity pointer
			File outputFile;
			try {
				outputFile = File.createTempFile("prefix", ".jpg.tmp", outputDir);
				FileOutputStream fos = new FileOutputStream(outputFile);
				fos.write(data);
				fos.close();
				imagePath = outputFile.getAbsolutePath();
			} catch (IOException e) {
				Logd(TAG, "Image write failed: " + e.getMessage());
			}
			
			ApiProtocolHandler.apiMessageImage(getVAR("REG_ID"), this.requestID, cam, imagePath);
			cs.stopCamera();
			
			stopCapture();
		}
	}
	
	final AutoFocusCallback afc = new AutoFocusCallback() {
		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					try {
						Logd(TAG, "Got focus.");
						focused = true;
					} catch (RuntimeException e) {
						Logd(TAG, e.getMessage());
					}
				}
			}, 2000);
		}
	};
}
