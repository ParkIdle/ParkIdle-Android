package app.parkidle;

/**
 * Created by simonestaffa on 08/01/18.
 */

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 *  IntentService for handling incoming intents that are generated as a result of requesting
 *  activity updates using
 *  {@link com.google.android.gms.location.ActivityRecognitionClient#requestActivityUpdates}.
 */
public class DetectedActivitiesIntentService extends IntentService {

    private Location l;
    private String activitiesJson;
    private SharedPreferences sharedPreferences = MainActivity.sharedPreferences;
    private SharedPreferences.Editor editor = MainActivity.editor;

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
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

        // Get the list of the probable activities associated with the current state of the
        // device. Each activity is associated with a confidence level, which is an int between
        // 0 and 100.
        ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();
        Log.w(TAG,detectedActivities.toString());

        if(sharedPreferences == null)
            sharedPreferences = getSharedPreferences("PARKIDLE_PREFENCES",MODE_PRIVATE);
        activitiesJson = sharedPreferences.getString("detectedActivities","");
        String maxActivity = null;
        String activity = null;
        int maxConfidence = 0;
        // Log each activity.
        for (DetectedActivity da: detectedActivities) {

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
            //Log.w(TAG,"RECOGNIZED -> " + activity + da.getConfidence() + "%");
            if(da.getConfidence() > maxConfidence){
                maxConfidence = da.getConfidence();
                maxActivity = activity;
            }

        }
        //Toast.makeText(this, activity + " " + da.getConfidence() + "%", Toast.LENGTH_SHORT).show();
        if(activity.equals("UNKNOWN") || activity.equals("TILTING")) {
            Log.w(TAG,activity + " non tenuta in considerazione.");
            return;
        }
        addDetectedActivity(maxActivity);
        createEvent(maxActivity);
    }

    private void createEvent(String activity) {
        Log.w(TAG,"Vediamo se è un evento valido...");;
        //SharedPreferences sharedPreferences = getSharedPreferences("PARKIDLE_PREFERENCES",MODE_PRIVATE);
        //activitiesJson = sharedPreferences.getString("detectedActivities","");
        String[] split = activitiesJson.split(",");
        if (split.length != 5) {
            Log.w(TAG, "[NO] Sequenza attività troppo corta per rilevare un evento ( size < 5)");
            return;
        }

        // se ho una sequenza !VEHICLE - !VEHICLE - !VEHICLE - VEHICLE - VEHICLE
        // se ho una sequenza !BICYCLE - !BICYCLE - !BICYCLE - BICYCLE - BICYCLE
        /*if(!split[0].equals("IN VEHICLE") && !split[0].equals("ON BICYCLE")) {
            Log.w(TAG, "[0] Vediamo se sei partito...");
            if(split[1].equals("IN VEHICLE") || split[1].equals("ON BICYCLE")){
                Log.w(TAG,"[1] No non sei partito...");
                return;
            }
            if(split[2].equals("IN VEHICLE") || split[2].equals("ON BICYCLE")){
                Log.w(TAG,"[2] No non sei partito...");
                return;
            }
            if(!split[3].equals("IN VEHICLE") && !split[3].equals("ON BICYCLE")) {
                Log.w(TAG, "[3] No non sei partito... (non sei in veicolo allo stato 3)");
                return;
            }
            if(!split[4].equals("IN VEHICLE") && !split[4].equals("ON BICYCLE")) {
                Log.w(TAG, "[4] No non sei partito...");
                return;
            }
            Log.w(TAG,"[x] Sei partito!");
            Date now = new Date();
            l = MainActivity.getMyLocation();
            Double latitude = l.getLatitude();
            Double longitude = l.getLongitude();
            Event event = new Event(MainActivity.deviceIdentifier, "DEPARTED", now.toString(), latitude.toString(), longitude.toString());
            EventHandler eh = new EventHandler(event);
            Thread handler = new Thread(eh);
            handler.setName("EventHandler");
            handler.start();
        }*/
        // vecchio check
        if (!split[0].equals("IN VEHICLE") && !split[0].equals("ON BICYCLE")){
            for(int i = 1; i < split.length; i++){
                if(i < 3){
                    if(split[i].equals("IN VEHICLE") || split[i].equals("ON BICYCLE")){
                        Log.w(TAG,"[NO] Le activity 2-3 sono inVehicle o onBicycle: CHIUDO");
                        return;
                    }
                }
                else {
                    if(!split[i].equals("IN VEHICLE") && !split[i].equals("ON BICYCLE")) {
                        Log.w(TAG, "[NO] La " + (i+1) + " non è 'IN VEHICLE o BICYCLE'");
                        return;
                    }
                }
            }
            Log.w(TAG,"[SI] Sei partito!");
            Date now = new Date();
            l = MainActivity.getMyLocation();
            if(l==null){
                Log.w(TAG,"La location è null non posso mandare l'evento(partenza)");
                return;
            }
            Double latitude = l.getLatitude();
            Double longitude = l.getLongitude();
            Event event = new Event(markerIdHashcode(latitude,longitude), "DEPARTED", now.toString(), latitude.toString(), longitude.toString());
            EventHandler eh = new EventHandler(event);
            Thread handler = new Thread(eh);
            handler.setName("EventHandler");
            handler.start();
        }

        // se ho una sequenza VEHICLE - !VEHICLE - !VEHICLE - !VEHICLE
        else{
            Log.w(TAG,"[?] Vediamo se sei arrivato...");
            if(split[0].equals("IN VEHICLE") || split[0].equals("ON BICYCLE")) {
                for (int i = 1; i < split.length; i++) {
                    if (split[i].equals("IN VEHICLE") || split[i].equals("ON BICYCLE")) {
                        Log.w(TAG, "[" + i + "] No non sei arrivato...");
                        return;
                    }
                }
                // creo l'evento di arrivo
                Log.w(TAG, "[SI] Sei arrivato!");
                l = MainActivity.getMyLocation();
                if(l==null){
                    Log.w(TAG,"[!] La location è null non posso inviare l'evento (arrivo)");
                    return;
                }
                Double latitude = l.getLatitude();
                Double longitude = l.getLongitude();
                saveParking();
                Date now = new Date();
                Event event = new Event(markerIdHashcode(latitude,longitude), "ARRIVED", now.toString(), latitude.toString(), longitude.toString());

            }
        }

    }

    public void saveParking() {
        //salvataggio parcheggio
        Log.w(TAG,"Sto salvando il parcheggio...");
        if(editor == null)
            editor = sharedPreferences.edit();
        if(l != null) {
            editor.putFloat("latpark", (float) l.getLatitude());
            editor.putFloat("longpark", (float) l.getLongitude());
            editor.apply();
            Log.w(TAG,"Parcheggio salvato!");
        }
        Log.w(TAG,"Parcheggio non salvato...");
    }

    private synchronized void addDetectedActivity(String activity){
        if(activitiesJson.equals("")) activitiesJson += activity;
        else activitiesJson = activitiesJson + "," + activity;

        String[] jsonSplit = activitiesJson.split(",");
        if(jsonSplit.length > 5){
            activitiesJson = jsonSplit[1] + "," + jsonSplit[2] + "," + jsonSplit[3] + "," + jsonSplit[4] + "," + jsonSplit[5];
        }
        if(editor == null)
            editor = sharedPreferences.edit();
        editor.putString("detectedActivities", activitiesJson);
        editor.apply();

        Log.w(TAG,"@@@ACTIVITY LIST@@@ : " + activitiesJson);
    }

    private String markerIdHashcode(Double lat, Double lon){
        long hasho = (long)((lat*15661+lon*27773)/33911);
        return ""+hasho;
    }
}
