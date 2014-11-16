package com.firescar96.nom;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.firescar96.nom.fragment.EventDetailFragment;
import com.firescar96.nom.org.json.JSONArray;
import com.firescar96.nom.org.json.JSONObject;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class MainActivity extends Activity{

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link FragmentPagerAdapter} derivative, which will keep every
	 * loaded fragment in memory. If this becomes too memory intensive, it
	 * may be best to switch to a
	 * {@link android.support.v13.app.FragmentStatePagerAdapter}.
	 */
	MainPagerAdapter mainPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	MainViewPager mViewPager;

	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

	public static MainActivity context;
	public static JSONObject appData;
	public boolean isForeground;
	
	GoogleCloudMessaging gcm;
	AtomicInteger msgId = new AtomicInteger();
	SharedPreferences prefs;
	LocationServices locServices;
	String regid;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		context = this;
		
		initAppData(getFilesDir().getAbsolutePath());
		
		//Check device for Play Services APK. If check succeeds, proceed with
		//GCM registration.
		if (checkPlayServices()) {
			Log.i("MainActivity", "we have the google play");
			System.out.println("we have the google play");
			gcm = GoogleCloudMessaging.getInstance(this);
			regid = GCMIntentService.getRegistrationId(this);
			System.err.println("need new register");
			if (regid.isEmpty())
				GCMIntentService.registerInBackground();
		} else
			System.out.println("No valid Google Play Services APK found.");
		
		if(getIntent().getDataString() != null)
			if(getIntent().getDataString().contains("nchinda2.mit.edu:666"))
	    	{				
	    		final String data = getIntent().getDataString().substring(28);
	    		if(data.startsWith("e"))
	    		{
	    			System.out.println("event sent "+data.substring(1));
	    			new AsyncTask<Object, Object, Object>() {
	    				@Override
	    				protected Object doInBackground(Object... arg0) {
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
	    						jsonObject.accumulate("to", MainActivity.appData.getString("host"));
	    						jsonObject.accumulate("hash", data.substring(1));

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
	    							msg = "it worked";
	    						else
	    							msg = "Did not work!";

	    					} catch (Exception e) {
	    						e.printStackTrace();
	    					}
	    					System.out.println(msg);
	    					return msg;
	    				}
	    			}.execute(null, null, null);
	    		}    
	    	}
		
			Bundle data = getIntent().getExtras();
			if(data != null)
				if(data.getString("sender").equals("chat"))
					try {
					JSONArray eve = appData.getJSONArray("events");
					for(int i =0; i < eve.length(); i++) {
						JSONObject curEve = eve.getJSONObject(i);
						if(curEve.getString("hash").equals(data.get("hash"))) {
							FragmentManager fragmentManager = getFragmentManager();
							EventDetailFragment detailFrag = new EventDetailFragment();
							detailFrag.setArguments(data);
							detailFrag.show(fragmentManager, "dialog");
							break;
						}
					}
				} catch (JSONException e) {e.printStackTrace();}
		
		locServices = new LocationServices();

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mainPagerAdapter = new MainPagerAdapter(getFragmentManager());
	
		 System.out.println("init main pager"+mainPagerAdapter);
		// Set up the ViewPager with the sections adapter.
		mViewPager = (MainViewPager) findViewById(R.id.main_activity);
		mViewPager.setAdapter(mainPagerAdapter);
		mViewPager.setOffscreenPageLimit(2);
		
		isForeground = true;
	}

	public static void initAppData(String fileDir)
	{
		File defFile = new File(fileDir+"/appData.txt");
		if(!defFile.exists())
			try {
				new PrintWriter(new FileWriter(defFile.getAbsolutePath()));
			}catch(IOException e) {}

		try {
			BufferedReader br = new BufferedReader(new FileReader(defFile));
			String line;
			StringBuilder datBuf = new StringBuilder();
			while ((line = br.readLine()) != null) {
				datBuf.append(line);
				datBuf.append('\n');
			}
			br.close();
			appData = new JSONObject(datBuf.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("recreating appdata");
			JSONArray eve = new JSONArray();
			JSONArray mate = new JSONArray();
			String usr = new String();
			appData = new JSONObject();
			try {
				appData.put("events", eve);
				appData.put("mates", mate);
				appData.put("host", usr);
			} catch (Exception e1) {}
		}
	}
	
	public static void closeAppData(String fileDir)
	{
		File defFile = new File(fileDir+"/appData.txt");
		PrintWriter out;
		 try {
			 out = new PrintWriter(new FileWriter(defFile.getAbsolutePath()));
			 out.println(appData.toString());
			 //out.println("");	//uncomment to reset the database
			 out.close();
		 }catch(IOException e) {}
	}
	
	@Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        locServices.connect();
    }

	// Play Services APK check here too.
	@Override
	protected void onResume() {
		super.onResume();
		checkPlayServices();

		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if(cm.getActiveNetworkInfo() == null)
			Toast.makeText(context, "No Internet connection detected, Nom entering offline mode", Toast.LENGTH_SHORT).show();
		
		isForeground = true;
	}   
