package app.parkidle;

import android.Manifest;
import android.animation.TypeEvaluator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.android.location.LostLocationEngine;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationView;
import com.mapbox.services.android.navigation.ui.v5.NavigationViewListener;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigation;
import com.mapbox.services.android.navigation.v5.navigation.NavigationEventListener;
import com.mapbox.services.android.telemetry.location.LocationEngine;

import java.util.List;
import io.predict.PredictIO;
import io.predict.PredictIOStatus;

public class MainActivity extends AppCompatActivity  implements SensorEventListener{
    private static final int ACCESS_FINE_LOCATION_PERMISSION = 1;
    private static final String TAG = "Main";
    public static MapboxMap mMap;
    private PIOManager pioManager;
    private MapView mapView;

    private Marker me;
    private Icon icona;
    //private MQTTSubscribe myMQTTSubscribe;
    private Icon mIcon;
    private boolean isCameraFollowing;
    private boolean isGpsEnabled;
    private FloatingActionButton ftb;
    public static Icon icona_parcheggio_libero;
    public static Icon icona_whereiparked;
    private SensorManager mSensorManager;
    private float degree;
    private CameraPosition position;
    private LocationManager locationManager;
    private Location mLastLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Prendo l'istanza di MapBox(API Maps) e inserisco la key
        Mapbox.getInstance(this, "pk.eyJ1Ijoic2ltb25lc3RhZmZhIiwiYSI6ImNqYTN0cGxrMjM3MDEyd25ybnhpZGNiNWEifQ._cTZOjjlwPGflJ46TpPoyA");
        MapboxNavigation navigation = new MapboxNavigation(this, "pk.eyJ1Ijoic2ltb25lc3RhZmZhIiwiYSI6ImNqYTN0cGxrMjM3MDEyd25ybnhpZGNiNWEifQ._cTZOjjlwPGflJ46TpPoyA");
        LocationEngine locationEngine = LostLocationEngine.getLocationEngine(this);
        navigation.setLocationEngine(locationEngine);
        setContentView(R.layout.activity_main);

        //icona
        icona = IconFactory.getInstance(MainActivity.this).fromResource(R.drawable.locator);
        icona_whereiparked = IconFactory.getInstance(MainActivity.this).fromResource(R.drawable.parking);
        icona_parcheggio_libero = IconFactory.getInstance(MainActivity.this).fromResource(R.drawable.my_car);

