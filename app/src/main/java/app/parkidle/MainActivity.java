package app.parkidle;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import com.mapbox.api.directions.v5.DirectionsCriteria;
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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import io.predict.PredictIO;
import io.predict.PredictIOStatus;

import static app.parkidle.LoginActivity.currentUser;
import static app.parkidle.LoginActivity.mAuth;
import static app.parkidle.LoginActivity.mGoogleApiClient;
import static app.parkidle.LoginActivity.noUserAccess;


public class MainActivity extends AppCompatActivity implements SensorEventListener,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {

    public static Set<String> events; // mantiene eventi(markers) precedentemente ricevuti
    public static LinkedList<String> detectedActivities; // mantiene le precedenti 4 activities


    private GoogleApiClient mApiClient;
    private ActivityRecognitionClient activityRecognitionClient;

    private DrawerLayout mDrawerLayout;
    private NavigationView mDrawerNav;
    private ActionBarDrawerToggle mActionBarDrawerToggle;

    private static final int ACCESS_FINE_LOCATION_PERMISSION = 1;
    private static final String TAG = "Main";

    public static Thread customizerThread;

    public static Bitmap profileBitmap;

    private boolean boo;

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
    private String unitType;
    private String mapStyleJSON = null;

    //MQTT STUFF
    private MQTTSubscribe mMQTTSubscribe;

    // sensori
    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private float[] mGravity;
    private float[] mGeomagnetic;
    private float azimut = 0;

    // status boolean
    private boolean isCameraFollowing;
    private boolean isGpsEnabled;

    // icons
    public static Icon mIcon; // il mio locator
    public static Icon icona_parcheggio_libero; // parcheggio libero (segna eventi departed)
    public static Icon icona_whereiparked; // dove ho parcheggiato io (segna eventi arrived)

