package app.parkidle.helper;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Created by Matteo on 11/12/17.
 */

public class MQTTHelper {
    public MqttAndroidClient mqttAndroidClient;

    final String serveruri = "mqtt://jxmgjhfc:XJooDxb2nx0f@m23.cloudmqtt.com";
    final String user = "jxmgjhfc";
    final String password = "XJooDxb2nx0f";

    final String subscriptionTopic = "sensor/temp";

    final String clientId = "ExampleAndroidClient";

    public MQTTHelper(final Context context){
        mqttAndroidClient =  new MqttAndroidClient(context,serveruri,clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                Log.w("Mqtt","connection completed with MQTT server");
            }

            @Override
            public void connectionLost(Throwable cause) {
               Log.w("Mqtt","connection lost with MQTT server :(");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.w("Mqtt","message arrived by server MQTT!.... here is the message:" + message.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
        connect();
    }

    public void setCallback(MqttCallbackExtended callback){
        mqttAndroidClient.setCallback(callback);
    }

    private void connect(){
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setUserName(user);
        mqttConnectOptions.setPassword(password.toCharArray());

        try{

            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    subscribeToTopic();
                    Log.w("Mqtt","connected with:" + serveruri);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt","Failed to connect to " + serveruri + exception.toString());
                }
            });

        }catch(MqttException e) {
            e.printStackTrace();
        }
    }
    private void subscribeToTopic(){
        try{
            mqttAndroidClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                    Log.w("Mqtt","Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt","Subscription failed :(");
                }
            });

        }catch (MqttException mq){
            System.err.println("Exceptionst subscribing");
            mq.printStackTrace();
        }
    }
}
