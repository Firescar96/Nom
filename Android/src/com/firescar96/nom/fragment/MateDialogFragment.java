package com.firescar96.nom.fragment;

import java.util.Locale;
import java.util.concurrent.Callable;

import org.json.JSONException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.EditText;

import com.firescar96.nom.MainActivity;
import com.firescar96.nom.R;

public class MateDialogFragment extends AddNameDialog {
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Build the dialog and set up the button click handlers
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();
		frame = inflater.inflate(R.layout.popup_checkmate,null);
		MainActivity.setupUI(frame);
		builder.setMessage("Enter Username")
		.setView(frame)
		.setPositiveButton("Add", new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int id) {
				Log.i("MateDialogFragment", "adds");
				new TaskWithCallback(new Callable<Object>() {
						   @Override
						public Object call() {addName();return null;}}).execute(null, null, null);
			}
			
			public void addName() {
				EditText matename = (EditText) frame.findViewById(R.id.nameText);
				if(!goodName  || matename.getText().length() == 0)
					return;
				
				try {
					MainActivity.appData.getJSONArray("mates").put(matename.getText().toString().toUpperCase(Locale.US));
					Message msg = new Message();
					Bundle data = new Bundle();
					data.putString("command", "updateNommates");
					msg.setData(data);
					contextHandler.sendMessage(msg);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		})
		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
			}
		});

		goodName = false;
		matchName = true;
		
		return builder.create();
	}
	
	private static Handler contextHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Message superMsg = new Message();
			superMsg.setData(msg.getData());
			AddNameDialog.contextHandler.sendMessage(superMsg);
			if(msg.getData().getString("command").equals("updateNommate"));
				context.updateNommates();
		}
	};
}
