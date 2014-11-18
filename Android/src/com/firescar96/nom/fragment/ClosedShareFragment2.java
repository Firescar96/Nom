package com.firescar96.nom.fragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import org.json.JSONException;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.util.Log;
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

import com.firescar96.nom.MainActivity;
import com.firescar96.nom.R;
import com.firescar96.nom.org.json.JSONArray;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class ClosedShareFragment2 extends ShareFragment {
	static MainActivity context;

	static private View frame;
	private static WeakReference<ClosedShareFragment2> weakThis;
	private ArrayList<String> mateList;
	private ArrayAdapter<String> mateAdapter;
	private ArrayList<Boolean> mateSelected;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		context = (MainActivity) getActivity();
		weakThis = new WeakReference<ClosedShareFragment2>(this);
		frame = inflater.inflate(R.layout.fragment_add_closed2, container, false);
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
				if((ColorDrawable)view.getBackground() == null)
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

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, final View view, final int position, long id)
			{
				AlertDialog alertDialog = new AlertDialog.Builder(context).create();
				alertDialog.setTitle("Confirm Action");
				alertDialog.setMessage("Remove this Nommate");
				alertDialog.setButton(AlertDialog.BUTTON_POSITIVE,"Yes", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

						view.setBackground(null);
						try {
							Object elem = MainActivity.appData.getJSONArray("mates").remove(position);
							Bundle data = new Bundle();
							data.putString("element", (String) elem);
							Message msg= new Message();
							msg.setData(data);
							msg.what = REMOVE_COMMAND;
							contextHandler.sendMessage(msg);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});
				alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE,"No", new DialogInterface.OnClickListener() {
					@Override
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

	public void populateUsers()
	{
		try {
			JSONArray mateData = MainActivity.appData.getJSONArray("mates");	
			mateList.clear();
			for(int i=0; i< mateData.length(); i++)
				mateList.add(mateData.getString(i));

		} catch (JSONException e) {}
		Log.i("ClosedShare", "data set changed");
		ListView useView = (ListView) frame.findViewById(R.id.matesList);
		mateAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, mateList);
		useView.setAdapter(mateAdapter);
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
		new MateDialogFragment().show(fragmentManager, "dialog");
	}

	private static int REMOVE_COMMAND = 1;
	private static Handler contextHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if(msg.what == REMOVE_COMMAND) {
				weakThis.get().mateAdapter.remove(msg.getData().getString("element"));
				weakThis.get().mateAdapter.notifyDataSetChanged();
				Log.i("ClosedShare", "data set remove "+msg.getData().getString("element"));
			}
		}
	};
	
	public void checkName()
	{
		MateDialogFragment.thisFrag.checkName();
	}
}