package com.firescar96.nom.fragment;

import com.firescar96.nom.R;
import com.firescar96.nom.R.layout;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ClosedShareFragment1 extends Fragment {
	static View frame;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
    	return inflater.inflate(R.layout.fragment_add_closed1, container, false);
    }
}