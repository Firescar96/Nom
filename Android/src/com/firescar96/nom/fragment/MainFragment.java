package com.firescar96.nom.fragment;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
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

import com.firescar96.nom.GCMIntentService;
import com.firescar96.nom.MainActivity;
import com.firescar96.nom.MainBroadcastReceiver;
import com.firescar96.nom.R;

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
import android.os.Looper;
import android.os.Message;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class MainFragment extends Fragment {

	static MainActivity context = MainActivity.context;
	static private MainFragment thisFrag;
	public View frame;
	static View hostPopView;
	
	public boolean privacy = false;
	
	private EventDetailFragment detailFrag;
	AlarmManager alarmMgr;
	PendingIntent alarmIntent;
	 @Override
	 public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	 {
		thisFrag = this;
		frame = inflater.inflate(R.layout.fragment_main, container, false);
		 if(MainActivity.appData != null)
		 {
			 populateEvents();

			 alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			 Intent intent = new Intent(context, MainBroadcastReceiver.class);
			 intent.setAction("com.firescar96.nom.update.times");
			 alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

			 alarmMgr.setRepeating (AlarmManager.RTC, ((int)System.currentTimeMillis()/60000)*60000, 60000, alarmIntent);
		 }
		 
		 try {
			if(MainActivity.appData.getString("host") == "")
				 requestHostname();
		 } catch (JSONException e) {
			 requestHostname();
		 }
		 return frame;
	 }
	 
	 public void populateEvents()
		{
			try {
				//Setup the listview adapter for open events
				ListView opView = (ListView) frame.findViewById(R.id.open_events);
				ArrayList<String> opList = new ArrayList<String>();
				//Setup the listview adapter for closed events
				ListView cloView = (ListView) frame.findViewById(R.id.closed_events);
				ArrayList<String> cloList = new ArrayList<String>();


				final JSONArray prEve = MainActivity.appData.getJSONArray("events");
				ArrayList<Object> transArr = new ArrayList<Object>();
				for(int i=0; i< prEve.length(); i++)
				{
					//System.out.println(Calendar.getInstance().getTimeInMillis()/1000);
					//System.out.println(Long.parseLong(((JSONObject) opDat.get(i)).getString("date"))/1000);
					/*if(((JSONObject) opDat.get(i)).getString("hour").equals("Now"))
					{
						JSONArrayremove(opDat,i);
						continue;
					}
					
					else*/ 
					if(Calendar.getInstance().getTimeInMillis()/1000 < Long.parseLong(((JSONObject) prEve.get(i)).getString("date"))/1000)
					{
						
						transArr.add(prEve.get(i));
	
						int date = (int) (Long.parseLong(((JSONObject) prEve.get(i)).getString("date")));
						int curDate = (int) Calendar.getInstance().getTimeInMillis();
						int diff = date-curDate;
						diff /=60000;
						int nHour = diff/60;
						int nMin = (diff%60);
						String info = ((JSONObject) prEve.get(i)).getString("host")+" - "+nHour+" "+
								singlePlural(nHour,"hour","hours")+" "+nMin+" "+
								singlePlural(nMin,"min","mins")+"\nat "+ ((JSONObject) prEve.get(i)).getString("location");
						if(prEve.getJSONObject(i).getString("privacy").equals("open"))
							opList.add(info);
						else
							cloList.add(info);
					}
				}
				final JSONArray eve = new JSONArray();
				for(Object item : transArr)
					eve.put(item);
				MainActivity.appData.put("events", eve);
				
				ArrayAdapter<String> opAdapter = new ArrayAdapter<String>(context,
						android.R.layout.simple_list_item_1, opList);
				opView.setAdapter(opAdapter);

				ArrayAdapter<String> cloAdapter = new ArrayAdapter<String>(context,
						android.R.layout.simple_list_item_1, cloList);
				cloView.setAdapter(cloAdapter);

				final SparseArray<JSONObject> opEve = new SparseArray<JSONObject>();
				final SparseArray<JSONObject> cloEve = new SparseArray<JSONObject>();
				for(int i =0; i < eve.length(); i++) {
					JSONObject curEve = eve.getJSONObject(i);
					if(curEve.getString("privacy").equals("open")) 
						opEve.append(i, curEve);
					else
						cloEve.append(i, curEve);
				}
				
				opView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, final View view, int position, long id) 
					{
						view.animate().setDuration(1000).alpha(0);
						view.animate().setDuration(1000).alpha(1);
						
						FragmentManager fragmentManager = getActivity().getFragmentManager();
						detailFrag = new EventDetailFragment();
						Bundle data = new Bundle();
						try {
							data.putString("hash", opEve.valueAt(position).getString("hash"));
							detailFrag.setArguments(data);
						} catch (JSONException e1) {e1.printStackTrace();}
						detailFrag.show(fragmentManager, "dialog");

						try {
							int numOpen=0;
							for(int i =0; i < eve.length(); i++) {
								JSONObject curEve = eve.getJSONObject(i);
								if(curEve.getString("privacy").equals("open") && numOpen == position)
									curEve.put("selected", true);
								else
									curEve.put("selected", false);
								if(curEve.getString("privacy").equals("open"))
									numOpen++;
							}
						} catch (JSONException e) {e.printStackTrace();}
			        	
					}
				});

				cloView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, final View view, int position, long id) 
					{
						view.animate().setDuration(1000).alpha(0);
						view.animate().setDuration(1000).alpha(1);

						FragmentManager fragmentManager = getActivity().getFragmentManager();
						detailFrag = new EventDetailFragment();
						Bundle data = new Bundle();
						try {
							data.putString("hash", cloEve.valueAt(position).getString("hash"));
							detailFrag.setArguments(data);
						} catch (JSONException e1) {e1.printStackTrace();}
						detailFrag.show(fragmentManager, "dialog");
						
						try {
							int numClosed=0;
							for(int i =0; i < eve.length(); i++) {
								JSONObject curEve = eve.getJSONObject(i);
								if(curEve.getString("privacy").equals("closed") && numClosed == position)
									curEve.put("selected", true);
								else
									curEve.put("selected", false);
								if(curEve.getString("privacy").equals("closed"))
									numClosed++;
							}
						} catch (JSONException e) {e.printStackTrace();}
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
		HostDialogFragment newFragment = new HostDialogFragment();
		newFragment.show(fragmentManager, "hostnameDialog");
	}

	public static class HostDialogFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Build the dialog and set up the button click handlers
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			// Get the layout inflater
			LayoutInflater inflater = getActivity().getLayoutInflater();
			hostPopView = inflater.inflate(R.layout.popup_checkmate,null);
			builder.setMessage("Choose a Username")
			.setView(hostPopView)
			.setPositiveButton("Add", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
		    		RelativeLayout good = (RelativeLayout) hostPopView.findViewById(R.id.nameGood);
					EditText hostname = (EditText) hostPopView.findViewById(R.id.nameText);
					if(good.getVisibility() != View.VISIBLE || hostname.getText().length() == 0)
					{
						thisFrag.requestHostname();
						return;
					}
					try {
						MainActivity.appData.put("host",hostname.getText().toString().toUpperCase(Locale.US));
						GCMIntentService.registerInBackground();
						System.out.println(hostname);
						System.out.println(MainActivity.appData.getString("host"));
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			});
			return builder.create();
		}
	}
	
	public void checkHostname()
	{           
		new AsyncTask<Object, Object, Object>() {
			
			@Override
			protected Object doInBackground(Object... arg0) {
				if (Looper.myLooper() == null) {
			        Looper.prepare();
			    }
            	Message msg = new Message();
        	    msg.obj = null;
        	    contextHandler.sendMessage(msg);
        	    
				InputStream inputStream = null;

				try {

					// 1. create HttpClient
					HttpClient httpclient = new DefaultHttpClient();

					// 2. make POST request to the given URL
    					EditText hostname = (EditText) hostPopView.findViewById(R.id.nameText);
    					System.out.println(hostname.getText());
   
    					HttpGet httpGet = new HttpGet("http://nchinda2.mit.edu:666?checkName="+hostname.getText().toString().toUpperCase(Locale.US));

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
					msg = new Message();
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
	
	private static Handler contextHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
           System.out.println(msg.obj);
    		ProgressBar hostProg = (ProgressBar) hostPopView.findViewById(R.id.nameProgress);
    		RelativeLayout good = (RelativeLayout) hostPopView.findViewById(R.id.nameGood);
    		RelativeLayout bad = (RelativeLayout) hostPopView.findViewById(R.id.nameBad);
    		if(msg.obj == null)
    		{
    			hostProg.setVisibility(View.VISIBLE);
    			good.setVisibility(View.GONE);
    			bad.setVisibility(View.GONE);
    		}else if((Boolean) msg.obj)
        	{
        		bad.setVisibility(View.VISIBLE);
        		hostProg.setVisibility(View.GONE);
        		good.setVisibility(View.GONE);
        	}
        	else
        	{
        		good.setVisibility(View.VISIBLE);
        		hostProg.setVisibility(View.GONE);
        		bad.setVisibility(View.GONE);
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
	    return sb.toString();
	}
	
	public EventDetailFragment getDetailFrag()
	{
		/*
		if(detailFrag == null)
		{
			try {
				JSONArray opMsgs = MainActivity.appData.getJSONArray("events").getJSONArray("open");
	        	JSONArray cloMsgs = MainActivity.appData.getJSONArray("events").getJSONArray("closed");
	        	for(int i = 0; i < opMsgs.length(); i++)
	            	if(opMsgs.getJSONObject(i).getBoolean("selected") == true)
	            	{
	            		FragmentManager fragmentManager = getActivity().getFragmentManager();
	            		detailFrag = new EventDetailFragment();
						detailFrag.hash = "hash";
						detailFrag.show(fragmentManager, "dialog");
	            	}
			} catch (JSONException e) {e.printStackTrace();}
		}*/
		
		return detailFrag;
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		alarmMgr.cancel(alarmIntent);
		if(detailFrag != null)
			detailFrag.dismiss();
	}
}