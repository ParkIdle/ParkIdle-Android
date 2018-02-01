package app.parkidle;

import android.content.SharedPreferences;

import com.mapbox.mapboxsdk.annotations.Marker;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Andrea on 22/01/2018.
 */

public class ColorManager implements Runnable {

    private List<Marker> markers;

    private Marker mak;

    @Override
    public void run() {

        Iterator<String> it = MainActivity.events.iterator();

        String now = new Date().toString();
        String time1 = now.split(" ")[3]; // current time
        String hour1 = time1.split(":")[0];
        String minutes1 = time1.split(":")[1];
        String seconds1 = time1.split(":")[2];

        markers = MainActivity.getmMap().getMarkers();
        Iterator<Marker> mark = markers.iterator();
        mak = mark.next();
        mak.getPosition()



        while (it.hasNext()){
            // event -> "UUID-event-date-latitude-longitude"
            String e = it.next();
            String[] event = e.split("-");
            String date = event[2];

            String time2 = date.split(" ")[3]; // event time
            String hour2 = time2.split(":")[0];
            String minutes2 = time2.split(":")[1];
            String seconds2 = time2.split(":")[2];


        if (Integer.parseInt(hour1) - Integer.parseInt(hour2) == 0){
            if (Integer.parseInt(minutes1) - Integer.parseInt(minutes2) > 5){
                return MainActivity.icona_parcheggio_libero_5mins;
            }
            return MainActivity.icona_parcheggio_libero;
        }
        return MainActivity.icona_parcheggio_libero;
    }

    MainActivity.getmMap().removeMarker();

    }
}
