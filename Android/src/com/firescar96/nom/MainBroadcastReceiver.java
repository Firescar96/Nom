package com.firescar96.nom;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class MainBroadcastReceiver extends WakefulBroadcastReceiver {
    
    @Override
    public void onReceive(Context context, Intent intent) 
    {
    	System.out.println("stating intent some thing");
    	if(intent.getAction() == null)
    		return;
    	System.out.println("stating intent"+intent.getAction());
		if(intent.getAction().equals("com.firescar96.nom.update.times") && MainActivity.context != null)
		{
			System.out.println("stating update");
			MainActivity.context.scheduleTimeUpdate();
		}
		
		if(intent.getAction().equals("com.google.android.c2dm.intent.RECEIVE"))
		{
	        // Explicitly specify that GcmIntentService will handle the intent.
	        ComponentName comp = new ComponentName(context.getPackageName(),
	                GCMIntentService.class.getName());
	        // Start the service, keeping the device awake while it is launching.
	        startWakefulService(context, (intent.setComponent(comp)));
	        setResultCode(Activity.RESULT_OK);
		}
    }
}
