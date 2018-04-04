package pi.parkidle;

/**
 * Created by simonestaffa on 08/01/18.
 */

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Environment;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;


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

    private String location;
    private String location2;
    private String location3;
    private String location4;

    private boolean profilingMode;
    private boolean testMode;

    public File logFile;

    private String dir;

    private LinkedList<String> firstFive;
    private LinkedList<String> lastFive;
    private LinkedList<MyDetectedActivity> activityList;

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


        //entrambi a false è la versione standard/di produzione, con testmode = true e profilingmode = false parte la modalità test,
        //con profilingmode = true e testmode = false parte la modalità di creazione file per i test
        testMode = false; //TODO: change this for Test Mode!
        profilingMode = false; // TODO: change this for Profiling Mode!

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
                if (testCase.getTest(4) == null){
                    new testCase(4);
                    }
                }catch(NullPointerException e){
                        new testCase(4);
                }
            maxActivity = testCase.getTest(testCase.getIndex());
            Log.w(TAG, "TEST MODE IN!");
            Log.w(TAG, maxActivity + " faked from index" + testCase.getIndex());

            if(maxActivity.equals("UNKNOWN") || maxActivity.equals("TILTING")) {
                Log.w(TAG,maxActivity + " non tenuta in considerazione.");
                return;
            }
            addDetectedActivity(maxActivity,testCase.getLocation(testCase.getIndex()));
            //testCase.incrementIndex();
            createEvent(maxActivity);
        }
    }

    private void createEvent(String activity) {
        Log.w(TAG,"CONTROLLO VALIDITA' LISTA...");

        // CHECK DIMENSIONI LISTA
        String[] activityWithLocation = activitiesJson.split(",");
        if (activityWithLocation.length < 10) {
            Log.w(TAG, "[NO] Sequenza attività troppo corta per rilevare un evento ( size < 10)");
            return;
        }
        String[] activityOnly = new String[10];
        for(int i = 0; i < 10; i++){
            activityOnly[i] = activityWithLocation[i].split("_")[0];
        }
        if(activityOnly.length < 10){
            Log.w(TAG, "[NO] Sequenza attività troppo corta per rilevare un evento ( size < 10)");
            return;
        }

        Log.w(TAG,"[?] CONTROLLO SE SEI PARTITO...");

        // CONTROLLO EVENTO
        /** PARTENZE VALIDE (S == !VEHICLE o !BICYCLE, V == VEHICLE o BICYCLE)
            S S S S S S V V V V
            S S S S S S S V V V
            S S S S S S S S V V
         **/
        /** ARRIVO VALIDO (S == !VEHICLE o !BICYCLE, V == VEHICLE o BICYCLE)
         V V V V V V S S S S
         V V V V V V V S S S
         **/
        if (checkLastFive(activityOnly,"departed")){
            // POTREBBE ESSERE UNA PARTENZA
            if(activityOnly[8].equals("IN VEHICLE") || activityOnly[8].equals("ON BICYCLE")){
                if(activityOnly[9].equals("IN VEHICLE") || activityOnly[9].equals("ON BICYCLE")){
                    if(checkFirstFive(activityOnly,"departed")) {
                        //if(sharedPreferences == null)
                          //  sharedPreferences = getSharedPreferences("PARKIDLE_PREFERENCES",MODE_PRIVATE);
                        //if(sharedPreferences.getBoolean("wasInVehicle",false) == false) {
                            Log.w(TAG, "[SI] SEI PARTITO");
                            Date now = new Date();
                            int interestedActivityIndex = getInterestedActivity(activityOnly, "departed");
                            Double latitude = Double.parseDouble(activityWithLocation[interestedActivityIndex].split("_")[1]);
                            Double longitude = Double.parseDouble(activityWithLocation[interestedActivityIndex].split("_")[2]);
                            Event event = new Event(markerIdHashcode(latitude, longitude), "DEPARTED", now.toString(), latitude.toString(), longitude.toString());
                            if (testMode) {
                                MainActivity.getmMap().addMarker(new MarkerOptions()
                                        .position(new LatLng(latitude, longitude))
                                        .title("TEST MARKER")
                                        .setIcon(MainActivity.icona_parcheggio_libero_test));
                            }
                            MQTTSubscribe.sendMessage("client/departed", event.toString());
                            MainActivity.parcheggisegnalati += 1;
                            MainActivity.editor.putInt("parcheggiorank", MainActivity.parcheggisegnalati);
                            MainActivity.editor.commit();
                            Log.w(TAG, "[X] PARTENZA SEGNALATA CON SUCCESSO!");
                            /*if (editor == null)
                                if (sharedPreferences == null)
                                    sharedPreferences = getSharedPreferences("PARKIDLE_PREFERENCES", MODE_PRIVATE);
                            editor = sharedPreferences.edit();
                            editor.putBoolean("wasInVehicle", true);
                            editor.commit();*/
                            return;
                       // }
                    }
                }
            }
            Log.w(TAG,"[NO] NON SEI PARTITO");
        }
        if(checkLastFive(activityOnly,"arrived")){
            // POTREBBE ESSERE UN ARRIVO
            if(!activityOnly[7].equals("IN VEHICLE") && !activityOnly[7].equals("ON BICYCLE")){
                if(!activityOnly[8].equals("IN VEHICLE") && !activityOnly[8].equals("ON BICYCLE")){
                    if(!activityOnly[9].equals("IN VEHICLE") && !activityOnly[9].equals("ON BICYCLE")){
                        if(checkFirstFive(activityOnly,"arrived")){
                            //if(sharedPreferences == null)
                                //sharedPreferences = getSharedPreferences("PARKIDLE_PREFERENCES",MODE_PRIVATE);
                            //if(sharedPreferences.getBoolean("wasInVehicle",false) == true){
                                Log.w(TAG, "[SI] SEI ARRIVATO!");
                                int interestedActivityIndex = getInterestedActivity(activityOnly,"arrived");
                                Double latitude = Double.parseDouble(activityWithLocation[interestedActivityIndex].split("_")[1]);
                                Double longitude = Double.parseDouble(activityWithLocation[interestedActivityIndex].split("_")[2]);
                                saveParking();
                                Date now = new Date();
                                Event event = new Event(markerIdHashcode(latitude,longitude), "ARRIVED", now.toString(), latitude.toString(), longitude.toString());
                                Log.w(TAG, "[X] ARRIVO SEGNALATO CON SUCCESSO!");
                                /*if(editor == null) {
                                    editor = sharedPreferences.edit();
                                    editor.putBoolean("wasInVehicle", false);
                                    editor.commit();
                                }*/
                                return;
                            }

                       // }
                    }
                }
            }
            Log.w(TAG,"[NO] NON SEI ARRIVATO");
        }
        Log.w(TAG,"[!] NESSUN EVENTO SEGNALATO");

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

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            Log.w("Profiling","NO SD CARD.");
            return;
        }
        else {
            dir = Environment.getExternalStorageDirectory() + File.separator + "parkidle";
            //create folder
            File folder = new File(dir); //folder name
            if (!folder.exists()) folder.mkdirs();
        }

        if (mode == 0) {
            text = "test.add("+text+");\\r\\n";
            logFile = new File(dir,"events.txt");
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
            logFile = new File(dir,"accuracy.txt");
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
            location = "loc = new Location('Fake Location'); \\r\\n";
            location2 = "loc.setLatitude("+text.split("@")[0]+");\\r\\n";
            location3 += "loc.setLongitude("+text.split("@")[1]+");\\r\\n";
            location4 += "locations.add(loc);\\r\\n";
            text = location + location2 + location3 + location4;
            logFile = new File(dir,"locations.txt");
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
            if (mode==2){
                
                //BufferedWriter for performance, true to set append to file flag
                BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
                buf.append(location);
                buf.newLine();
                buf.append(location2);
                buf.newLine();
                buf.append(location3);
                buf.newLine();
                buf.append(location4);
                buf.newLine();
                buf.newLine();
                buf.newLine();
                Log.w("FILE WRITTEN",text);
                buf.newLine();
                buf.flush();
                buf.close();

            }
            else {
                //BufferedWriter for performance, true to set append to file flag
                BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
                buf.append(text);
                Log.w("FILE WRITTEN", text);
                buf.newLine();
                buf.flush();
                buf.close();
            }
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

        if (profilingMode && !testMode) { //scrivo logs su file
            appendLog(activity, 0);
            appendLog(activityLocation.getLatitude() + "@" + activityLocation.getLongitude(), 2);
        }

        MyDetectedActivity myDetectedActivity = new MyDetectedActivity(activity,activityLocation.getLatitude(),activityLocation.getLongitude());
        if(activitiesJson.equals("")) activitiesJson += myDetectedActivity.toString();
        else activitiesJson = activitiesJson + "," + myDetectedActivity.toString();
        String[] jsonSplit = activitiesJson.split(",");
        if(jsonSplit.length > 10){
            activitiesJson = jsonSplit[1] + "," + jsonSplit[2] + "," + jsonSplit[3] + "," + jsonSplit[4] + "," + jsonSplit[5] +
                    jsonSplit[6] + "," + jsonSplit[7] + "," + jsonSplit[8] + "," + jsonSplit[9] + "," + jsonSplit[10] ;
        }

        if(editor == null)
            editor = sharedPreferences.edit();
        editor.putString("detectedActivities", activitiesJson);
        //editor.putString("activitiesLocations",activitiesLocations);
        editor.commit();

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

    // questo metodo serve a controllare le ultime 5 activity nella lista (ossia le più recenti)
    // e verificare che vadano bene secondo l'algoritmo
    private boolean checkLastFive(String[] activities,String event){
        if(event.equals("departed")) {
            if (!activities[5].equals("IN VEHICLE") && !activities[5].equals("ON BICYCLE") &&
                    !activities[6].equals("IN VEHICLE") && !activities[6].equals("ON BICYCLE") &&
                    !activities[7].equals("IN VEHICLE") && !activities[7].equals("ON BICYCLE")) {
                return true;
            } else if (!activities[5].equals("IN VEHICLE") && !activities[5].equals("ON BICYCLE") &&
                    !activities[6].equals("IN VEHICLE") && !activities[6].equals("ON BICYCLE")) {
                return true;
            } else if (!activities[5].equals("IN VEHICLE") && !activities[5].equals("ON BICYCLE")) {
                return true;
            } else
                return false;
        }
        else{
            if (activities[5].equals("IN VEHICLE") || activities[5].equals("ON BICYCLE") ||
                    activities[6].equals("IN VEHICLE") || activities[6].equals("ON BICYCLE")) {
                return true;
            } else if (activities[5].equals("IN VEHICLE") || activities[5].equals("ON BICYCLE")) {
                return true;
            } else {
                return false;
            }
        }
    }

    // questo metodo serve a controllare le prime 5 activity nella lista (ossia le più vecchie)
    // e verificare che vadano bene secondo l'algoritmo
    private boolean checkFirstFive(String[] activities,String event){
        if(event.equals("departed")) {
            for (int i = 0; i < 5; i++) {
                if (activities[i].equals("IN VEHICLE") || activities[i].equals("ON BICYCLE")) {
                    return false;
                }
            }
            return true;
        }
        else {
            for (int i = 0; i < 5; i++) {
                if (!activities[i].equals("IN VEHICLE") && !activities[i].equals("ON BICYCLE")) {
                    return false;
                }
            }
            return true;
        }

    }

    // questo metodo serve a scegliere la location da usare per l'evento
    // e si basa sulla prima activity interessante ossia sul primo VEHICLE o STILL
    private int getInterestedActivity(String[] activities,String event){
        if(event.equals("departed")){
            for(int i = 6; i < 10; i++){
                if(activities[i].equals("IN VEHICLE") || activities[i].equals("ON BICYCLE")){
                    return i;
                }
            }
        }
        else{
            for(int i = 6; i < 10; i++){
                if(!activities[i].equals("IN VEHICLE") && !activities[i].equals("ON BICYCLE")){
                    return i;
                }
            }
        }
        return 8;
    }

}