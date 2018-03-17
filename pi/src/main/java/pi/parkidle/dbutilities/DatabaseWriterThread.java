package pi.parkidle.dbutilities;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by simonestaffa on 18/12/17.
 */

public class DatabaseWriterThread implements Runnable{
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private ParkingSpot parkingSpot;

    public DatabaseWriterThread(ParkingSpot parkingSpot){
        this.parkingSpot = parkingSpot;
    }

    @Override
    public void run() {
        writeNewParkingSpot();
    }

    private void writeNewParkingSpot() {
        String key = myRef.child("parkingSpots").push().getKey();

        Map<String, Object> parkingSpotsValues = parkingSpot.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/parkingSpots", parkingSpotsValues);

        myRef.updateChildren(childUpdates);

    }

}
