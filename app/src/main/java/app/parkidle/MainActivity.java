package app.parkidle;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import com.google.android.gms.actions.SearchIntents;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bugfender.sdk.Bugfender;
import com.crashlytics.android.Crashlytics;
import com.facebook.login.LoginManager;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.BaseMarkerOptions;
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
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.services.android.navigation.ui.v5.NavigationActivity;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationViewOptions;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigationOptions;
import com.mapbox.services.android.navigation.v5.navigation.NavigationConstants;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationUnitType;

import junit.framework.TestResult;

import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import io.fabric.sdk.android.Fabric;
import io.fabric.sdk.android.services.common.Crash;
import io.predict.PIOTripSegment;
import io.predict.PredictIO;
import io.predict.PredictIOStatus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static app.parkidle.LoginActivity.currentUser;
import static app.parkidle.LoginActivity.mAuth;

import static app.parkidle.LoginActivity.mGoogleApiClient;
import static app.parkidle.MainActivity.parkingIconEvaluator;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener, Callback<DirectionsResponse> {

    public static String mosquittoBrokerAWS;

    public static String deviceIdentifier;

    public static int language; //0 italian, 1 english
    public static int metric; //0 metri, 1 miglia
    public static SharedPreferences sharedPreferences;
    public static SharedPreferences.Editor editor;

    public static String NOTIFICATION_CHANNEL_ID = "parking_spot_channel";
    public boolean fromNewIntent = false;

    public static Set<String> events; // mantiene eventi(markers) precedentemente ricevuti
    public static LinkedList<String> detectedActivities; // mantiene le precedenti 4 activities

    private GoogleApiClient mApiClient;
    public static ActivityRecognitionClient activityRecognitionClient;

    private DrawerLayout mDrawerLayout;
    private NavigationView mDrawerNav;
    private ActionBarDrawerToggle mActionBarDrawerToggle;

    private static final int ACCESS_FINE_LOCATION_PERMISSION = 1;
    private static final String TAG = "Main";

    public static Thread customizerThread;

    public static Bitmap profileBitmap;
    private DatabasedMarker dtb;
    private boolean boo;

    public static ProgressDialog logout_dialog;

    public static final String accessToken = "pk.eyJ1Ijoic2ltb25lc3RhZmZhIiwiYSI6ImNqYTN0cGxrMjM3MDEyd25ybnhpZGNiNWEifQ._cTZOjjlwPGflJ46TpPoyA";

    // map stuff
    public static MapboxMap mMap; // riferimento statico alla mappa richiamabile in tutte le classi
    private MapView mapView;
    private FloatingActionButton ftb; // bottone che se lo premi ricentra la camera sul mio Marker (vedi recenterCamera())
    private FloatingActionButton nearestPSpot; // bottone che se lo premi ti porta al parcheggio più vicino
    private CameraPosition position; // la posizione dell'inquadratura (in base ad isCameraFollowing mi segue)
    private LocationManager locationManager; // gestisce la localizzazione
    public static Location mLastLocation; // la mia ultima localizzazione (costantemente aggiornata con onLocationChanged)
    public static Point destination; // my destination if I click on one free parking spot marker (it start navigation)
    private Marker me; // ha sempre come riferimento il mio Marker
    private String unitType;
    private String mapStyleJSON = null;
    private List<MarkerOptions> markerList;

    //MQTT STUFF
    private MQTTSubscribe mMQTTSubscribe;
    public static MqttClient MQTTClient;
    public static MqttAsyncClient AsyncMQTTClient;

    // status boolean
    private boolean isCameraFollowing;
    private boolean isGpsEnabled;

    // icons
    public static Icon mIcon; // il mio locator
    public static Icon icona_parcheggio_libero; // parcheggio libero (segna eventi departed)
    public static Icon icona_parcheggio_libero_5mins; // parcheggio libero (segna eventi departed)
    public static Icon icona_parcheggio_libero_10mins; // parcheggio libero (segna eventi departed)
    public static Icon icona_parcheggio_libero_20mins; // parcheggio libero (segna eventi departed)

    public static Icon icona_whereiparked; // dove ho parcheggiato io (segna eventi arrived)
    public static Icon house_icon;
    public static Icon work_icon;

    //private PIOManager pioManager; //gestisce l'ascolto degli eventi PredictIO
    //private String deviceIdentifier;

    //shared prefs per salvare il parcheggio
    private double latpark;
    private double longpark;
    //shared pref per salvare posto di lavoro
    public static  double latwork;
    public static double longwork;

    //shared pref per salvare casa
    public static double lathouse;
    public static double longhouse;
    //contatore parcheggi per punteggio
    public static int parcheggisegnalati=0;



    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.w(TAG,"OnCreate()");
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // la mappa non ruota
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_main);

        Bugfender.init(this, "ciCsDGK2Y1mlUar2wq7WUySADw0v84gZ", BuildConfig.DEBUG);
        Bugfender.enableLogcatLogging();
        Bugfender.enableUIEventLogging(this.getApplication());
        Bugfender.enableCrashReporting();
        Bugfender.setDeviceString("user.email",currentUser.getEmail());

        /*final Fabric fabric = new Fabric.Builder(this)
                .kits(new Crashlytics())
                .debuggable(true)           // Enables Crashlytics debugger
                .build();
        Fabric.with(fabric);*/

        // controllo se ho i permessi per la FINE_LOCATION (precisione accurata nella localizzazione)
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //se non li ho, li richiedo associando al permesso un int definito da me per riconoscerlo (vedi dichiarazioni iniziali)
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION_PERMISSION);
        }
        // se ho gia i permessi posso chiedere di localizzarmi
        locationManager = (LocationManager) getApplicationContext().getSystemService(getApplicationContext().LOCATION_SERVICE);
        checkGPSEnabled(locationManager); // controllo lo stato del GPS
        mLastLocation = getLastLocation(); // localizzo


        isCameraFollowing = true; // imposto di default la camera che mi segue
        sharedPreferences = getSharedPreferences("PARKIDLE_PREFERENCES",MODE_PRIVATE);
        editor = sharedPreferences.edit();
        language = sharedPreferences.getInt("language",0);

        deviceIdentifier = sharedPreferences.getString("deviceIdentifier","0");
        if(deviceIdentifier.equals("0")){
            deviceIdentifier = UUID.randomUUID().toString();
            editor.putString("deviceIdentifier",deviceIdentifier);
            editor.commit();
        }
        Log.w(TAG,"Device Identifier > " + deviceIdentifier);

        // icona
        mIcon = IconFactory.getInstance(MainActivity.this).fromResource(R.drawable.marcatore_posizione100x100);
        icona_whereiparked = IconFactory.getInstance(MainActivity.this).fromResource(R.drawable.my_car_parked);
        house_icon =IconFactory.getInstance(MainActivity.this).fromResource((R.drawable.houseicon));
        icona_parcheggio_libero = IconFactory.getInstance(MainActivity.this).fromResource(R.drawable.p_marker_white70x70);
        icona_parcheggio_libero_5mins = IconFactory.getInstance(MainActivity.this).fromResource(R.drawable.p_marker_green70x70);
        icona_parcheggio_libero_10mins = IconFactory.getInstance(MainActivity.this).fromResource(R.drawable.p_marker_yellow70x70);
        icona_parcheggio_libero_20mins = IconFactory.getInstance(MainActivity.this).fromResource(R.drawable.p_marker_red70x70);
        work_icon = IconFactory.getInstance(MainActivity.this).fromResource(R.drawable.workicon);
        GetServerURITask gsut = new GetServerURITask();
        gsut.execute();
        try {
            mosquittoBrokerAWS = gsut.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        } catch (ExecutionException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }

        events = sharedPreferences.getStringSet("events", new HashSet<String>());

        //Prendo l'istanza di MapBox(API Maps) e inserisco la key
        Mapbox.getInstance(MainActivity.this, "pk.eyJ1Ijoic2ltb25lc3RhZmZhIiwiYSI6ImNqYTN0cGxrMjM3MDEyd25ybnhpZGNiNWEifQ._cTZOjjlwPGflJ46TpPoyA");
        // mapView sarebbe la vista della mappa e l'associo ad un container in XML
        mapView = (MapView) findViewById(R.id.mapView);
        // creo la mappa
        mapView.onCreate(savedInstanceState);

        // preparo la mappa
        //prepareMap(mapView);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final MapboxMap mapboxMap) {
                mMap = mapboxMap;
                //Log.w(TAG,"Check these: " + events);
                //checkEvents(events);
                //Log.w(TAG,"We have: " + events);
                CheckEventsTask cet = new CheckEventsTask();
                cet.execute(events);
                try {
                    events = cet.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                }
                // Camera Position definisce la posizione della telecamera

                position = new CameraPosition.Builder()
                        .target(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())) // Sets the new camera position
                        .zoom(17) // Sets the zoom to level 17
                        .bearing(mLastLocation.getBearing())// non funziona, ho provato altri 300 metodi deprecati ma non va - azimut here
                        .tilt(0) // Set the camera tilt to 20 degrees
                        .build(); // Builds the CameraPosition object from the builder
                if (isItalian()) {
                    me = mapboxMap.addMarker(new MarkerOptions()
                            .position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                            .title("Tu")
                            .setIcon(mIcon));

                } else {
                    // add marker aggiunge un marker sulla mappa con data posizione e titolo
                    me = mapboxMap.addMarker(new MarkerOptions()
                            .position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                            .title("You")
                            .setIcon(mIcon));
                }

                Log.w(TAG + "(Renderer)","Render DONE...");
                if(!events.isEmpty()) {
                    Log.w(TAG, "Starting render task: " + events);
                    renderEvents(events, getmMap());
                    Log.w(TAG, "End render task");
                    //setRepeatingAsyncTask(getmMap(),events);
                }else{
                    Log.w(TAG,"Nessun evento da renderizzare");
                }
                editor.remove("events");

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
                        Log.w("LONG CLICK LISTENER","long clicking...");

                        Marker m = mapboxMap.addMarker(new MarkerOptions()
                                .setIcon(icona_parcheggio_libero)
                                .position(point)
                                .setTitle("Parcheggio libero"));

                        //notification(point.getLatitude(),point.getLongitude()); // per testare le notifiche

                        //TEST STUFF
                        Date d = new Date();
                        Event p = new Event(markerIdHashcode(m.getPosition().getLatitude(),m.getPosition().getLongitude()),"DEPARTED",d.toString(),Double.toString(point.getLatitude()),Double.toString(point.getLongitude()));
                        parcheggisegnalati++;
                        editor.putInt("parcheggiorank",parcheggisegnalati);
                        //PIOTripSegment pts = new PIOTripSegment("TEST","PROVA",d,mLastLocation,d,null,null,null,null,false);
                        EventHandler peh = new EventHandler(p);
                        Thread t5 = new Thread(peh);
                        t5.start();

                    }
                });

                mapboxMap.setInfoWindowAdapter(new MapboxMap.InfoWindowAdapter() {
                    @Nullable
                    @Override
                    public View getInfoWindow(@NonNull final Marker marker) {
                        final View window; // Creating an instance for View Object
                        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        window = inflater.inflate(R.layout.parkidle_info_window, null);

                        Button nav = (Button) window.findViewById(R.id.info_navigation);
                        nav.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                destination = Point.fromLngLat(
                                        marker.getPosition().getLongitude(),
                                        marker.getPosition().getLatitude());
                                launchNavigation();
                            }
                        });

                        LatLng myLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                        String distanza = calculateDistance(marker.getPosition(), myLatLng);
                        Icon icon = marker.getIcon();
                        if (icon.equals(icona_parcheggio_libero) ||icon.equals(icona_parcheggio_libero_5mins) ||icon.equals(icona_parcheggio_libero_10mins) ||icon.equals(icona_parcheggio_libero_20mins)) {

                            TextView title = (TextView) window.findViewById(R.id.info_title);
                            title.setText(marker.getTitle());

                            TextView minutes = (TextView) window.findViewById(R.id.info_minutes);
                            TextView distance = (TextView) window.findViewById(R.id.info_distance);
                            String date = "";
                            //long markerID = marker.getId();
                            //String date = getDateFromMarkerID(markerID);
                            if (icon.equals(icona_parcheggio_libero)) {
                                date = "< 5";
                            }
                            if (icon.equals(icona_parcheggio_libero_5mins)) {
                                date = "> 5";
                            }
                            if (icon.equals(icona_parcheggio_libero_10mins)) {
                                date = "> 10";
                            }
                            if (icon.equals(icona_parcheggio_libero_20mins)) {
                                date = "> 20";
                            }

                            if (isItalian()) {
                                minutes.setText("Libero da:    " + date + " minuti");
                                distance.setText("Distanza(linea d'aria):      " + distanza);
                            } else {
                                minutes.setText("Since:    " + date + " minutes");
                                distance.setText("Distance:      " + distanza);
                            }


                            return window;
                        }
                        else if (icon.equals(mIcon)) {
                            return null;
                        }
                        else if (icon.equals(house_icon) || icon.equals(work_icon) ||icon.equals(icona_whereiparked)){


                            TextView title = (TextView) window.findViewById(R.id.info_title);
                            title.setText(marker.getTitle());

                            TextView minutes = (TextView) window.findViewById(R.id.info_minutes);
                            TextView distance = (TextView) window.findViewById(R.id.info_distance);

                            if (isItalian()) {
                                minutes.setText("");
                                distance.setText("Distanza(linea d'aria):      " + distanza);
                            } else {
                                minutes.setText("");
                                distance.setText("Distance:      " + distanza);
                            }
                            return window;
                        }


                        return window;
                    }



                });


            }

        });

        //Log.w(TAG,"[EVENTS] -> " + events.toString());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                ftb = findViewById(R.id.center_camera); // tasto per recenterCamera()
                ftb.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) { // imposto il listener per il tasto
                        // Code here executes on main thread after user presses button
                        recenterCamera();

                    }
                });

                nearestPSpot = findViewById(R.id.nearest_parking_spot);
                nearestPSpot.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        nearestParkingSpot();
                    }
                });

                // Swipe-left Menu
                mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
                mDrawerNav = (NavigationView) findViewById(R.id.drawer_navigation);

                if (isItalian()) {
                    Menu m = mDrawerNav.getMenu();
                    m.findItem(R.id.db).setTitle("Profilo");
                    m.findItem(R.id.mycar).setTitle("La tua macchina");
                    m.findItem(R.id.settings).setTitle("Impostazioni");
                    m.findItem(R.id.myhouse).setTitle("Casa");
                    m.findItem(R.id.myjob).setTitle("Posto di lavoro");
                }

                mActionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, mDrawerLayout,
                        R.string.drawer_open, R.string.drawer_close) {
                    /**
                     * Called when a drawer has settled in a completely closed state.
                     */
                    public void onDrawerClosed(View view) {
                        super.onDrawerClosed(view);
                        //getActionBar().setTitle("Settings");
                        invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                    }

                    /**
                     * Called when a drawer has settled in a completely open state.
                     */
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
                        switch (item.getItemId()) {
                            case R.id.logout:
                                signOut();
                                break;

                            case R.id.db:
                                dashboard();
                                break;

                            case R.id.mycar:
                                myCar();
                                break;

                            case R.id.settings:
                                settings();
                                break;

                            case R.id.feedback:
                                feedback_activity();
                                break;

                            case R.id.myhouse:
                                myhouse();
                                break;

                            case R.id.myjob:
                                mywork();
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

                ImageView profile_img = drawerHeader.findViewById(R.id.menu_photo);
                TextView display_name = drawerHeader.findViewById(R.id.menu_display_name);
                TextView email = drawerHeader.findViewById(R.id.menu_email);
                final String image_uri = LoginActivity.getUser().getPhotoUrl().toString();
                ProfileBitmapTask pbt = new ProfileBitmapTask();
                pbt.execute(image_uri);
                if (image_uri.contains(".jpg") || image_uri.contains(".png")){
                    //profile_img.setImageBitmap(getImageBitmap(image_uri));
                    try {
                        profile_img.setImageBitmap(pbt.get());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Crashlytics.logException(e);
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                        Crashlytics.logException(e);
                    }
                }
                // Display Name nel Menu laterale
                display_name.setText(LoginActivity.getUser().getDisplayName());

                // Email nel Menu laterale
                email.setText(LoginActivity.getUser().getEmail());
            }
        });



        // MqttSubscribe dopo che la mappa viene assegnata in modo
        // da evitare NullPointerException quando inserisco un marker
        // di un parcheggio rilevato
        Intent mqttSubscribeService = new Intent(this,MQTTSubscribe.class);
        mqttSubscribeService.putExtra("deviceIdentifier",deviceIdentifier);
        startService(mqttSubscribeService);
        /*mMQTTSubscribe = new MQTTSubscribe(deviceIdentifier + Math.random(), getmMap(),MainActivity.this);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                mMQTTSubscribe.subscribe();
            }
        });
        t.start();*/
        /*Thread mqttThread = new Thread(mMQTTSubscribe);
        mqttThread.setName("MqttThread");
        mqttThread.setPriority(Thread.NORM_PRIORITY);
        mqttThread.run();*/

        buildGoogleApiClient();

        // Controllo del Tutorial
        boo = sharedPreferences.getBoolean("done",true);

        if (boo){
            editor.putBoolean("done",false);
            editor.commit();
            Intent tutorial = new Intent(MainActivity.this,TutorialActivity.class);
            startActivity(tutorial);
        }
        /*Thread render = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.w("RENDER THREAD", "Waiting for CHECK THREAD...");
                try {
                    shared.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                checkEvents(events);
                Log.w("RENDER THREAD", "Starting render task");
                renderEvents(events,getmMap());
                Log.w("RENDER THREAD", "End render task");


            }
        });
        render.start();*/


    if(getIntent().getAction() != null && getIntent().getAction().equals("com.google.android.gms.actions.SEARCH_ACTION")){
        String query = getIntent().getStringExtra(SearchManager.QUERY);
        char f = query.toLowerCase().charAt(0);
        if(f=='p'){
            nearestParkingSpot();
        }
    }
    }//qua finisce oncreate

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        switch(id){
            case android.R.id.home:
                if(!mDrawerLayout.isDrawerOpen(Gravity.LEFT))
                    mDrawerLayout.openDrawer(Gravity.LEFT);
                else mDrawerLayout.closeDrawers();
                break;
            case R.id.refresh:
                Toast.makeText(this, "Refresh", Toast.LENGTH_SHORT).show();
                CheckEventsTask cet = new CheckEventsTask();
                cet.execute(events);
                try {
                    events = cet.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                }
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setMessage("Refreshing..");
                dialog.show();
                renderEvents(events,getmMap());
                dialog.dismiss();
                break;
        }
        return true;
    }

    public static void cambialingua(){
        Layer mapText = mMap.getLayer("place-city-lg-n");
        if (language==0)
            mapText.setProperties(textField("{name_it}"));
        else
            mapText.setProperties(textField("{name}"));
    }

    public void myCar(){

        latpark = sharedPreferences.getFloat("latpark",0);
        longpark = sharedPreferences.getFloat("longpark",0);
        if(latpark==0 && longpark==0){
            if(isItalian())
                Toast.makeText(this, "Parcheggio non salvato ", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, "Your car position is not saved", Toast.LENGTH_SHORT).show();
        }
        else{
            LatLng parcheggio = new LatLng(latpark,longpark);
            isCameraFollowing=false;
            if(isItalian()) {
                Marker parkmarker = getmMap().addMarker(new MarkerOptions()
                        .position(new LatLng(latpark, longpark))
                        .title("La tua macchina")
                        .setIcon(icona_whereiparked));
            }else{
                Marker parkmarker = getmMap().addMarker(new MarkerOptions()
                        .position(new LatLng(new LatLng(latpark, longpark)))
                        .title("Your Car")
                        .setIcon(icona_whereiparked));
            }
            mDrawerLayout.closeDrawers();
            CameraPosition position = new CameraPosition.Builder()
                    .target(parcheggio) // Sets the new camera position
                    .zoom(17) // Sets the zoom to level 10
                    .bearing(mLastLocation.getBearing()) // degree - azimut
                    .tilt(0) // Set the camera tilt to 20 degrees
                    .build(); // Builds the CameraPosition object from the builder
            getmMap().animateCamera(CameraUpdateFactory
                    .newCameraPosition(position), null);
        }
    }


    public void myhouse(){

        lathouse =Double.parseDouble(sharedPreferences.getString("lathouse","0"));
        longhouse = Double.parseDouble(sharedPreferences.getString("longhouse","0"));

        if(lathouse==0 && longhouse==0){
            if(isItalian())
                Toast.makeText(this, "Casa non salvata (Inseriscila nelle impostazioni)", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, "Home not saved (Add it in the settings menù)", Toast.LENGTH_SHORT).show();
        }
        else{
            LatLng casa =new LatLng(lathouse,longhouse);
            isCameraFollowing=false;

            if(isItalian()) {
                Marker casamarker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(lathouse, longhouse))
                        .title("Casa")
                        .setIcon(house_icon));
            }else{
                Marker casamarker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(new LatLng(lathouse, longhouse)))
                        .title("Home")
                        .setIcon(house_icon));
            }
            mDrawerLayout.closeDrawers();
            CameraPosition position = new CameraPosition.Builder()
                    .target(casa) // Sets the new camera position
                    .zoom(17) // Sets the zoom to level 10
                    .bearing(mLastLocation.getBearing()) // degree - azimut
                    .tilt(0) // Set the camera tilt to 20 degrees
                    .build(); // Builds the CameraPosition object from the builder
            mMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(position), null);
        }
    }

    public void mywork(){

        latwork =Double.parseDouble(sharedPreferences.getString("latwork","0"));
        longwork = Double.parseDouble(sharedPreferences.getString("longwork","0"));

        if(latwork==0 && longwork==0){
            if(isItalian())
                Toast.makeText(this, "Posto di lavoro non salvato (Inseriscilo nelle impostazioni)", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, "Work place not saved (Add it in the settings menù)", Toast.LENGTH_SHORT).show();
        }
        else{
            LatLng lavoro =new LatLng(latwork,longwork);
            isCameraFollowing=false;

            if(isItalian()) {
                Marker workmarker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latwork, longwork))
                        .title("Lavoro")
                        .setIcon(work_icon));
            }else{
                Marker casamarker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(new LatLng(latwork, longwork)))
                        .title("Home")
                        .setIcon(work_icon));
            }
            mDrawerLayout.closeDrawers();
            CameraPosition position = new CameraPosition.Builder()
                    .target(lavoro) // Sets the new camera position
                    .zoom(17) // Sets the zoom to level 10
                    .bearing(mLastLocation.getBearing()) // degree - azimut
                    .tilt(0) // Set the camera tilt to 20 degrees
                    .build(); // Builds the CameraPosition object from the builder
            mMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(position), null);
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
                    Toast.makeText(this, "Devi attivare il GPS per usare ParkIdle.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        Log.w(TAG,"OnStart(): " + events);
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
        Log.w(TAG,"OnResume(): " + events);
        mapView.onResume();
        //checkEvents(events);
        //activatePredictIOTracker();
        if(!fromNewIntent) {
            mLastLocation = getLastLocation();
            fromNewIntent = false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.w(TAG,"OnPause(): " + events);
        editor.remove("events");
        editor.putStringSet("events",events);
        editor.commit();
        mapView.onPause();

    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
        Log.w(TAG,"OnStop(): " + events);
       // editor.putBoolean("colorThreadIsRunning", false);
        //Log.w(TAG,"We have: "+ events);
        editor.remove("events");
        editor.putStringSet("events",events);
        editor.commit();
        Log.w(TAG,"Saving sharedPrefs: " + events);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.w(TAG,"OnDestroy(): " + events);
        editor.remove("events");
        editor.putStringSet("events",events);
        editor.commit();
        Log.w(TAG,"Saving sharedPrefs: " + events);

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
        //Toast.makeText(this, "Tasto disattivato", Toast.LENGTH_SHORT).show();
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
                //drawMarker(location);
                mLastLocation = location;
                LatLng point = new LatLng(location.getLatitude(),location.getLongitude());
                if (isCameraFollowing) {
                    if(getmMap() != null) {
                        // Customize map with markers, polylines, etc.
                        //Camera Position definisce la posizione della telecamera
                        position = new CameraPosition.Builder()
                                .target(new LatLng(location.getLatitude(), location.getLongitude())) // Sets the new camera position
                                .zoom(17) // Sets the zoom to level 10
                                .bearing(location.getBearing()) // degree - azimut
                                .tilt(0) // Set the camera tilt to 20 degrees
                                .build(); // Builds the CameraPosition object from the builder
                        getmMap().animateCamera(CameraUpdateFactory
                                .newCameraPosition(position), 800);
                    }
                }
                if(me != null) {
                    ValueAnimator markerAnimator = ObjectAnimator.ofObject(me, "position",
                            new LatLngEvaluator(), me.getPosition(), point);
                    markerAnimator.setDuration(800);
                    markerAnimator.start();
                }

            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
                /*activatePredictIOTracker();
                checkPredictIOStatus();
                drawMarker(getLastLocation());*/
            }

            public void onProviderEnabled(String provider) {
                Toast.makeText(getBaseContext(), "onProviderEnabled", Toast.LENGTH_SHORT).show();
                
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

        return bestLocation;
    }



    private void recenterCamera() {
        if (!isCameraFollowing) {
            CameraPosition position = new CameraPosition.Builder()
                    .target(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())) // Sets the new camera position
                    .zoom(17) // Sets the zoom to level 10
                    .bearing(mLastLocation.getBearing()) // degree - azimut
                    .tilt(0) // Set the camera tilt to 20 degrees
                    .build(); // Builds the CameraPosition object from the builder
            mMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(position), null);
            isCameraFollowing = true;
        }
    }

    private void nearestParkingSpot(){
        isCameraFollowing = false;
        if(getMyLocation() == null){
            Toast.makeText(this, "Non riesco a localizzarti, prova a spostarti!", Toast.LENGTH_SHORT).show();
            return;
        }
        Double myLat = getMyLocation().getLatitude();
        Double myLng = getMyLocation().getLongitude();

        List<Marker> list = getmMap().getMarkers();
        ListIterator<Marker> it = list.listIterator();

        if(list.isEmpty()){
            Toast.makeText(this, "Non ci sono parcheggi liberi vicino a te.", Toast.LENGTH_SHORT).show();
            return;
        }
        Marker nearest = null;

        float distanceMin = 99999999;
        float[] results = new float[3];
        while (it.hasNext()) {
            Marker aux = it.next();
            if (!aux.getIcon().equals(mIcon) && !aux.getIcon().equals(house_icon) && !aux.getIcon().equals(work_icon)) {
                Double markerLat = aux.getPosition().getLatitude();
                Double markerLng = aux.getPosition().getLongitude();
                Location.distanceBetween(myLat, myLng, markerLat, markerLng, results);

                if (results[0] < distanceMin) {
                    distanceMin = results[0];
                    nearest = aux;
                }
            }
        }
        if(nearest == null) {
            Toast.makeText(this, "Nessun parcheggio libero vicino", Toast.LENGTH_SHORT).show();
            return;
        }
        CameraPosition position = new CameraPosition.Builder()
                .target(nearest.getPosition()) // Sets the new camera position
                .zoom(17) // Sets the zoom to level 10
                .bearing(mLastLocation.getBearing()) // degree - azimut
                .tilt(0) // Set the camera tilt to 20 degrees
                .build(); // Builds the CameraPosition object from the builder
        mMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(position), null);
    }

    public void checkGPSEnabled(LocationManager locationManager) {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessage(); // costruisce un alert che propone di attivare il GPS
        }
        if(!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            if(isItalian())
                Toast.makeText(this, "Per una localizzazione più precisa attiva il WiFi", Toast.LENGTH_LONG).show();
            else
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
        MapboxNavigationOptions mNavOptions;

        if(metric==0) {
            mNavOptions = MapboxNavigationOptions.builder()
                    .unitType(1)
                    .enableNotification(true)
                    .enableOffRouteDetection(true)
                    .build();

        }

        else{
            mNavOptions= MapboxNavigationOptions.builder()
                .unitType(0)
                .enableNotification(true)
                .enableOffRouteDetection(true)
                .build();

        }
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
        Locale lingua = Locale.ITALIAN;

        if (language==0) {
            lingua = Locale.ITALIAN;
        }
        else if(language==1){
            lingua= Locale.ENGLISH;
        }


        NavigationViewOptions options = NavigationViewOptions.builder()
                .origin(getOrigin())
                .destination(getDestination())
                .awsPoolId(null)
                .shouldSimulateRoute(false)
                .navigationOptions(mNavOptions)
                .build();

        NavigationLauncher.startNavigation(this, options);

                //startNavigation(this, options);
    }

    private void signOut() {
        //if (isWithGoogle())
        //if (LoginActivity.getUser() != null) {

        FirebaseAuth instance = FirebaseAuth.getInstance();
        instance.signOut();
        mAuth.signOut();
        mGoogleApiClient.clearDefaultAccountAndReconnect(); // disconnect from google
        LoginManager.getInstance().logOut(); // disconnect from facebook
        currentUser = null;
        activityRecognitionClient.removeActivityUpdates(getActivityDetectionPendingIntent());


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

    private void settings() {
        Intent i = new Intent(MainActivity.this,SettingsActivity.class);
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

    private synchronized void checkEvents(Set<String> events){
        //Log.w(TAG,"Checking events... : " + events);
        Iterator<String> it = events.iterator();
        String now = new Date().toString();

        String time1 = now.split(" ")[3]; // current time
        String hour1 = time1.split(":")[0];
        String minutes1 = time1.split(":")[1];
        String seconds1 = time1.split(":")[2];
        while (it.hasNext()){
            // event -> "UUID-event-date-latitude-longitude"
            try {
                String e = it.next();
                String[] event = e.split("-");
                String date = event[2];

                String time2 = date.split(" ")[3]; // event time
                String hour2 = time2.split(":")[0];
                String minutes2 = time2.split(":")[1];
                String seconds2 = time2.split(":")[2];
                if (Integer.parseInt(hour1) - Integer.parseInt(hour2) >= 1) {
                    if (Integer.parseInt(minutes1) - Integer.parseInt(minutes2) >= 0)
                        events.remove(e);
                }
            }catch(ConcurrentModificationException e){
                return;
            }
        }
        Log.w(TAG,"Check DONE...");
    }

    private void renderEvents(Set<String> events,MapboxMap mapboxMap){
        //Log.w(TAG,"Rendering events...: " + events);
        Iterator<String> it = events.iterator();
        if(mapboxMap == null){
            Log.w(TAG + "(Renderer)","Cannot RENDER, Map is null");
            return;
        }
        while(it.hasNext()){
            // event -> "UUID-event-date-latitude-longitude"
            String e = it.next();
            String[] event = e.split("-");
            LatLng point = new LatLng(Double.parseDouble(event[3]),Double.parseDouble(event[4]));
            long ID = Long.valueOf(event[0]).longValue();
            if(isItalian()){
                Marker m = mapboxMap.addMarker(new MarkerOptions()
                        .position(point)
                        .title("Parcheggio libero")
                        .setIcon(parkingIconEvaluator(e)));
                //m.setId(ID); DA PROBLEMI PER CLICCARE I MARKER

            }
            else {
                Marker m = mapboxMap.addMarker(new MarkerOptions()
                        .position(point)
                        .title("Free Parking Spot")
                        .setIcon(parkingIconEvaluator(e)));
                //m.setId(ID); DA PROBLEMI PER CLICCARE MARKER

            }

        }
        Log.w(TAG + "(Renderer)","Render DONE...");
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
        activityRecognitionClient = new ActivityRecognitionClient(this);
        Task task = activityRecognitionClient.requestActivityUpdates(20*1000, getActivityDetectionPendingIntent());
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.w(TAG,"ActivityRecognition ACTIVE");
                /*Toast.makeText(getApplicationContext(),
                        "ActivityRecognition ACTIVE",
                        Toast.LENGTH_SHORT)
                        .show();*/
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG,"ActivityRecognition NOT ACTIVE");
                /*Toast.makeText(getApplicationContext(),
                        "ActivityRecognition NOT ACTIVE",
                        Toast.LENGTH_SHORT)
                        .show();*/
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

    public static Icon parkingIconEvaluator(String msg){

        String now = new Date().toString();
        String time1 = now.split(" ")[3]; // current time
        String hour1 = time1.split(":")[0];
        String minutes1 = time1.split(":")[1];
        String seconds1 = time1.split(":")[2];

        String[] event = msg.split("-");
        String date = event[2];
        String time2 = date.split(" ")[3]; // event time
        String hour2 = time2.split(":")[0];
        String minutes2 = time2.split(":")[1];
        String seconds2 = time2.split(":")[2];

        if (Integer.parseInt(hour1) - Integer.parseInt(hour2) == 0){
            if ((Integer.parseInt(minutes1) - Integer.parseInt(minutes2) > 5) && (Integer.parseInt(minutes1) - Integer.parseInt(minutes2) < 10)){
                return icona_parcheggio_libero_5mins;
            }
            /*if(true){
                return icona_parcheggio_libero_20mins;
            }*/
            else if ((Integer.parseInt(minutes1) - Integer.parseInt(minutes2) > 10) && (Integer.parseInt(minutes1) - Integer.parseInt(minutes2) < 20)){
                return icona_parcheggio_libero_10mins;
            }
            else if ((Integer.parseInt(minutes1) - Integer.parseInt(minutes2) > 20) && (Integer.parseInt(minutes1) - Integer.parseInt(minutes2) < 30)){
                return icona_parcheggio_libero_20mins;
            }
            else return icona_parcheggio_libero;
        }
        return icona_parcheggio_libero;
    }

    public static String calculateDistance(LatLng p1, LatLng p2) {
        Double markerlat = p1.getLatitude();
        Double mylat = p2.getLatitude();

        Double markerlong = p1.getLongitude();
        Double mylong = p2.getLongitude();

        float[] results = new float[3]; //0 distance, 1 initial bearing, 2 final bearing
        Location.distanceBetween(mylat, mylong, markerlat, markerlong, results);

        String distance = "";
        if(results[0] >= 1000) {
            distance = String.valueOf(results[0] / 1000);
            int ind = distance.indexOf(".");
            distance = distance.substring(0,ind+2) + "km";
        }
        else {
            distance = String.valueOf(results[0]);
            int ind = distance.indexOf(".");
            distance = distance.substring(0,ind) + "m";
        }
        return distance;
    }

    public static float calculateDistanceInMeters(LatLng p1, LatLng p2) {
        Double markerlat = p1.getLatitude();
        Double mylat = p2.getLatitude();

        Double markerlong = p1.getLongitude();
        Double mylong = p2.getLongitude();

        float[] results = new float[3]; //0 distance, 1 initial bearing, 2 final bearing
        Location.distanceBetween(mylat, mylong, markerlat, markerlong, results);

        return results[0];
    }

    protected synchronized void buildGoogleApiClient() {
        mApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(ActivityRecognition.API)
                .build();
        mApiClient.connect();
    }

    private boolean isItalian(){
        return language == 0;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        fromNewIntent = true;
        //Log.w(TAG,intent.getAction() + " - " + intent.toString() + " - " + intent.getStringExtra("action"));
        if(intent.hasExtra("action")){
            //Log.w(TAG,"[INTENT] Si ha degli extra");
            String action = intent.getStringExtra("action");
            Double lat = intent.getDoubleExtra("lat",0);
            Double lng = intent.getDoubleExtra("lng",0);
            if(action.equals("toNotifiedParkingSpot")){
                CameraPosition position = new CameraPosition.Builder()
                        .target(new LatLng(lat,lng)) // Sets the new camera position
                        .zoom(17) // Sets the zoom to level 10
                        .bearing(0) // degree - azimut
                        .tilt(0) // Set the camera tilt to 20 degrees
                        .build(); // Builds the CameraPosition object from the builder
                mMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(position), null);
            }
        }
    }

    private String markerIdHashcode(Double lat, Double lon){
        long hasho = (long)((lat*15661+lon*27773)/33911);
        return ""+hasho;
    }

    private void feedback_activity(){
        Intent i = new Intent(this,FeedBackActivity.class);
        startActivity(i);
    }

    private String getDateFromMarkerID(long id){
        Iterator<String> it = events.iterator();

        while(it.hasNext()){
            String e = it.next();
            String[] split = e.split("-");
            if(split[0].equals(String.valueOf(id))) {
                return split[1];
            }
        }
        return "?";
    }

    private void setRepeatingAsyncTask(final MapboxMap map,final Set<String> events) {

        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            ColorManagerTask colorTask = new ColorManagerTask(map);
                            colorTask.execute(events);
                        } catch (Exception e) {
                            // error, do something
                        }
                    }
                });
            }
        };

        timer.schedule(task, 0, 3*60*1000);  // interval of 3 minute

    }

    @Override
    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {

    }

    @Override
    public void onFailure(Call<DirectionsResponse> call, Throwable t) {

    }
}

