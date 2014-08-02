package com.firescar96.nom;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v13.app.FragmentPagerAdapter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Toast;

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
		
		if(getIntent().getDataString() != null)
    	{	
			if(getIntent().getDataString() != null && getIntent().getDataString().contains("nchinda2.mit.edu:666"))
	    	{
				//Check device for Play Services APK. If check succeeds, proceed with
				//GCM registration.
				if (checkPlayServices()) {
					gcm = GoogleCloudMessaging.getInstance(this);
					regid = GCMIntentService.getRegistrationId(this);

					if (regid.isEmpty()) {
						GCMIntentService.registerInBackground();
					}
				} else {
					System.out.println("No valid Google Play Services APK found.");
				}
				
	    		String nommate = getIntent().getDataString().substring(28);
	        	try {
					appData.getJSONArray("mates").put(nommate);
				} catch (JSONException e) {}
	    	}
    	}
		
		locServices = new LocationServices();
		
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mainPagerAdapter = new MainPagerAdapter(getFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (MainViewPager) findViewById(R.id.main_activity);
		mViewPager.setAdapter(mainPagerAdapter);
		mViewPager.setOffscreenPageLimit(2);
	}

	public static void initAppData(String fileDir)
	{
		File defFile = new File(fileDir+"/appData.txt");
		if(!defFile.exists())
		{
			try {
				new PrintWriter(new FileWriter(defFile.getAbsolutePath()));
			}catch(IOException e) {}
		}

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
		} catch (JSONException e) {
			System.out.println("recreating appdata");
			JSONObject eve = new JSONObject();
			JSONArray mate = new JSONArray();
			String usr = new String();
			JSONArray open = new JSONArray();
			JSONArray cloe = new JSONArray();
			appData = new JSONObject();
			try {
				eve.put("open", open);
				eve.put("closed", cloe);
				appData.put("events", eve);
				appData.put("mates", mate);
				appData.put("host", usr);
			} catch (JSONException e1) {}
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
			Toast.makeText(context, "No Internet connection detected, Nom entering offline mode", Toast.LENGTH_SHORT).show();;
	}   

	 private boolean checkPlayServices() {
		 int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		 if (resultCode != ConnectionResult.SUCCESS) {
			 if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				 GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						 PLAY_SERVICES_RESOLUTION_REQUEST).show();
			 } else {
				 System.out.println("This device is not supported.");
				 finish();
			 }
			 return false;
		 }
		 return true;
	 }

	 public void onPrivacySelect(View v)
	 {
		 try {
			 System.out.println(appData.getString("host"));
			if(appData.getString("host").length() == 0)
				 {
					System.out.println(mainPagerAdapter.main);
				 	mainPagerAdapter.main.requestHostname();
				 	return;
				 }
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		 v.setSelected(true);

		 if(v.getId() == R.id.open_button)
		 {
			 System.out.println("open");
			 findViewById(R.id.closed_button).setSelected(false);
			 mainPagerAdapter.main.privacy = false;
			 mainPagerAdapter.setCount(2);
		 }
		 else
		 {
			 System.out.println("close");
			 findViewById(R.id.open_button).setSelected(false);
			 mainPagerAdapter.main.privacy = true;
			 mainPagerAdapter.setCount(3);
		 }

		 mainPagerAdapter.oldFragments.add(1);
		 mainPagerAdapter.oldFragments.add(2);
		 mainPagerAdapter.notifyDataSetChanged();
	 }

	 public void checkHostname(View v)
	 {
		 mainPagerAdapter.main.checkHostname(v);
	 }
	 
	 public void onShareClick(View v) 
	 {
		 mViewPager.setPagingEnabled(true);
		 if(v.equals(findViewById(R.id.openShare)))
		 {
			mainPagerAdapter.open.openShare(v);
			//mainPagerAdapter.setCount(1);
			//mainPagerAdapter.notifyDataSetChanged();
		 }
		 else if(v.equals(findViewById(R.id.closeShare)))
		 {
			 mainPagerAdapter.closed2.closeShare(v);
			 //mainPagerAdapter.setCount(1);
			 //mainPagerAdapter.notifyDataSetChanged();
		 }
		 else
		 {
			 Intent shareIntent = new Intent(Intent.ACTION_SEND);
			 shareIntent.setType("text/plain");
			 shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Add me on Nom so we can schedule a meetup: http://nchinda2.mit.edu:666/FIRESCAR96");
			 startActivity(Intent.createChooser(shareIntent, "Share via"));
		 }
	 } 

	 public void addNommate(View v)
	 {
		 mainPagerAdapter.closed2.addNommate(v);
	 }
	    
	 public void onEventMembershipChanged(View v)
	 {
		mainPagerAdapter.main.detailFrag.onEventMembershipChanged(v); 
	 }
	 
	 public void onChatMsg(View v)
	 {
		 mainPagerAdapter.main.detailFrag.onChatMsg(v);
	 }
	 
	 protected void onStop()
	 {
		 super.onStop();
		closeAppData(getFilesDir().getAbsolutePath());
		 
		 locServices.disconnect();
	 }
}
