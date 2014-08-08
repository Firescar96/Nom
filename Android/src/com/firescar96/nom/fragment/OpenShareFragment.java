package com.firescar96.nom.fragment;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import com.firescar96.nom.MainActivity;
import com.firescar96.nom.R;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TimePicker;

public class OpenShareFragment extends Fragment {
	static MainActivity context = MainActivity.context;
	
	 @Override
	 public View onCreateView(LayoutInflater inflater, ViewGroup container,
			 Bundle savedInstanceState) {
		 // Inflate the layout for this fragment
		 return inflater.inflate(R.layout.fragment_add_open, container, false);
	 }
	 
	public void openShare(View v) {
		try {
			JSONObject jsonObject = new JSONObject();
			JSONObject eventSon = new JSONObject();
			eventSon.accumulate("privacy", "open");
			TimePicker opTime = (TimePicker) context
					.findViewById(R.id.openTime);
			Calendar curTime = Calendar.getInstance();
			curTime.set(curTime.get(Calendar.YEAR),
					curTime.get(Calendar.MONTH), curTime.get(Calendar.DATE),
					opTime.getCurrentHour(), opTime.getCurrentMinute(), 0);
			System.out.println(curTime.getTimeInMillis());
			eventSon.accumulate("date", curTime.getTimeInMillis());
			eventSon.accumulate("host", context.appData.getString("host"));
			eventSon.accumulate("location", ((EditText) context
					.findViewById(R.id.openLocation)).getText());

			byte[] hostBytes = context.appData.getString("host").getBytes(
					"UTF-8");
			Double longTime = Math.floor(curTime.getTimeInMillis() / 1000) * 1000;
			byte[] timeBytes = longTime.toString().getBytes("UTF-8");
			byte[] locBytes = ((EditText) context
					.findViewById(R.id.openLocation)).getText().toString()
					.getBytes("UTF-8");
			Byte[] hostByt = new Byte[hostBytes.length], timeByt = new Byte[timeBytes.length], locByt = new Byte[locBytes.length];
			int i = 0;
			for (byte b : hostBytes)
				hostByt[i++] = b;
			i = 0;
			for (byte b : timeBytes)
				timeByt[i++] = b;
			i = 0;
			for (byte b : locBytes)
				locByt[i++] = b;

			ArrayList<Byte> both = new ArrayList<Byte>(Arrays.asList(hostByt));
			both.addAll(Arrays.asList(timeByt));
			both.addAll(Arrays.asList(locByt));
			i = 0;
			byte[] hotilocBytes = new byte[both.size()];
			for (Byte b : both) {
				hotilocBytes[i++] = b.byteValue();
			}
			MessageDigest md;
			md = MessageDigest.getInstance("MD5");

			System.out.println(Arrays.toString(hotilocBytes));
			byte[] thedigest;
			md.update(hotilocBytes);
			thedigest = md.digest();
			BigInteger bigInt = new BigInteger(1, thedigest);
			String digHash = bigInt.toString(30);
			System.out.println(Arrays.toString(thedigest));
			System.out.println(digHash);
			eventSon.accumulate("hash", digHash);
			jsonObject.accumulate("event", eventSon);

			context.sendJSONToBackend(jsonObject);
		} catch (NoSuchAlgorithmException | JSONException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}