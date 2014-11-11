package com.firescar96.nom.fragment;

import java.util.Calendar;

import com.firescar96.nom.R;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.TimePicker;

public class ClosedShareFragment1 extends Fragment {
	static View frame;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		 final View frame = inflater.inflate(R.layout.fragment_add_closed1, container, false);

		 TimePicker timePicker = (TimePicker) frame.findViewById(R.id.closeTime);
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
}