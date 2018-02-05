package app.parkidle;

import android.location.Location;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;

import io.predict.PIOTripSegment;
import io.predict.PIOZone;
import io.predict.PredictIO;

/**
 * Created by simonestaffa on 25/11/17.
 */

public class EventHandler implements Runnable{

    private Event event;
    private final String TAG = "EventHandler";
    private String myServerURL = "http://ec2-35-177-216-151.eu-west-2.compute.amazonaws.com:3000/pioevent";

    public EventHandler(Event event){
        this.event = event;
    }

    @Override
    public void run() { // la run crea la connessione con il server e invia la POST
        try {
            Log.w(TAG,"Creating connection...");
            myServerURL = "http://"+MainActivity.mosquittoBrokerAWS+":3000/pioevent";
            URL url = new URL(myServerURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoInput(true);

            JSONObject jsonParam = new JSONObject();
            Log.w(TAG,"Preparing JSON...");
            prepareJSON(jsonParam);

            Log.w(TAG,"Opening output stream...");
            DataOutputStream os = new DataOutputStream(conn.getOutputStream());

            Log.w(TAG,"Writing to server...");
            os.write(jsonParam.toString().getBytes("utf-8"));
            os.flush();
            os.close();
            Log.w(TAG,"OS Closed...");
            StringBuilder sb = new StringBuilder();
            Log.w(TAG,"Waiting for HTTP Result");
            int HttpResult = conn.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK) {
                Log.w(TAG,"HTTP_OK...");
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), "utf-8"));
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                System.out.println("" + sb.toString());
                Log.w(TAG,"" + sb.toString());
            } else {
                System.out.println(conn.getResponseMessage());
                Log.w(TAG,conn.getResponseMessage());
            }
            Log.w(TAG,"Disconnecting...");
            conn.disconnect();
            Log.w(TAG,"POST Request completed!");
        }catch(ProtocolException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
        return;
    }


    // questa funzione prepara il JSONObject in base all'evento da inviare
    private void prepareJSON(JSONObject jsonParam){
        Double eventLatitude = null;
        Double eventLongitude = null;
        String eventDate = null;
        try {
            jsonParam.put("id", event.getID());
            jsonParam.put("event", event.getEvent());
            switch (event.getEvent()) {
                case "DEPARTED":
                    eventLatitude = event.getLatitude();
                    eventLongitude = event.getLongitude();
                    eventDate = event.getDate();
                    break;
                /*case "ARRIVED":
                    eventLocation = pioTripSegment.arrivalLocation;
                    eventDate = pioTripSegment.arrivalTime;
                    break;
                    */
            }
            jsonParam.put("date", eventDate);
            jsonParam.put("latitude", "" + eventLatitude);
            jsonParam.put("longitude", "" + eventLongitude);
            Log.w(TAG,"JsonParam Ready");
        }catch(JSONException e){
            e.printStackTrace();
        }
    }
}