/*
	public void onConfigurationChanged() {
		mainPagerAdapter.getMain()
	}*/
	
	 private boolean checkPlayServices() {
		 System.out.println("checking play services");
		 int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		 if (resultCode != ConnectionResult.SUCCESS) {
			 if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						 PLAY_SERVICES_RESOLUTION_REQUEST).show();
			else {
				 System.out.println("This device is not supported.");
				 finish();
			 }
			 return false;
		 }
		 return true;
	 }

	 public static void hideSoftKeyboard(Activity activity) {
		    InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
		    inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
		}
	 
	 public static void setupUI(View view) {

		    //Set up touch listener for non-text box views to hide keyboard.
		    if(!(view instanceof EditText))
				view.setOnTouchListener(new OnTouchListener() {

		            @Override
					public boolean onTouch(View v, MotionEvent event) {
		                hideSoftKeyboard(context);
		                return false;
		            }

		        });

		    //If a layout container, iterate over children and seed recursion.
		    if (view instanceof ViewGroup)
				for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
		            View innerView = ((ViewGroup) view).getChildAt(i);
		            setupUI(innerView);
		        }
		}
	 
	 public static void sendJSONToBackend(final JSONObject jsonObject)
		{
			new AsyncTask<Object, Object, Object>() {
				@Override
				protected Object doInBackground(Object... arg0) {
					if (Looper.myLooper() == null)
						Looper.prepare();
					String msg = "";
					InputStream inputStream = null;

					try {

						// 1. create HttpClient
						HttpClient httpclient = new DefaultHttpClient();

						// 2. make POST request to the given URL
						HttpPost httpPost = new HttpPost("http://nchinda2.mit.edu:666");

						String json = "";

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
							msg = "it worked";
						else
							msg = "Did not work!";

					} catch (Exception e) {
						e.printStackTrace();
					}
					System.out.println(msg);
					return msg;
				}
			}.execute(null, null, null);
		}
	 
	 public void onPrivacySelect(View v)
	 {
		 try {
			 System.out.println(appData.getString("host"));
			if(appData.getString("host").length() == 0)
				 {
					System.out.println(mainPagerAdapter.getMain());
				 	mainPagerAdapter.getMain().requestHostname();
				 	return;
				 }
		} catch (Exception e) {
			e.printStackTrace();
		}
		 
		 v.setSelected(true);

		 if(v.getId() == R.id.open_button)
		 {
			 System.out.println(mainPagerAdapter);
			 System.out.println(mainPagerAdapter.getMain());
			 
			 findViewById(R.id.closed_button).setSelected(false);
			 mainPagerAdapter.getMain().privacy = false;
			 mainPagerAdapter.setCount(2);
		 }
		 else
		 {
			 System.out.println("close");
			 findViewById(R.id.open_button).setSelected(false);
			 mainPagerAdapter.getMain().privacy = true;
			 mainPagerAdapter.setCount(3);
		 }

		 mainPagerAdapter.updateView(1);
		 mainPagerAdapter.updateView(2);
		 mViewPager.setCurrentItem(1);
	 }

	 public void checkHostname(View v)
	 {
		 if(mViewPager.getCurrentItem() == 0)
			 mainPagerAdapter.getMain().checkName();
		 if(mViewPager.getCurrentItem() == 2)
			 mainPagerAdapter.getClosed2().checkName();
	 }
	 
	 public void onShareClick(View v) 
	 {
		 mViewPager.setPagingEnabled(true);
		 if(v.equals(findViewById(R.id.openShare)))
			mainPagerAdapter.getOpen().openShare(v);
		 else if(v.equals(findViewById(R.id.closeShare)))
			 mainPagerAdapter.getClosed2().closeShare(v);
		 else
			 mainPagerAdapter.getClosed2().mediaShare(v);
		 
		 mViewPager.setCurrentItem(0);
	 } 

	 public void addNommate(View v)
	 {
		 mainPagerAdapter.getClosed2().addNommate(v);
	 }
	    
	 public void updateNommates()
	 {
		 mainPagerAdapter.getClosed2().populateUsers();
	 }
	 
	 public void onEventMembershipChanged(View v)
	 {
		mainPagerAdapter.getMain().onEventMembershipChanged(); 
	 }
	 
	 public void onChatMsg(View v)
	 {
		 mainPagerAdapter.getMain().onChatMsg();
	 }
	 
	 public boolean getForeground() {
		return isForeground;
	 }
	 
	 @Override
	protected void onPause()
	 {
		 super.onPause();
		 closeAppData(getFilesDir().getAbsolutePath());
		 isForeground = false;
	 }
	 
	 @Override
	protected void onStop()
	 {
		 super.onStop();
		closeAppData(getFilesDir().getAbsolutePath());
		 
		 locServices.disconnect();
	 }
}
