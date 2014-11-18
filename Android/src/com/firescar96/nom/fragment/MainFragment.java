package com.firescar96.nom.fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONException;

import android.app.AlarmManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.firescar96.nom.MainActivity;
import com.firescar96.nom.MainBroadcastReceiver;
import com.firescar96.nom.R;
import com.firescar96.nom.org.json.JSONArray;
import com.firescar96.nom.org.json.JSONObject;

public class MainFragment extends Fragment {

	static MainActivity context = MainActivity.context;
	static private MainFragment thisFrag;
	public View frame;
	
	public boolean privacy = false;
	
	AlarmManager alarmMgr;
	PendingIntent alarmIntent;
	 @Override
	 public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	 {
		thisFrag = this;
		frame = inflater.inflate(R.layout.fragment_main, container, false);
		 if(MainActivity.appData != null)
		 {
			 updateEvents();

			 alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			 Intent intent = new Intent(context, MainBroadcastReceiver.class);
			 intent.setAction("com.firescar96.nom.update.times");
			 alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		 }
		 
		 try {
			if(MainActivity.appData.getString("host") == "")
				 requestHostname();
		 } catch (JSONException e) {
			 requestHostname();
		 }
		 return frame;
	 }
	 
	 @Override
	 public void onResume() {
		 super.onResume();
		 alarmMgr.setRepeating (AlarmManager.RTC, (int)System.currentTimeMillis()/60000*60000, 60000, alarmIntent);
	 }
	 
	 public void updateEvents()
		{
			try {
				Log.i("MainFrag", "updating events");
				//Setup the listview adapter for open events
				ListView opView = (ListView) frame.findViewById(R.id.open_events);
				ArrayList<String> opList = new ArrayList<String>();
				//Setup the listview adapter for closed events
				ListView cloView = (ListView) frame.findViewById(R.id.closed_events);
				ArrayList<String> cloList = new ArrayList<String>();


				final JSONArray eve = MainActivity.appData.getJSONArray("events");
				for(int i=0; i< eve.length(); i++)
					//System.out.println(Calendar.getInstance().getTimeInMillis()/1000);
					//System.out.println(Long.parseLong(((JSONObject) opDat.get(i)).getString("date"))/1000);
					/*if(((JSONObject) opDat.get(i)).getString("hour").equals("Now"))
					{
						JSONArrayremove(opDat,i);
						continue;
					}
					
					else*/ 
					if(Calendar.getInstance().getTimeInMillis()/1000 < Long.parseLong(eve.getJSONObject(i).getString("date"))/1000)
					{	
						int date = (int) Long.parseLong(eve.getJSONObject(i).getString("date"));
						int curDate = (int) Calendar.getInstance().getTimeInMillis();
						int diff = date-curDate;
						diff /=60000;
						int nHour = diff/60;
						int nMin = diff%60;
						String info = eve.getJSONObject(i).getString("host")+" - "+nHour+" "+
								singlePlural(nHour,"hour","hours")+" "+nMin+" "+
								singlePlural(nMin,"min","mins")+"\nat "+ eve.getJSONObject(i).getString("location");
						if(eve.getJSONObject(i).getString("privacy").equals("open"))
							opList.add(info);
						else
							cloList.add(info);
					}
					else
						eve.remove(i);
				MainActivity.appData.put("events", eve);

				Log.i("MainFrag", eve.toString());
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
						EventDetailFragment detailFrag = new EventDetailFragment();
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
						EventDetailFragment detailFrag = new EventDetailFragment();
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
		for (final JSONObject obj : objs)
			ja.put(obj);

		return ja;
	}
	public static List<JSONObject> asList(final JSONArray ja) {
			final int len = ja.length();
			final ArrayList<JSONObject> result = new ArrayList<JSONObject>(len);
			for (int i = 0; i < len; i++) {
				final JSONObject obj = ja.optJSONObject(i);
				if (obj != null)
					result.add(obj);
			}
			return result;
		}

	public void requestHostname() //User needs to create a username if they want to use this app
	{
		FragmentManager fragmentManager = getActivity().getFragmentManager();
		new HostDialogFragment().show(fragmentManager, "hostnameDialog");
	}

	
	public void onEventMembershipChanged()
	{
		if(EventDetailFragment.thisFrag!=null)
			EventDetailFragment.thisFrag.onEventMembershipChanged();
	}
	
	public void onChatMsg()
	{
		if(EventDetailFragment.thisFrag!=null)
			EventDetailFragment.thisFrag.onChatMsg();
	}
	
	public void checkName()
	{
		if(HostDialogFragment.thisFrag !=null)
			HostDialogFragment.thisFrag.checkName();
	}
	
	public void updateDetailFrag()
	{
		System.out.println(EventDetailFragment.thisFrag);
		if(EventDetailFragment.thisFrag!=null)
			EventDetailFragment.thisFrag.updateList();
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		alarmMgr.cancel(alarmIntent);
	}
}