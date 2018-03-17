package pi.parkidle;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static pi.parkidle.MainActivity.parkingIconEvaluator;
import static pi.parkidle.MainActivity.sharedPreferences;


/**
 * Created by simonestaffa on 23/11/17.
 */

public class MQTTSubscribe extends Service implements MqttCallback{


    //private MqttClient client;
    private MqttAsyncClient client;
    private final String TAG = "MQTTSubscribe";
    private String serverIP;
    private String mosquittoBrokerAWS;
    private final String mMQTTBroker = "tcp://m23.cloudmqtt.com:15663"; // host CloudMQTT
    private String deviceIdentifier;
    private boolean isConnected = false;
    private Set<String> events;

    @Override
    public void onCreate() {
        if(!isConnected) {
            SharedPreferences sharedPreferences = getSharedPreferences("PARKIDLE_PREFERENCES", MODE_PRIVATE);
            deviceIdentifier = sharedPreferences.getString("deviceIdentifier", "0");
            Log.w(TAG, "Starting service with ID = " + deviceIdentifier + ".");
            subscribe();
        }
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    /*public MQTTSubscribe() {
        super("MQTTService");
    }*/


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //deviceIdentifier = intent.getStringExtra("deviceIdentifier");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //TODO do something useful
        //deviceIdentifier = intent.getStringExtra("deviceIdentifier");
        if(!isConnected) {
            SharedPreferences sharedPreferences = getSharedPreferences("PARKIDLE_PREFERENCES", MODE_PRIVATE);
            deviceIdentifier = sharedPreferences.getString("deviceIdentifier", "0");

            Log.w(TAG, "Starting service with ID = " + deviceIdentifier + ".");
            subscribe();

        }
        return Service.START_STICKY;
    }

    /*public MQTTSubscribe(String deviceIdentifier, MapboxMap mapboxMap, Context context) {
        this.deviceIdentifier = deviceIdentifier;
        this.mapboxMap = mapboxMap;
        this.context = context;
    }*/

    /*@Override
    public void run() {
        subscribe();
    }*/

    public void subscribe() {
        try {
            serverIP = MainActivity.mosquittoBrokerAWS;
            events = MainActivity.events;
            SharedPreferences prefs = getSharedPreferences("PARKIDLE_PREFERENCES",MODE_PRIVATE);
            if(serverIP == null){
                serverIP = prefs.getString("serverIP","-");
            }
            if(events == null){
                events = prefs.getStringSet("events",new HashSet<String>());
            }
            mosquittoBrokerAWS = "tcp://"+serverIP+":1883";
            Log.w(TAG,"Subscribing...");

            //client = new MqttClient(mosquittoBrokerAWS, deviceIdentifier,new MemoryPersistence()); // imposto il client MQTT (in questo caso sono un subscriber
            client = new MqttAsyncClient(mosquittoBrokerAWS, deviceIdentifier, new MemoryPersistence()); //client con buffer
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(false);
            options.setAutomaticReconnect(true);

            //preparo il buffer
            DisconnectedBufferOptions dbo = new DisconnectedBufferOptions();
            dbo.setPersistBuffer(true);
            dbo.setBufferEnabled(true);
            dbo.setBufferSize(50);
            dbo.setDeleteOldestMessages(true);
            Log.w(TAG,"Connecting...");
            //client.connect(options); // mi connetto al broker
            try {
                IMqttToken token = client.connect(options);
                token.waitForCompletion();
                isConnected = true;
                //Log.w(TAG, token.toString());
                Log.w(TAG, token.getException());
                Log.w(TAG,"Connected!");
            }catch(Exception e){
                while(!isConnected) {
                    client.disconnectForcibly();
                    client.connect();
                }
            }
            //options.setUserName("sgmzzqjb");
            //options.setPassword("1xCzGYi15ogy".toCharArray());
            client.setBufferOpts(dbo);
            client.setCallback(this);
            client.subscribe("server/departed", 1); // mi sottoscrivo al topic server/departed
            client.subscribe("server/arrival",1); // same
            //client.subscribe("server/advice",1);
            Log.w(TAG,"Successfully subscribed!");

            //MainActivity.MQTTClient = client;
            MainActivity.AsyncMQTTClient = client;

        } catch (MqttException e) {
            Log.w(TAG,e.getMessage());
        }
    }

