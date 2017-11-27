package app.parkidle;

import android.app.Application;
import android.content.IntentFilter;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;

import java.io.IOException;
import java.net.HttpURLConnection;

import java.net.ProtocolException;
import java.net.URL;


import io.predict.PIOTripSegment;
import io.predict.PredictIO;
import io.predict.PredictIOListener;
import io.predict.TransportationMode;


import static app.parkidle.MainActivity.icona_parcheggio_libero;
import static app.parkidle.MainActivity.icona_whereiparked;

/**
 * Created by simonestaffa on 16/11/17.
 */

public class PIOManager extends Application{

    public static final String myServerURL = "https://requestb.in/wfoypqwf";

    private TransportationMode mTrasportationMode;

    public void onApplicationCreate(){
        // The following code sample instantiate predict.io SDK and sets the callbacks:
        PredictIO predictIO = PredictIO.getInstance(this);
        predictIO.setAppOnCreate(this);
        predictIO.setListener(mPredictIOListener);
        //Toast.makeText(this, "PIOManager onApplicationCreate()", Toast.LENGTH_SHORT).show();
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
                        .title("You departed from here").setIcon(icona_parcheggio_libero));

            PIOEventHandler peh = new PIOEventHandler(pioTripSegment, PredictIO.DEPARTED_EVENT);
            Thread t = new Thread(peh);
            t.start();
            Toast.makeText(PIOManager.this, "Departure - Sending position", Toast.LENGTH_SHORT).show();
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
                    .title("You parked here").setIcon(icona_whereiparked));

            PIOEventHandler peh = new PIOEventHandler(pioTripSegment, PredictIO.ARRIVED_EVENT);
            Thread t = new Thread(peh);
            t.start();
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
            //Toast.makeText(PIOManager.this, (pioTripSegment.transportationMode.getClass().getSimpleName()), Toast.LENGTH_SHORT).show();

        }

        @Override
        public void traveledByAirplane(PIOTripSegment pioTripSegment) {

        }
    };


}
