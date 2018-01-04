package app.parkidle;

import java.util.List;

/**
 * Created by simonestaffa on 23/12/17.
 */

public class PIOEvent {

    private String UUID;
    private String event;
    private String date;
    private Double latitude;
    private Double longitude;

    public PIOEvent(String UUID, String event, String date, String latitude, String longitude){
        this.UUID = UUID;
        this.event = event;
        this.date = date;
        this.latitude = Double.valueOf(latitude);
        this.longitude = Double.valueOf(longitude);
        MainActivity.events.add(this.toString());
    }

    public String getUUID(){
        return UUID;
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
        return UUID + "-" + event + "-" + date + "-" + latitude + "-" + longitude;
    }
}