    @Override
    public void connectionLost(Throwable cause) { // viene chiamato quando MQTT lancia un eccezione e viene interrotta la connessione
        Log.w(TAG,"Connection lost...." + cause.toString());
        try {
            Log.w(TAG,"Reconnecting...");
            //client.disconnectForcibly();
            client.connect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
        //subscribe();
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) // viene chiamato quando arriva un messaggio
            throws Exception {
        Log.w(TAG,"Message arrived from topic :"+ topic);
                //Toast.makeText(this, "Message arrived from topic :"+ topic, Toast.LENGTH_SHORT).show();

        if(topic.equals("server/advice")){
            adviceNotification(message.toString());
            //Toast.makeText(this, message.toString(), Toast.LENGTH_SHORT).show();

        }

        else{

            Event event = parseMqttMessage(message);
            events.add(event.toString());
            //Log.w(TAG,event.toString());
            if(event.getEvent().equals("DEPARTED")) {

                if (!checkEventDate(event)) { // se l'evento è di 1 ora fa non lo disegno
                    return;
                }
                if (MainActivity.getmMap() != null){
                    final Marker m = MainActivity.getmMap().addMarker(new MarkerOptions()
                            .position(new LatLng(event.getLatitude(), event.getLongitude()))
                            .title("Parcheggio Libero").setIcon(parkingIconEvaluator(event.toString())));
                }

                LatLng me = new LatLng(MainActivity.getMyLocation().getLatitude(),MainActivity.getMyLocation().getLongitude());
                float distanza = MainActivity.calculateDistanceInMeters(me,new LatLng(event.getLatitude(), event.getLongitude()));
                //Log.w(TAG," " + distanza + " - " + MainActivity.sharedPreferences.getInt("progressKm",50)*1000);
                //aggiunto controllo distanza notifiche
                if ( distanza < MainActivity.sharedPreferences.getInt("progressKm",50)*1000) {
                    if(sharedPreferences.getBoolean("isAppForeground",true) == true)
                        notification(event.getLatitude(), event.getLongitude());
                    else{
                        if(sharedPreferences.getBoolean("backgroundNotify",false) == true){
                            notification(event.getLatitude(), event.getLongitude());
                        }
                    }
                }

                long ID = Long.valueOf(event.getID()).longValue();
                //m.setId(ID);
                //notification(event.getLatitude(),event.getLongitude());
            }
            else if(event.getEvent().equals("ARRIVAL")){
                // TODO:
                Log.w(TAG, "Arrival Event just received");
            }
        }


        //boolean isRunningColor = MainActivity.sharedPreferences.getBoolean("colorThreadIsRunning",false);
        /*if(!isRunningColor) {
            ColorManager colorManager = new ColorManager();
            Thread colorThread = new Thread(colorManager);
            colorThread.setName("ColorEvaluationThread");
            colorThread.setPriority(Thread.NORM_PRIORITY);
            colorThread.run();
            Log.w(TAG,"COLOR THREAD:");
        }*/
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // TODO Auto-generated method stub
        Log.w(TAG,"Delivery complete....");
    }

    // parsing del messaggio ricevuto
    private Event parseMqttMessage(MqttMessage message){
        String m = message.toString();
        String[] splitted = m.split(",");
        Event event = new Event(splitted[0],splitted[1],splitted[2],splitted[3],splitted[4]);
        return event;
    }

    private void notification(Double lat, Double lng){
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), MainActivity.NOTIFICATION_CHANNEL_ID);
        int requestID = (int) System.currentTimeMillis();
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);

        //**add this line**
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.putExtra("action","toNotifiedParkingSpot");
        notificationIntent.putExtra("lat",lat);
        notificationIntent.putExtra("lng",lng);
        sendBroadcast(notificationIntent);
        //**edit this line to put requestID as requestCode**
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), requestID,notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if(MainActivity.language == 0)
            notificationBuilder.setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.ic_local_parking_black_24dp)
                    .setTicker("ParkIdle")
                    .setPriority(Notification.PRIORITY_MAX) // this is deprecated in API 26 but you can still use for below 26. check below update for 26 API
                    .setContentTitle("C'è un nuovo parcheggio libero vicino a te!")
                    .setContentText("Clicca qui per vedere dove")
                    .setContentInfo("Parcheggio libero")
                    .setContentIntent(contentIntent);
        else notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_local_parking_black_24dp)
                .setTicker("ParkIdle")
                .setPriority(Notification.PRIORITY_MAX) // this is deprecated in API 26 but you can still use for below 26. check below update for 26 API
                .setContentTitle("There's a new empty parking spot near you!")
                .setContentText("Click here to get there")
                .setContentInfo("Parking Spot")
                .setContentIntent(contentIntent);

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    MainActivity.NOTIFICATION_CHANNEL_ID, "NewParkingSpot", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }
        notificationManager.notify(1, notificationBuilder.build());
    }

    private void adviceNotification(String msg){
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), MainActivity.NOTIFICATION_CHANNEL_ID);
        int requestID = (int) System.currentTimeMillis();
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);

        //**add this line**
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.putExtra("action","toNotifyAdvice");
        notificationIntent.putExtra("message",msg);
        //**edit this line to put requestID as requestCode**
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), requestID,notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if(MainActivity.language == 0)
            notificationBuilder.setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.ic_local_parking_black_24dp)
                    .setTicker("ParkIdle")
                    .setPriority(Notification.PRIORITY_MAX) // this is deprecated in API 26 but you can still use for below 26. check below update for 26 API
                    .setContentTitle("Messaggio:")
                    .setContentText(msg)
                    .setContentIntent(contentIntent);
        else notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_local_parking_black_24dp)
                .setTicker("ParkIdle")
                .setPriority(Notification.PRIORITY_MAX) // this is deprecated in API 26 but you can still use for below 26. check below update for 26 API
                .setContentTitle("Message")
                .setContentText(msg)
                .setContentIntent(contentIntent);

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notificationBuilder.build());
    }

    /*@Override
    protected void onHandleIntent(@Nullable Intent intent) {
        deviceIdentifier = intent.getStringExtra("deviceIdentifier");
        Log.w(TAG,"Starting service with ID = " + deviceIdentifier + ".");
        subscribe();
    }*/

    /*@Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }*/

    private boolean checkEventDate(Event ev){
        String now = new Date().toString();

        String time1 = now.split(" ")[3]; // current time
        String hour1 = time1.split(":")[0];
        String minutes1 = time1.split(":")[1];
        String seconds1 = time1.split(":")[2];

        String e = ev.toString();
        String[] event = e.split("-");
        String date = event[2];

        String time2 = date.split(" ")[3]; // event time
        String hour2 = time2.split(":")[0];
        String minutes2 = time2.split(":")[1];
        String seconds2 = time2.split(":")[2];
        if (Integer.parseInt(hour1) - Integer.parseInt(hour2) == 1) {
            if (Integer.parseInt(minutes1) - Integer.parseInt(minutes2) >= 0) {
                MainActivity.events.remove(e);
                return false;
            }
        }else if (Integer.parseInt(hour1) - Integer.parseInt(hour2) > 1){
            MainActivity.events.remove(e);
            return false;
        }
        return true;
    }
}
