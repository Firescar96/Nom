package com.firescar96.nom.fragment;

import java.util.ArrayList;
import java.util.Locale;

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
import android.os.Build;
import android.os.Bundle;
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
            		   	mateList.remove(position);
            		   	mateData = new JSONArray();
						for(int i=0; i< mateList.size(); i++)
						{
							mateData.put(mateList.get(i));
						}
						try {
							MainActivity.appData.put("mates", mateData);
						} catch (JSONException e) {}
						view.setBackground(null);
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
			// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		TimePicker cloTime = (TimePicker) context.findViewById(R.id.closeTime);
		Editable loc = ((EditText) context.findViewById(R.id.closeLocation)).getText();
		
		super.share(groupMatesNames,"closed",cloTime,loc,true);
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
						MainActivity.appData.getJSONArray("mates").put(name.getText().toString().toUpperCase(Locale.US));
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