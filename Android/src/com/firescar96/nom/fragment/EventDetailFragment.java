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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.firescar96.nom.MainActivity;
import com.firescar96.nom.R;

public class EventDetailFragment extends DialogFragment {
	static MainActivity context = MainActivity.context;
	public View frame;
	public String privacy;
	public int position;

	static JSONArray detailData;
	static ArrayList<String> detailList;
	static ArrayAdapter<String> detailAdapter;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Build the dialog and set up the button click handlers
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();
		frame = inflater.inflate(R.layout.fragment_event_details,null);
		builder.setView(frame);
		
		detailList = new ArrayList<String>();
		try {
			detailData = context.appData.getJSONObject("events")
					.getJSONArray(privacy)
					.getJSONObject(position)
					.getJSONArray("chat");
			
			for(int i = 0; i < detailData.length(); i++)
				detailList.add(detailData.getJSONObject(i).getString("host")+": "+detailData.getJSONObject(i).getString("message"));
			
			//change the editable parts depending on whether the user is part of the group
			configureEditable();
		} catch (JSONException e) {
			try {
				context.appData.getJSONObject("events").getJSONArray(privacy).getJSONObject(position).put("chat", new JSONArray());
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		//Setup the listview adapter for closed events
		ListView useView = (ListView) frame.findViewById(R.id.eventMembershipList);
		detailAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, detailList);
		useView.setAdapter(detailAdapter);
		
		useView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, final View view, int position, long id) 
			{
				view.animate().setDuration(1000).alpha(0);
				view.animate().setDuration(1000).alpha(1);
			}
		});
		
		return builder.create();
	}
	
	public void onEventMembershipChanged(View v)
	{
		try {
			JSONObject curEve = context.appData.getJSONObject("events").getJSONArray(privacy).getJSONObject(position);
			curEve.put("member", !curEve.getBoolean("member"));
			configureEditable();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	public void configureEditable() throws JSONException
	{
		if(context.appData.getJSONObject("events").getJSONArray(privacy).getJSONObject(position).getBoolean("member"))
		{
			frame.findViewById(R.id.open_button).setVisibility(View.GONE);
			frame.findViewById(R.id.closed_button).setVisibility(View.VISIBLE);
			frame.findViewById(R.id.eventChatText).setEnabled(true);
			frame.findViewById(R.id.eventChatButton).setEnabled(true);
			postMsg("has joined!");
		}
		else
		{
			frame.findViewById(R.id.closed_button).setVisibility(View.GONE);
			frame.findViewById(R.id.open_button).setVisibility(View.VISIBLE);
			frame.findViewById(R.id.eventChatText).setEnabled(false);
			frame.findViewById(R.id.eventChatButton).setEnabled(false);
			postMsg("has left.");
		}
	}
	
	public void updateList()
	{
		try {
			detailData = context.appData.getJSONObject("events")
					.getJSONArray(privacy)
					.getJSONObject(position)
					.getJSONArray("chat");

			detailList.clear();
			for(int i = 0; i < detailData.length(); i++)
				detailList.add(detailData.getJSONObject(i).getString("host")+": "+detailData.getJSONObject(i).getString("message"));

			detailAdapter.notifyDataSetChanged();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void onChatMsg(View v)
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
					jsonObject.accumulate("chat", "true");
					jsonObject.accumulate("host", context.appData.getString("host"));
					jsonObject.accumulate("date", context.appData.getJSONObject("events").getJSONArray(privacy).getJSONObject(position).getString("date"));
					jsonObject.accumulate("message", message);

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