class ColorManagerTask extends AsyncTask<Set<String>, Void, Void> {
    private final String TAG = "Main";
    private MapboxMap map;

    public ColorManagerTask(MapboxMap m){
        this.map = m;
    }

    protected Void doInBackground(Set<String>... events) {

        if(map.equals(MainActivity.getmMap())){
            Log.w(TAG + "(Map Equalizer)","Maps are equals!");
        }

        Log.w("COLOR THREAD", "CHIAMATO");
        if(map != null){
            Log.w("COLOR THREAD", "CHIAMATO");
            Log.w("COLOR THREAD", "CHIAMATO");
            Log.w("COLOR THREAD", "CHIAMATO");
            Log.w("COLOR THREAD", "CHIAMATO");
            Log.w("COLOR THREAD", "CHIAMATO");
            Log.w("COLOR THREAD", "CHIAMATO");
            Log.w("COLOR THREAD", "CHIAMATO");

            //MainActivity.editor.putBoolean("colorThreadIsRunning", true);
            List<Marker> listMarker = map.getMarkers();
            Log.w(TAG + "(ColorManager)", "STARTED");
            Iterator<Marker> it = listMarker.iterator();
            while (it.hasNext()) {
                Marker MMM = it.next();
                String markerID = String.valueOf(MMM.getId());
                Log.w(TAG + "(ColorManager)", markerID+" is going to be colorEvaluated");

                Iterator<String> checkIterator = events[0].iterator();
                while (checkIterator.hasNext()) {
                    String markerSearcher = checkIterator.next();
                    String[] event = markerSearcher.split("-");
                    //Log.d("MarkerFound: ", "Evaluating Marker color");
                    /*MMM.setIcon(parkingIconEvaluator(markerSearcher));
                    map.removeMarker(MMM);
                    Marker j = map.addMarker(new MarkerOptions().position(MMM.getPosition())
                            .title("Parcheggio libero")
                            .setIcon(parkingIconEvaluator(markerSearcher)));
                    j.setId(MMM.getId());*/
                    if (event[0].equals(markerID)) {
                        Log.w(TAG + "(ColorManager)", "Marker found -> Evaluating Marker color");
                        MMM.setIcon(parkingIconEvaluator(markerSearcher));
                        map.updateMarker(MMM);
                    }
                }
            }
            Log.w(TAG + "(ColorManager)", "DONE");
            return null;
        }
        Log.w(TAG + "(ColorManager)", "Map is null");
        return null;

    }

}



