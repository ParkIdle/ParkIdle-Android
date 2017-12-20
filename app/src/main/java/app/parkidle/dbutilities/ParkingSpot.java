package app.parkidle.dbutilities;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by simonestaffa on 18/12/17.
 */

public class ParkingSpot {
    public Double latitude;
    public Double longitude;
    public Date date;

    public ParkingSpot(){}

    public ParkingSpot(Double lat,Double lng, Date date){
        latitude = lat;
        longitude = lng;
        this.date = date;
        writeNewParkingSpot(lat,lng,date);
    }

    private void writeNewParkingSpot(Double lat, Double lng, Date date){
        ParkingSpot parkingSpot = new ParkingSpot(lat,lng,date);
        DatabaseWriterThread dwt = new DatabaseWriterThread(parkingSpot);
        Thread t = new Thread(dwt);
        t.start();
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Date getDate() {
        return date;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("Latitude", getLatitude());
        result.put("Longitude", getLongitude());
        result.put("Date", getDate());
        return result;
    }
}
