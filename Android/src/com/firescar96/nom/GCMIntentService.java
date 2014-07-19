/*
 * Copyright (c) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.firescar96.nom;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.cloud.backend.core.Consts;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateFormat;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class manages Google Cloud Messaging push notifications and CloudQuery
 * subscriptions.
 */
public class GCMIntentService extends IntentService {

    public GCMIntentService(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
    private static final String PROPERTY_REG_ID = "registration_id";

    private static final String PROPERTY_APP_VERSION = "app_version";

    public static final String BROADCAST_ON_MESSAGE = "on-message-event";

    static String SENDER_ID = "81193489522";
    
    static GoogleCloudMessaging gcm;
    static AtomicInteger msgId = new AtomicInteger();
    static MainActivity context = MainActivity.context;
    static String regId;
    
    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM will be
             * extended in the future with new message types, just ignore any message types you're
             * not interested in, or that you don't recognize.
             */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                Log.i(Consts.TAG, "onHandleIntent: message error");
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                Log.i(Consts.TAG, "onHandleIntent: message deleted");
            // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
            	// Post notification of received message.
                System.out.println("Received: " + extras.toString());
                
				try {			
	                if(extras.get("mate") != null)
	                {
						//context.appData.getJSONArray("mates").put(extras.get("mate"));
	                }
	                
	                if(extras.get("event") != null)
	                {
	                	JSONObject eveData=new JSONObject("{\"event\":"+extras.get("event")+"}").getJSONObject("event");
	                	if(eveData.getLong("date") < Calendar.getInstance().getTimeInMillis())
	                		return;
	                	
	                	String info = "Food in "+eveData.getString("hour")+":"+eveData.getString("minute")+" with "+eveData.getString("host");
	                	System.out.println(info);
						context.appData.getJSONObject("events").getJSONArray(eveData.getString("privacy")).put(eveData);
						Message msg = new Message();
						Bundle data = new Bundle();
						data.putString("type", "event."+eveData.getString("privacy"));
						data.putString("host", eveData.getString("host"));
						data.putString("hour", eveData.getString("hour"));
						data.putString("minute", eveData.getString("minute"));
					    msg.obj = data;
					    contextHandler.sendMessage(msg);
	                }
                
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        MainBroadcastReceiver.completeWakefulIntent(intent);
    }

    private Handler contextHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(((Bundle)msg.obj).getString("type").equals("event.closed"))
                {
                	int hour = Integer.parseInt(((Bundle)msg.obj).getString("hour"));
    				int minute = Integer.parseInt(((Bundle)msg.obj).getString("minute"));

    				int curHour = Integer.parseInt(DateFormat.format("HH", new Date()).toString());
    				int curMin = Integer.parseInt(DateFormat.format("mm", new Date()).toString());

    				/*if(curHour==hour && curMin==minute)
    				{
    					((JSONObject) opDat.get(i)).put("hour", "Now");
    					((JSONObject) opDat.get(i)).put("minute", "Now");
    				}*/

    				int nHour = Math.min(Math.abs(curHour-hour), Math.abs(curHour+12-hour));
    				int nMin = Math.min(Math.abs(curMin-minute), Math.abs(curMin+12-minute));
    				
                	Notify("Food in"+nHour+":"+nMin,"Eat with"+((Bundle)msg.obj).getString("host")); 
                }
                if(((Bundle)msg.obj).getString("type").equals("event.closed") || ((Bundle)msg.obj).getString("type").equals("event.open"))
                {
                	context.mainPagerAdapter.main.populateEvents();
                }
            }
        };
        
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
		private void Notify(String notificationTitle, String notificationMessage) 
        {
        	NotificationCompat.Builder mBuilder =
        	        new NotificationCompat.Builder(this)
        	        .setSmallIcon(R.drawable.ic_launcher)
        	        .setContentTitle(notificationTitle)
        	        .setContentText(notificationMessage);
        	// Creates an explicit intent for an Activity in your app
        	Intent resultIntent = new Intent(this, MainActivity.class);

        	// The stack builder object will contain an artificial back stack for the
        	// started Activity.
        	// This ensures that navigating backward from the Activity leads out of
        	// your application to the Home screen.
        	TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        	// Adds the back stack for the Intent (but not the Intent itself)
        	stackBuilder.addParentStack(MainActivity.class);
        	// Adds the Intent that starts the Activity to the top of the stack
        	stackBuilder.addNextIntent(resultIntent);
        	PendingIntent resultPendingIntent =
        	        stackBuilder.getPendingIntent(
        	            0,
        	            PendingIntent.FLAG_UPDATE_CURRENT
        	        );
        	mBuilder.setContentIntent(resultPendingIntent);
        	NotificationManager mNotificationManager =
        	    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        	// mId allows you to update the notification later on.
        	mNotificationManager.notify((int) System.currentTimeMillis(), mBuilder.build());
        	 }
    
    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    public static String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGcmPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if(regId != null)
        {
        	System.out.println("Registration found." + registrationId);
        	return regId;
        }
        if (registrationId.isEmpty()) {
        	System.out.println("Registration not found.");
            return "";
        }
        else
        	System.out.println("Registration found." + registrationId);
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            System.out.println("App version changed.");
            return "";
        }
        return registrationId;
    }
    
    /**
     * @return the stored SharedPreferences for GCM
     */
    private static SharedPreferences getGcmPreferences(Context context) {
        return context.getSharedPreferences(GCMIntentService.class.getSimpleName(), MODE_PRIVATE);
    }
    
    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    public static void registerInBackground() {
        new AsyncTask<Object, Object, Object>() {
            protected String doInBackground(Object... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regId = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regId;

                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    sendRegistrationIdToBackend(regId);

                    // For this demo: we don't need to send it because the device
                    // will send upstream messages to a server that echo back the
                    // message using the 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regId);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }

                System.out.println(msg);
                return msg;
            }

        }.execute(null, null, null);
    }
    

    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
     * or CCS to send messages to your app. Not needed for this demo since the
     * device sends upstream messages to a server that echoes back the message
     * using the 'from' address in the message.
     * @return 
     */
    private static String sendRegistrationIdToBackend(String regID) {
        String msg = "";
    	final String regId = regID;
        new AsyncTask<Object,Object,Object>() {
			@Override
			protected Object doInBackground(Object ... param) {
                String msg = "";
                InputStream inputStream = null;
                
                try {
                    // 1. create HttpClient
                    HttpClient httpclient = new DefaultHttpClient();
         
                    // 2. make POST request to the given URL
                    HttpPost httpPost = new HttpPost("http://nchinda2.mit.edu:666");
         
                    String json = "";
         
                    // 3. build jsonObject
                    JSONObject jsonObject = new JSONObject();
                    String id = Integer.toString(msgId.incrementAndGet());
                    jsonObject.accumulate("id", id);
                    jsonObject.accumulate("regId", regId);
                    jsonObject.accumulate("host", "FIRESCAR96");
         
                    // 4. convert JSONObject to JSON to String
                    json = jsonObject.toString();
         
                    // 5. set json to StringEntity
                    StringEntity se = new StringEntity(json);
         
                    // 6. set httpPost Entity
                    httpPost.setEntity(se);
         
                    // 7. Set some headers to inform server about the type of the content   
                    httpPost.setHeader("Accept", "application/json");
                    httpPost.setHeader("Content-type", "application/json");
                    
                    HttpParams httpParams = httpclient.getParams();
                    HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
                    HttpConnectionParams.setSoTimeout(httpParams, 5000);
                    httpPost.setParams(httpParams);
                    
                    // 8. Execute POST request to the given URL
                    System.out.println("executing"+json);
                    HttpResponse httpResponse = httpclient.execute(httpPost);
                    // 9. receive response as inputStream
                    inputStream = httpResponse.getEntity().getContent();
         
                    // 10. convert inputstream to string
                    if(inputStream != null)
                        msg = convertInputStreamToString(inputStream);
                    else
                        msg = "Did not work!";
         
                } catch (Exception e) {
                    e.printStackTrace();;
                }
                System.out.println(msg);
                return msg;
            }
        }.execute(null, null, null);
        System.out.println(msg);
        return msg;
    }
    
    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;
 
        inputStream.close();
        return result;
 
    } 
    
    public GCMIntentService() {
        super(Consts.PROJECT_NUMBER);
    }
    
    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private static void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        System.out.println("Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }
    
    /**
     * @return Application's {@code SharedPreferences}.
     */
    private static SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return context.getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }
    
    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
}