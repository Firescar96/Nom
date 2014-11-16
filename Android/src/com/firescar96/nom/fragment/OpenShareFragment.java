package com.firescar96.nom.fragment;

import java.util.Calendar;

import com.firescar96.nom.R;

import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

public class OpenShareFragment extends ShareFragment {
	
	 @Override
	 public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		 final View frame = inflater.inflate(R.layout.fragment_add_open, container, false);

		 TimePicker timePicker = (TimePicker) frame.findViewById(R.id.openTime);
		 timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
			 public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
				TextView distTime = (TextView) frame.findViewById(R.id.distTime);
				Calendar eveTime = Calendar.getInstance();
				eveTime.set(eveTime.get(Calendar.YEAR),
						eveTime.get(Calendar.MONTH), eveTime.get(Calendar.DATE),
							hourOfDay, minute, 0);

				long diff;
				if(Calendar.getInstance().getTimeInMillis() > eveTime.getTimeInMillis())
					eveTime.add(Calendar.DATE, 1);
					
				diff = eveTime.getTimeInMillis()-Calendar.getInstance().getTimeInMillis();
				
				long hour = (diff/3600000)%24;
				long min = (diff/60000)%60;
				distTime.setText(hour+":"+min);
			 }
		 });
		 return frame;
	 }

	 public void openShare(View v) {
		 TimePicker opTime = (TimePicker) context.findViewById(R.id.openTime);
		 Editable loc = ((EditText) context.findViewById(R.id.openLocation)).getText();
		 super.share("open",opTime,loc);
	 }
}