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

public class PIOEventHandler implements Runnable{

    private PIOTripSegment pioTripSegment;
    private String event;
    private final String TAG = "PIOEventHandler";
    private final String myServerURL = "http://ec2-35-177-168-144.eu-west-2.compute.amazonaws.com:3000/pioevent";

    public PIOEventHandler(PIOTripSegment pioTripSegment, String event){
        this.pioTripSegment = pioTripSegment;
        this.event = event;
    }

    @Override
    public void run() {
       // Looper.prepare();
        try {
            //URL url = new URL(PIOManager.myServerURL);
            Log.w(TAG,"Creating connection...");
            URL url = new URL(myServerURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            //conn.setRequestProperty("USER-AGENT", "Chrome/7.0.517.41");
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
            StringBuilder sb = new StringBuilder();
            int HttpResult = conn.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), "utf-8"));
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                System.out.println("" + sb.toString());
            } else {
                System.out.println(conn.getResponseMessage());
            }

            conn.disconnect();
            Log.w(TAG,"Disconnecting...");
        }catch(ProtocolException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
        //Looper.loop();
        return;
    }


    // questa funzione prepara il JSONObject in base all'evento da inviare
    private void prepareJSON(JSONObject jsonParam){
        Location eventLocation = null;
        Date eventDate = null;
        PIOZone eventZone = null;
        try {
            jsonParam.put("UUID", pioTripSegment.UUID);
            jsonParam.put("event", event);
            switch (event) {
                case PredictIO.DEPARTED_EVENT:
                    eventLocation = pioTripSegment.departureLocation;
                    eventDate = pioTripSegment.departureTime;
                    //eventZone = pioTripSegment.departureZone;
                    break;
                case PredictIO.ARRIVED_EVENT:
                    eventLocation = pioTripSegment.arrivalLocation;
                    eventDate = pioTripSegment.arrivalTime;
                    //eventZone = pioTripSegment.arrivalZone;
                    break;
            }
            jsonParam.put("date", eventDate);
            jsonParam.put("latitude", 15.00);
            jsonParam.put("longitude", 12.00);
            //jsonParam.put("zone", eventZone);
        }catch(JSONException e){
            e.printStackTrace();
        }
    }
}
