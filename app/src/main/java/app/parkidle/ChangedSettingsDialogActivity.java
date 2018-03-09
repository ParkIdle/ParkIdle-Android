package app.parkidle;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ChangedSettingsDialogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changed_settings_dialog);
        setContentView(R.layout.activity_gps_dialog);
        buildAlertMessage();
    }

    public void buildAlertMessage() {
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.cambio_preferenze))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.conferma), new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {

                        Intent i = getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage( getBaseContext().getPackageName() );
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);

                    }
                })
                .setNegativeButton(getResources().getString(R.string.nega), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {

                        finish();
                        dialog.cancel();
                    }
                });

        final android.app.AlertDialog alert = builder.create();
        alert.show();
    }
}
