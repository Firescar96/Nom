package com.firescar96.nom.fragment;

import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.firescar96.nom.MainActivity;
import com.firescar96.nom.R;
import com.firescar96.nom.org.json.JSONArray;
import com.firescar96.nom.org.json.JSONObject;

public class EventDetailFragment extends DialogFragment {
	static MainActivity context = MainActivity.context;
	static EventDetailFragment thisFrag;
	public View frame;
	private String hash;

	static JSONArray detailData;
	static ArrayList<String> detailList;
	static ListView detailListView;
	static ArrayAdapter<String> detailAdapter;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		hash = getArguments().getString("hash");
		thisFrag = this;
		
		// Build the dialog and set up the button click handlers
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();
		frame = inflater.inflate(R.layout.fragment_event_details,null);
		MainActivity.setupUI(frame);
		builder.setView(frame);
		
		detailList = new ArrayList<String>();
		//change the editable parts depending on whether the user is part of the group
		configureEditable(false);
		
		//Setup the listview adapter for closed events
		detailListView = (ListView) frame.findViewById(R.id.eventMembershipList);
		detailAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, detailList);
		detailListView.setAdapter(detailAdapter);

		updateList();	
		detailListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, final View view, int position, long id) 
			{
				view.animate().setDuration(1000).alpha(0);
				view.animate().setDuration(1000).alpha(1);
			}
		});

		try {
			JSONArray eve = MainActivity.appData.getJSONArray("events");
			for(int i =0; i < eve.length(); i++) {
				JSONObject curEve = eve.getJSONObject(i);
				if(curEve.getString("hash").equals(hash)) {
					eve.getJSONObject(i).put("selected", true);
					break;
				}
			}
		} catch (JSONException e) {e.printStackTrace();}
		
		return builder.create();
	}
	
	public void onEventMembershipChanged()
	{
		try {
			JSONArray eve = MainActivity.appData.getJSONArray("events");
			for(int i =0; i < eve.length(); i++) {
				JSONObject curEve = eve.getJSONObject(i);
				if(curEve.getString("hash").equals(hash)) {
					curEve.put("member", !curEve.getBoolean("member"));
					break;
				}
			}
			configureEditable(true);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void configureEditable(boolean post)
	{
		try {
			JSONArray eve = MainActivity.appData.getJSONArray("events");
			for(int i =0; i < eve.length(); i++) {
				JSONObject curEve = eve.getJSONObject(i);
				if(curEve.getString("hash").equals(hash)) {
					if(curEve.getBoolean("member"))
					{
						frame.findViewById(R.id.open_button).setVisibility(View.GONE);
						frame.findViewById(R.id.closed_button).setVisibility(View.VISIBLE);
						frame.findViewById(R.id.eventChatText).setEnabled(true);
						frame.findViewById(R.id.eventChatButton).setEnabled(true);
						if(post)
							postMsg("has joined!");
					}
					else
					{
						frame.findViewById(R.id.closed_button).setVisibility(View.GONE);
						frame.findViewById(R.id.open_button).setVisibility(View.VISIBLE);
						frame.findViewById(R.id.eventChatText).setEnabled(false);
						frame.findViewById(R.id.eventChatButton).setEnabled(false);
						if(post)
							postMsg("has left.");
					}
					break;
				}
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void updateList()
	{
		try {
			JSONArray eve = MainActivity.appData.getJSONArray("events");
			for(int i =0; i < eve.length(); i++) {
				JSONObject curEve = eve.getJSONObject(i);
				if(curEve.getString("hash").equals(hash)) {
					detailData = curEve.getJSONArray("chat");
					break;
				}
			}
			

			detailList.clear();
			for(int i = 0; i < detailData.length(); i++)
				detailList.add(detailData.getJSONObject(i).getString("author")+": "+detailData.getJSONObject(i).getString("message"));

			detailAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, detailList);
			detailListView.setAdapter(detailAdapter);
			detailListView.setSelection(detailAdapter.getCount()-1);
			Log.i("detailAdapter", detailList.toString());
		} catch (JSONException e) {
			try {
				JSONArray eve = MainActivity.appData.getJSONArray("events");
				for(int i =0; i < eve.length(); i++) {
					JSONObject curEve = eve.getJSONObject(i);
					if(curEve.getString("hash").equals(hash)) {
						curEve.put("chat", new JSONArray());
						break;
					}
				}
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	public void onChatMsg()
	{
		postMsg(((EditText)frame.findViewById(R.id.eventChatText)).getText().toString());
		((EditText)frame.findViewById(R.id.eventChatText)).setText("");
	}	
	
	public void postMsg(final String message)
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
					JSONArray eve = MainActivity.appData.getJSONArray("events");
					for(int i =0; i < eve.length(); i++) {
						JSONObject curEve = eve.getJSONObject(i);
						if(curEve.getString("hash").equals(hash)) {
							jsonObject.accumulate("host", curEve.getString("host"));
							jsonObject.accumulate("date", curEve.getString("date"));
							break;
						}
					}
					jsonObject.accumulate("chat", "true");
					jsonObject.accumulate("author", MainActivity.appData.getString("host"));
					jsonObject.accumulate("message", message);
					jsonObject.accumulate("hash", hash);

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
	
	@Override
	public void onStop()
	 {
		super.onStop();
		thisFrag=null;
	 }
}