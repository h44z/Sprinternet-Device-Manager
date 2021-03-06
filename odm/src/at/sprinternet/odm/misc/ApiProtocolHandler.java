package at.sprinternet.odm.misc;

import static at.sprinternet.odm.misc.CommonUtilities.Logd;
import static at.sprinternet.odm.misc.CommonUtilities.changeStatusIcon;
import static at.sprinternet.odm.misc.CommonUtilities.displayMessage;
import static at.sprinternet.odm.misc.CommonUtilities.getVAR;
import static at.sprinternet.odm.misc.CommonUtilities.setVAR;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import at.sprinternet.odm.R;

public class ApiProtocolHandler {
	/**
	 * Tag used on log messages.
	 */
	static final String TAG = "ApiProtocolHandler";

	private static final String API_CONNECTOR = "connector.php";

	private static String buildRequestURL(String request) {
		return getVAR("SERVER_URL") + API_CONNECTOR + "?cmd=" + request;
	}

	@SuppressWarnings("rawtypes")
	public static JSONObject apiCall(String requestType, Map<String, String> pparams, String blobFilename) {
		String endpoint = buildRequestURL(requestType);
		String html = null;
		Boolean checkSSL = true;		
        List<PostParameter> params = new ArrayList<PostParameter>();
        
		Logd(TAG, "Starting apicall post...");
		
		checkSSL = getVAR("VALID_SSL").equals("true") ? true : false;
		
		params.add(new PostParameter<String>("username", getVAR("USERNAME")));
		params.add(new PostParameter<String>("password", getVAR("ENC_KEY")));
		
		Iterator<Entry<String, String>> iterator = pparams.entrySet().iterator();
		
		// constructs the POST body using the parameters
		while (iterator.hasNext()) {
			Entry<String, String> param = iterator.next();
			params.add(new PostParameter<String>(param.getKey(), param.getValue()));
		}
		
		if(blobFilename != null) {
			params.add(new PostParameter<File>("blob", new File(blobFilename)));
		}
		
		MultipartPost post = new MultipartPost(params);
		try {
			html = post.send(endpoint, checkSSL);
		} catch (Exception e) {
			Logd(TAG, "Posting failed: " + e.getMessage());
		}
		
		Logd(TAG, "Completed post");

		
		JSONObject json  = null;
		try {
			json = (JSONObject) new JSONParser().parse(html);
		} catch(Exception e) {
			Logd(TAG,"Jsonex: " + e.getMessage());
		}
		return json;
	}
	
	public static void apiRegister(final Context context, final String regId) {
		AsyncTask<String, Void, Void> postTask;
		
		postTask = new AsyncTask<String, Void, Void>() {
			@Override
			protected Void doInBackground(String... params) {
				String regId = params[0];
				Map<String, String> postparams = new HashMap<String, String>();
				
				Logd(TAG, "Registering device (regId = " + regId + ")");
				
				postparams.put("reg_id", regId);
				postparams.put("dev_name", getVAR("NAME"));
				
				displayMessage(context, context.getString(R.string.reg_attempting_to_register));
				JSONObject json = apiCall("register", postparams, null);
				
				if(json != null ) {
					if (((Boolean) json.get("result")) == true) {
						String token = (String) json.get("token");
						setVAR("TOKEN", token);
						SharedPreferences mPrefs = context.getSharedPreferences("usersettings", 0);
						SharedPreferences.Editor mEditor = mPrefs.edit();
						mEditor.putString("TOKEN", token).commit();
						if( ((String)json.get("message")).equals("rereg")) {
							displayMessage(context, context.getString(R.string.reg_reregistered));
							changeStatusIcon(context,R.drawable.lock_green);
						} else {
							displayMessage(context, context.getString(R.string.reg_successful));
							changeStatusIcon(context,R.drawable.lock_green);
						}
					} else {
						displayMessage(context, context.getString(R.string.reg_unsuccessful));
						changeStatusIcon(context,R.drawable.lock_red);
						Logd(TAG, "Server registration failed with: " + ((String) json.get("message")));
					}
				} else {
					displayMessage(context, context.getString(R.string.reg_failed_to_register));
					changeStatusIcon(context,R.drawable.lock_red);
					Logd(TAG, "Server registration failed, json exception");
				}
				
				return null;
			}
		};
		postTask.execute(regId, null, null);
	}
	
