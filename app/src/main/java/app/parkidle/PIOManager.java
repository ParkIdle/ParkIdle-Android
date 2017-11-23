package app.parkidle;

import android.app.Application;
import android.content.IntentFilter;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import io.predict.PIOTripSegment;
import io.predict.PIOZone;
import io.predict.PredictIO;
import io.predict.PredictIOListener;
import io.predict.PredictIOStatus;

/**
 * Created by simonestaffa on 16/11/17.
 */

public class PIOManager extends Application{

    public static final String myServerURL = "https://requestb.in/z06i3hz0";

    public void onApplicationCreate(){
        // The following code sample instantiate predict.io SDK and sets the callbacks:
        PredictIO predictIO = PredictIO.getInstance(this);
        predictIO.setAppOnCreate(this);
        predictIO.setListener(mPredictIOListener);
        Toast.makeText(this, "PIOManager onApplicationCreate()", Toast.LENGTH_SHORT).show();
    }

    public PredictIOListener getmPredictIOListener(){
        return mPredictIOListener;
    }


    /**
     ****************************************
     ********* Events Via Listener **********
     ****************************************
     */

    private PredictIOListener mPredictIOListener = new PredictIOListener() {
        @Override
        public void departed(PIOTripSegment pioTripSegment) {
            Marker m = MainActivity.mMap.addMarker(new MarkerOptions()
                .position(new LatLng(pioTripSegment.departureLocation.getLatitude(), pioTripSegment.departureLocation.getLongitude()))
                        .title("You departed from here"));

            try {
                URL url = new URL(myServerURL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");

                DataOutputStream os = new DataOutputStream(conn.getOutputStream());

                JSONObject jsonParam = new JSONObject();
                try {
                    jsonParam.put("UUID",pioTripSegment.UUID);
                    jsonParam.put("event", PredictIO.DEPARTED_EVENT);
                    jsonParam.put("time", pioTripSegment.departureTime);
                    jsonParam.put("latitude", pioTripSegment.departureLocation.getLatitude());
                    jsonParam.put("longitude", pioTripSegment.departureLocation.getLongitude());
                    jsonParam.put("zone", pioTripSegment.departureZone);

                    os.write(jsonParam.toString().getBytes("utf-8"));
                    os.flush();
                }catch(JSONException e){
                    e.printStackTrace();
                }

                conn.disconnect();


            }catch(ProtocolException e){
                e.printStackTrace();
            }catch(IOException e){
                e.printStackTrace();
            }
        }

        @Override
        public void searchingInPerimeter(Location location) {

        }

        @Override
        public void suspectedArrival(PIOTripSegment pioTripSegment) {

        }

        @Override
        public void arrived(PIOTripSegment pioTripSegment) {
            Marker m1 = MainActivity.mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(pioTripSegment.arrivalLocation.getLatitude(), pioTripSegment.arrivalLocation.getLongitude()))
                    .title("You arrived here"));

            try {
                URL url = new URL(myServerURL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");

                DataOutputStream os = new DataOutputStream(conn.getOutputStream());

                JSONObject jsonParam = new JSONObject();
                try {
                    jsonParam.put("UUID",pioTripSegment.UUID);
                    jsonParam.put("event", PredictIO.ARRIVED_EVENT);
                    jsonParam.put("time", pioTripSegment.arrivalTime);
                    jsonParam.put("latitude", pioTripSegment.arrivalLocation.getLatitude());
                    jsonParam.put("longitude", pioTripSegment.arrivalLocation.getLongitude());
                    jsonParam.put("zone", pioTripSegment.arrivalZone);

                    os.write(jsonParam.toString().getBytes("utf-8"));
                    os.flush();
                }catch(JSONException e){
                    e.printStackTrace();
                }
                conn.disconnect();

            }catch(ProtocolException e){
                e.printStackTrace();
            }catch(IOException e){
                e.printStackTrace();
            }
        }

        @Override
        public void beingStationaryAfterArrival(PIOTripSegment pioTripSegment) {

        }

        @Override
        public void canceledDeparture(PIOTripSegment pioTripSegment) {

        }

        @Override
        public void didUpdateLocation(Location location) {

        }

        @Override
        public void detectedTransportationMode(PIOTripSegment pioTripSegment) {
            Toast.makeText(PIOManager.this, (pioTripSegment.transportationMode.getClass().getSimpleName()), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void traveledByAirplane(PIOTripSegment pioTripSegment) {

        }
    };


}
