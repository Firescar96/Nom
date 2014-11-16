package com.firescar96.nom.fragment;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.concurrent.Callable;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.DialogFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.EditText;

import com.firescar96.nom.GCMIntentService;
import com.firescar96.nom.MainActivity;
import com.firescar96.nom.R;

public abstract class AddNameDialog extends DialogFragment {
	
	static MainActivity context = MainActivity.context;
	protected static View frame;
	protected static boolean precise; //whether we are matching the entered name
	protected static boolean goodName; //whether the foundName is good

	public void checkName()
	{   
		EditText hostname = (EditText) frame.findViewById(R.id.nameText);
		if(hostname.getText().length() == 0) {
			Message msg = new Message();
			Bundle data = new Bundle();
			data.putString("command", "checkName");
			data.putString("value", "false");
			msg.setData(data);
			contextHandler.sendMessage(msg);
		}
		else
			new TaskWithCallback(null).execute(null, null, null);
	}
	
	public class TaskWithCallback extends AsyncTask<Object, Object, Object> {

		Callable<Object> callback;
		public TaskWithCallback(Callable<Object> callback)
		{
			this.callback = callback != null? callback : new Callable<Object>() {
				   @Override
				public Object call() {return null;}
				};
		}
		
			@Override
			protected Object doInBackground(Object... arg0) {
				if (Looper.myLooper() == null)
					Looper.prepare();
				Message msg = new Message();
				Bundle data = new Bundle();
				data.putString("command", "checkName");
				data.putString("value", "progress");
				msg.setData(data);
				contextHandler.sendMessage(msg);

				InputStream inputStream = null;

				try {

					// 1. create HttpClient
					HttpClient httpclient = new DefaultHttpClient();

					// 2. make POST request to the given URL
					EditText name = (EditText) frame.findViewById(R.id.nameText);
					System.out.println(name.getText());

					HttpGet httpGet;
					if(precise)
						httpGet = new HttpGet("http://nchinda2.mit.edu:666?checkName="+name.getText().toString().toUpperCase(Locale.US)+"&regId="+GCMIntentService.getRegistrationId(context));
					else
						httpGet = new HttpGet("http://nchinda2.mit.edu:666?checkName="+name.getText().toString().toUpperCase(Locale.US));
					
					// 7. Set some headers to inform server about the type of the content   
					httpGet.setHeader("Accept", "application/json");
					httpGet.setHeader("Content-type", "application/json");

					HttpParams httpParams = httpclient.getParams();
					HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
					HttpConnectionParams.setSoTimeout(httpParams, 5000);
					httpGet.setParams(httpParams);

					// 8. Execute POST request to the given URL
					System.out.println("executing");
					HttpResponse httpResponse = httpclient.execute(httpGet);
					// 9. receive response as inputStream
					inputStream = httpResponse.getEntity().getContent();

					// 10. convert inputstream to string
					msg = new Message();
					data = new Bundle();
					data.putString("command", "checkName");
					if(inputStream != null)
						if(convertStreamToString(inputStream).contains("true"))
							data.putString("value", "true");
						else
							data.putString("value", "false");
					else
						data.putString("value", "progress");
					msg.setData(data);
					msg.obj = callback;
					contextHandler.sendMessage(msg);

				} catch (Exception e) {
					e.printStackTrace();
				}
				return false;
			}
		}
		

	protected static Handler contextHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if(msg.getData().getString("command").equals("checkName"))
			{
				View visible;
				View gone1;
				View gone2;
				if(msg.getData().getString("value").equals("true"))
				{
					visible = frame.findViewById(R.id.nameGood);
					gone1 = frame.findViewById(R.id.nameBad);
					gone2 = frame.findViewById(R.id.nameProgress);
					goodName = true;
					try {
						((Callable<?>)msg.obj).call();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				else if(msg.getData().getString("value").equals("false"))
				{
					
					goodName = false;
					visible = frame.findViewById(R.id.nameBad);
					gone1 = frame.findViewById(R.id.nameGood);
					gone2 = frame.findViewById(R.id.nameProgress);
				}
				else {
					goodName = false;
					visible = frame.findViewById(R.id.nameProgress);
					gone1 = frame.findViewById(R.id.nameGood);
					gone2 = frame.findViewById(R.id.nameBad);
				}
				visible.setVisibility(View.VISIBLE);
				gone1.setVisibility(View.GONE);
				gone2.setVisibility(View.GONE);
			}
		}
	};

	protected String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null)
				sb.append(line + "\n");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
}