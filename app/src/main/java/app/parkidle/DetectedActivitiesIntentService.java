package app.parkidle;

/**
 * Created by simonestaffa on 08/01/18.
 */

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;
import java.util.Date;


/**
 *  IntentService for handling incoming intents that are generated as a result of requesting
 *  activity updates using
 *  {@link com.google.android.gms.location.ActivityRecognitionClient#requestActivityUpdates}.
 */
public class DetectedActivitiesIntentService extends IntentService {

    protected static final String TAG = "DetectedActivitiesIS";

    /**
     * This constructor is required, and calls the super IntentService(String)
     * constructor with the name for a worker thread.
     */
    public DetectedActivitiesIntentService() {
        // Use the TAG to name the worker thread.
        super(TAG);

    }

    @Override
    public void onCreate() {
        super.onCreate();
        //Log.w(TAG,"Creating an IntentService");
    }

    /**
     * Handles incoming intents.
     * @param intent The Intent is provided (inside a PendingIntent) when requestActivityUpdates()
     *               is called.
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void onHandleIntent(Intent intent) {
        //Log.w(TAG,"HANDLING INTENT");
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

        // Get the list of the probable activities associated with the current state of the
        // device. Each activity is associated with a confidence level, which is an int between
        // 0 and 100.
        ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();
        Log.w(TAG,detectedActivities.toString());

        // Log each activity.
        for (DetectedActivity da: detectedActivities) {
            /*if(da.getType() == DetectedActivity.IN_VEHICLE) {
                Log.w(TAG,"IN VEHICLE");
                Toast.makeText(this, "IN VEHICLE!", Toast.LENGTH_SHORT).show();
            }
            if(da.getType() == DetectedActivity.STILL){
                Log.w(TAG,"STILL");
            }*/
            Looper.getMainLooper();
            String activity = null;
            switch(da.getType()){
                case DetectedActivity.IN_VEHICLE:
                    Log.w(TAG,"IN VEHICLE " + da.getConfidence() + "%");
                    Toast.makeText(this, "IN VEHICLE", Toast.LENGTH_SHORT).show();
                    activity = "IN VEHICLE";
                    break;
                case DetectedActivity.ON_BICYCLE:
                    Log.w(TAG,"ON BICYCLE " + da.getConfidence() + "%");
                    Toast.makeText(this, "ON BICYCLE", Toast.LENGTH_SHORT).show();
                    activity = "ON BICYCLE";
                    break;
                case DetectedActivity.WALKING:
                    Log.w(TAG,"WALKING " + da.getConfidence() + "%");
                    Toast.makeText(this, "WALKING", Toast.LENGTH_SHORT).show();
                    activity = "WALKING";
                    break;
                case DetectedActivity.ON_FOOT:
                    Log.w(TAG,"ON FOOT " + da.getConfidence() + "%");
                    Toast.makeText(this, "ON FOOT", Toast.LENGTH_SHORT).show();
                    activity = "ON FOOT";
                    break;
                case DetectedActivity.RUNNING:
                    Log.w(TAG,"RUNNING " + da.getConfidence() + "%");
                    Toast.makeText(this, "RUNNING", Toast.LENGTH_SHORT).show();
                    activity = "IRUNNING";
                    break;
                case DetectedActivity.STILL:
                    Log.w(TAG,"STILL " + da.getConfidence() + "%");
                    Toast.makeText(this, "STILL", Toast.LENGTH_SHORT).show();
                    activity = "STILL";
                    break;
                case DetectedActivity.TILTING:
                    Log.w(TAG,"TILTING " + da.getConfidence() + "%");
                    Toast.makeText(this, "TILTING", Toast.LENGTH_SHORT).show();
                    activity = "TILTING";
                    break;
                case DetectedActivity.UNKNOWN:
                    Log.w(TAG,"UNKNOWN " + da.getConfidence() + "%");
                    Toast.makeText(this, "UNKNOWN", Toast.LENGTH_SHORT).show();
                    activity = "UNKNOWN";
                    break;
            }
            Looper.loop();
            createEvent(activity);
        }
    }

    private void createEvent(String activity){
        Date now = new Date();
        Location l = MainActivity.getMyLocation();
        Double latitude = l.getLatitude();
        Double longitude = l.getLongitude();
        Event event = new Event("numerocasuale",activity,now.toString(),latitude.toString(),longitude.toString());
    }
}
