package app.parkidle;

import android.location.Location;
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

    public PIOEventHandler(PIOTripSegment pioTripSegment, String event){
        this.pioTripSegment = pioTripSegment;
        this.event = event;
    }

    @Override
    public void run() {
        try {
            //URL url = new URL(PIOManager.myServerURL);
            URL url = new URL("https://requestb.in/1amp6dc1");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            //conn.setRequestProperty("USER-AGENT", "Chrome/7.0.517.41");
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setDoInput(true);

            DataOutputStream os = new DataOutputStream(conn.getOutputStream());

            JSONObject jsonParam = new JSONObject();

            prepareJSON(jsonParam);
            os.write(jsonParam.toString().getBytes("utf-8"));
            os.flush();
            os.close();

            conn.disconnect();
        }catch(ProtocolException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
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
                    eventZone = pioTripSegment.departureZone;
                    break;
                case PredictIO.ARRIVED_EVENT:
                    eventLocation = pioTripSegment.arrivalLocation;
                    eventDate = pioTripSegment.arrivalTime;
                    eventZone = pioTripSegment.arrivalZone;
                    break;
            }
            jsonParam.put("date", eventDate);
            jsonParam.put("latitude", eventLocation.getLatitude());
            jsonParam.put("longitude", eventLocation.getLongitude());
            jsonParam.put("zone", eventZone);
        }catch(JSONException e){
            e.printStackTrace();
        }
    }
}
