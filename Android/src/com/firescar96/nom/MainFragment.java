package com.firescar96.nom;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class MainFragment extends Fragment {

	static MainActivity context = MainActivity.context;
	static MainFragment thisFrag;
	static View frame;
	static View hostPopView;
	AlarmManager alarmMgr;
	PendingIntent alarmIntent;
	 @Override
	 public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	 {
		thisFrag = this;
		frame = inflater.inflate(R.layout.fragment_main, container, false);
		 if(((MainActivity)getActivity()).appData != null)
		 {
			 populateEvents();

			 alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			 Intent intent = new Intent(context, MainBroadcastReceiver.class);
			 intent.setAction("com.firescar96.nom.update.times");
			 alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

			 alarmMgr.setRepeating (AlarmManager.RTC, ((int)System.currentTimeMillis()/60000)*60000, 60000, alarmIntent);
		 }
		 
		 try {
			if(context.appData.getString("host") == "")
				 requestHostname();
		 } catch (JSONException e) {
			 requestHostname();
		 }
		 return frame;
	 }
	 
	 public void populateEvents()
		{
			try {
				JSONArray opDat = context.appData.getJSONObject("events").getJSONArray("open");
				JSONArray cloDat = context.appData.getJSONObject("events").getJSONArray("closed");


				//Setup the listview adapter for open events
				ListView opView = (ListView) frame.findViewById(R.id.open_events);
				ArrayList<String> opList = new ArrayList<String>();
				//Setup the listview adapter for closed events
				ListView cloView = (ListView) frame.findViewById(R.id.closed_events);
				ArrayList<String> cloList = new ArrayList<String>();

				for(int i=0; i< opDat.length(); i++)
				{System.out.println(opDat.get(i));

				System.out.println(Calendar.getInstance().getTimeInMillis()/1000);
				System.out.println(Long.parseLong(((JSONObject) opDat.get(i)).getString("date"))/1000);
				/*if(((JSONObject) opDat.get(i)).getString("hour").equals("Now"))
				{
					JSONArrayremove(opDat,i);
					continue;
				}
				else*/ if(Calendar.getInstance().getTimeInMillis()/1000 > Long.parseLong(((JSONObject) opDat.get(i)).getString("date"))/1000)
				{

					System.out.println("removes");
					JSONArrayremove(opDat,i);
					context.appData.getJSONObject("events").put("open", opDat);
					continue;
				}

				int hour = Integer.parseInt(((JSONObject) opDat.get(i)).getString("hour"));
				int minute = Integer.parseInt(((JSONObject) opDat.get(i)).getString("minute"));

				int curHour = Integer.parseInt(DateFormat.format("HH", new Date()).toString());
				int curMin = Integer.parseInt(DateFormat.format("mm", new Date()).toString());

				/*if(curHour==hour && curMin==minute)
					{
						((JSONObject) opDat.get(i)).put("hour", "Now");
						((JSONObject) opDat.get(i)).put("minute", "Now");
					}*/
				int nHour = Math.min(Math.abs(curHour-hour), Math.abs(curHour+12-hour));
				int nMin = Math.min(Math.abs(curMin-minute), Math.abs(curMin+12-minute));
				String info = ((JSONObject) opDat.get(i)).getString("host")+" - "+nHour+" "+
						singlePlural(nHour,"hour","hours")+" "+nMin+" "+
						singlePlural(nMin,"min","mins")+"\nat "+ ((JSONObject) opDat.get(i)).getString("location");
				opList.add(info);
				}

				for(int i=0; i< cloDat.length(); i++)
				{
					/*if(((JSONObject) cloDat.get(i)).getString("hour").equals("Now"))
					{
						JSONArrayremove(cloDat,i);
						continue;
					}
					else*/ if(Calendar.getInstance().getTimeInMillis()/1000 > Long.parseLong(((JSONObject) cloDat.get(i)).getString("date"))/1000)
					{
						JSONArrayremove(cloDat,i);
						context.appData.getJSONObject("events").put("closed", cloDat);
						continue;
					}

					int hour = Integer.parseInt(((JSONObject) cloDat.get(i)).getString("hour"));
					int minute = Integer.parseInt(((JSONObject) cloDat.get(i)).getString("minute"));

					int curHour = Integer.parseInt(DateFormat.format("HH", new Date()).toString());
					int curMin = Integer.parseInt(DateFormat.format("mm", new Date()).toString());

					/*if(curHour==hour && curMin==minute)
					{
						((JSONObject) cloDat.get(i)).put("hour", "Now");
						((JSONObject) cloDat.get(i)).put("minute", "Now");
					}*/

					int nHour = Math.min(Math.abs(curHour-hour), Math.abs(curHour+12-hour));
					int nMin = Math.min(Math.abs(curMin-minute), Math.abs(curMin+12-minute));
					String info = ((JSONObject) cloDat.get(i)).getString("host")+" - "+nHour+" "+
							singlePlural(nHour,"hour","hours")+" "+nMin+" "+
							singlePlural(nMin,"min","mins")+"\nat "+ ((JSONObject) cloDat.get(i)).getString("location");
					cloList.add(info);
				}

				ArrayAdapter<String> opAdapter = new ArrayAdapter<String>(context,
						android.R.layout.simple_list_item_1, opList);
				opView.setAdapter(opAdapter);

				ArrayAdapter<String> cloAdapter = new ArrayAdapter<String>(context,
						android.R.layout.simple_list_item_1, cloList);
				cloView.setAdapter(cloAdapter);

				opView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, final View view, int position, long id) 
					{
						view.animate().setDuration(1000).alpha(0);
						view.animate().setDuration(1000).alpha(1);
					}
				});

				cloView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, final View view, int position, long id) 
					{
						view.animate().setDuration(1000).alpha(0);
						view.animate().setDuration(1000).alpha(1);
					}
				});
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	 
	 public String singlePlural(int count, String singular, String plural)
	 {
	   return count==1 ? singular : plural;
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

	public void requestHostname() //User needs to create a username if they want to use this app
	{
		FragmentManager fragmentManager = getActivity().getFragmentManager();
		CustomDialogFragment newFragment = new CustomDialogFragment();
		newFragment.show(fragmentManager, "hostnameDialog");
	}

	public static class CustomDialogFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Build the dialog and set up the button click handlers
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			// Get the layout inflater
			LayoutInflater inflater = getActivity().getLayoutInflater();
			hostPopView = inflater.inflate(R.layout.popup_set_hostname,null);
			builder.setMessage("Choose a Username")
			.setView(hostPopView)
			.setPositiveButton("Add", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
		    		RelativeLayout good = (RelativeLayout) hostPopView.findViewById(R.id.hostnameGood);
					EditText hostname = (EditText) hostPopView.findViewById(R.id.hostnameText);
					if(good.getVisibility() != View.VISIBLE || hostname.getText().length() == 0)
					{
						thisFrag.requestHostname();
						return;
					}
					try {
						context.appData.put("host",hostname.getText().toString().toUpperCase(Locale.US));
						GCMIntentService.sendRegistrationIdToBackend();
						System.out.println(hostname);
						System.out.println(context.appData.getString("host"));
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			});
			return builder.create();
		}
	}
	
	public void checkHostname(View v)
	{
		new Thread(new Runnable() {
            public void run() {
            	
            	Message msg = new Message();
        	    msg.obj = null;
        	    contextHandler.sendMessage(msg);
        	    
        		new AsyncTask<Object, Object, Object>() {
        			@Override
        			protected Object doInBackground(Object... arg0) {
        				InputStream inputStream = null;

        				try {

        					// 1. create HttpClient
        					HttpClient httpclient = new DefaultHttpClient();

        					// 2. make POST request to the given URL
        					EditText hostname = (EditText) hostPopView.findViewById(R.id.hostnameText);
        					System.out.println(hostname.getText());
        					HttpGet httpGet = new HttpGet("http://nchinda2.mit.edu:666?checkName="+hostname.getText().toString().toUpperCase(Locale.US)+"&regId="+GCMIntentService.getRegistrationId(context));

        					// 7. Set some headers to inform server about the type of the content   
        					httpGet.setHeader("Accept", "application/json");
        					httpGet.setHeader("Content-type", "application/json");

        					HttpParams httpParams = httpclient.getParams();
        					HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
        					HttpConnectionParams.setSoTimeout(httpParams, 5000);
        					httpGet.setParams(httpParams);

        					// 8. Execute POST request to the given URL
        					System.out.println("executing");
        					HttpResponse httpResponse = httpclient.execute(httpGet);
        					// 9. receive response as inputStream
        					inputStream = httpResponse.getEntity().getContent();

        					// 10. convert inputstream to string
        					Message msg = new Message();
        					if(inputStream != null)
        						if(convertStreamToString(inputStream).contains("true"))
        							msg.obj = true;
        						else
        							msg.obj = false;
        					else
        						msg.obj = false;
        					contextHandler.sendMessage(msg);

        				} catch (Exception e) {
        					e.printStackTrace();
        				}
        				return false;
        			}
        		}.execute(null, null, null);
            }
        }).start();
	}
	
	private static Handler contextHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
           System.out.println(msg.obj);
    		ProgressBar hostProg = (ProgressBar) hostPopView.findViewById(R.id.hostnameProgress);
    		RelativeLayout good = (RelativeLayout) hostPopView.findViewById(R.id.hostnameGood);
    		RelativeLayout bad = (RelativeLayout) hostPopView.findViewById(R.id.hostnameBad);
    		if(msg.obj == null)
    		{
    			hostProg.setVisibility(View.VISIBLE);
    			good.setVisibility(View.GONE);
    			bad.setVisibility(View.GONE);
    		}else if((Boolean) msg.obj)
        	{
        		good.setVisibility(View.VISIBLE);
        		hostProg.setVisibility(View.GONE);
        		bad.setVisibility(View.GONE);
        	}
        	else
        	{
        		bad.setVisibility(View.VISIBLE);
        		hostProg.setVisibility(View.GONE);
        		good.setVisibility(View.GONE);
        	}
        }
    };
	
	private String convertStreamToString(InputStream is) {
	    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	    StringBuilder sb = new StringBuilder();

	    String line = null;
	    try {
	        while ((line = reader.readLine()) != null) {
	            sb.append(line + "\n");
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        try {
	            is.close();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	    System.out.println(sb.toString());
	    return sb.toString();
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		alarmMgr.cancel(alarmIntent);
	}
}