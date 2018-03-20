package pi.parkidle;

import android.app.Activity;

import com.mapbox.mapboxsdk.annotations.Marker;

/**
 * Created by Andrea on 01/02/2018.
 */

public class DatabasedMarker extends Activity{

    //private variables
    private String _id;
    private String _event;
    private String _date;
    private String _latitude;
    private String _longitude;
    private String _markerObj;
    private DatabaseHandler database;


    // constructor

    public DatabasedMarker(){
    }

    public DatabasedMarker(String e, Marker m){
        String[] event = e.split("-");
        this._id = event[0];
        this._event = event[1];
        this._date = event[2];
        this._latitude = event[3];
        this._longitude = event[4];
        this._markerObj = ""+m;
        addToDatabase();
    }

    public void addToDatabase(){

        database = new DatabaseHandler(this);
        database.addMarker(this);
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String get_event() {
        return _event;
    }

    public void set_event(String _event) {
        this._event = _event;
    }

    public String get_date() {
        return _date;
    }

    public void set_date(String _date) {
        this._date = _date;
    }

    public String get_latitude() {
        return _latitude;
    }

    public void set_latitude(String _latitude) {
        this._latitude = _latitude;
    }

    public String get_longitude() {
        return _longitude;
    }

    public void set_longitude(String _longitude) {
        this._longitude = _longitude;
    }

    public String get_markerObj() {
        return _markerObj;
    }

    public void set_markerObj(String _markerObj) {
        this._markerObj = _markerObj;
    }
}
