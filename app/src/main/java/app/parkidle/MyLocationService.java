package app.parkidle;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;


import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;

import static app.parkidle.MainActivity.getmMap;
import static app.parkidle.MainActivity.language;
import static app.parkidle.MainActivity.mIcon;
import static app.parkidle.MainActivity.mLastLocation;
import static app.parkidle.MainActivity.me;
import static app.parkidle.MainActivity.sharedPreferences;
import static com.amazonaws.AmazonServiceException.ErrorType.Service;

/**
 * Created by simonestaffa on 06/03/18.
 */

public class MyLocationService extends android.app.Service {
    private static final String TAG = "MyLocationService";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL_FOREGROUND = 1000;
    private static final int LOCATION_INTERVAL_BACKGROUND = 3000;
    private static final float LOCATION_DISTANCE = 10f;
    private boolean isAppForeground;
    public static boolean isLocationRunning;

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider) {
            Log.w(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.w(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);
            MainActivity.mLastLocation = location;
            LatLng point = new LatLng(location.getLatitude(), location.getLongitude());
            if (MainActivity.getmMap() != null) {
                if (me == null) {
                    me = getmMap().addMarker(new MarkerOptions().position(point).title("You").icon(mIcon));
                }
                if (MainActivity.isCameraFollowing) {
                    // Customize map with markers, polylines, etc.
                    //Camera Position definisce la posizione della telecamera
                    MainActivity.position = new CameraPosition.Builder()
                            .target(new LatLng(location.getLatitude(), location.getLongitude())) // Sets the new camera position
                            .zoom(16) // Sets the zoom to level 10
                            .bearing(location.getBearing()) // degree - azimut
                            .tilt(0) // Set the camera tilt to 20 degrees
                            .build(); // Builds the CameraPosition object from the builder
                    MainActivity.getmMap().animateCamera(CameraUpdateFactory
                            .newCameraPosition(MainActivity.position), 800);
                }
            }
            if (me != null) {
                ValueAnimator markerAnimator = ObjectAnimator.ofObject(me, "position",
                        new LatLngEvaluator(), me.getPosition(), point);
                markerAnimator.setDuration(800);
                markerAnimator.start();
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.w(TAG, "onProviderDisabled: " + provider);
            if(provider.equals("gps")){
                Log.w(TAG,"Checking gps..");
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.w(TAG, "onProviderEnabled: " + provider);

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.w(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.w(TAG, "onStartCommand id " + startId + " - flags " + flags + " - " + intent);
        if(!isLocationRunning) isLocationRunning = true;
        //super.onStartCommand(intent, flags, startId);
        SharedPreferences sharedPreferences = getSharedPreferences("PARKIDLE_PREFERENCES",MODE_PRIVATE);
        isAppForeground = sharedPreferences.getBoolean("isAppForeground",true);
        if(isAppForeground) {
            if (mLocationManager == null) {
                mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            }
            checkGPSEnabled(mLocationManager);
        }
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        Log.w(TAG, "onCreate");
        isLocationRunning = true;
        SharedPreferences sharedPreferences = getSharedPreferences("PARKIDLE_PREFERENCES",MODE_PRIVATE);
        isAppForeground = sharedPreferences.getBoolean("isAppForeground",true);
        initializeLocationManager();
        try {
            Log.w(TAG,"isAppForeground = " + sharedPreferences.getBoolean("isAppForeground",true));
            if(isAppForeground == true)
                mLocationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL_FOREGROUND, LOCATION_DISTANCE,
                        mLocationListeners[1]);
            else
                mLocationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL_BACKGROUND, LOCATION_DISTANCE,
                        mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.w(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.w(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            if(isAppForeground == true)
                mLocationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, LOCATION_INTERVAL_FOREGROUND, LOCATION_DISTANCE,
                        mLocationListeners[0]);
            else
                mLocationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, LOCATION_INTERVAL_BACKGROUND, LOCATION_DISTANCE,
                        mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.w(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.w(TAG, "gps provider does not exist " + ex.getMessage());
        }


    }

    @Override
    public void onDestroy()
    {
        Log.w(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
        isLocationRunning = false;
    }

    private void initializeLocationManager() {
        Log.w(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
        if(isAppForeground)
            Log.w(TAG,"Checking gps at initialize...");
            //checkGPSEnabled(mLocationManager);
    }

    public void checkGPSEnabled(LocationManager locationManager) {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.w(TAG, "Building alert...");
            //buildAlertMessage(this); // costruisce un alert che propone di attivare il GPS
            Intent i = new Intent(this, GpsDialogActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            stopSelf();
        }
        if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            if (language == 0)
                Toast.makeText(this, "Per una localizzazione più precisa attiva il WiFi", Toast.LENGTH_LONG).show();
            else
                Toast.makeText(this, "For a more accurate localization turn ON the WiFi service", Toast.LENGTH_LONG).show();
        }
    }

}

class LatLngEvaluator implements TypeEvaluator<LatLng> {
    // Method is used to interpolate the marker animation.

    private LatLng latLng = new LatLng();

    @Override
    public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
        latLng.setLatitude(startValue.getLatitude()
                + ((endValue.getLatitude() - startValue.getLatitude()) * fraction));
        latLng.setLongitude(startValue.getLongitude()
                + ((endValue.getLongitude() - startValue.getLongitude()) * fraction));
        return latLng;
    }


}