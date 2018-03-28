package pi.parkidle;

public class MyDetectedActivity {

    private String activity;
    private Double latitude;
    private Double longitude;

    public MyDetectedActivity(String a, Double lat, Double lng){
        activity = a;
        latitude = lat;
        longitude = lng;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getActivity() {

        return activity;
    }

    public String toString(){
        return activity + "_" + latitude + "_" + longitude;
    }
}