	public static int apiVersion() {
		Map<String, String> postparams = new HashMap<String, String>();
				
		JSONObject json = apiCall("version", postparams, null);
		
		if(json != null ) {
			if (((Boolean) json.get("result")) == true) {
				String version = json.get("apk_version").toString();
				Logd(TAG, "Response version: " + version);
				
				return Integer.parseInt(version);
			} else {
				Logd(TAG, "Server registration failed with: " + ((String) json.get("message")));
			}
		} else {
			Logd(TAG, "Version response could not be parsed or was null.");
		}
		
		return 0;
	}
	
	public static void apiMessageLocation(final String regId, String requestId, String longitude, String latitude, String locationtype, String timestamp, String altitude, String accuracy) {
		AsyncTask<String, Void, Void> postTask;
		
		postTask = new AsyncTask<String, Void, Void>() {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			protected Void doInBackground(String... params) {
				String regId = params[0];
				String requestId = params[1];
				String longitude = params[2];
				String latitude = params[3];
				String locationtype = params[4];
				String timestamp = params[5];
				String altitude = params[6];
				String accuracy = params[7];
				String type = "location"; // this is static!
				
				Map<String, String> postparams = new HashMap<String, String>();

				JSONObject json = new JSONObject();
				LinkedList datarow = new LinkedList();
				LinkedHashMap[] rows = new LinkedHashMap[6]; // we have only one datarow
				
				for(int i=0;i<rows.length;i++) {
					rows[i] = new LinkedHashMap();
				}
				
				rows[0].put("key", "longitude");
				rows[0].put("value", longitude);
				rows[1].put("key", "latitude");
				rows[1].put("value", latitude);
				rows[2].put("key", "type");
				rows[2].put("value", locationtype);
				rows[3].put("key", "timestamp");
				rows[3].put("value", timestamp);
				rows[4].put("key", "altitude");
				rows[4].put("value", altitude);
				rows[5].put("key", "accuracy");
				rows[5].put("value", accuracy);
				
				for( LinkedHashMap row: rows ) {
					datarow.add(row);
				}
				json.put("datarow", datarow);
				json.put("type", type);
				
				StringWriter out = new StringWriter();
				try {
					json.writeJSONString(out);
				} catch (IOException e) {
					Logd(TAG, "JSON string creation failed: " + e.getMessage());
				}
				String jsonText = out.toString();
				
				Logd(TAG, "Sending message (json = " + jsonText + ")");
				
				postparams.put("reg_id", regId);
				postparams.put("request_id", requestId);
				postparams.put("json_data", jsonText);
				JSONObject jsonResponse = apiCall("message", postparams, null);
				
				if(jsonResponse != null ) {
					if (((Boolean) jsonResponse.get("result")) == true) {
						Logd(TAG, "Location successfuly logged");
					} else {
						Logd(TAG, "Location logging failed with: " + ((String) jsonResponse.get("message")));
					}
				} else {
					Logd(TAG, "Location logging failed, json response exception");
				}
				
				return null;
			}
		};
		postTask.execute(regId, requestId, longitude, latitude, locationtype, timestamp, altitude, accuracy, null, null);
	}
	
