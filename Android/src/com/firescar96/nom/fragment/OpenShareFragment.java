package com.firescar96.nom.fragment;

import com.firescar96.nom.R;

import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TimePicker;

public class OpenShareFragment extends ShareFragment {
	
	 @Override
	 public View onCreateView(LayoutInflater inflater, ViewGroup container,
			 Bundle savedInstanceState) {
		 // Inflate the layout for this fragment
		 return inflater.inflate(R.layout.fragment_add_open, container, false);
	 }
	 
	public void openShare(View v) {
		TimePicker opTime = (TimePicker) context.findViewById(R.id.openTime);
		Editable loc = ((EditText) context.findViewById(R.id.openLocation)).getText();
		super.share("open",opTime,loc);
	}
}