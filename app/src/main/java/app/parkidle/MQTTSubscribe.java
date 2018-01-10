package app.parkidle;

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


    MqttClient client;
    private final String TAG = "MQTTSubscribe";
    private final String mMQTTBroker = "tcp://m23.cloudmqtt.com:15663"; // host CloudMQTT
    private final String deviceIdentifier;
    private final MapboxMap mapboxMap;

    public MQTTSubscribe(String deviceIdentifier, MapboxMap mapboxMap) {
        this.deviceIdentifier = deviceIdentifier;
        this.mapboxMap = mapboxMap;
    }

    public void subscribe() {
        try {
            Log.w(TAG,"Subscribing....");
            client = new MqttClient(mMQTTBroker, deviceIdentifier,new MemoryPersistence()); // imposto il client MQTT (in questo caso sono un subscriber
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setUserName("sgmzzqjb");
            options.setPassword("1xCzGYi15ogy".toCharArray());
            Log.w(TAG,"Connecting....");
            client.connect(options); // mi connetto al broker
            Log.w(TAG,"Connected$....");
            client.setCallback(this);
            client.subscribe("server/departed"); // mi sottoscrivo al topic server/departed
            client.subscribe("server/arrival"); // same
            Log.w(TAG,"Successfully subscribed");
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connectionLost(Throwable cause) { // viene chiamato quando MQTT lancia un eccezione e viene interrotta la connessione

        Log.w(TAG,"Connection lost...." + cause.toString());
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
        if(event.getEvent().equals("DEPARTED_EVENT")) {

            final Marker m = mapboxMap.addMarker(new MarkerOptions()
                    .position(new LatLng(event.getLatitude(), event.getLongitude()))
                    .title("Empty parking spot").setIcon(icona_parcheggio_libero));
        }
        else if(event.getEvent().equals("ARRIVAL_EVENT")){
            // TODO:
            Log.w(TAG, "Arrival Event just received");
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // TODO Auto-generated method stub
        Log.w(TAG,"Delivery complete....");
    }

    @Override
    public void run() {
        subscribe();
    }

    // parsing del messaggio ricevuto
    private Event parseMqttMessage(MqttMessage message){
        String m = message.toString();
        String[] splitted = m.split(",");
        Event event = new Event(splitted[0],splitted[1],splitted[2],splitted[3],splitted[4]);
        return event;
    }
}