	public static void apiMessageSysinfo(final String regId, String requestId, String osVersion, String osApiLevel, String device, String model, String product, String batteryLevel, String deviceID, String phoneNr, String appVersion) {
		AsyncTask<String, Void, Void> postTask;
		
		postTask = new AsyncTask<String, Void, Void>() {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			protected Void doInBackground(String... params) {
				String regId = params[0];
				String requestId = params[1];
				String osVersion = params[2];
				String osApiLevel = params[3];
				String device = params[4];
				String model = params[5];
				String product = params[6];
				String batteryLevel = params[7];
				String deviceID = params[8];
				String phoneNr = params[9];
				String appVersion = params[10];
				String type = "info"; // this is static!
				
				Map<String, String> postparams = new HashMap<String, String>();

				JSONObject json = new JSONObject();
				LinkedList datarow = new LinkedList();
				LinkedHashMap[] rows = new LinkedHashMap[9]; // we have only one datarow
				
				for(int i=0;i<rows.length;i++) {
					rows[i] = new LinkedHashMap();
				}
				
				rows[0].put("key", "version");
				rows[0].put("value", osVersion);
				rows[1].put("key", "apilevel");
				rows[1].put("value", osApiLevel);
				rows[2].put("key", "device");
				rows[2].put("value", device);
				rows[3].put("key", "model");
				rows[3].put("value", model);
				rows[4].put("key", "product");
				rows[4].put("value", product);
				rows[5].put("key", "batterylevel");
				rows[5].put("value", batteryLevel);
				rows[6].put("key", "deviceid");
				rows[6].put("value", deviceID);
				rows[7].put("key", "phonenr");
				rows[7].put("value", phoneNr);
				rows[8].put("key", "appversion");
				rows[8].put("value", appVersion);
				
				for( LinkedHashMap row: rows ) {
					datarow.add(row);
				}
				json.put("datarow", datarow);
				json.put("type", type);
				
				StringWriter out = new StringWriter();
				try {
					json.writeJSONString(out);
				} catch (IOException e) {
					Logd(TAG, "JSON string creation failed: " + e.getMessage());
				}
				String jsonText = out.toString();
				
				
				Logd(TAG, "Sending message (json = " + jsonText + ")");
				
				postparams.put("reg_id", regId);
				postparams.put("request_id", requestId);
				postparams.put("json_data", jsonText);
				JSONObject jsonResponse = apiCall("message", postparams, null);
				
				if(jsonResponse != null ) {
					if (((Boolean) jsonResponse.get("result")) == true) {
						Logd(TAG, "Sysinfo successfuly logged");
					} else {
						Logd(TAG, "Sysinfo logging failed with: " + ((String) jsonResponse.get("message")));
					}
				} else {
					Logd(TAG, "Sysinfo logging failed, json response exception");
				}
				
				return null;
			}
		};
		postTask.execute(regId, requestId, osVersion, osApiLevel, device, model, product, batteryLevel, deviceID, phoneNr, appVersion, null, null);
	}
	
	public static void apiMessageImage(final String regId, String requestId, String camera, String filePath) {
		AsyncTask<String, Void, Void> postTask;
		
		postTask = new AsyncTask<String, Void, Void>() {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			protected Void doInBackground(String... params) {
				String regId = params[0];
				String requestId = params[1];
				String camera = params[2];
				String filepath = params[3];
				String type = "photo"; // this is static!
		        
				Map<String, String> postparams = new HashMap<String, String>();

				JSONObject json = new JSONObject();
				LinkedList datarow = new LinkedList();
				LinkedHashMap[] rows = new LinkedHashMap[2]; // we have only one datarow
				
				for(int i=0;i<rows.length;i++) {
					rows[i] = new LinkedHashMap();
				}
				
				rows[0].put("key", "camera");
				rows[0].put("value", camera);
				rows[1].put("key", "photo");
				rows[1].put("blob", "image/jpeg"); 	// FIXME: THIS IS A WORKAROUND!! JSON_SIMPLE doesnt like to encode the whole file...
													// so we store blobs in a seperate post field called blob
				
				
				for( LinkedHashMap row: rows ) {
					datarow.add(row);
				}
				json.put("datarow", datarow);
				json.put("type", type);
				
				StringWriter out = new StringWriter();
				try {
					json.writeJSONString(out);
				} catch (IOException e) {
					Logd(TAG, "JSON string creation failed: " + e.getMessage());
				}
				String jsonText = out.toString();				
				
				Logd(TAG, "Sending message (json = " + jsonText + ")");
				
				postparams.put("reg_id", regId);
				postparams.put("request_id", requestId);
				postparams.put("json_data", jsonText);
				
				JSONObject jsonResponse = apiCall("message", postparams, filepath);
				
				if(jsonResponse != null ) {
					if (((Boolean) jsonResponse.get("result")) == true) {
						Logd(TAG, "Image successfuly logged");
					} else {
						Logd(TAG, "Image logging failed with: " + ((String) jsonResponse.get("message")));
					}
				} else {
					Logd(TAG, "Image logging failed, json response exception");
				}
				
				return null;
			}
		};
		
		postTask.execute(regId, requestId, camera, filePath, null, null);
	}
	
