package com.firescar96.nom;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.cloud.backend.*;
import com.google.cloud.backend.core.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends Activity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    
    String SENDER_ID = "81193489522";
    
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    SharedPreferences prefs;
    Context context;
    String regid;
    
    JSONObject appData;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        
     // Check device for Play Services APK. If check succeeds, proceed with
        //  GCM registration.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);

            if (regid.isEmpty()) {
                registerInBackground();
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
				out.println("");
	            out.close();
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
		    System.out.println(datBuf+"THE BUFFS");
	        //appData = new JSONObject(datBuf.toString());
		    appData = new JSONObject();
		    
	        JSONArray useArr = new JSONArray();
	        useArr.put("1");
	        useArr.put("2");
	        useArr.put("3");
	        useArr.put("4");
	        useArr.put("5");
	        useArr.put("6");
	        useArr.put("7");
	        JSONObject eveObj = new JSONObject();
	        JSONArray eveCloseArr = new JSONArray();
	        JSONArray eveOpenArr = new JSONArray();
	        eveObj.put("open",eveCloseArr);
	        eveObj.put("closed",eveOpenArr);
	        eveCloseArr.put("12");
	        eveCloseArr.put("13");
	        eveCloseArr.put("14");
	        eveOpenArr.put("15");
	        eveOpenArr.put("16");
	        eveOpenArr.put("17");
	        
	        appData.put("users", useArr);
	        appData.put("events", eveObj);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		// Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());
        
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.main_activity);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);
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

    public void populateEvents(View view)
    {
    	JSONArray listData = null;
		try {
			appData.getJSONObject("events");
			listData = appData.getJSONObject("events").getJSONArray("closed");
		}catch (JSONException e) {}
    	
    	//Setup the listview adapter for closed events
    	ListView listview = (ListView) view.findViewById(R.id.closed_events);

		ArrayList<String> list = new ArrayList<String>();
		System.out.println(appData);
		for(int i = 0; i<listData.length(); i++) 
		{
			try {
				list.add((String) listData.get(i));
			}catch (JSONException e) {}
		}
		
		EventsArrayAdapter adapter = new EventsArrayAdapter(this,
		    android.R.layout.simple_list_item_1, list);
		System.out.println(listview);
		listview.setAdapter(adapter);

		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, final View view, int position, long id) 
			{
				final String item = (String) parent.getItemAtPosition(position);
				view.animate().setDuration(1000).alpha(0);
				view.animate().setDuration(1000).alpha(1);
			}

		});
		
		//Setup the same for open events
		listview = (ListView) view.findViewById(R.id.open_events);
		String[] values = new String[] { "WebOS", "Ubuntu", "Windows7", "Max OS X",
		    "Linux", "OS/2", "Ubuntu", "Windows7"};

		list = new ArrayList<String>();
		for (int i = 0; i < values.length; ++i) {
		  list.add(values[i]);
		}
		adapter = new EventsArrayAdapter(this,
		    android.R.layout.simple_list_item_1, list);
		System.out.println(listview);
		listview.setAdapter(adapter);

		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, final View view, int position, long id) 
			{
				final String item = (String) parent.getItemAtPosition(position);
				view.animate().setDuration(1000).alpha(0);
				view.animate().setDuration(1000).alpha(1);
			}

		});
    }

    public void populateUsers(View view)
    {
    	
    }
    
    private class EventsArrayAdapter extends ArrayAdapter<String> {

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

    
    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

    	public ArrayList<Integer> oldFragments = new ArrayList<Integer>();
    	private ArrayList<Fragment> views = new ArrayList<Fragment>();
    	
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
        	
        	Fragment newFragment = null;
        	System.out.println("switching"+position);
        	
            switch (position)
            {
            case 0:
            	newFragment = new MainFragment();
            	break;
            case 1:
            	if(findViewById(R.id.open_button) == null)
            	{
            		newFragment = new OpenShareFragment();
            		System.out.println("null still");
            	}
            	else if(findViewById(R.id.open_button).isSelected())
            	{
            		newFragment = new OpenShareFragment();
            		System.out.println("open");
            	}
            	else if(findViewById(R.id.closed_button).isSelected())
            	{
            		newFragment = new ClosedShareFragment();
            		System.out.println("closed");
            	}
            	else
            	{
            		newFragment = new ClosedShareFragment();
            		System.out.println("else");
            	}
            	break;
            default:
            	newFragment = new MainFragment();
            	break;
            }
            
            views.add(newFragment);
            return newFragment;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
        
        @Override
        public int getItemPosition(Object object)
        {
        	int index = views.indexOf (object);
        	for(int i = 0; i < oldFragments.size(); i++)
        		if(oldFragments.get(i) == index)
        		{
        			views.remove(index);
        			oldFragments.remove(i);
        			return POSITION_NONE;
        		}
        	
              return POSITION_UNCHANGED;
        }
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
    
    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
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
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(MainActivity.class.getSimpleName(),
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
    
    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Object, Object, Object>() {
            protected String doInBackground(Object... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device
                    // will send upstream messages to a server that echo back the
                    // message using the 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                
                return msg;
            }

        }.execute(null, null, null);
    }
    
    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
     * or CCS to send messages to your app. Not needed for this demo since the
     * device sends upstream messages to a server that echoes back the message
     * using the 'from' address in the message.
     */
    private void sendRegistrationIdToBackend() {
    	new AsyncTask<Object, Object, Object>() {
			@Override
			protected Object doInBackground(Object... arg0) {
                String msg = "";
                try {
                    Bundle data = new Bundle();
                        data.putString("username","FIRESCAR");
                        String id = Integer.toString(msgId.incrementAndGet());
                        gcm.send(SENDER_ID + "@gcm.googleapis.com", id, data);
                        msg = "Sent message";
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                System.out.println(msg);
                return msg;
            }
        }.execute(null, null, null);
    }
    
    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        System.out.println("Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }
    
    public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Explicitly specify that GcmIntentService will handle the intent.
            ComponentName comp = new ComponentName(context.getPackageName(),
                    GcmIntentService.class.getName());
            // Start the service, keeping the device awake while it is launching.
            startWakefulService(context, (intent.setComponent(comp)));
            setResultCode(Activity.RESULT_OK);
        }
    }
    
    public class GcmIntentService extends IntentService {
        public static final int NOTIFICATION_ID = 1;
        private NotificationManager mNotificationManager;
        NotificationCompat.Builder builder;

        public GcmIntentService() {
            super("GcmIntentService");
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            Bundle extras = intent.getExtras();
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
            // The getMessageType() intent parameter must be the intent you received
            // in your BroadcastReceiver.
            String messageType = gcm.getMessageType(intent);

            if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
                /*
                 * Filter messages based on message type. Since it is likely that GCM
                 * will be extended in the future with new message types, just ignore
                 * any message types you're not interested in, or that you don't
                 * recognize.
                 */
                if (GoogleCloudMessaging.
                        MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                    sendNotification("Send error: " + extras.toString());
                } else if (GoogleCloudMessaging.
                        MESSAGE_TYPE_DELETED.equals(messageType)) {
                    sendNotification("Deleted messages on server: " +
                            extras.toString());
                // If it's a regular GCM message, do some work.
                } else if (GoogleCloudMessaging.
                        MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                    // Post notification of received message.
                    sendNotification("Received: " + extras.toString());
                    System.out.println("Received: " + extras.toString());
                    
                    if(extras.get("username") != null)
                    {
                    	try {
							appData.getJSONArray("usernames").put(extras.get("username"));
						} catch (JSONException e) {
							e.printStackTrace();
						}
                    	
                    	mSectionsPagerAdapter.oldFragments.add(1);
                    	mSectionsPagerAdapter.notifyDataSetChanged();
                    }
                    
                    if(extras.get("event") != null)
                    {
                    	try {
							appData.getJSONArray("events").put(extras.get("events"));
						} catch (JSONException e) {
							e.printStackTrace();
						}
                    	
                    	mSectionsPagerAdapter.oldFragments.add(0);
                    	mSectionsPagerAdapter.notifyDataSetChanged();
                    }
                }
            }
            // Release the wake lock provided by the WakefulBroadcastReceiver.
            GcmBroadcastReceiver.completeWakefulIntent(intent);
        }

        // Put the message into a notification and post it.
        // This is just one simple example of what you might choose to do with
        // a GCM message.
        private void sendNotification(String msg) {
            mNotificationManager = (NotificationManager)
                    this.getSystemService(Context.NOTIFICATION_SERVICE);

            PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, MainActivity.class), 0);

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
            //.setSmallIcon(R.drawable.ic_stat_gcm)
            .setContentTitle("GCM Notification")
            .setStyle(new NotificationCompat.BigTextStyle()
            .bigText(msg))
            .setContentText(msg);

            mBuilder.setContentIntent(contentIntent);
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        }
    }
    
    public void onPrivacySelect(View v)
    {
    	v.setSelected(true);
    	
    	if(v.getId() == R.id.open_button)
    		findViewById(R.id.closed_button).setSelected(false);
    	else
    		findViewById(R.id.open_button).setSelected(false);
    	
    	mSectionsPagerAdapter.oldFragments.add(1);
    	mSectionsPagerAdapter.notifyDataSetChanged();
    }
    
    public void onShareClick(View v) 
    {
    	if(findViewById(R.id.open_button).isSelected())
    	{
    		new AsyncTask<Object, Object, Object>() {
    			@Override
    			protected Object doInBackground(Object... arg0) {
	                String msg = "";
	                try {
	                    Bundle data = new Bundle();
	                        data.putString("eat_time", "anytime");
	                        String id = Integer.toString(msgId.incrementAndGet());
	                        gcm.send(SENDER_ID + "@gcm.googleapis.com", id, data);
	                        msg = "Sent message";
	                } catch (IOException ex) {
	                    msg = "Error :" + ex.getMessage();
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
	    	shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Lets meetup, here's my id" + getRegistrationId(context));
	    	startActivity(Intent.createChooser(shareIntent, "Share via"));
    	}
    }
	
	protected void onDestroy()
	{
		super.onDestroy();
		File defFile = new File(getFilesDir().getAbsolutePath()+"/appData.txt");
        	PrintWriter out;
			try {
				out = new PrintWriter(new FileWriter(defFile.getAbsolutePath()));
				out.println(appData.toString());
	            out.close();
			}catch(IOException e) {}
	}
}