    private PIOManager pioManager; //gestisce l'ascolto degli eventi PredictIO
    private String deviceIdentifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.w("onCreate()","creating...");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_main);

        //Prendo l'istanza di MapBox(API Maps) e inserisco la key
        Mapbox.getInstance(this, "pk.eyJ1Ijoic2ltb25lc3RhZmZhIiwiYSI6ImNqYTN0cGxrMjM3MDEyd25ybnhpZGNiNWEifQ._cTZOjjlwPGflJ46TpPoyA");

        // icona
        mIcon = IconFactory.getInstance(MainActivity.this).fromResource(R.drawable.marcatore_posizione100x100);
        icona_whereiparked = IconFactory.getInstance(MainActivity.this).fromResource(R.drawable.my_car_parked);
        icona_parcheggio_libero = IconFactory.getInstance(MainActivity.this).fromResource(R.drawable.parking_spot);

        // sensori android
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);


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
                recenterCamera();

            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences("PARKIDLE_PREFERENCES",MODE_PRIVATE);
        events = sharedPreferences.getStringSet("events",new HashSet<String>());
        //Log.w(TAG,"[EVENTS] -> " + events.toString());
        final Thread check = new Thread(new Runnable() {
            @Override
            public void run() {
                checkEvents(events);
                Log.w("CHECK THREAD", "I'm done!");
            }
        });
        check.start();

        // mapView sarebbe la vista della mappa e l'associo ad un container in XML
        mapView = (MapView) findViewById(R.id.mapView);
        // creo la mappa
        mapView.onCreate(savedInstanceState);
        // preparo la mappa
        //prepareMap(mapView);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final MapboxMap mapboxMap) {
                // MqttSubscribe dopo che la mappa viene assegnata in modo
                // da evitare NullPointerException quando inserisco un marker
                // di un parcheggio rilevato
                mMQTTSubscribe = new MQTTSubscribe(deviceIdentifier + Math.random(),mapboxMap);
                Thread mqttThread = new Thread(mMQTTSubscribe);
                mqttThread.setName("MqttThread");
                mqttThread.setPriority(Thread.NORM_PRIORITY);
                mqttThread.run();

                // to test MQTT
                /*Date today = new Date();
                PIOTripSegment pts = new PIOTripSegment("TEST","PROVA",today,mLastLocation,today,null,null,null,null,false);
                EventHandler peh = new EventHandler(pts,PredictIO.DEPARTED_EVENT);
                Thread t5 = new Thread(peh);
                t5.start();*/

                // Customize map with markers, polylines, etc.
                // Camera Position definisce la posizione della telecamera
                position = new CameraPosition.Builder()
                        .target(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())) // Sets the new camera position
                        .zoom(17) // Sets the zoom to level 17
                        .bearing(mLastLocation.getBearing())// non funziona, ho provato altri 300 metodi deprecati ma non va - azimut here
                        .tilt(0) // Set the camera tilt to 20 degrees
                        .build(); // Builds the CameraPosition object from the builder
                // add marker aggiunge un marker sulla mappa con data posizione e titolo
                me = mapboxMap.addMarker(new MarkerOptions()
                        .position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                        .title("You")
                        .setIcon(mIcon));

                mapboxMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(position), 5000);

                mapboxMap.addOnScrollListener(new MapboxMap.OnScrollListener() {
                    @Override
                    public void onScroll() {
                        //Log.w("SCROLL LISTENER","scrolling...");
                        isCameraFollowing = false;
                    }
                });

                mapboxMap.addOnFlingListener(new MapboxMap.OnFlingListener() {
                    @Override
                    public void onFling() {
                        //Log.w("FLING LISTENER","flinging...");
                        isCameraFollowing = false;
                    }
                });

                mapboxMap.addOnMapLongClickListener(new MapboxMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(@NonNull LatLng point) {
                        //Log.w("LONG CLICK LISTENER","long clicking...");
                        mapboxMap.addMarker(new MarkerOptions()
                                .position(point)
                                .title("Parcheggio libero")
                                .setIcon(icona_parcheggio_libero));
                        // TEST STUFF
                        /*Date d = new Date();
                        Event p = new Event("TEST","departed",d.toString(),Double.toString(point.getLatitude()),Double.toString(point.getLongitude()));
                        PIOTripSegment pts = new PIOTripSegment("TEST","PROVA",d,mLastLocation,d,null,null,null,null,false);
                        EventHandler peh = new EventHandler(pts,PredictIO.DEPARTED_EVENT);
                        Thread t5 = new Thread(peh);
                        t5.start();*/
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
                mMap = mapboxMap;
                Thread render = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.w("RENDER THREAD","Waiting for CHECK THREAD...");
                            check.join();
                            Log.w("RENDER THREAD", "Starting render task");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        renderEvents(events,mapboxMap);
                    }
                });
                render.start();

            }

        });

        /*
        // attivo PredictIO
        activatePredictIOTracker();
        // controllo il suo stato per verificarne l'attivazione avvenuta con successo
        checkPredictIOStatus();
        // attivo il manager di eventi PredictIO
        pioManager = new PIOManager();
        PredictIO.getInstance(this).setListener(pioManager.getmPredictIOListener());
        deviceIdentifier = PredictIO.getInstance(this).getDeviceIdentifier();
        //PredictIO.getInstance(this).setWebhookURL("https://requestb.in/t1fw7lt1");
        */
        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mApiClient.connect();

        // Swipe-left Menu
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerNav = (NavigationView) findViewById(R.id.drawer_navigation);
        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {
            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                //getActionBar().setTitle("Settings");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //getActionBar().setTitle("Settings");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle);

        // Set the list's click listener
        mDrawerNav.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.logout:
                        signOut();
                        break;

                    case R.id.db:
                        dashboard();
                        break;

                    // TODO: inserire le funzioni per tutti gli altri tasti qui
                    // case R.id.bottoneEsempio:
                    //      buttonStuff....
                }
                return true;
            }
        });

        View drawerHeader = mDrawerNav.getHeaderView(0);
        // Profile Image nel Menu laterale

        if (!(noUserAccess)) {
            ImageView profile_img = drawerHeader.findViewById(R.id.menu_photo);
            TextView display_name = drawerHeader.findViewById(R.id.menu_display_name);
            TextView email = drawerHeader.findViewById(R.id.menu_email);
            final String image_uri = LoginActivity.getUser().getPhotoUrl().toString();
            if (image_uri.contains(".jpg") || image_uri.contains(".png"))
                profile_img.setImageBitmap(getImageBitmap(image_uri));

            // Display Name nel Menu laterale
            display_name.setText(LoginActivity.getUser().getDisplayName());

            // Email nel Menu laterale
            email.setText(LoginActivity.getUser().getEmail());
        }


        // Controllo del Tutorial
        SharedPreferences TutorialPreferences = getPreferences(MODE_PRIVATE);
        boo = TutorialPreferences.getBoolean("done",true);
        if (boo){
            SharedPreferences.Editor editor = TutorialPreferences.edit();
            editor.putBoolean("done",false);
            editor.commit();
            Intent tutorial = new Intent(MainActivity.this,TutorialActivity.class);
            startActivity(tutorial);
        }
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerNav);
        //menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

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
        Log.w("onStart()","starting...");
        mapView.onStart();
        /*if(currentUser != null){
            return;
        }else{
            signOut();
        }*/
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.w("onResume()","resuming...");
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
        checkEvents(events);
        mapView.onResume();
        //activatePredictIOTracker();
        mLastLocation = getLastLocation();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.w("onPause()","stopping...");
        mapView.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
        Log.w("onStop()","stopping...");
        SharedPreferences sharedPreferences = getSharedPreferences("PARKIDLE_PREFERENCES",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet("events",events);
        editor.commit();
        /*Task<Void> task = activityRecognitionClient.removeActivityUpdates(getActivityDetectionPendingIntent());
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.w(TAG,"Removing recognition updates.");
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Failed to disable activity recognition.");
            }
        });*/
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

    @Override
    public void onBackPressed() {
        //super.onBackPressed(); // se commento questo, il tasto back non funziona piu
        finishAffinity();
    }

    @SuppressLint("MissingPermission")
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
    // TODO: fix drawMarker
    public void drawMarker(final Location location) {
        if (location == null) {
            //Toast.makeText(this, "You have to enable GPS to use the app", Toast.LENGTH_SHORT).show();
            return;
        }
        mLastLocation = location;
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final MapboxMap mapboxMap) {

                if (isCameraFollowing) {
                    // Customize map with markers, polylines, etc.
                    //Camera Position definisce la posizione della telecamera
                    position = new CameraPosition.Builder()
                            .target(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())) // Sets the new camera position
                            .zoom(17) // Sets the zoom to level 10
                            .bearing(mLastLocation.getBearing()) // degree - azimut
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

                mapboxMap.addOnScrollListener(new MapboxMap.OnScrollListener() {
                    @Override
                    public void onScroll() {
                        //Log.w("SCROLL LISTENER","scrolling...");
                        isCameraFollowing = false;
                    }
                });

                mapboxMap.addOnFlingListener(new MapboxMap.OnFlingListener() {
                    @Override
                    public void onFling() {
                        //Log.w("FLING LISTENER","flinging...");
                        isCameraFollowing = false;
                    }
                });

                /*mapboxMap.addOnMapLongClickListener(new MapboxMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(@NonNull LatLng point) {
                        //Log.w("LONG CLICK LISTENER","long clicking...");
                        mapboxMap.addMarker(new MarkerOptions()
                                .position(point)
                                .title("Parcheggio libero")
                                .setIcon(icona_parcheggio_libero));
                        // TEST STUFF
                        Date d = new Date();
                        Event p = new Event("TEST","departed",d.toString(),Double.toString(point.getLatitude()),Double.toString(point.getLongitude()));
                    }
                });*/

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
        if(!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Toast.makeText(this, "For a more accurate localization turn ON the WiFi service", Toast.LENGTH_LONG).show();
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

    public static Point getOrigin() {
        return Point.fromLngLat(mLastLocation.getLongitude(), mLastLocation.getLatitude());
    }

    public static Point getDestination() {
        return destination;
    }

    private void launchNavigation() {

        this.unitType = DirectionsCriteria.METRIC;

        /*NavigationNotification mNavNotification = new NavigationNotification() {
            @Override
            public Notification getNotification() {

            }

            @Override
            public int getNotificationId() {
                return 0;
            }

            @Override
            public void updateNotification(RouteProgress routeProgress) {

            }
        }*/

        MapboxNavigationOptions mNavOptions = MapboxNavigationOptions.builder()
                .unitType(1)
                .enableNotification(true)
                .enableOffRouteDetection(true)
                .build();

        NavigationViewOptions options = NavigationViewOptions.builder()
                .origin(getOrigin())
                .destination(getDestination())
                .awsPoolId(null)
                .shouldSimulateRoute(false)
                .navigationOptions(mNavOptions)
                .build();

        NavigationLauncher.startNavigation(this, options);
    }

    private void signOut() {
        //if (isWithGoogle())
        //if (LoginActivity.getUser() != null) {
        FirebaseAuth istance = FirebaseAuth.getInstance();
        istance.signOut();
        noUserAccess = false;

        mAuth.signOut();
        mGoogleApiClient.clearDefaultAccountAndReconnect(); // disconnect from google
        LoginManager.getInstance().logOut(); // disconnect from facebook
        currentUser = null;
        Intent i = new Intent(MainActivity.this,LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        if (customizerThread != null) {
            customizerThread.interrupt();
        }
    }

    private void dashboard() {
        Intent i = new Intent(MainActivity.this,DashboardActivity.class);
        startActivity(i);
    }

    public static Bitmap getImageBitmap(final String uri){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bm = null;
                try {
                    URL aURL = new URL(uri);
                    URLConnection conn = aURL.openConnection();
                    conn.connect();
                    InputStream is = conn.getInputStream();
                    BufferedInputStream bis = new BufferedInputStream(is);
                    profileBitmap = BitmapFactory.decodeStream(bis);

                    bis.close();
                    is.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error getting bitmap", e);
                }
            }
        });
        t.start();
        try {
            t.join(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return profileBitmap;
    }

    public static MapboxMap getmMap() {
        return mMap;
    }

    private void checkEvents(Set<String> events){
        Log.w(TAG,"Checking events...");
        Iterator<String> it = events.iterator();
        String now = new Date().toString();

        String time1 = now.split(" ")[3]; // current time
        String hour1 = time1.split(":")[0];
        String minutes1 = time1.split(":")[1];
        String seconds1 = time1.split(":")[2];
        while (it.hasNext()){
            // event -> "UUID-event-date-latitude-longitude"
            String e = it.next();
            String[] event = e.split("-");
            String date = event[2];

            String time2 = date.split(" ")[3]; // event time
            String hour2 = time2.split(":")[0];
            String minutes2 = time2.split(":")[1];
            String seconds2 = time2.split(":")[2];
            if(Integer.parseInt(hour1) - Integer.parseInt(hour2) >= 1) {
                if (Integer.parseInt(minutes1) - Integer.parseInt(minutes2) >= 0)
                    events.remove(e);
            }
        }
        Log.w(TAG,"Check DONE...");
    }

    private void renderEvents(Set<String> events,MapboxMap mapboxMap){
        Log.w(TAG,"Rendering events...");
        Iterator<String> it = events.iterator();
        while(it.hasNext()){
            // event -> "UUID-event-date-latitude-longitude"
            String e = it.next();
            String[] event = e.split("-");
            LatLng point = new LatLng(Double.parseDouble(event[3]),Double.parseDouble(event[4]));
            Marker m = mapboxMap.addMarker(new MarkerOptions()
                    .position(point)
                    .title("Parcheggio libero")
                    .setIcon(icona_parcheggio_libero));

        }
        Log.w(TAG,"Render DONE...");
    }

    /**
     * Gets a PendingIntent to be sent for each activity detection.
     */
    private PendingIntent getActivityDetectionPendingIntent() {
        Intent intent = new Intent(this, DetectedActivitiesIntentService.class);

        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // requestActivityUpdates() and removeActivityUpdates().
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.w(TAG,"CONNECTED");
        activityRecognitionClient = ActivityRecognition.getClient(this);
        Task task = activityRecognitionClient.requestActivityUpdates(30*1000, getActivityDetectionPendingIntent());
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.w(TAG,"ActivityRecognition ACTIVE");
                Toast.makeText(getApplicationContext(),
                        "ActivityRecognition ACTIVE",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG,"ActivityRecognition NOT ACTIVE");
                Toast.makeText(getApplicationContext(),
                        "ActivityRecognition NOT ACTIVE",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.w(TAG,"CONNECTION SUSPENDED");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG,"CONNECTION FAILED");
    }

    public static Location getMyLocation(){
        return mLastLocation;
    }

    public static void addDetectedActivity(String activity){
        if(detectedActivities == null)
            detectedActivities = new LinkedList<>();
        if(detectedActivities.size() > 4){
            detectedActivities.removeFirst();
        }
        detectedActivities.add(activity);
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