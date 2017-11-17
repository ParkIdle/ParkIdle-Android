package app.parkidle;

import android.Manifest;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import io.predict.PIOTripSegment;
import io.predict.PIOZone;
import io.predict.PredictIO;
import io.predict.PredictIOListener;
import io.predict.PredictIOStatus;

/**
 * Created by simonestaffa on 16/11/17.
 */

public class PIOManager extends Application implements PredictIOListener{

    private PredictIOStatus PIOStatus;
    private PredictIO.PIOActivationListener PIOActivationListenerInstance;
    private String deviceIdentifier;
    private Boolean searchInPerimeter;
    private PIOZone homeZone;
    private PIOZone workZone;
    private String myServerURL;

    public void onCreate(){
        // The following code sample instantiate predict.io SDK and sets the callbacks:
        PredictIO predictIO = PredictIO.getInstance(getApplicationContext());
        predictIO.setAppOnCreate(this);
        predictIO.setListener(this);
        PIOActivationListenerInstance = predictIO.getActivationListener();

        // set this to get event callbacks in broadcast receiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PredictIO.DEPARTED_EVENT);
        intentFilter.addAction(PredictIO.CANCELED_DEPARTURE_EVENT);
        intentFilter.addAction(PredictIO.SUSPECTED_ARRIVAL_EVENT);
        intentFilter.addAction(PredictIO.ARRIVED_EVENT);
        intentFilter.addAction(PredictIO.SEARCHING_PARKING_EVENT);
        intentFilter.addAction(PredictIO.DETECTED_TRANSPORTATION_MODE_EVENT);
        intentFilter.addAction(PredictIO.BEING_STATIONARY_AFTER_ARRIVAL_EVENT);
        intentFilter.addAction(PredictIO.TRAVELED_BY_AIRPLANE_EVENT);
        //LocalBroadcastManager.getInstance(this).registerReceiver(this, intentFilter);

        /**
         * This method starts predict.io tracker, it will validate the API KEY (Asynchronously),
         * if predict.io activated successfully 'onActivated' method of PIOActivationListener will be
         * invoked otherwise 'onActivationFailed' method will be invoked with appropriate error code.
         *
         * @throws Exception with message "Google Play Services Error" if Google Play services are not installed
         *                   with message "Permission is not granted by user." if required dangerous permission is not granted.
         */
        try {
            predictIO.start(PIOActivationListenerInstance);
        }catch(Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    /**
     ****************************************
     ********* Events Via Listener **********
     ****************************************
     */

    /**
     * This method is invoked when predict.io detects that the user has just departed from his location
     * and have started a new trip
     *
     * @param tripSegment PIOTripSegment contains details about departed event. The following properties
     *                    are populated currently
     *                    UUID: Unique ID for a trip segment, e.g. to link departure and arrival events
     *                    event_UUID: Unique ID for particular event, e.g. departed event Id
     *                    departureLocation: The Location from where the user departed
     *                    departureTime: Time of departure
     *                    departureZone: Departure zone
     */
    @Override
    public void departed(PIOTripSegment pioTripSegment) {

    }

    /**
     * This method is invoked when predict.io detects that the user has just arrived at destination
     *
     * @param tripSegment: PIOTripSegment contains details about arrived event. The following properties
     *                     are populated currently
     *                     UUID: Unique ID for a trip segment, e.g. to link departure and arrival events
     *                     event_UUID: Unique ID for particular event, e.g. arrived event Id
     *                     departureLocation: The Location from where the user departed
     *                     arrivalLocation: The Location where the user arrived and ended the trip
     *                     departureTime: Time of departure
     *                     arrivalTime: Time of arrival
     *                     transportationMode: Mode of transportation
     *                     departureZone: Departure zone
     *                     arrivalZone: Arrival Zone
     */
    @Override
    public void arrived(PIOTripSegment pioTripSegment) {

    }

    /**
     * This method is invoked when predict.io suspects that the user has just arrived at his location
     * and have ended a trip. Most of the time it is followed by a confirmed arrived event. If you
     * need only confirmed arrival events, use {@link #arrived(PIOTripSegment tripSegment)} method instead
     *
     * @param tripSegment: PIOTripSegment contains details about arrival suspected event. The following
     *                     properties are populated currently
     *                     UUID: Unique ID for a trip segment, e.g. to link departure and arrival events
     *                     event_UUID: Unique ID for particular event, e.g. suspected arrival event Id
     *                     departureLocation: The Location from where the user departed
     *                     arrivalLocation: The Location where the user arrived and ended the trip
     *                     departureTime: Time of departure
     *                     arrivalTime: Time of arrival
     *                     transportationMode: Mode of transportation
     *                     departureZone: Departure zone
     *                     arrivalZone: Arrival Zone
     */
    @Override
    public void suspectedArrival(PIOTripSegment pioTripSegment) {

    }

    /**
     * This method is invoked when predict.io detects that the user is searching for a
     * parking space at a specific location
     *
     * @param location: The Location where predict.io identifies that user is searching for a parking space
     */
    @Override
    public void searchingInPerimeter(Location location) {

    }

    /**
     * This method is invoked when predict.io is unable to validate the last departure event. This
     * can be due to invalid data received from sensors or the trip amplitude. i.e. If the trip takes
     * less than 5 minutes or the distance travelled is less than 1km
     *
     * @param tripSegment: PIOTripSegment have departure canceled event information. The following properties
     *                     are populated currently
     *                     UUID: Unique ID for a trip segment, e.g. to link departure and departure canceled events
     *                    `event_UUID: Unique ID for particular event, e.g. departure cancellation event Id
     *                     departureLocation: The Location from where the user departed
     *                     departureTime: Time of departure
     *                     transportationMode: Mode of transportation
     */
    @Override
    public void canceledDeparture(PIOTripSegment pioTripSegment) {

    }

    /**
     * This is invoked when new location information is received from location services
     * Implemented this method if you need raw GPS data, instead of creating new location manager
     * Since, it is not recommended to use multiple location managers in a single app
     *
     * @param location: New location
     */
    @Override
    public void didUpdateLocation(Location location) {

    }

    /**
     * This method is invoked when predict.io detects transportation mode
     *
     * @param tripSegment: PIOTripSegment contains details about users transportation mode. The
     *                     following properties are populated currently
     *                     UUID: Unique ID for a trip segment, e.g. to link departure and arrival events
     *                     event_UUID: Unique ID for particular event, e.g. detected transport mode event Id
     *                     departureLocation: The Location from where the user departed
     *                     departureTime: Time of departure
     *                     transportationMode: Mode of transportation
     *                     departureZone: Departure zone
     */
    @Override
    public void detectedTransportationMode(PIOTripSegment pioTripSegment) {

    }

    /**
     *  This method is invoked after few minutes of arriving at the destination and detects if the user
     *  is stationary or not
     *
     * @param tripSegment: PIOTripSegment contains details about arrived event. The following properties
     *                     are populated currently
     *                     UUID: Unique ID for a trip segment, e.g. to link departure and arrival events
     *                     event_UUID: Unique ID for particular event, e.g. departure cancellation event Id
     *                     departureLocation: The Location from where the user departed
     *                     arrivalLocation: The Location where the user arrived and ended the trip
     *                     departureTime: Time of departure
     *                     arrivalTime: Time of arrival
     *                     transportationMode: Mode of transportation
     *                     departureZone: Departure zone
     *                     arrivalZone: Arrival Zone
     *                     stationaryAfterArrival: User activity status as stationary or not
     */
    @Override
    public void beingStationaryAfterArrival(PIOTripSegment pioTripSegment) {

    }

    /**
     * This method is invoked when predict.io detects that the user has traveled by airplane and
     * just arrived at destination, this event is independent of usual vehicle trip detection and
     * will not have predecessor departed event
     *
     * @param tripSegment: PIOTripSegment contains details about traveled by airplane event. The
     *                     following properties are populated currently
     *                     UUID: Unique ID for a trip segment
     *                     event_UUID: Unique ID for particular event, e.g. travel by airplane event Id
     *                     departureLocation: The Location from where the user started journey
     *                     arrivalLocation: The Location where the user arrived and ended the journey
     *                     departureTime: Time of departure
     *                     arrivalTime: Time of arrival
     */
    @Override
    public void traveledByAirplane(PIOTripSegment pioTripSegment) {

    }

}
