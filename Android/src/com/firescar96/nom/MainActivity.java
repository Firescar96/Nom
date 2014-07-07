package com.firescar96.nom;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TimePicker;

public class MainActivity extends Activity{

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    MainPagerAdapter eventsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    MainViewPager mViewPager;

    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    
    protected static MainActivity context;
    
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    SharedPreferences prefs;
    String regid;
    
    JSONObject appData;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        
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
        
        File defFile = new File(getFilesDir().getAbsolutePath()+"/appData.txt");
        if(!defFile.exists())
        {
        	PrintWriter out;
			try {
				out = new PrintWriter(new FileWriter(defFile.getAbsolutePath()));
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
			JSONObject usr = new JSONObject();
			JSONArray open = new JSONArray();
			JSONArray cloe = new JSONArray();
			appData = new JSONObject();
			try {
				eve.put("open", open);
				eve.put("closed", cloe);
				appData.put("events", eve);
				appData.put("username", usr);
			} catch (JSONException e1) {}
		}
		//System.out.println(appData);
		// Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        eventsPagerAdapter = new MainPagerAdapter(getFragmentManager());
        
        // Set up the ViewPager with the sections adapter.
        mViewPager = (MainViewPager) findViewById(R.id.main_activity);
        mViewPager.setAdapter(eventsPagerAdapter);
        mViewPager.setOffscreenPageLimit(1);
        
		AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, MainActivity.class);
		intent.setAction("com.firescar96.nom.update.times");
		PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

