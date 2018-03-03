package app.parkidle;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


public class FeedBackActivity extends AppCompatActivity {

    private final String TAG = "FeedBackActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final TextInputLayout box_parere = findViewById(R.id.textInputLayout);
                TextView titolo = findViewById(R.id.text_feedback_desc);
                titolo.setText("Salve! Ti ringraziamo per voler contribuire al migliorare la nostra applicazione!" +
                        "  In questo semplice form potete inserire problemi o eventuali bug che avete riscontrato durante" +
                        " l'utilizzo dell'applicazione, questo aiuterà noi a migliorare sempre più la qualità del servizio!");


                final TextInputEditText feedback_text = findViewById(R.id.feedback_text);


                Button sendFeedback = findViewById(R.id.send_feedback_button);
                sendFeedback.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            final String message = feedback_text.getText().toString();
                            //MainActivity.MQTTClient.publish("client/feedbacks",new MqttMessage(message.getBytes()));
                            MainActivity.AsyncMQTTClient.publish("client/feedbacks",new MqttMessage(message.getBytes()));
                            Log.w(TAG,"Feedback successfully published");
                            Toast.makeText(FeedBackActivity.this, "Feedback successfully published!", Toast.LENGTH_SHORT).show();
                            feedback_text.setText("");
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }

                    }
                });
            }
        });


    }
}
