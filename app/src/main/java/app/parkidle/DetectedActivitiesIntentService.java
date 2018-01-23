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
import java.util.LinkedList;


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
                    activity = "IN VEHICLE";
                    break;
                case DetectedActivity.ON_BICYCLE:
                    activity = "ON BICYCLE";
                    break;
                case DetectedActivity.WALKING:
                    activity = "WALKING";
                    break;
                case DetectedActivity.ON_FOOT:
                    activity = "ON FOOT";
                    break;
                case DetectedActivity.RUNNING:
                    activity = "RUNNING";
                    break;
                case DetectedActivity.STILL:
                    activity = "STILL";
                    break;
                case DetectedActivity.TILTING:
                    activity = "TILTING";
                    break;
                case DetectedActivity.UNKNOWN:
                    activity = "UNKNOWN";
                    break;
            }
            Log.w(TAG,activity + da.getConfidence() + "%");
            //Toast.makeText(this, activity + " " + da.getConfidence() + "%", Toast.LENGTH_SHORT).show();
            Looper.loop();
            MainActivity.addDetectedActivity(activity);
            createEvent(activity);
        }
    }

    private void createEvent(String activity) {
        if (activity != "IN VEHICLE")
            return;
        LinkedList<String> activityList = MainActivity.detectedActivities;
        int size = activityList.size();
        if (size != 6)
            return;
        // se ho una sequenza ON FOOT - ON FOOT - !VEHICLE - VEHICLE - VEHICLE - VEHICLE
        // posso mandare l'evento di PARTENZA, perche lo ritengo valido
        if (!activityList.getFirst().equals("IN VEHICLE")){
            for(int i = 3; i < size; i++){
                if(!activityList.get(i).equals("IN VEHICLE")) {
                    Log.w(TAG, "Activity(" + i + ") isn't 'IN VEHICLE'");
                    return;
                }
            }
            Log.w(TAG,"Sequence Accepted! Creating DEPARTED event...");
            Date now = new Date();
            Location l = MainActivity.getMyLocation();
            Double latitude = l.getLatitude();
            Double longitude = l.getLongitude();
            Event event = new Event("numerocasuale", "DEPARTED", now.toString(), latitude.toString(), longitude.toString());
        }
        /*
        // se ho una sequenza VEHICLE - !VEHICLE - !VEHICLE - !VEHICLE
        // posso mandare l'evento di ARRIVO, perche lo ritengo valido
        if (activityList.getFirst().equals("IN VEHICLE")) {
            for (int i = 3; i < size; i++) {
                if (activityList.get(i).equals("IN VEHICLE")) {
                    Log.w(TAG, "Activity(" + i + ") is 'IN VEHICLE'");
                    return;
                }
            }
            // creo l'evento di arrivo
            Log.w(TAG,"Sequence Accepted! Creating ARRIVAL event...");
            Date now = new Date();
            Location l = MainActivity.getMyLocation();
            Double latitude = l.getLatitude();
            Double longitude = l.getLongitude();
            Event event = new Event("numerocasuale", "ARRIVED", now.toString(), latitude.toString(), longitude.toString());
        }
        */
    }
}
