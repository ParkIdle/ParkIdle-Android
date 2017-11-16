package app.parkidle;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import io.predict.PredictIO;
import io.predict.PredictIOListener;
import io.predict.PredictIOStatus;

public class MainActivity extends AppCompatActivity{

    private static final int ACCESS_FINE_LOCATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION_PERMISSION);
            return;
        }

    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode) {
            case ACCESS_FINE_LOCATION_PERMISSION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted.
                    Toast.makeText(this, "GPS permission successfully granted!", Toast.LENGTH_LONG).show();
                } else {
                    // User refused to grant permission. You can add AlertDialog here
                    Toast.makeText(this, "GPS permission not granted. Please allow GPS using to use this app.", Toast.LENGTH_LONG).show();

                }
            }
        }
    }

}
