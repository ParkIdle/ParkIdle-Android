package app.parkidle;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import io.predict.PredictIO;

/**
 * Created by simonestaffa on 23/11/17.
 */

public class MQTTSubscribe implements MqttCallback{
    MqttClient client;
    private final String myMQTTPublisher = "";
    private final String deviceIdentifier;
    public MQTTSubscribe(String deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    public void subscribe() {
        try {
            client = new MqttClient(myMQTTPublisher, deviceIdentifier);
            client.connect();
            client.setCallback(this);
            client.subscribe("foo");
            MqttMessage message = new MqttMessage();
            message.setPayload("A single message from my computer fff"
                    .getBytes());
            client.publish("foo", message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        // TODO Auto-generated method stub

    }

    @Override
    public void messageArrived(String topic, MqttMessage message)
            throws Exception {
        System.out.println(message);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // TODO Auto-generated method stub

    }

}
