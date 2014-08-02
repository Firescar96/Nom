package com.firescar96.nom.fragment;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

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

import com.firescar96.nom.MainActivity;
import com.firescar96.nom.R;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TimePicker;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class ClosedShareFragment2 extends Fragment {
	static MainActivity context = MainActivity.context;


	static ClosedShareFragment2 thisFrag;
	static View frame;
	static JSONArray mateData;
	static ArrayList<String> mateList;
	static ArrayAdapter<String> mateAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		thisFrag = this;
		frame = inflater.inflate(R.layout.fragment_add_closed2, container, false);
		try {
			mateData = context.appData.getJSONArray("mates");
		} catch (JSONException e) {}
		mateList = new ArrayList<String>();
		//Setup the listview adapter for closed events
		ListView useView = (ListView) frame.findViewById(R.id.matesList);
		mateAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, mateList);
		useView.setAdapter(mateAdapter);
		
		useView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, final View view, int position, long id) 
			{
				view.animate().setDuration(1000).alpha(0);
				view.animate().setDuration(1000).alpha(1);
				if(((ColorDrawable)view.getBackground()) == null)
					view.setBackgroundColor(Color.rgb(224, 0, 224));
				else
					view.setBackground(null);
			}
		});
		useView.setOnItemLongClickListener(new OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> parent, final View view, final int position, long id)
            {
            	AlertDialog alertDialog = new AlertDialog.Builder(context).create();
            	alertDialog.setTitle("Confirm Action");
            	alertDialog.setMessage("Remove this Nommate");
            	alertDialog.setButton(AlertDialog.BUTTON_POSITIVE,"Yes", new DialogInterface.OnClickListener() {
            	   public void onClick(DialogInterface dialog, int which) {
            		   	mateList.remove(position);
            		   	mateData = new JSONArray();
						for(int i=0; i< mateList.size(); i++)
						{
							mateData.put(mateList.get(i));
						}
						try {
							context.appData.put("mates", mateData);
						} catch (JSONException e) {}
   	            		mateAdapter.notifyDataSetChanged();
            	   }
            	});
            	alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE,"No", new DialogInterface.OnClickListener() {
	            	   public void onClick(DialogInterface dialog, int which) {}
	            });
            	// Set the Icon for the Dialog
            	alertDialog.setIcon(android.R.drawable.ic_menu_delete);
            	alertDialog.show();
                return true;
            }
        }); 
		
		populateUsers();
		return frame;
	}
	
	@Override
	public void onResume()
	 {
		 super.onResume();
		 InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		 imm.hideSoftInputFromWindow(frame.getWindowToken(), 0);
	 }

	public static void populateUsers()
	{
		try {
			mateData = context.appData.getJSONArray("mates");	
			mateList.clear();
			for(int i=0; i< mateData.length(); i++)
			{
				mateList.add(mateData.getString(i));
			}
			
		} catch (JSONException e) {}
		mateAdapter.notifyDataSetChanged();
	}

	public void closeShare(View v)
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
					ListView listView = (ListView) context.findViewById(R.id.matesList);
					SparseBooleanArray groupMates = listView.getCheckedItemPositions();
					//ArrayList<Integer> groupMatesNames = new ArrayList<Integer>();
					String groupMatesNames = "";
					JSONArray useDat = context.appData.getJSONArray("mates");
					for(int i=0; i < groupMates.size(); i++)
					{
						if(groupMates.get(i))
							groupMatesNames+=","+useDat.get(i);
					}
					System.out.println(groupMatesNames);
					
					JSONObject jsonObject = new JSONObject();
					jsonObject.accumulate("to", context.appData.getString("host")+groupMatesNames);
					JSONObject eventSon = new JSONObject();
					eventSon.accumulate("privacy", "closed");
					TimePicker cloTime = (TimePicker)context.findViewById(R.id.closeTime);
					eventSon.accumulate("hour", cloTime.getCurrentHour());
					eventSon.accumulate("minute", cloTime.getCurrentMinute());
					Calendar curTime = Calendar.getInstance();
					curTime.set(curTime.get(Calendar.YEAR), curTime.get(Calendar.MONTH), curTime.get(Calendar.DATE), cloTime.getCurrentHour(), cloTime.getCurrentMinute());
					eventSon.accumulate("date", curTime.getTimeInMillis());
					eventSon.accumulate("host", context.appData.getString("host"));
					eventSon.accumulate("location", ((EditText)context.findViewById(R.id.closeLocation)).getText());
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

	public void addNommate(View v)
	{
		FragmentManager fragmentManager = getActivity().getFragmentManager();
		CustomDialogFragment newFragment = new CustomDialogFragment();
		newFragment.show(fragmentManager, "dialog");
	}

	public static class CustomDialogFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Build the dialog and set up the button click handlers
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			// Get the layout inflater
			LayoutInflater inflater = getActivity().getLayoutInflater();
			final View popView = inflater.inflate(R.layout.popup_add_nommate,null);
			builder.setMessage("Enter Username")
			.setView(popView)
			.setPositiveButton("Add", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					EditText name = (EditText) popView.findViewById(R.id.hostnameText);
					try {
						context.appData.getJSONArray("mates").put(name.getText().toString().toUpperCase(Locale.US));
						populateUsers();
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			})
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					// Send the negative button event back to the host activity
					//mListener.onDialogNegativeClick(NoticeDialogFragment.this);
				}
			});
			return builder.create();
		}
	}
}