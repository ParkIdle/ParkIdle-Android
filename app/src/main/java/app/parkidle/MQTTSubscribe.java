package app.parkidle;

import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;

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
    private final String mMQTTBroker = "tcp://m23.cloudmqtt.com:15663";
    private final String deviceIdentifier;

    public MQTTSubscribe(String deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    public void subscribe() {
        try {
            Log.w(TAG,"Subscribing....");
            client = new MqttClient(mMQTTBroker, deviceIdentifier,new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setUserName("sgmzzqjb");
            options.setPassword("1xCzGYi15ogy".toCharArray());
            Log.w(TAG,"Connecting....");
            client.connect(options);
            Log.w(TAG,"Connected$....");
            client.setCallback(this);
            client.subscribe("server/departed");
            client.subscribe("server/arrival");
            Log.w(TAG,"Successfully subscribed");
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        // TODO Auto-generated method stub
        Log.w(TAG,"Connection lost...." + cause.toString());
    }

    @Override
    public void messageArrived(String topic, MqttMessage message)
            throws Exception {
        Log.w(TAG,"Message arrived....");
        /*if(topic.equals("server/departed")) {
            Log.w(TAG, "Departed: " + message.toString());
        }
        if(topic.equals("server/arrival")){
            Log.w(TAG, "Arrival: " + message.toString());
        }*/

        PIOEvent pioEvent = parseMqttMessage(message);
        if(pioEvent.getEvent().equals("DEPARTED_EVENT")) {
            Marker m = MainActivity.mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(pioEvent.getLatitude(), pioEvent.getLongitude()))
                    .title("Empty parking spot").setIcon(icona_parcheggio_libero));
        }
        else if(pioEvent.getEvent().equals("ARRIVAL_EVENT")){
            // TODO:
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

    private PIOEvent parseMqttMessage(MqttMessage message){
        String m = message.toString();
        String[] splitted = m.split(",");
        PIOEvent pioEvent = new PIOEvent(splitted[0],splitted[1],splitted[2],splitted[3],splitted[4]);
        return pioEvent;
    }
}
