package com.firescar96.nom.fragment;

import java.util.Locale;
import java.util.concurrent.Callable;

import org.json.JSONException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;

import com.firescar96.nom.GCMIntentService;
import com.firescar96.nom.MainActivity;
import com.firescar96.nom.R;

public class HostDialogFragment extends AddNameDialog {
	static HostDialogFragment thisFrag;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		thisFrag = this;
		
		// Build the dialog and set up the button click handlers
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();
		frame = inflater.inflate(R.layout.popup_checkmate,null);
		MainActivity.setupUI(frame);
		builder.setMessage("Choose a Username")
		.setView(frame)
		.setPositiveButton("Add", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				new TaskWithCallback(new Callable<Object>() {
						   @Override
						public Object call() {addName();return null;}}).execute(null, null, null);
			}
			
			public void addName() {
				EditText hostname = (EditText) frame.findViewById(R.id.nameText);
				if(hostname.getText().length() == 0)
					return;

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
		
		precise = true;
		return builder.create();
	}
	
	@Override
	public void onStop()
	 {
		super.onStop();
		thisFrag=null;
	 }
}