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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
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

    /*private Application mApplication;



    public PIOManager(Application application){
        this.mApplication = application;
    }*/

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
        }

        @Override
        public void searchingInPerimeter(Location location) {

        }

        @Override
        public void suspectedArrival(PIOTripSegment pioTripSegment) {

        }

        @Override
        public void arrived(PIOTripSegment pioTripSegment) {
            Marker m = MainActivity.mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(pioTripSegment.arrivalLocation.getLatitude(), pioTripSegment.arrivalLocation.getLongitude()))
                    .title("You arrived here"));
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

        }

        @Override
        public void traveledByAirplane(PIOTripSegment pioTripSegment) {

        }
    };


}
