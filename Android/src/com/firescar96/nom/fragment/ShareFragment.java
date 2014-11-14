package com.firescar96.nom.fragment;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import org.json.JSONException;

import android.app.Fragment;
import android.content.Intent;
import android.text.Editable;
import android.widget.TimePicker;

import com.firescar96.nom.MainActivity;
import com.firescar96.nom.org.json.JSONObject;

public abstract class ShareFragment extends Fragment{

	static MainActivity context = MainActivity.context;
	
	public void share(String privacy,TimePicker time, Editable loc) {
		share("", privacy, time, loc,false);
	}
	
	public void share(String mates,String privacy,TimePicker time, Editable loc, boolean makeIntent) {
		try {
			JSONObject jsonObject = new JSONObject();
			jsonObject.accumulate("to", MainActivity.appData.getString("host")+mates);
			JSONObject eventSon = new JSONObject();
			eventSon.accumulate("privacy", privacy);

			Calendar curTime = Calendar.getInstance();
			curTime.set(curTime.get(Calendar.YEAR),
					curTime.get(Calendar.MONTH), curTime.get(Calendar.DATE),
					time.getCurrentHour(), time.getCurrentMinute(), 0);
			if(Calendar.getInstance().getTimeInMillis() > curTime.getTimeInMillis())
				curTime.add(Calendar.DATE, 1);
			System.out.println(curTime.getTimeInMillis());
			eventSon.accumulate("date", curTime.getTimeInMillis());
			eventSon.accumulate("host", MainActivity.appData.getString("host"));
			eventSon.accumulate("location", loc);

			byte[] hostBytes = MainActivity.appData.getString("host").getBytes(
					"UTF-8");
			Double longTime = Math.floor(curTime.getTimeInMillis() / 1000) * 1000;
			byte[] timeBytes = longTime.toString().getBytes("UTF-8");
			byte[] locBytes = loc.toString().getBytes("UTF-8");
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
			for (Byte b : both)
				hotilocBytes[i++] = b.byteValue();
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

			MainActivity.sendJSONToBackend(jsonObject);
			
			if(makeIntent) {
				Intent shareIntent = new Intent(Intent.ACTION_SEND);
				shareIntent.setType("text/plain");
				shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, "I'm hosting an event on Nom: http://nchinda2.mit.edu:666/e"+digHash);
				startActivity(Intent.createChooser(shareIntent, "Share via"));
			}
		} catch (NoSuchAlgorithmException | JSONException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

}