        //sensori android
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);


        //controllo se ho i permessi per la FINE_LOCATION (precisione accurata nella localizzazione)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //se non li ho, li richiedo associando al permesso un int definito da me per riconoscerlo (vedi dichiarazioni iniziali)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION_PERMISSION);
        }

        //se ho gia i permessi posso chiedere di localizzarmi
        locationManager = (LocationManager) getApplicationContext().getSystemService(getApplicationContext().LOCATION_SERVICE);
        checkGPSEnabled(locationManager);
        mLastLocation = getLastLocation();
        isCameraFollowing = true;
        ftb = findViewById(R.id.center_camera);

        ftb.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                recenterCamera();
            }
        });
        mIcon = IconFactory.getInstance(this).fromResource(R.drawable.map_marker_dark);
        //mapView sarebbe la vista della mappa e l'associo ad un container in XML
        mapView = (MapView) findViewById(R.id.mapView);
        //creo la mappa
        mapView.onCreate(savedInstanceState);
        //preparo la mappa
        prepareMap(mapView);

        activatePredictIOTracker();
        checkPredictIOStatus();
        PIOManager p = new PIOManager();
        PredictIO.getInstance(this).setListener(p.getmPredictIOListener());
        //PredictIO.getInstance(this).setWebhookURL("https://requestb.in/t1fw7lt1");

        //myMQTTSubscribe = new MQTTSubscribe(PredictIO.getInstance(this).getDeviceIdentifier());
        Test t1 = new Test(this,mLastLocation,"APP-OPENING");
        Thread t = new Thread(t1);
        t.start();
        Toast.makeText(this, "Opening-Sending position", Toast.LENGTH_SHORT).show();


    }
    //questo metodo viene chiamato in risposta ad una richiesta di permessi
    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //faccio uno switch sul requestCode per vedere se corrisponde al mio int assegnato per il dato permeso
        switch (requestCode) {
            case ACCESS_FINE_LOCATION_PERMISSION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted.
                    // ora posso chiedere di nuovo la localizzazione
                    mLastLocation = getLastLocation();
                    //Toast.makeText(this, "GPS permission successfully granted!", Toast.LENGTH_LONG).show();
                } else {
                    // User refused to grant permission. You can add AlertDialog here
                    // messaggio di avvertimento
                    Toast.makeText(this, "GPS permission not granted. Please allow GPS using to use this app.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void onSensorChanged(SensorEvent event) {
        degree = Math.round(event.values[0]);
        drawMarker(mLastLocation);
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),SensorManager.SENSOR_DELAY_GAME);
        mapView.onResume();

        mLastLocation = getLastLocation();
        activatePredictIOTracker();

    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();

        mSensorManager.unregisterListener(this);

    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    public Location getLastLocation() {
        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) getApplicationContext().getSystemService(getApplicationContext().LOCATION_SERVICE);
        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                //makeUseOfNewLocation(location);
                //update my position
                drawMarker(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
                activatePredictIOTracker();
                checkPredictIOStatus();
                drawMarker(getLastLocation());
            }

            public void onProviderEnabled(String provider) {
                Toast.makeText(getBaseContext(), "onProviderDisabled", Toast.LENGTH_SHORT).show();
            }

            public void onProviderDisabled(String provider) {
                Toast.makeText(getBaseContext(), "onProviderDisabled", Toast.LENGTH_SHORT).show();
            }
        };
        //lista dei possibili providers a cui affidarsi per la localizzazione (NETWORK,GPS,PASSIVE)
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            //Toast.makeText(this, provider, Toast.LENGTH_LONG).show();
            locationManager.requestLocationUpdates(provider, 3500, 10, locationListener);
            Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) continue;
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        //Toast.makeText(this, bestLocation.getLatitude() + "," + bestLocation.getLongitude(), Toast.LENGTH_LONG).show();
        return bestLocation;
    }

    //questo metodo lo richiamo ogni volta che viene segnalato un location change (metodo "OnLocationChanged" in "getLastLocation")
    public void drawMarker(final Location location) {
        if(location == null){
            //Toast.makeText(this, "You have to enable GPS to use the app", Toast.LENGTH_SHORT).show();
            return;
        }
        mLastLocation = location;
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {

                if (isCameraFollowing) {
                    // Customize map with markers, polylines, etc.
                    //Camera Position definisce la posizione della telecamera
                    position = new CameraPosition.Builder()
                            .target(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())) // Sets the new camera position
                            .zoom(17) // Sets the zoom to level 10
                            .bearing(degree)
                            .tilt(0) // Set the camera tilt to 20 degrees
                            .build(); // Builds the CameraPosition object from the builder
                    mapboxMap.animateCamera(CameraUpdateFactory
                            .newCameraPosition(position), null);
                }
                //me è un oggetto Marker, il metodo setPosition su un Marker aggiorna la posizione del mio marker
                //controllare lo spostamento della mappa
                if(me == null){
                    me = mapboxMap.addMarker(new MarkerOptions()
                            .position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                            .title("You")
                            .setIcon(icona));
                }
                me.setPosition(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));

                /*ValueAnimator markerAnimator = ObjectAnimator.ofObject(me, "position",
                        new LatLngEvaluator(), me.getPosition(), new LatLng(mLastLocation.getLatitude(),mLastLocation.getAltitude()));
                markerAnimator.setDuration(2000);
                markerAnimator.start();*/
            }

        });
    }

    public void checkPredictIOStatus() {
        String message;
        switch (PredictIO.getInstance(getApplication()).getStatus()) {
            case ACTIVE:
                message = "'predict.io' tracker is in working state.";
                break;
            case LOCATION_DISABLED:
                message = "'predict.io' tracker is not in running state. GPS is disabled.";
                break;
            case AIRPLANE_MODE_ENABLED:
                message = "'predict.io' tracker is not in running state. Airplane mode is enabled.";
                break;
            case INSUFFICIENT_PERMISSION:
                message = "'predict.io' tracker is not in running state. Location permission is not granted.";
                break;
            default:
            case IN_ACTIVE:
                message = "'predict.io' tracker is in in-active state.";
                break;
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public void activatePredictIOTracker() {
        //Get PredictIO instance
        final PredictIO predictIO = PredictIO.getInstance(getApplication());
        //Set modes
        predictIO.enableSearchingInPerimeter(true);

        //Validate tracker not already running
        if (predictIO.getStatus() == PredictIOStatus.ACTIVE) {
            return;
        }

        //All validations cleared, start tracker
        try {
            //noinspection ResourceType
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            predictIO.start(new PredictIO.PIOActivationListener() {
                @Override
                public void onActivated() {
                    Toast.makeText(MainActivity.this, "Activated listener", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onActivationFailed(int error) {
                    Toast.makeText(MainActivity.this, "Activation failed!" , Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    private void recenterCamera(){
        if(!isCameraFollowing){
            CameraPosition position = new CameraPosition.Builder()
                    .target(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())) // Sets the new camera position
                    .zoom(17) // Sets the zoom to level 10
                    .bearing(0)
                    .tilt(0) // Set the camera tilt to 20 degrees
                    .build(); // Builds the CameraPosition object from the builder
            mMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(position), null);
            isCameraFollowing = true;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void checkGPSEnabled(LocationManager locationManager) {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            buildAlertMessage();
        }
    }

    private void buildAlertMessage() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                isGpsEnabled = true;
                                onPause();
                                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                onResume();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                isGpsEnabled = false;
                                dialog.cancel();
                            }
                        });

        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void prepareMap(MapView mapView){
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final MapboxMap mapboxMap) {
                mMap = mapboxMap;
                // Customize map with markers, polylines, etc.
                //Camera Position definisce la posizione della telecamera
                position = new CameraPosition.Builder()
                        .target(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())) // Sets the new camera position
                        .zoom(17) // Sets the zoom to level 17
                        .bearing(degree)//non funziona, ho provato altri 300 metodi deprecati ma non va
                        .tilt(0) // Set the camera tilt to 20 degrees
                        .build(); // Builds the CameraPosition object from the builder
                //add marker aggiunge un marker sulla mappa con data posizione e titolo
                me = mapboxMap.addMarker(new MarkerOptions()
                        .position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                        .title("You")
                        .setIcon(icona));

                mapboxMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(position), 7000);
                mapboxMap.setOnScrollListener(new MapboxMap.OnScrollListener() {
                    @Override
                    public void onScroll() {
                        isCameraFollowing = false;
                    }
                });
                mapboxMap.setOnFlingListener(new MapboxMap.OnFlingListener() {
                    @Override
                    public void onFling() {
                        isCameraFollowing = false;
                    }
                });
                mapboxMap.setOnMapLongClickListener(new MapboxMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(@NonNull LatLng point) {
                        mapboxMap.addMarker(new MarkerOptions()
                                .position(point)
                                .title("Parcheggio libero")
                                .setIcon(icona_parcheggio_libero));
                    }
                });
                mapboxMap.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(@NonNull Marker marker) {
                        //if(marker.getIcon() == icona_parcheggio_libero
                            startNav(Point.fromLngLat(
                                    marker.getPosition().getLongitude(),
                                    marker.getPosition().getLatitude()));
                            return true;
                    }
                });

            }

        });
    }

    private void startNav(Point destination){
        Point origin = Point.fromLngLat(mLastLocation.getLongitude(),mLastLocation.getLatitude());
        NavigationLauncher.startNavigation(this, origin, destination, null, false);
    }

}

class LatLngEvaluator implements TypeEvaluator<LatLng> {
    // Method is used to interpolate the marker animation.

    private LatLng latLng = new LatLng();

    @Override
    public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
        latLng.setLatitude(startValue.getLatitude()
                + ((endValue.getLatitude() - startValue.getLatitude()) * fraction));
        latLng.setLongitude(startValue.getLongitude()
                + ((endValue.getLongitude() - startValue.getLongitude()) * fraction));
        return latLng;
    }
}