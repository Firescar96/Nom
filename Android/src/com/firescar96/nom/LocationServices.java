package com.firescar96.nom;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;

import com.firescar96.nom.org.json.JSONObject;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class LocationServices implements
GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener, LocationListener{

	MainActivity context = MainActivity.context;
	LocationClient locClient;
	LocationRequest locRequest;
	
	public LocationServices() 
	{
		locClient = new LocationClient(context, this, this);
		
		locRequest = LocationRequest.create();
        // Use high accuracy
        locRequest.setPriority(
        		LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval to 5 seconds
        locRequest.setInterval(5000);
        // Set the fastest update interval to 5 second
        locRequest.setFastestInterval(5000);
	}
	
	public void connect()
	{
		locClient.connect();
		
	}
	
	public void disconnect()
	{
		locClient.disconnect();
	}
	
	public Location getLastLocation()
	{
		return locClient.getLastLocation();
	}
	
	/*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        // Display the connection status
        //Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
    	locClient.requestLocationUpdates(locRequest, this);
    	sendLocationToBackend();
    }

    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onDisconnected() {
        // Display the connection status
        //Toast.makeText(this, "Lost location, using lastKnown coordinates",
                //Toast.LENGTH_SHORT).show();
    }

    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution())
			System.out.println("location could not happen but a solution exists");
			/*
			 * Thrown if Google Play services canceled the original
			 * PendingIntent
			 */
		else
			/*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            System.exit(1);
    }
    
    @Override
    public void onLocationChanged(Location location) {
    	sendLocationToBackend();
    }
    
    public void sendLocationToBackend() {
        new AsyncTask<Object,Object,Object>() {
			@Override
			protected Object doInBackground(Object ... param) {
                try {
                    
                    JSONObject jsonObject = new JSONObject();
                    JSONObject lokiSon = new JSONObject();
                    Location loki = getLastLocation();
                    lokiSon.accumulate("latitude", loki.getLatitude());
                    lokiSon.accumulate("longitude", loki.getLongitude());
                    jsonObject.put("location", lokiSon);
                    jsonObject.accumulate("host", MainActivity.appData.getString("host"));
         
                    MainActivity.sendJSONToBackend(jsonObject);
         
                } catch (Exception e) {
                    e.printStackTrace();;
                }
				return param;
            }
        }.execute(null, null, null);
    }
}
