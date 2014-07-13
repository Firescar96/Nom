package com.firescar96.nom;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainFragment extends Fragment {

	static MainActivity context = MainActivity.context;
	
	static View frame;
	
	 @Override
	 public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	 {
		frame = inflater.inflate(R.layout.fragment_main, container, false);
		 if(((MainActivity)getActivity()).appData != null)
		 {
			 populateEvents();

			 AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			 Intent intent = new Intent(context, MainBroadcastReceiver.class);
			 intent.setAction("com.firescar96.nom.update.times");
			 PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

			 alarmMgr.setRepeating (AlarmManager.RTC, ((int)System.currentTimeMillis()/60000)*60000, 60000, alarmIntent);
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
				if(((JSONObject) opDat.get(i)).getString("hour").equals("Now"))
				{
					JSONArrayremove(opDat,i);
					continue;
				}
				else if(Calendar.getInstance().getTimeInMillis()/1000 > Long.parseLong(((JSONObject) opDat.get(i)).getString("date"))/1000)
				{
					JSONArrayremove(opDat,i);
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
				String info = ((JSONObject) opDat.get(i)).getString("host")+"-"+nHour+":"+nMin+" at "+ "place";
				opList.add(info);
				}

				for(int i=0; i< cloDat.length(); i++)
				{
					if(((JSONObject) cloDat.get(i)).getString("hour").equals("Now"))
					{
						JSONArrayremove(cloDat,i);
						continue;
					}
					else if(Calendar.getInstance().getTimeInMillis()/1000 > Long.parseLong(((JSONObject) cloDat.get(i)).getString("date"))/1000)
					{
						JSONArrayremove(cloDat,i);
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
					String info = "Food in "+nHour+":"+nMin+" with "+((JSONObject) cloDat.get(i)).getString("host");
					cloList.add(info);
				}

				ArrayAdapter<String> opAdapter = new ArrayAdapter<String>(context,
						android.R.layout.simple_list_item_1, opList);
				opView.setAdapter(opAdapter);
				//mainPagerAdapter.notifyDataSetChanged();

				ArrayAdapter<String> cloAdapter = new ArrayAdapter<String>(context,
						android.R.layout.simple_list_item_1, cloList);
				cloView.setAdapter(cloAdapter);
				//mainPagerAdapter.notifyDataSetChanged();

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
			} catch (JSONException e) {}
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
}