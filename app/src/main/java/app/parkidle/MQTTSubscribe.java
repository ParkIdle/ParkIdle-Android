package app.parkidle;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import static app.parkidle.MainActivity.icona_parcheggio_libero;


/**
 * Created by simonestaffa on 23/11/17.
 */

public class MQTTSubscribe implements MqttCallback,Runnable{


    private MqttClient client;
    private final String TAG = "MQTTSubscribe";

    private final String mosquittoBrokerAWS = "tcp://ec2-35-177-106-206.eu-west-2.compute.amazonaws.com:1883";
    private final String mMQTTBroker = "tcp://m23.cloudmqtt.com:15663"; // host CloudMQTT
    private final String deviceIdentifier;
    private final MapboxMap mapboxMap;
    private final Context context;

    public MQTTSubscribe(String deviceIdentifier, MapboxMap mapboxMap, Context context) {
        this.deviceIdentifier = deviceIdentifier;
        this.mapboxMap = mapboxMap;
        this.context = context;
    }

    @Override
    public void run() {
        subscribe();
    }

    public void subscribe() {
        try {
            Log.w(TAG,"Subscribing....");
            // TODO: inserire Mosquitto Broker hostato su AWS
            client = new MqttClient(mosquittoBrokerAWS, deviceIdentifier,new MemoryPersistence()); // imposto il client MQTT (in questo caso sono un subscriber
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            //options.setAutomaticReconnect(true);
            //options.setUserName("sgmzzqjb");
            //options.setPassword("1xCzGYi15ogy".toCharArray());
            Log.w(TAG,"Connecting....");
            client.connect(options); // mi connetto al broker
            Log.w(TAG,"Connected$....");
            client.setCallback(this);
            client.subscribe("server/departed"); // mi sottoscrivo al topic server/departed
            client.subscribe("server/arrival"); // same
            Log.w(TAG,"Successfully subscribed");
            MainActivity.MQTTClient = client;
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connectionLost(Throwable cause) { // viene chiamato quando MQTT lancia un eccezione e viene interrotta la connessione
        Log.w(TAG,"Connection lost...." + cause.toString());
        subscribe();
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) // viene chiamato quando arriva un messaggio
            throws Exception {
        Log.w(TAG,"Message arrived....");
        /*if(topic.equals("server/departed")) {
            Log.w(TAG, "Departed: " + message.toString());
        }
        if(topic.equals("server/arrival")){
            Log.w(TAG, "Arrival: " + message.toString());
        }*/

        Event event = parseMqttMessage(message);
        if(event.getEvent().equals("DEPARTED")) {

            final Marker m = mapboxMap.addMarker(new MarkerOptions()
                    .position(new LatLng(event.getLatitude(), event.getLongitude()))
                    .title("Empty parking spot").setIcon(icona_parcheggio_libero));

            notification(event.getLatitude(),event.getLongitude());
            long ID = Long.valueOf(event.getID()).longValue();
            m.setId(ID);
        }
        else if(event.getEvent().equals("ARRIVAL")){
            // TODO:
            Log.w(TAG, "Arrival Event just received");
        }
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
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, MainActivity.NOTIFICATION_CHANNEL_ID);
        int requestID = (int) System.currentTimeMillis();
        Intent notificationIntent = new Intent(context, MainActivity.class);

        //**add this line**
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.putExtra("action","toNotifiedParkingSpot");
        notificationIntent.putExtra("lat",lat);
        notificationIntent.putExtra("lng",lng);
        //**edit this line to put requestID as requestCode**
        PendingIntent contentIntent = PendingIntent.getActivity(context, requestID,notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

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

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notificationBuilder.build());
    }
}
