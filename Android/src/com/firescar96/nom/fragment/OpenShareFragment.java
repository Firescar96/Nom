package com.firescar96.nom.fragment;

import java.io.InputStream;
import java.util.Calendar;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
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
	 
	 public void openShare(View v)
		{
		 new AsyncTask<Object, Object, Object>() {
			 @Override
			 protected Object doInBackground(Object... arg0) {
				 String msg = "";
				 InputStream inputStream = null;

				 try {

					 // 1. create HttpClient
					 HttpClient httpclient = new DefaultHttpClient();

					 // 2. make POST request to the given URL
					 HttpPost httpPost = new HttpPost("http://nchinda2.mit.edu:666");

					 String json = "";

					 // 3. build jsonObject
					 JSONObject jsonObject = new JSONObject();
					 JSONObject eventSon = new JSONObject();
					 eventSon.accumulate("privacy", "open");
					 TimePicker opTime = (TimePicker)context.findViewById(R.id.openTime);
					 eventSon.accumulate("hour", opTime.getCurrentHour());
					 eventSon.accumulate("minute", opTime.getCurrentMinute());
					 Calendar curTime = Calendar.getInstance();
					 curTime.set(curTime.get(Calendar.YEAR), curTime.get(Calendar.MONTH), curTime.get(Calendar.DATE), opTime.getCurrentHour(), opTime.getCurrentMinute());
					 eventSon.accumulate("date", curTime.getTimeInMillis());
					 eventSon.accumulate("host", context.appData.getString("host"));
					 eventSon.accumulate("location", ((EditText)context.findViewById(R.id.openLocation)).getText());
					 jsonObject.accumulate("event", eventSon);

					 // 4. convert JSONObject to JSON to String
					 json = jsonObject.toString();

					 // 5. set json to StringEntity
					 StringEntity se = new StringEntity(json);

					 // 6. set httpPost Entity
					 httpPost.setEntity(se);

					 // 7. Set some headers to inform server about the type of the content   
					 httpPost.setHeader("Accept", "application/json");
					 httpPost.setHeader("Content-type", "application/json");

					 HttpParams httpParams = httpclient.getParams();
					 HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
					 HttpConnectionParams.setSoTimeout(httpParams, 5000);
					 httpPost.setParams(httpParams);

					 // 8. Execute POST request to the given URL
					 System.out.println("executing"+json);
					 HttpResponse httpResponse = httpclient.execute(httpPost);
					 // 9. receive response as inputStream
					 inputStream = httpResponse.getEntity().getContent();

					 // 10. convert inputstream to string
					 if(inputStream != null)
						 msg = "It worked";
					 else
						 msg = "Did not work!";

				 } catch (Exception e) {
					 e.printStackTrace();
				 }
				 System.out.println(msg);
				 return msg;
			 }
		 }.execute(null, null, null);
		}
}