package pi.parkidle;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andrea on 01/02/2018.
 */

public class DatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    public static final int DATABASE_VERSION = 1;

    // Database Name
    public static final String DATABASE_NAME = "markerManager";

    // Markers table name
    public static final String TABLE_MARKERS = "marker";

    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_EVENT = "event";
    private static final String KEY_DATE= "date";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_MARKER = "marker";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_MARKER_TABLE = "CREATE TABLE " + TABLE_MARKERS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_EVENT + " TEXT,"
                + KEY_DATE + " TEXT," + KEY_LATITUDE + " TEXT," + KEY_LONGITUDE + " TEXT," + KEY_MARKER + " TEXT" + ")";
        db.execSQL(CREATE_MARKER_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MARKERS);

        // Create tables again
        onCreate(db);
    }



    // Adding new marker
    public void addMarker(DatabasedMarker marker) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, marker.get_id());
        values.put(KEY_EVENT, marker.get_event());
        values.put(KEY_DATE, marker.get_date());
        values.put(KEY_LATITUDE, marker.get_latitude());
        values.put(KEY_LONGITUDE, marker.get_longitude());
        values.put(KEY_MARKER, marker.get_markerObj());

        // Inserting Row
        db.insert(TABLE_MARKERS, null, values);
        db.close(); // Closing database connection
    }

    public List<DatabasedMarker> getAllMarker() {
        List<DatabasedMarker> contactList = new ArrayList<DatabasedMarker>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_MARKERS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                DatabasedMarker marker = new DatabasedMarker();
                marker.set_id(cursor.getString(0));
                marker.set_event(cursor.getString(1));
                marker.set_date(cursor.getString(2));
                marker.set_latitude(cursor.getString(3));
                marker.set_longitude(cursor.getString(4));
                marker.set_markerObj(cursor.getString(5));
                // Adding contact to list
                contactList.add(marker);
            } while (cursor.moveToNext());
        }

        // return contact list
        return contactList;
    }

    public void clearDatabase(){
        this.getWritableDatabase().delete(TABLE_MARKERS, null, null);
    }



}
