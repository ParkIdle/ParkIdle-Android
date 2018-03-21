package pi.parkidle;

import android.location.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by misui on 20/03/2018.
 */

public class testCase {
    private static int index;
    private static List<String> test;
    private static List<Integer> accuracy;
    private static List<Location> locations;
    private Location loc;

    public testCase(int mode){
        test = new ArrayList<String>();
        accuracy = new ArrayList<Integer>();
        locations = new ArrayList<Location>();
        index = 0;

        switch(mode){
            case 0:
                test.add("ON FOOT");
                test.add("STILL");
                test.add("ON FOOT");
                test.add("IN VEHICLE");
                test.add("IN VEHICLE");

                accuracy.add(80);
                accuracy.add(50);
                accuracy.add(90);
                accuracy.add(80);
                accuracy.add(100);

                loc = new Location("Fake Location number 0");
                loc.setLatitude(41.91485540000001);
                loc.setLongitude(12.4117458);
                locations.add(loc);

                loc = new Location("Fake Location number 1");
                loc.setLatitude(41.91485540000001);
                loc.setLongitude(12.4117458);
                locations.add(loc);

                loc = new Location("Fake Location number 2");
                loc.setLatitude(41.91485540000001);
                loc.setLongitude(12.4117458);
                locations.add(loc);

                loc = new Location("Fake Location number 3");
                loc.setLatitude(41.89379036975438);
                loc.setLongitude(12.492442428766594);
                locations.add(loc);

                loc = new Location("Fake Location number 4");
                loc.setLatitude(41.89408187170548);
                loc.setLongitude(12.493151873051033);
                locations.add(loc);
        }
    }

    public static String getTest(int index){
        return test.get(index);
    }

    public static int getAccuracy(int index){
        return accuracy.get(index);
    }

    public static Location getLocation(int index){
        return locations.get(index);
    }

    public static void incrementIndex(){
        index++;
        if (index > test.size()-1){
            index = 0;
        }
    }

    public static void setIndex(int x){
        index = x;
    }

    public static int getIndex(){
        return index;
    }

}
