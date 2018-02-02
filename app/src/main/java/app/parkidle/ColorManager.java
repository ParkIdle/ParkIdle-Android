package app.parkidle;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Andrea on 22/01/2018.
 */

public class ColorManager extends Activity implements Runnable {

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
    public void run() {

        while (true) {
            Log.w("COLOR: ", "STARTED");
            map = MainActivity.getmMap();
            if(map == null){
                try {
                    Thread.sleep( 3 * 60 * 1000);
                    continue;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            listMarker = map.getMarkers();
            if(listMarker.isEmpty()){
                try {
                    Thread.sleep( 3 * 60 * 1000);
                    continue;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            it = listMarker.iterator();
            while (it.hasNext()) {
                MMM = it.next();
                String markerID = String.valueOf(MMM.getId());
                checkIterator = MainActivity.events.iterator();
                while (checkIterator.hasNext()) {
                    String markerSearcher = checkIterator.next();
                    String[] event = markerSearcher.split("-");
                    if (event[0] == markerID) {
                        Log.d("MarkerFound: ", "Evaluating Marker color");
                        MMM.setIcon(MainActivity.parkingIconEvaluator(markerSearcher));
                    }
                }
            }

            Log.w("COLOR: ", "DONE");

            try {
                Thread.sleep( 3 * 60 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
