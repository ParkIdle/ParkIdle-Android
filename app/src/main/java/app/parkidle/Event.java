package app.parkidle;

import android.util.Log;
import android.widget.Toast;

import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.List;

/**
 * Created by simonestaffa on 23/12/17.
 */

public class Event {

    private String ID;
    private String event;
    private String date;
    private Double latitude;
    private Double longitude;

    public Event(String ID, String event, String date, String latitude, String longitude){
        this.ID = ID;
        this.event = event;
        this.date = date;
        this.latitude = Double.valueOf(latitude);
        this.longitude = Double.valueOf(longitude);
        MainActivity.events.add(this.toString());

        Log.w("Main","[NEW EVENT] -> " + this.toString());
    }

    public String getID(){
        return ID;
    }

    public String getEvent(){
        return event;
    }

    public String getDate(){
        return date;
    }

    public Double getLatitude(){
        return latitude;
    }

    public Double getLongitude(){
        return longitude;
    }

    @Override
    public String toString() {
        return ID + "-" + event + "-" + date + "-" + latitude + "-" + longitude;
    }
}
