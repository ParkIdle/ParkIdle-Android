package pi.parkidle;

/**
 * Created by simonestaffa on 08/01/18.
 */

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


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
    private Date trafficThreshold;

    private boolean profilingMode;
    private boolean testMode;

    public File logFile;

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

        testMode = false; //TODO: change this for Test Mode!
        profilingMode = true; // TODO: change this for Profiling Mode!

        if (testMode==false) {
            for (DetectedActivity da : detectedActivities) {

                switch (da.getType()) {
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
                if (da.getConfidence() > maxConfidence) {
                    maxConfidence = da.getConfidence();
                    maxActivity = activity;
                }

            }
            if(maxActivity.equals("UNKNOWN") || maxActivity.equals("TILTING")) {
                Log.w(TAG,maxActivity + " non tenuta in considerazione.");
                return;
            }

            if (profilingMode){ //scrivo logs su file
                appendLog(maxConfidence+"", 1);
            }

            addDetectedActivity(maxActivity,MainActivity.getMyLocation());
            createEvent(maxActivity);

        } else {
            try {
                if (testCase.getTest(0) == null){
                    new testCase(0);
                    }
                }catch(NullPointerException e){
                        new testCase(0);
                }
            maxActivity = testCase.getTest(testCase.getIndex());
            Log.w(TAG, "TEST MODE IN!");
            Log.w(TAG, maxActivity + " faked from index" + testCase.getIndex());

            if(maxActivity.equals("UNKNOWN") || maxActivity.equals("TILTING")) {
                Log.w(TAG,maxActivity + " non tenuta in considerazione.");
                return;
            }
            addDetectedActivity(maxActivity,testCase.getLocation(testCase.getIndex()));
            testCase.incrementIndex();
            createEvent(maxActivity);
        }
    }

    private void createEvent(String activity) {
        Log.w(TAG,"Vediamo se è un evento valido...");
        //SharedPreferences sharedPreferences = getSharedPreferences("PARKIDLE_PREFERENCES",MODE_PRIVATE);
        //activitiesJson = sharedPreferences.getString("detectedActivities","");
        String[] split = activitiesJson.split(",");
        //String[] split2 = activitiesLocations.split(",");
        if (split.length < 5) {
            Log.w(TAG, "[NO] Sequenza attività troppo corta per rilevare un evento ( size < 5)");
            return;
        }

        //TODO: problems- NO Scrolling dinamico delle activity, ma vengono aggiornate solo quando arrivano a 10 + Nuovo assetto da splittare ancora nei check perchè ce sta pure la location

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


        if (!split[0].split("_")[0].equals("IN VEHICLE") && !split[0].split("_")[0].equals("ON BICYCLE")){
            for(int i = 1; i < split.length; i++){
                if(i < 3){
                    if(split[i].split("_")[0].equals("IN VEHICLE") || split[i].split("_")[0].equals("ON BICYCLE")){
                        Log.w(TAG,"[NO] Le activity 2-3 sono inVehicle o onBicycle: CHIUDO");
                        return;
                    }
                }
                else {
                    if(!split[i].split("_")[0].equals("IN VEHICLE") && !split[i].split("_")[0].equals("ON BICYCLE")) {
                        Log.w(TAG, "[NO] La " + (i+1) + " non è 'IN VEHICLE o BICYCLE'");
                        return;
                    }
                }
            }
            Log.w(TAG,"[SI] Sei partito!");
            Date now = new Date();
            /*4l = MainActivity.getMyLocation();
            if(l==null){
                Log.w(TAG,"La location è null non posso mandare l'evento(partenza)");
                return;
            }*/
            //Double latitude = l.getLatitude();
            //Double longitude = l.getLongitude();
            //Double latitude = Double.parseDouble(activitiesLocations.split(",")[2].split("-")[0]);
            //Double longitude = Double.parseDouble(activitiesLocations.split(",")[2].split("-")[1]);
            //if(trafficCheck(now)) {
                Double latitude = Double.parseDouble(activitiesJson.split(",")[2].split("_")[1]);
                Double longitude = Double.parseDouble(activitiesJson.split(",")[2].split("_")[2]);
                Event event = new Event(markerIdHashcode(latitude, longitude), "DEPARTED", now.toString(), latitude.toString(), longitude.toString());
                if (testMode){
                    MainActivity.getmMap().addMarker(new MarkerOptions()
                            .position(new LatLng(latitude, longitude))
                            .title("TEST MARKER")
                            .setIcon(MainActivity.icona_parcheggio_libero_test));
                }

                MQTTSubscribe.sendMessage("client/departed", event.toString());

                /*EventHandler eh = new EventHandler(event);
                Thread handler = new Thread(eh);
                handler.setName("EventHandler");
                handler.start();*/
                // aggiungi parcheggio tra i segnalati nel profilo
                MainActivity.parcheggisegnalati += 1;
                MainActivity.editor.putInt("parcheggiorank", MainActivity.parcheggisegnalati);
                MainActivity.editor.commit();
            //}
        }

        // se ho una sequenza VEHICLE - !VEHICLE - !VEHICLE - !VEHICLE
        else{
            Log.w(TAG,"[?] Vediamo se sei arrivato...");
            if(split[0].split("_")[0].equals("IN VEHICLE") || split[0].split("_")[0].equals("ON BICYCLE")) {
                for (int i = 1; i < split.length; i++) {
                    if (split[i].split("_")[0].equals("IN VEHICLE") || split[i].equals("ON BICYCLE")) {
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
                Double latitude = Double.parseDouble(activitiesLocations.split(",")[3].split("_")[1]);
                Double longitude = Double.parseDouble(activitiesLocations.split(",")[3].split("_")[2]);
                saveParking();
                Date now = new Date();
                //trafficThreshold = now;
                Event event = new Event(markerIdHashcode(latitude,longitude), "ARRIVED", now.toString(), latitude.toString(), longitude.toString());


            }
        }

    }

    public boolean trafficCheck(Date now){
        if (trafficThreshold==null) return true;

        Log.w("trafficCHECK"," checking if we're in a traffic queue");
        String now1 = now.toString();
        String time1 = now1.split(" ")[3]; // current time
        String hour1 = time1.split(":")[0];
        String minutes1 = time1.split(":")[1];
        String seconds1 = time1.split(":")[2];

        String lastChecked = trafficThreshold.toString();
        String time2 = lastChecked.split(" ")[3]; // event time
        String hour2 = time2.split(":")[0];
        String minutes2 = time2.split(":")[1];
        String seconds2 = time2.split(":")[2];

        //controllo che tra il TrafficThreshold e now siano passati almeno 7 minuti
        if((Integer.parseInt(hour1)==Integer.parseInt(hour2) && Integer.parseInt(minutes1)-Integer.parseInt(minutes2) > 7) ||
                (Integer.parseInt(hour1) > Integer.parseInt(hour2) && Integer.parseInt(minutes1)+60-Integer.parseInt(minutes2) > 7 ) ||
                (Integer.parseInt(hour1) < Integer.parseInt(hour2) && Integer.parseInt(minutes1)+60-Integer.parseInt(minutes2) > 7 )
                ){
            return true;
        }
        else{
            return false;
        }
    }

    public void appendLog(String text, int mode){

        if (mode == 0) {
            text = "test.add("+text+");\\r\\n";
            logFile = new File("sdcard/events.file");
            if (!logFile.exists()) {
                try {
                    logFile.createNewFile();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        else if (mode == 1) {
            text = "accuracy.add("+text+");\\r\\n";
            logFile = new File("sdcard/accuracy.file");
            if (!logFile.exists()) {
                try {
                    logFile.createNewFile();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        else {
            String location = "loc = new Location('Fake Location'); \\r\\n";
            location += "loc.setLatitude("+text.split("@")[0]+");\\r\\n";
            location += "loc.setLongitude("+text.split("@")[1]+");\\r\\n";
            location += "locations.add(loc);\\r\\n";
            text = location;
            logFile = new File("sdcard/locations.file");
            if (!logFile.exists()) {
                try {
                    logFile.createNewFile();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        try
        {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));

            buf.append(text);
            Log.w("FILE WRITTEN",text);
            buf.newLine();
            buf.flush();
            buf.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
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

        if (profilingMode && !testMode){ //scrivo logs su file

            appendLog(activity, 0);
            appendLog(activityLocation.getLatitude() + "@" + activityLocation.getLongitude(), 2);


        }
        activity = activity + "_" + activityLocation.getLatitude() + "_" + activityLocation.getLongitude();
        if(activitiesJson.equals("")) activitiesJson += activity;
        else activitiesJson = activitiesJson + "," + activity;
        String[] jsonSplit = activitiesJson.split(",");
        if(jsonSplit.length > 5){
            //activitiesJson = activitiesJson.substring(activitiesJson.indexOf(",")+1);
            activitiesJson = jsonSplit[6] + "," + jsonSplit[7] + "," + jsonSplit[8] + "," + jsonSplit[9] + "," + jsonSplit[10];
        }

        /*if(activitiesLocations.equals("")) activitiesLocations += activityLocation.getLatitude() + "-" + activityLocation.getLongitude();
        else activitiesLocations = activitiesLocations + "," + activityLocation.getLatitude() + "-" + activityLocation.getLongitude();
        String[] locationsSplit = activitiesLocations.split(",");
        if(locationsSplit.length > 5){
            activitiesLocations = locationsSplit[1] + "," + locationsSplit[2] + "," + locationsSplit[3] + "," + locationsSplit[4] + "," + locationsSplit[5];
        }*/

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

    private void sendEvent(Event event) {
        // aggiungi parcheggio tra i segnalati nel profilo
        MQTTSubscribe.sendMessage("client/departed",event.toString());
        MainActivity.parcheggisegnalati += 1;
        MainActivity.editor.putInt("parcheggiorank", MainActivity.parcheggisegnalati);
        MainActivity.editor.commit();

        if (editor == null)
            editor = sharedPreferences.edit();
        editor.putFloat("latpark", 0);
        editor.putFloat("longpark", 0);
        editor.commit();
        Log.w(TAG, "Sei partito. Parcheggio cancellato!");
    }


    /*@Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }*/
}