class ProfileBitmapTask extends AsyncTask<String, Integer, Bitmap> {
    private final String TAG = "ProfileBitmapTask";
    protected Bitmap doInBackground(String... urls) {
        int count = urls.length;
        long totalSize = 0;
        Bitmap profileBitmap = null;
        for (int i = 0; i < count; i++) {
            try {
                URL aURL = new URL(urls[i]);
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
            // Escape early if cancel() is called
            if (isCancelled()) break;
        }
        return profileBitmap;
    }

}

class CheckEventsTask extends AsyncTask<Set<String>, Void, Set<String>> {
    private final String TAG = "Main";

    protected Set<String> doInBackground(Set<String>... events) {

        Log.w(TAG + "(CheckTask)","Checking events..");
        int count = events.length;
        for (int i = 0; i < count; i++) {
            Iterator<String> it = events[i].iterator();
            String now = new Date().toString();

            String time1 = now.split(" ")[3]; // current time
            String hour1 = time1.split(":")[0];
            String minutes1 = time1.split(":")[1];
            String seconds1 = time1.split(":")[2];
            while (it.hasNext()) {
                // event -> "UUID-event-date-latitude-longitude"
                try {
                    String e = it.next();
                    String[] event = e.split("-");
                    String date = event[2];

                    String time2 = date.split(" ")[3]; // event time
                    String hour2 = time2.split(":")[0];
                    String minutes2 = time2.split(":")[1];
                    String seconds2 = time2.split(":")[2];
                    if (Integer.parseInt(hour1) - Integer.parseInt(hour2) == 1) {
                        if (Integer.parseInt(minutes1) - Integer.parseInt(minutes2) >= 0)
                            events[i].remove(e);
                    }else if (Integer.parseInt(hour1) - Integer.parseInt(hour2) > 1){
                        events[i].remove(e);
                    }
                } catch (ConcurrentModificationException e) {
                    Log.w(TAG + "(CheckTask)","WARNING! -> Exception: " + e.getMessage());
                    return null;
                }
            }
            Log.w(TAG + "(CheckTask)", "Check DONE...");

            return events[0];
        }

        return events[0];
    }

}

class GetServerURITask extends AsyncTask<Void, Void, String> {
    private final String TAG = "GetServerURITask";

    protected String doInBackground(Void... v) {
        String url = "https://parkidle.github.io/serveruri.html";
            try {
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                // optional default is GET
                con.setRequestMethod("GET");


                int responseCode = con.getResponseCode();
                Log.w(TAG,"\nGetting server IP from: " + url);
                Log.w(TAG,"Response Code : " + responseCode);

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return response.toString();
            }catch(Exception e){
                e.printStackTrace();
            }
        return "";
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