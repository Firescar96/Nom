package com.firescar96.nom.fragment;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;

import com.firescar96.nom.MainActivity;
import com.firescar96.nom.R;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class ClosedShareFragment2 extends ShareFragment {
	static MainActivity context;


	static ClosedShareFragment2 thisFrag;
	static View frame;
	static JSONArray mateData;
	static ArrayList<String> mateList;
	static ArrayAdapter<String> mateAdapter;
	static ArrayList<Boolean> mateSelected;
	static View matePopView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		thisFrag = this;
		context = (MainActivity) getActivity();
		frame = inflater.inflate(R.layout.fragment_add_closed2, container, false);
		try {
			mateData = MainActivity.appData.getJSONArray("mates");
		} catch (JSONException e) {}
		mateList = new ArrayList<String>();
		mateSelected = new ArrayList<Boolean>();
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
				{
					view.setBackgroundColor(Color.rgb(224, 0, 224));
					while(mateSelected.size() < position+1) mateSelected.add(false);
					mateSelected.set(position, true);
				}
				else
				{
					view.setBackground(null);
					while(mateSelected.size() < position+1) mateSelected.add(false);
					mateSelected.set(position, false);
				}
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

       					view.setBackground(null);
   	            		Bundle data = new Bundle();
   	            		data.putString("command", "removeNommate");
   	            		data.putInt("position", position);
   	            		Message msg = new Message();
   	            		msg.setData(data);
   	            		contextHandler.sendMessage(msg);
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
		 
		ListView listView = (ListView) frame.findViewById(R.id.matesList);
		listView.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, mateList)); 
	 }

	public static void populateUsers()
	{
		try {
			mateData = MainActivity.appData.getJSONArray("mates");	
			mateList.clear();
			for(int i=0; i< mateData.length(); i++)
			{
				mateList.add(mateData.getString(i));
			}
			
		} catch (JSONException e) {}
		mateAdapter.notifyDataSetChanged();
	}

	public void closeShare(View v) {

		String groupMatesNames = "";
		try {
			JSONArray useDat;
			useDat = MainActivity.appData.getJSONArray("mates");
			for (int i = 0; i < mateSelected.size(); i++) {
				System.out.println(i);
				if (mateSelected.get(i))
					groupMatesNames += "," + useDat.get(i);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		TimePicker cloTime = (TimePicker) context.findViewById(R.id.closeTime);
		Editable loc = ((EditText) context.findViewById(R.id.closeLocation)).getText();
		
		super.share(groupMatesNames,"closed",cloTime,loc,false);
	}

	public void mediaShare(View v) {
		String groupMatesNames = "";
		try {
			JSONArray useDat;
			useDat = MainActivity.appData.getJSONArray("mates");
			for (int i = 0; i < mateSelected.size(); i++) {
				System.out.println(i);
				if (mateSelected.get(i))
					groupMatesNames += "," + useDat.get(i);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		TimePicker cloTime = (TimePicker) context.findViewById(R.id.closeTime);
		Editable loc = ((EditText) context.findViewById(R.id.closeLocation)).getText();
		
		super.share(groupMatesNames,"closed",cloTime,loc,true);
	}
	
	public void addNommate(View v)
	{
		FragmentManager fragmentManager = getActivity().getFragmentManager();
		MateDialogFragment newFragment = new MateDialogFragment();
		newFragment.show(fragmentManager, "dialog");
	}
	
	public static class MateDialogFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Build the dialog and set up the button click handlers
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			// Get the layout inflater
			LayoutInflater inflater = getActivity().getLayoutInflater();
			matePopView = inflater.inflate(R.layout.popup_checkmate,null);
			builder.setMessage("Enter Username")
			.setView(matePopView)
			.setPositiveButton("Add", new DialogInterface.OnClickListener() {
	    		RelativeLayout good = (RelativeLayout) matePopView.findViewById(R.id.nameGood);
				EditText matename = (EditText) matePopView.findViewById(R.id.nameText);
				public void onClick(DialogInterface dialog, int id) {
					EditText name = (EditText) matePopView.findViewById(R.id.nameText);
					if(good.getVisibility() != View.VISIBLE || matename.getText().length() == 0)
					{
						thisFrag.addNommate(null);
						return;
					}
					try {
						MainActivity.appData.getJSONArray("mates").put(name.getText().toString().toUpperCase(Locale.US));
						mateList.add(name.getText().toString().toUpperCase(Locale.US));
						populateUsers();
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			})
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
				}
			});
			return builder.create();
		}
	}
	
	public void checkHostname()
	{           
        System.out.println("working the check");
		new AsyncTask<Object, Object, Object>() {
			
			@Override
			protected Object doInBackground(Object... arg0) {
				if (Looper.myLooper() == null) {
			        Looper.prepare();
			    }
            	Message msg = new Message();
            	Bundle data = new Bundle();
            	data.putString("command", "checkName");
            	data.putString("value", "progress");
				msg.setData(data);
        	    contextHandler.sendMessage(msg);
        	    
				InputStream inputStream = null;

				try {

					// 1. create HttpClient
					HttpClient httpclient = new DefaultHttpClient();

					// 2. make POST request to the given URL
    					EditText matename = (EditText) matePopView.findViewById(R.id.nameText);
    					System.out.println(matename.getText());
   
    					HttpGet httpGet = new HttpGet("http://nchinda2.mit.edu:666?checkName="+matename.getText().toString().toUpperCase(Locale.US));

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
	            	data = new Bundle();
	            	data.putString("command", "checkName");
					if(inputStream != null)
						if(convertStreamToString(inputStream).contains("true"))
			            	data.putString("value", "true");
						else
			            	data.putString("value", "false");
					else
		            	data.putString("value", "progress");
					msg.setData(data);
					contextHandler.sendMessage(msg);

				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
		}.execute(null, null, null);
	}
	
	private static Handler contextHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	super.handleMessage(msg);
           System.out.println(msg.getData());
           if(msg.getData().getString("command").equals("removeNommate"))
           {
	        	mateList.remove(msg.getData().getInt("position"));
	            System.out.println(mateList);
			   	mateData = new JSONArray();
				for(int i=0; i< mateList.size(); i++)
				{
					mateData.put(mateList.get(i));
				}
				try {
					MainActivity.appData.put("mates", mateData);
				} catch (JSONException e) {}
	      		mateAdapter.notifyDataSetChanged();
           }
           if(msg.getData().getString("command").equals("checkName"))
           {
        	   ProgressBar mateProg = (ProgressBar) matePopView.findViewById(R.id.nameProgress);
	    		RelativeLayout good = (RelativeLayout) matePopView.findViewById(R.id.nameGood);
	    		RelativeLayout bad = (RelativeLayout) matePopView.findViewById(R.id.nameBad);
	    		if(msg.getData().getString("value").equals("progress"))
	    		{
	    			mateProg.setVisibility(View.VISIBLE);
	    			good.setVisibility(View.GONE);
	    			bad.setVisibility(View.GONE);
	    		}
		    	if(msg.getData().getString("value").equals("true"))
	        	{
	        		bad.setVisibility(View.VISIBLE);
	        		mateProg.setVisibility(View.GONE);
	        		good.setVisibility(View.GONE);
	        	}
		    	if(msg.getData().getString("value").equals("false"))
	        	{
	        		good.setVisibility(View.VISIBLE);
	        		mateProg.setVisibility(View.GONE);
	        		bad.setVisibility(View.GONE);
	        	}
           }
           System.out.println("done");
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
}