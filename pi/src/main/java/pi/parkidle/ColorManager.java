package pi.parkidle;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.util.Iterator;
import java.util.List;

/**
 * Created by Andrea on 22/01/2018.
 */

public class ColorManager implements Runnable {

    private List<Marker> markers;
    private SQLiteOpenHelper mDbHelper;
    private SQLiteDatabase data;
    private Marker selectedMarker;
    private Icon newIcon;
    private Marker mak;
    public MapboxMap map;
    private List<Marker> listMarker;
    private Iterator<Marker> it;
    private Marker MMM;
    private Iterator<String> checkIterator;

    @Override
    public synchronized void run() {
            map = MainActivity.getmMap();
            if(map == null){
                MainActivity.editor.putBoolean("colorThreadIsRunning", false);
                return;
            }
            MainActivity.editor.putBoolean("colorThreadIsRunning", true);
            listMarker = map.getMarkers();
            Log.w("COLOR: ", "STARTED");
            it = listMarker.iterator();
            while (it.hasNext()) {
                MMM = it.next();
                String markerID = String.valueOf(MMM.getId());
                checkIterator = MainActivity.events.iterator();
                while (checkIterator.hasNext()) {
                    String markerSearcher = checkIterator.next();
                    String[] event = markerSearcher.split("-");
                    if (event[0] == markerID) {
                        Log.w("MarkerFound: ", "Evaluating Marker color");
                        MMM.setIcon(MainActivity.parkingIconEvaluator(markerSearcher));
                    }
                }
            }
            Log.w("COLOR: ", "DONE");


        }


}
