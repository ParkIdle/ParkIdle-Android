package app.parkidle;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class GpsDialogActivity extends Activity {

    private boolean isGpsEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps_dialog);
        isGpsEnabled = false;
        buildAlertMessage();
    }

    public void buildAlertMessage() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Il tuo GPS è disattivo. Per usare l'app è necessario attivarlo, vuoi farlo?")
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
                        finish();
                        dialog.cancel();
                    }
                });

        final AlertDialog alert = builder.create();
        alert.show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(isGpsEnabled){
            Intent i = new Intent(this,MyLocationService.class);
            i.setAction("fromGpsDialog");
            startService(i);
            finish();
        }
    }
}