		alarmMgr.setRepeating (AlarmManager.RTC, /*((int)System.currentTimeMillis()/60000)*60000*/System.currentTimeMillis(), 60000, alarmIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    // Play Services APK check here too.
    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) 
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void populateEvents(View view) //TODO:Restructure appData to list publicity as child of events not sister
    {
    	try {
			JSONArray opDat = appData.getJSONObject("events").getJSONArray("open");
			JSONArray cloDat = appData.getJSONObject("events").getJSONArray("closed");
			
	
	    	//Setup the listview adapter for open events
			ListView opView = (ListView) view.findViewById(R.id.open_events);
			ArrayList<String> opList = new ArrayList<String>();
			//Setup the listview adapter for closed events
			ListView cloView = (ListView) view.findViewById(R.id.closed_events);
			ArrayList<String> cloList = new ArrayList<String>();
			
			for(int i=0; i< opDat.length(); i++)
			{System.out.println(opDat.get(i));
				if(((JSONObject) opDat.get(i)).getString("hour").equals("Now"))
				{
					JSONArrayremove(opDat,i);
					continue;
				}
				
				int hour = Integer.parseInt(((JSONObject) opDat.get(i)).getString("hour"));
				int minute = Integer.parseInt(((JSONObject) opDat.get(i)).getString("minute"));
				
				int curHour = Integer.parseInt(DateFormat.format("HH", new Date()).toString());
				int curMin = Integer.parseInt(DateFormat.format("mm", new Date()).toString());
				
				/*if(curHour < hour)
					if(curMin < 59)
						curMin++;
					else
					{
						curMin = 0;
						curHour++;
					}
				else
					curMin++;*/
				
				if(curHour==hour && curMin==minute)
				{
					((JSONObject) opDat.get(i)).put("hour", "Now");
					((JSONObject) opDat.get(i)).put("minute", "Now");
				}

				int nHour = Math.min(Math.abs(curHour-hour), Math.abs(curHour+12-hour));
				int nMin = Math.min(Math.abs(curMin-minute), Math.abs(curMin+12-minute));
				String info = "Food in "+nHour+":"+nMin+" with "+((JSONObject) opDat.get(i)).getString("host");
				opList.add(info);
			}
			
			for(int i=0; i< cloDat.length(); i++)
			{
				if(((JSONObject) cloDat.get(i)).getString("hour").equals("Now"))
				{
					JSONArrayremove(opDat,i);
					continue;
				}
				
				int hour = Integer.parseInt(((JSONObject) cloDat.get(i)).getString("hour"));
				int minute = Integer.parseInt(((JSONObject) cloDat.get(i)).getString("minute"));
				
				int curHour = Integer.parseInt(DateFormat.format("HH", new Date()).toString());
				int curMin = Integer.parseInt(DateFormat.format("mm", new Date()).toString());
				
				/*if(curHour < hour)
				if(curMin < 59)
					curMin++;
				else
				{
					curMin = 0;
					curHour++;
				}
			else
				curMin++;*/
			
			if(curHour==hour && curMin==minute)
			{
				((JSONObject) cloDat.get(i)).put("hour", "Now");
				((JSONObject) cloDat.get(i)).put("minute", "Now");
			}
			
			int nHour = Math.min(Math.abs(curHour-hour), Math.abs(curHour+12-hour));
			int nMin = Math.min(Math.abs(curMin-minute), Math.abs(curMin+12-minute));
			String info = "Food in "+nHour+":"+nMin+" with "+((JSONObject) cloDat.get(i)).getString("host");
				cloList.add(info);
			}
			
			EventsArrayAdapter opAdapter = new EventsArrayAdapter(context,
			    android.R.layout.simple_list_item_1, opList);
			opView.setAdapter(opAdapter);
	    	eventsPagerAdapter.notifyDataSetChanged();
	    	
			EventsArrayAdapter adapter = new EventsArrayAdapter(context,
			    android.R.layout.simple_list_item_1, cloList);
			cloView.setAdapter(adapter);
	    	eventsPagerAdapter.notifyDataSetChanged();
	
	    	opView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, final View view, int position, long id) 
				{
					final String item = (String) parent.getItemAtPosition(position);
					view.animate().setDuration(1000).alpha(0);
					view.animate().setDuration(1000).alpha(1);
				}
			});
	    	
			cloView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, final View view, int position, long id) 
				{
					final String item = (String) parent.getItemAtPosition(position);
					view.animate().setDuration(1000).alpha(0);
					view.animate().setDuration(1000).alpha(1);
				}
			});
    	} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public class EventsArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public EventsArrayAdapter(Context context, int textViewResourceId,
            List<String> objects) {
          super(context, textViewResourceId, objects);
          for (int i = 0; i < objects.size(); ++i) {
            mIdMap.put(objects.get(i), i);
          }
        }

        @Override
        public long getItemId(int position) {
          String item = getItem(position);
          return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
          return true;
        }

      }

    public void populateUsers(View view)
    {
    	
    }
    
    
    public void scheduleTimeUpdate()
    {
    	System.out.println("Hello");
    	try {
			JSONArray opDat = appData.getJSONObject("events").getJSONArray("open");
			JSONArray cloDat = appData.getJSONObject("events").getJSONArray("closed");
			

        	//Setup the listview adapter for open events
    		ListView opView = (ListView) findViewById(R.id.open_events);
    		ArrayList<String> opList = new ArrayList<String>();
    		//Setup the listview adapter for closed events
    		ListView cloView = (ListView) findViewById(R.id.closed_events);
    		ArrayList<String> cloList = new ArrayList<String>();
    		
			for(int i=0; i< opDat.length(); i++)
			{System.out.println(opDat.get(i));
				if(((JSONObject) opDat.get(i)).getString("hour").equals("Now"))
				{
					JSONArrayremove(opDat,i);
					continue;
				}
				
				int hour = Integer.parseInt(((JSONObject) opDat.get(i)).getString("hour"));
				int minute = Integer.parseInt(((JSONObject) opDat.get(i)).getString("minute"));
				
				int curHour = Integer.parseInt(DateFormat.format("HH", new Date()).toString());
				int curMin = Integer.parseInt(DateFormat.format("mm", new Date()).toString());
				
				/*if(curHour < hour)
				if(curMin < 59)
					curMin++;
				else
				{
					curMin = 0;
					curHour++;
				}
			else
				curMin++;*/
			
			if(curHour==hour && curMin==minute)
			{
				((JSONObject) opDat.get(i)).put("hour", "Now");
				((JSONObject) opDat.get(i)).put("minute", "Now");
			}

			int nHour = Math.min(Math.abs(curHour-hour), Math.abs(curHour+12-hour));
			int nMin = Math.min(Math.abs(curMin-minute), Math.abs(curMin+12-minute));
			String info = "Food in "+nHour+":"+nMin+" with "+((JSONObject) opDat.get(i)).getString("host");
				opList.add(info);
			}
			
			for(int i=0; i< cloDat.length(); i++)
			{
				if(((JSONObject) cloDat.get(i)).getString("hour").equals("Now"))
				{
					JSONArrayremove(opDat,i);
					continue;
				}
				
				int hour = Integer.parseInt(((JSONObject) cloDat.get(i)).getString("hour"));
				int minute = Integer.parseInt(((JSONObject) cloDat.get(i)).getString("minute"));
				
				int curHour = Integer.parseInt(DateFormat.format("HH", new Date()).toString());
				int curMin = Integer.parseInt(DateFormat.format("mm", new Date()).toString());
				
				/*if(curHour < hour)
				if(curMin < 59)
					curMin++;
				else
				{
					curMin = 0;
					curHour++;
				}
			else
				curMin++;*/
			
			if(curHour==hour && curMin==minute)
			{
				((JSONObject) cloDat.get(i)).put("hour", "Now");
				((JSONObject) cloDat.get(i)).put("minute", "Now");
			}

			int nHour = Math.min(Math.abs(curHour-hour), Math.abs(curHour+12-hour));
			int nMin = Math.min(Math.abs(curMin-minute), Math.abs(curMin+12-minute));
			String info = "Food in "+nHour+":"+nMin+" with "+((JSONObject) cloDat.get(i)).getString("host");
				cloList.add(info);
			}
    		
    		EventsArrayAdapter opAdapter = new EventsArrayAdapter(context,
    		    android.R.layout.simple_list_item_1, opList);
    		opView.setAdapter(opAdapter);
        	eventsPagerAdapter.notifyDataSetChanged();
        	
    		EventsArrayAdapter adapter = new EventsArrayAdapter(context,
    		    android.R.layout.simple_list_item_1, cloList);
    		cloView.setAdapter(adapter);
        	eventsPagerAdapter.notifyDataSetChanged();
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public static JSONArray JSONArrayremove(final JSONArray from,final int idx) {
        final List<JSONObject> objs = asList(from);
        objs.remove(idx);

        final JSONArray ja = new JSONArray();
        for (final JSONObject obj : objs) {
            ja.put(obj);
        }

        return ja;
    }

    public static List<JSONObject> asList(final JSONArray ja) {
        final int len = ja.length();
        final ArrayList<JSONObject> result = new ArrayList<JSONObject>(len);
        for (int i = 0; i < len; i++) {
            final JSONObject obj = ja.optJSONObject(i);
            if (obj != null) {
                result.add(obj);
            }
        }
        return result;
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class MainFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        	View view = inflater.inflate(R.layout.fragment_main, container, false);
        	if(((MainActivity)getActivity()).appData != null)
            {
        		((MainActivity)getActivity()).populateEvents(view);
        		((MainActivity)getActivity()).populateUsers(view);
            }
        	
            return view;
        }
    }
    
    public static class OpenShareFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            return inflater.inflate(R.layout.fragment_add_open, container, false);
        }
    }
    
    public static class ClosedShareFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            return inflater.inflate(R.layout.fragment_add_closed, container, false);
        }
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
    	v.setSelected(true);
    	
    	if(v.getId() == R.id.open_button)
    		findViewById(R.id.closed_button).setSelected(false);
    	else
    		findViewById(R.id.open_button).setSelected(false);
    	
    	eventsPagerAdapter.oldFragments.add(1);
    	eventsPagerAdapter.notifyDataSetChanged();
    }
    
    public void onShareClick(View v) 
    {
    	mViewPager.setPagingEnabled(true);
    	if(findViewById(R.id.open_button).isSelected())
    	{
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
	                        String id = Integer.toString(msgId.incrementAndGet());
	                        jsonObject.accumulate("to", "Firescar96");
	                        JSONObject eventSon = new JSONObject();
	                        eventSon.accumulate("privacy", "open");
	                        TimePicker opTime = (TimePicker)findViewById(R.id.openTime);
	                        eventSon.accumulate("hour", opTime.getCurrentHour());
	                        eventSon.accumulate("minute", opTime.getCurrentMinute());
	                        eventSon.accumulate("host", "Firescar96");
	                        jsonObject.accumulate("event", eventSon);
	             
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
	                        Log.d("InputStream", e.getLocalizedMessage());
	                    }
	                System.out.println(msg);
	                return msg;
	            }
	        }.execute(null, null, null);
    	}
        else
    	{
    		Intent shareIntent = new Intent(Intent.ACTION_SEND);
	    	shareIntent.setType("text/plain");
	    	shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Lets meetup, here's my id" + GCMIntentService.getRegistrationId(context));
	    	startActivity(Intent.createChooser(shareIntent, "Share via"));
    	}
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
    
	protected void onStop()
	{
		super.onDestroy();
		File defFile = new File(getFilesDir().getAbsolutePath()+"/appData.txt");
        	PrintWriter out;
			try {
				out = new PrintWriter(new FileWriter(defFile.getAbsolutePath()));
				out.println(appData.toString());
				//out.println("");
	            out.close();
			}catch(IOException e) {}
	}
}
