package com.firescar96.nom;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.firescar96.nom.MainActivity.EventsArrayAdapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;

public class ClosedShareFragment extends Fragment {
    static MainActivity context = MainActivity.context;
    
	static View frame;
	static ClosedShareFragment thisFrag;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
    	thisFrag = this;
    	
    	frame = inflater.inflate(R.layout.fragment_add_closed, container, false);
    	ListView listView = (ListView) frame.findViewById(R.id.matesList);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        populateUsers();
        return frame;
    }
    
    public static void populateUsers()
    {
    	try {
			JSONArray useDat = context.appData.getJSONArray("mates");
			System.out.println(useDat);
			//Setup the listview adapter for closed events
			ListView useView = (ListView) frame.findViewById(R.id.matesList);
			ArrayList<String> useList = new ArrayList<String>();

			for(int i=0; i< useDat.length(); i++)
			{
				useList.add(useDat.getString(i));
			}
			
			UsersArrayAdapter useAdapter = thisFrag.new UsersArrayAdapter(context,
			    android.R.layout.simple_list_item_1, useList);
			useView.setAdapter(useAdapter);
	    	context.mainPagerAdapter.notifyDataSetChanged();
	    	
	    	useView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, final View view, int position, long id) 
				{
					final String item = (String) parent.getItemAtPosition(position);
					view.animate().setDuration(1000).alpha(0);
					view.animate().setDuration(1000).alpha(1);
				}
			});
    	} catch (JSONException e) {}
    }
    
    public class UsersArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public UsersArrayAdapter(Context context, int textViewResourceId,
            List<String> objects) {
          super(context, textViewResourceId, objects);
          for (int i = 0; i < objects.size(); ++i) {
            mIdMap.put(objects.get(i), i);
          }
        }

        @Override
        public long getItemId(int position) {
          String item = getItem(position);
          return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
          return true;
        }
    }
    
    //
    public void addNommate(View v)
	{
    	showDialog();
	}
    
    public void showDialog() {
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
            			EditText name = (EditText) popView.findViewById(R.id.nomMateName);
            			try {
							context.appData.getJSONArray("mates").put(name.getText().toString());
							System.out.println(name);
							System.out.println(context.appData.getJSONArray("mates"));
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