	public static void apiMessageAudio(final String regId, String requestId, String length, String filePath) {
		AsyncTask<String, Void, Void> postTask;
		
		postTask = new AsyncTask<String, Void, Void>() {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			protected Void doInBackground(String... params) {
				String regId = params[0];
				String requestId = params[1];
				String length = params[2];
				String filepath = params[3];
				String type = "audio"; // this is static!
		        
				Map<String, String> postparams = new HashMap<String, String>();

				JSONObject json = new JSONObject();
				LinkedList datarow = new LinkedList();
				LinkedHashMap[] rows = new LinkedHashMap[2]; // we have only one datarow
				
				for(int i=0;i<rows.length;i++) {
					rows[i] = new LinkedHashMap();
				}
				
				rows[0].put("key", "length");
				rows[0].put("value", length);
				rows[1].put("key", "audio");
				rows[1].put("blob", "audio/3gpp"); 	// FIXME: THIS IS A WORKAROUND!! JSON_SIMPLE doesnt like to encode the whole file...
													// so we store blobs in a seperate post field called blob
				
				
				for( LinkedHashMap row: rows ) {
					datarow.add(row);
				}
				json.put("datarow", datarow);
				json.put("type", type);
				
				StringWriter out = new StringWriter();
				try {
					json.writeJSONString(out);
				} catch (IOException e) {
					Logd(TAG, "JSON string creation failed: " + e.getMessage());
				}
				String jsonText = out.toString();				
				
				Logd(TAG, "Sending message (json = " + jsonText + ")");
				
				postparams.put("reg_id", regId);
				postparams.put("request_id", requestId);
				postparams.put("json_data", jsonText);
				
				JSONObject jsonResponse = apiCall("message", postparams, filepath);
				
				if(jsonResponse != null ) {
					if (((Boolean) jsonResponse.get("result")) == true) {
						Logd(TAG, "AudioCapture successfuly logged");
					} else {
						Logd(TAG, "AudioCapture logging failed with: " + ((String) jsonResponse.get("message")));
					}
				} else {
					Logd(TAG, "AudioCapture logging failed, json response exception");
				}
				
				return null;
			}
		};
		
		postTask.execute(regId, requestId, length, filePath, null, null);
	}
	
	public static void apiLocation(final String regId, String longitude, String latitude, String locationtype, String timestamp, String altitude, String accuracy) {
		AsyncTask<String, Void, Void> postTask;
		
		postTask = new AsyncTask<String, Void, Void>() {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			protected Void doInBackground(String... params) {
				String regId = params[0];
				String longitude = params[1];
				String latitude = params[2];
				String locationtype = params[3];
				String timestamp = params[4];
				String altitude = params[5];
				String accuracy = params[6];
				
				Map<String, String> postparams = new HashMap<String, String>();

				JSONObject json = new JSONObject();
				LinkedList datarow = new LinkedList();
				LinkedHashMap[] rows = new LinkedHashMap[1]; // we have only one datarow
				
				for(int i=0;i<rows.length;i++) {
					rows[i] = new LinkedHashMap();
				}
				
				rows[0].put("longitude", longitude);
				rows[0].put("latitude", latitude);
				rows[0].put("type", locationtype);
				rows[0].put("timestamp", timestamp);
				rows[0].put("altitude", altitude);
				rows[0].put("accuracy", accuracy);
				
				for( LinkedHashMap row: rows ) {
					datarow.add(row);
				}
				json.put("datarow", datarow);
				
				StringWriter out = new StringWriter();
				try {
					json.writeJSONString(out);
				} catch (IOException e) {
					Logd(TAG, "JSON string creation failed: " + e.getMessage());
				}
				String jsonText = out.toString();
				
				Logd(TAG, "Sending message (json = " + jsonText + ")");
				
				postparams.put("reg_id", regId);
				postparams.put("json_data", jsonText);
				JSONObject jsonResponse = apiCall("location", postparams, null);
				
				if(jsonResponse != null ) {
					if (((Boolean) jsonResponse.get("result")) == true) {
						Logd(TAG, "Location history successfuly logged");
					} else {
						Logd(TAG, "Location history logging failed with: " + ((String) jsonResponse.get("message")));
					}
				} else {
					Logd(TAG, "Location history logging failed, json response exception");
				}
				
				return null;
			}
		};
		postTask.execute(regId, longitude, latitude, locationtype, timestamp, altitude, accuracy, null, null);
	}
}
