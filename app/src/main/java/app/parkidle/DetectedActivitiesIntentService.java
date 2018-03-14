package app.parkidle;

/**
 * Created by simonestaffa on 08/01/18.
 */

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Looper;
import android.support.annotation.Nullable;
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
import java.util.concurrent.ExecutionException;


/**
 *  IntentService for handling incoming intents that are generated as a result of requesting
 *  activity updates using
 *  {@link com.google.android.gms.location.ActivityRecognitionClient#requestActivityUpdates}.
 */
public class DetectedActivitiesIntentService extends IntentService {

    private Location l;
    private Location eventLocation;
    private Date eventDate;
    private boolean wasInVehicle;
    private String activitiesJson;
    private String activitiesLocations;
    private SharedPreferences sharedPreferences = MainActivity.sharedPreferences;
    private SharedPreferences.Editor editor = MainActivity.editor;
    private Date lastSignal;
    private Boolean parkedOnce=false;

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
        activitiesLocations = sharedPreferences.getString("activitiesLocations","");
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
        if(maxActivity.equals("UNKNOWN") || maxActivity.equals("TILTING")) {
            Log.w(TAG,maxActivity + " non tenuta in considerazione.");
            return;
        }
        /*if(maxActivity.equals("IN VEHICLE") || maxActivity.equals("ON BICYCLE")){
            if(wasInVehicle == false) {
                wasInVehicle = true;
                eventLocation = MainActivity.getMyLocation(); // when i first started
                //eventDate = new Date(); // now
            }
            /*if(eventDate != null){
                Date now = new Date();
                String time1 = eventDate.toString().split(" ")[3];
                String hour1 = time1.split(":")[0];
                String minutes1 = time1.split(":")[1];
                String seconds1 = time1.split(":")[2];

                String time2 = now.toString().split(" ")[3];
                String hour2 = time2.split(":")[0];
                String minutes2 = time2.split(":")[1];
                String seconds2 = time2.split(":")[2];

                if(Integer.parseInt(hour2) > Integer.parseInt(hour1)){
                    if(60 - Integer.parseInt(minutes2) - Integer.parseInt(minutes1) >= 20){
                        wasInVehicle = false;
                    }
                }else if(Integer.parseInt(hour2) == Integer.parseInt(hour1)){
                    if(Integer.parseInt(minutes2) - Integer.parseInt(minutes1) >= 20){
                        wasInVehicle = false;
                    }
                }
            }   */
        //}
        addDetectedActivity(maxActivity,MainActivity.getMyLocation());
        createEvent(maxActivity);
    }

    private void createEvent(String activity) {
        Log.w(TAG,"Vediamo se è un evento valido...");
        //SharedPreferences sharedPreferences = getSharedPreferences("PARKIDLE_PREFERENCES",MODE_PRIVATE);
        //activitiesJson = sharedPreferences.getString("detectedActivities","");
        String[] split = activitiesJson.split(",");
        String[] split2 = activitiesLocations.split(",");
        if (split.length != 5 || split2.length != 5) {
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
            //Double latitude = l.getLatitude();
            //Double longitude = l.getLongitude();
            //Double latitude = eventLocation.getLatitude(); //location di quando sei partito
            //Double longitude = eventLocation.getLongitude();
            Double latitude = Double.parseDouble(activitiesLocations.split(",")[2].split("-")[0]);
            Double longitude = Double.parseDouble(activitiesLocations.split(",")[2].split("-")[1]);
            Event event = new Event(markerIdHashcode(latitude, longitude), "DEPARTED", now.toString(), latitude.toString(), longitude.toString());
            MainActivity.parcheggisegnalati+=1;
            MainActivity.editor.putInt("parcheggiorank", MainActivity.parcheggisegnalati);
            MainActivity.editor.commit();

            EventHandler eh = new EventHandler(event);
            Thread handler = new Thread(eh);
            handler.setName("EventHandler");
            handler.start();

            if(editor == null)
                editor = sharedPreferences.edit();
            editor.putFloat("latpark", 0);
            editor.putFloat("longpark", 0);
            editor.commit();
            Log.w(TAG,"Sei partito. Parcheggio cancellato!");
            /*if(trafficCheck(now)) {//TRUE se non sei nel traffico, FALSE se sei nel traffico
                parkedOnce=false;
                Event event = new Event(markerIdHashcode(latitude, longitude), "DEPARTED", now.toString(), latitude.toString(), longitude.toString());
                MainActivity.parcheggisegnalati+=1;
                MainActivity.editor.putInt("parcheggiorank", MainActivity.parcheggisegnalati);
                MainActivity.editor.commit();

                EventHandler eh = new EventHandler(event);
                Thread handler = new Thread(eh);
                handler.setName("EventHandler");
                handler.start();
            }*/
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
                //Double latitude = l.getLatitude();
                //Double longitude = l.getLongitude();
                Double latitude = Double.parseDouble(activitiesLocations.split(",")[3].split("-")[0]);
                Double longitude = Double.parseDouble(activitiesLocations.split(",")[3].split("-")[1]);
                saveParking();
                Date now = new Date();
                /*if (!parkedOnce){
                    parkedOnce = true; // non ti fà parcheggiare piu di una volta
                    setLastSignal(now);
                }*/
                Event event = new Event(markerIdHashcode(latitude,longitude), "ARRIVED", now.toString(), latitude.toString(), longitude.toString());

                //wasInVehicle = false;
            }
        }

    }

    public Date getLastSignal(){
        return this.lastSignal;
    }

    public void setLastSignal(Date d){
        this.lastSignal = d;
        return;
    }

    public boolean trafficCheck(Date now){

        if (getLastSignal()==null){
            setLastSignal(now);
            return true;
        }

        Log.w("trafficCHECK"," checking if we're in a traffic queue");
        String now1 = now.toString();
        String time1 = now1.split(" ")[3]; // current time
        String hour1 = time1.split(":")[0];
        String minutes1 = time1.split(":")[1];
        String seconds1 = time1.split(":")[2];

        String lastChecked = getLastSignal().toString();
        String time2 = lastChecked.split(" ")[3]; // event time
        String hour2 = time2.split(":")[0];
        String minutes2 = time2.split(":")[1];
        String seconds2 = time2.split(":")[2];

        //controllo che tra il LastSignal e la data attuale siano passati almeno 7 minuti
        if((Integer.parseInt(hour1)==Integer.parseInt(hour2) && Integer.parseInt(minutes1)-Integer.parseInt(minutes2) > 7) ||
                (Integer.parseInt(hour1) > Integer.parseInt(hour2) && Integer.parseInt(minutes1)+60-Integer.parseInt(minutes2) > 7 ) ||
                (Integer.parseInt(hour1) < Integer.parseInt(hour2) && Integer.parseInt(minutes1)+60-Integer.parseInt(minutes2) > 7 )
                ){
            setLastSignal(now);
            return true;
        }
        else{
            setLastSignal(now);
            return false;
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
        else Log.w(TAG,"Parcheggio non salvato...");
    }

    private synchronized void addDetectedActivity(String activity, Location activityLocation){
        if(activityLocation == null) {
            Log.w(TAG,"Location is null for this activity");
            return;
        }
        if(activitiesJson.equals("")) activitiesJson += activity;
        else activitiesJson = activitiesJson + "," + activity;
        String[] jsonSplit = activitiesJson.split(",");
        if(jsonSplit.length > 5){
            activitiesJson = jsonSplit[1] + "," + jsonSplit[2] + "," + jsonSplit[3] + "," + jsonSplit[4] + "," + jsonSplit[5];
        }

        if(activitiesLocations.equals("")) activitiesLocations += activityLocation.getLatitude() + "-" + activityLocation.getLongitude();
        else activitiesLocations = activitiesLocations + "," + activityLocation.getLatitude() + "-" + activityLocation.getLongitude();
        String[] locationsSplit = activitiesLocations.split(",");
        if(locationsSplit.length > 5){
            activitiesLocations = locationsSplit[1] + "," + locationsSplit[2] + "," + locationsSplit[3] + "," + locationsSplit[4] + "," + locationsSplit[5];
        }

        if(editor == null)
            editor = sharedPreferences.edit();
        editor.putString("detectedActivities", activitiesJson);
        editor.putString("activitiesLocations",activitiesLocations);
        editor.apply();

        Log.w(TAG,"@@@ACTIVITY LIST@@@ : " + activitiesJson + "\n@@@ACTIVITY LOCATIONS@@@ : " + activitiesLocations);
    }

    private String markerIdHashcode(Double lat, Double lon){
        long hasho = (long)((lat*15661+lon*27773)/33911);
        return ""+hasho;
    }

    /*@Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }*/
}
