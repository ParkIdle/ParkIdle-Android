package app.parkidle;

import android.Manifest;
import android.animation.TypeEvaluator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.directions.v5.models.RouteLeg;
import com.mapbox.api.directions.v5.models.RouteOptions;
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
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationViewOptions;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigationOptions;
import com.mapbox.services.android.navigation.v5.navigation.NavigationUnitType;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import app.parkidle.helper.MQTTHelper;
import io.predict.PIOTripSegment;
import io.predict.PIOZone;
import io.predict.PredictIO;
import io.predict.PredictIOStatus;
import io.predict.TransportationMode;

import static app.parkidle.LoginActivity.EXTRA_ACCOUNT;


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private DrawerLayout menuDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle menuActionBarDrawerToggle;
    private static final int ACCESS_FINE_LOCATION_PERMISSION = 1;
    private static final String TAG = "Main";

    public static final String accessToken = "pk.eyJ1Ijoic2ltb25lc3RhZmZhIiwiYSI6ImNqYTN0cGxrMjM3MDEyd25ybnhpZGNiNWEifQ._cTZOjjlwPGflJ46TpPoyA";

    // map stuff
    public static MapboxMap mMap; // riferimento statico alla mappa richiamabile in tutte le classi
    private MapView mapView;
    private FloatingActionButton ftb; // bottone che se lo premi ricentra la camera sul mio Marker (vedi recenterCamera())
    private CameraPosition position; // la posizione dell'inquadratura (in base ad isCameraFollowing mi segue)
    private LocationManager locationManager; // gestisce la localizzazione
    public static Location mLastLocation; // la mia ultima localizzazione (costantemente aggiornata con onLocationChanged)
    public static Point destination; // my destination if I click on one free parking spot marker (it start navigation)
    private Marker me; // ha sempre come riferimento il mio Marker

    //MQTT STUFF
    MQTTHelper mqttHelper;
    TextView dataReceived;

    // sensori
    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private float[] mGravity;
    private float[] mGeomagnetic;
    private float azimut = 0;
    ;

    // status boolean
    private boolean isCameraFollowing;
    private boolean isGpsEnabled;

    // icons
    public static Icon mIcon; // il mio locator
    public static Icon icona_parcheggio_libero; // parcheggio libero (segna eventi departed)
    public static Icon icona_whereiparked; // dove ho parcheggiato io (segna eventi arrived)

    private PIOManager pioManager; //gestisce l'ascolto degli eventi PredictIO

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        // Swipe-left Menu
        menuDrawerLayout = new DrawerLayout(this, (AttributeSet) findViewById(R.id.drawer_menu));
        menuActionBarDrawerToggle = new ActionBarDrawerToggle(this, menuDrawerLayout, R.string.Open, R.string.Close);
        menuDrawerLayout.addDrawerListener(menuActionBarDrawerToggle);
        menuActionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        setContentView(R.layout.activity_main);
        NavigationView drawerNav = (NavigationView) findViewById(R.id.drawer_navigation);
        View drawerHeader = drawerNav.getHeaderView(0);


        drawerNav.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                Toast.makeText(MainActivity.this, id, Toast.LENGTH_SHORT).show();
                if(id == R.id.logout)
                    signOut();
                return true;
            }
        });


        // Profile Image nel Menu laterale
        ImageView profile_img = drawerHeader.findViewById(R.id.menu_photo);
        TextView display_name = drawerHeader.findViewById(R.id.menu_display_name);
        TextView email = drawerHeader.findViewById(R.id.menu_email);
        DrawerMenuCustomizerThread customizer = new DrawerMenuCustomizerThread(profile_img,display_name,email);
        Thread customizerThread = new Thread(customizer);
        customizerThread.start();

        //Prendo l'istanza di MapBox(API Maps) e inserisco la key
        Mapbox.getInstance(this, "pk.eyJ1Ijoic2ltb25lc3RhZmZhIiwiYSI6ImNqYTN0cGxrMjM3MDEyd25ybnhpZGNiNWEifQ._cTZOjjlwPGflJ46TpPoyA");

        // icona
        mIcon = IconFactory.getInstance(MainActivity.this).fromResource(R.drawable.marcatore_posizione100x100);
        icona_whereiparked = IconFactory.getInstance(MainActivity.this).fromResource(R.drawable.my_car_parked);
        icona_parcheggio_libero = IconFactory.getInstance(MainActivity.this).fromResource(R.drawable.parking_spot);

        // sensori android
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        //accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);


        // controllo se ho i permessi per la FINE_LOCATION (precisione accurata nella localizzazione)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //se non li ho, li richiedo associando al permesso un int definito da me per riconoscerlo (vedi dichiarazioni iniziali)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION_PERMISSION);
        }

        // se ho gia i permessi posso chiedere di localizzarmi
        locationManager = (LocationManager) getApplicationContext().getSystemService(getApplicationContext().LOCATION_SERVICE);
        checkGPSEnabled(locationManager); // controllo lo stato del GPS
        mLastLocation = getLastLocation(); // localizzo

        isCameraFollowing = true; // imposto di default la camera che mi segue
        ftb = findViewById(R.id.center_camera); // tasto per recenterCamera()
        ftb.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { // imposto il listener per il tasto
                // Code here executes on main thread after user presses button
                signOut();
                recenterCamera();
            }
        });

        // mapView sarebbe la vista della mappa e l'associo ad un container in XML
        mapView = (MapView) findViewById(R.id.mapView);
        // creo la mappa
        mapView.onCreate(savedInstanceState);
        // preparo la mappa
        prepareMap(mapView);

        // attivo PredictIO
        activatePredictIOTracker();
        // controllo il suo stato per verificarne l'attivazione avvenuta con successo
        checkPredictIOStatus();
        // attivo il manager di eventi PredictIO
        pioManager = new PIOManager();
        PredictIO.getInstance(this).setListener(pioManager.getmPredictIOListener());

        //PredictIO.getInstance(this).setWebhookURL("https://requestb.in/t1fw7lt1");

        //startMQTT();

        //il log non si vede, serve per prendere i dati dell'account  e metterli nel menu
        Intent i = getIntent();
        String account = i.getStringExtra(EXTRA_ACCOUNT);
        //Toast.makeText(this, LoginActivity.getGoogleAccount().getPhotoUrl().toString(), Toast.LENGTH_SHORT).show();
        //Log.w("prova","account"+ account);


    }


    /*public boolean onOptionsItemSelected(MenuItem item){
        if (menuActionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return true;
    }
*/

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


    public void onSensorChanged(SensorEvent event) {/*
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimut = orientation[0]; // orientation contains: azimut, pitch and roll
                drawMarker(mLastLocation);
            }
        }*/
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
        mapView.onResume();
        activatePredictIOTracker();
        mLastLocation = getLastLocation();
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
                // makeUseOfNewLocation(location);
                // update my position
                drawMarker(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
                /*activatePredictIOTracker();
                checkPredictIOStatus();
                drawMarker(getLastLocation());*/
            }

            public void onProviderEnabled(String provider) {
                //Toast.makeText(getBaseContext(), "onProviderEnabled", Toast.LENGTH_SHORT).show();
            }

            public void onProviderDisabled(String provider) {
                //Toast.makeText(getBaseContext(), "onProviderDisabled", Toast.LENGTH_SHORT).show();
            }
        };
        // lista dei possibili providers a cui affidarsi per la localizzazione (NETWORK,GPS,PASSIVE)
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            //Toast.makeText(this, provider, Toast.LENGTH_LONG).show();
            locationManager.requestLocationUpdates(provider, 500, 10, locationListener);
            Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) continue;
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        //*/Toast.makeText(this, bestLocation.getLatitude() + "," + bestLocation.getLongitude(), Toast.LENGTH_LONG).show();
        /*Test t1= new Test(MainActivity.this, bestLocation,"App opened");
        Thread t= new Thread(t1);
        t.start();*/

        return bestLocation;
    }

    // questo metodo lo richiamo ogni volta che viene segnalato un location change (metodo "OnLocationChanged" in "getLastLocation")
    public void drawMarker(final Location location) {
        if (location == null) {
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
                            .bearing(0) // degree - azimut
                            .tilt(0) // Set the camera tilt to 20 degrees
                            .build(); // Builds the CameraPosition object from the builder
                    mapboxMap.animateCamera(CameraUpdateFactory
                            .newCameraPosition(position), null);
                }
                //me è un oggetto Marker, il metodo setPosition su un Marker aggiorna la posizione del mio marker
                //controllare lo spostamento della mappa
                if (me == null) {
                    me = mapboxMap.addMarker(new MarkerOptions()
                            .position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                            .title("You")
                            .setIcon(mIcon));
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
        //Toast.makeText(this, message, Toast.LENGTH_LONG).show();
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
                    //Toast.makeText(MainActivity.this, "Activated listener", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onActivationFailed(int error) {
                    //Toast.makeText(MainActivity.this, "Activation failed!" , Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    private void recenterCamera() {
        if (!isCameraFollowing) {
            CameraPosition position = new CameraPosition.Builder()
                    .target(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())) // Sets the new camera position
                    .zoom(17) // Sets the zoom to level 10
                    .bearing(0) // degree - azimut
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
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessage(); // costruisce un alert che propone di attivare il GPS
        }
    }

    private void buildAlertMessage() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        isGpsEnabled = true;
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
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

    private void prepareMap(MapView mapView) {
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final MapboxMap mapboxMap) {
                mMap = mapboxMap;
                // Customize map with markers, polylines, etc.
                // Camera Position definisce la posizione della telecamera
                position = new CameraPosition.Builder()
                        .target(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())) // Sets the new camera position
                        .zoom(17) // Sets the zoom to level 17
                        .bearing(0)// non funziona, ho provato altri 300 metodi deprecati ma non va - azimut here
                        .tilt(0) // Set the camera tilt to 20 degrees
                        .build(); // Builds the CameraPosition object from the builder
                // add marker aggiunge un marker sulla mappa con data posizione e titolo
                me = mapboxMap.addMarker(new MarkerOptions()
                        .position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                        .title("You")
                        .setIcon(mIcon));

                mapboxMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(position), 5000);
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
                        if (marker.getIcon() == icona_parcheggio_libero) {
                            destination = Point.fromLngLat(
                                    marker.getPosition().getLongitude(),
                                    marker.getPosition().getLatitude());
                            launchNavigation();
                        }
                        return true;
                    }
                });

            }

        });
    }

    public static Point getOrigin() {
        return Point.fromLngLat(mLastLocation.getLongitude(), mLastLocation.getLatitude());
    }

    public static Point getDestination() {
        return destination;
    }

    private void launchNavigation() {


        /*NavigationLauncher.startNavigation(this, new NavigationViewOptions() {
            @Nullable
            @Override
            public DirectionsRoute directionsRoute() {
                return null;
            }

            @Nullable
            @Override
            public String directionsProfile() {
                return null;
            }

            @Nullable
            @Override
            public Point origin() {
                return getOrigin();
            }

            @Nullable
            @Override
            public Point destination() {
                return getDestination();
            }

            @Nullable
            @Override
            public String awsPoolId() {
                return null;
            }

            @Override
            public MapboxNavigationOptions navigationOptions() {
                return MapboxNavigationOptions.builder()
                        .unitType(1)
                        .build();
            }

            @Override
            public boolean shouldSimulateRoute() {
                return false;
            }
        });*/
    }
    
    private void signOut(){
        LoginActivity.googleSignIn.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Logging out", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }else Toast.makeText(MainActivity.this, "disable to log out", Toast.LENGTH_SHORT).show();
            }
        });
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