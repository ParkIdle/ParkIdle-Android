package app.parkidle;

import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.services.android.navigation.ui.v5.route.RouteViewModel;

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static app.parkidle.MainActivity.editor;
import static app.parkidle.MainActivity.metric;
import static app.parkidle.MainActivity.sharedPreferences;

public class SettingsActivity extends AppCompatActivity {
    //roba per il menù
    public static SeekBar seek_bar;
    private static TextView text_view;
    private Spinner spinner_unita_misura;
    private ArrayAdapter adapter;
    private Switch switch_risparmio_batteria;
    private Switch switchBackgroundNotification;
    private Button trovabutton;
    private EditText indirizzo;
    private TextView casa;
    private TextView lavoro;
    private boolean needRefresh;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    public void geocoding(String posizione){
        Geocoder geoc= new Geocoder(this);
        try {
            List<Address> list = geoc.getFromLocationName(posizione,1);
            if (list.isEmpty()){
                Toast.makeText(this, "Impossibile trovare indirizzo", Toast.LENGTH_SHORT).show();
                return;
            }
            Address add= list.get(0);
            String locality = add.getLocality();
            if(MainActivity.sharedPreferences.getInt("language",0)==0)
                Toast.makeText(this, "Posizione inserita,la tua casa si trova a " + locality, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, "Address added, Home is in " + locality, Toast.LENGTH_SHORT).show();
            double lat=add.getLatitude();
            double longi=add.getLongitude();
            //Toast.makeText(this, (float)lat +" " + (float)longi, Toast.LENGTH_SHORT).show();

            MainActivity.editor.putString("lathouse", String.valueOf(lat));
            MainActivity.editor.commit();
            MainActivity.editor.putString("longhouse",  String.valueOf(longi));
            MainActivity.editor.commit();

        } catch (IOException e) {
            Toast.makeText(this, "Impossibile trovare luogo", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void geocodingLavoro(String posizione){
        Geocoder geoc= new Geocoder(this);
        try {
            List<Address> list = geoc.getFromLocationName(posizione,1);
            if (list.isEmpty()){
                Toast.makeText(this, "Impossibile trovare indirizzo", Toast.LENGTH_SHORT).show();
                return;
            }
            Address add= list.get(0);
            String locality = add.getLocality();
            if(MainActivity.sharedPreferences.getInt("language",0)==0)
                Toast.makeText(this, "Posizione inserita, il tuo posto di lavoro si trova a " + locality, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, "Address added, workplace is in " + locality, Toast.LENGTH_SHORT).show();
            double lat=add.getLatitude();
            double longi=add.getLongitude();
            //Toast.makeText(this, (float)lat +" " + (float)longi, Toast.LENGTH_SHORT).show();

            MainActivity.editor.putString("latwork", String.valueOf(lat));
            MainActivity.editor.commit();
            MainActivity.editor.putString("longwork",  String.valueOf(longi));
            MainActivity.editor.commit();

        } catch (IOException e) {
            Toast.makeText(this, "Impossibile trovare luogo", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /*public void switch_batteria(){
        switch_risparmio_batteria=(Switch) findViewById(R.id.switch1);
        switch_risparmio_batteria.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    Toast.makeText(SettingsActivity.this, "Risparmio batteria attivato", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(SettingsActivity.this, "Risparmio batteria disattivato", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }*/

    public void switch_background(){
        switchBackgroundNotification = (Switch) findViewById(R.id.switch_notification);
        switchBackgroundNotification.setChecked(sharedPreferences.getBoolean("backgroundNotify",false));

        switchBackgroundNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    Toast.makeText(SettingsActivity.this, "Notifiche in background attivate", Toast.LENGTH_SHORT).show();
                    editor.putBoolean("backgroundNotify",true);
                    editor.commit();
                }
                else{
                    Toast.makeText(SettingsActivity.this, "Notifiche in background disattivate", Toast.LENGTH_SHORT).show();
                    editor.putBoolean("backgroundNotify",false);
                    editor.commit();
                }
            }
        });
    }


    public void seekbar(){
        seek_bar=(SeekBar)findViewById(R.id.seek_Bar);
        text_view=(TextView)findViewById(R.id.text_view);
        seek_bar.setProgress(MainActivity.sharedPreferences.getInt("progressKm",2));
        text_view.setText(seek_bar.getProgress()+"km");


        seek_bar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    int progress_value;
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        progress_value = progress;
                        text_view.setText(progress+" km");

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        text_view.setText(progress_value+" km");
                        MainActivity.editor.putInt("progressKm",progress_value);
                        MainActivity.editor.commit();

                    }
                }
        );

    }
    public void casa(){
        casa=(TextView) findViewById(R.id.text_view_casa);
        if (MainActivity.sharedPreferences.getString("lathouse","0").equals("0")
                && MainActivity.sharedPreferences.getString("longhouse","0").equals("0"))
            casa.setText(getResources().getString(R.string.casa_non_inserita));
        else casa.setText(getResources().getString(R.string.casa_inserita));

    }

    public void cas(View view1){
        AlertDialog.Builder mBuilder=new AlertDialog.Builder(SettingsActivity.this);
        View view =getLayoutInflater().inflate(R.layout.geocoding_layout,null);
        final EditText mindirizzo= (EditText) view.findViewById((R.id.indirizzo_txt));
        Button trova= (Button) view.findViewById((R.id.trova_button_1));
        mBuilder.setView(view);
        final AlertDialog dialog =mBuilder.create();
        dialog.show();

        trova.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mindirizzo.getText().toString().isEmpty()) {
                    Toast.makeText(SettingsActivity.this, "Inserisci un indirizzo", Toast.LENGTH_SHORT).show();
                    return;
                }
                else{
                    geocoding(mindirizzo.getText().toString());
                    dialog.cancel();
                    return;
                }
            }
        });

        return;
    }

    public void lavoro(){
        lavoro=(TextView) findViewById(R.id.text_view_lavoro);

        if (MainActivity.sharedPreferences.getString("latwork","0").equals("0")
                && MainActivity.sharedPreferences.getString("longwork","0").equals("0"))
            lavoro.setText(getResources().getString(R.string.lavoro_non_inserito));
        else lavoro.setText(getResources().getString(R.string.lavoro_inserito));

    }

   public void lav(View view1){

       AlertDialog.Builder mBuilder=new AlertDialog.Builder(SettingsActivity.this);
       View view =getLayoutInflater().inflate(R.layout.geocoding_layout,null);
       final EditText mindirizzo= (EditText) view.findViewById((R.id.indirizzo_txt));
       Button trova= (Button) view.findViewById((R.id.trova_button_1));
       mBuilder.setView(view);
       final AlertDialog dialog =mBuilder.create();
       dialog.show();
       trova.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               if(mindirizzo.getText().toString().isEmpty()) {
                   Toast.makeText(SettingsActivity.this, "Inserisci un indirizzo", Toast.LENGTH_SHORT).show();
                   return;
               }
               else{
                   geocodingLavoro(mindirizzo.getText().toString());
                   dialog.cancel();
                   return;
               }
           }
       });

       return;



   }

    public void sceglilingua(){
        Configuration conf = getResources().getConfiguration();

        if (sharedPreferences.getInt("language",0)==0) {
            //Toast.makeText(this, "Lingua settata su italiano", Toast.LENGTH_SHORT).show();
            Log.w("LINGUA","Lingua settata su Italiano");
            conf.locale = new Locale("it"); //ita language locale
        }
        else {
            //Toast.makeText(this, "Lingua settata su inglese", Toast.LENGTH_SHORT).show();
            Log.w("LINGUA","Language now is English");
            conf.locale=new Locale("en");

        }
        getResources().updateConfiguration(conf,getResources().getDisplayMetrics());


    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        needRefresh = false;
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if(key.equals("language") || key.equals("metric") || key.equals("latwork") || key.equals("longwork")
                        || key.equals("lathouse") || key.equals("longhouse") || key.equals("progressKm") || key.equals("backgroundNotify")
                        || key.equals("longwork")){
                    Log.w("Settings","Shared Preferences changed!");
                    needRefresh = true;
                }
            }
        };
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {


                TextView languageLabel = (TextView)findViewById(R.id.language_label);
                languageLabel.setText(getResources().getString(R.string.lingua));
                final Spinner spinner = (Spinner) findViewById(R.id.language_spinner);
                // Create an ArrayAdapter using the string array and a default spinner layout
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(SettingsActivity.this,
                        R.array.language_options, android.R.layout.simple_spinner_item);
                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                // Apply the adapter to the spinner
                spinner.setAdapter(adapter);
                spinner.setSelection(MainActivity.sharedPreferences.getInt("language",0));
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String text = spinner.getItemAtPosition(position).toString();
                        spinner.setSelection(position);

                        if(position != MainActivity.sharedPreferences.getInt("language",0)) {
                            MainActivity.editor.putInt("language", position);
                            MainActivity.editor.commit();
                        }
                        sceglilingua();
                        //Toast.makeText(SettingsActivity.this, "Riavvia l'app per i cambiamenti \nRestart the app to apply changes", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                seekbar();
                //switch_batteria();
                switch_background();
                casa();
                lavoro();

                adapter=ArrayAdapter.createFromResource(SettingsActivity.this, R.array.spinner_options,android.R.layout.simple_spinner_item);
                spinner_unita_misura=(Spinner) findViewById(R.id.unita_misura_spinner);
                if( MainActivity.sharedPreferences.getInt("metric",0)==0) {
                    spinner_unita_misura.setSelection(0);
                }
                else {
                    spinner_unita_misura.setSelection(1);
                }
                spinner_unita_misura.setAdapter(adapter);
                spinner_unita_misura.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String text1 = spinner_unita_misura.getItemAtPosition(position).toString();

                        TextView spinner_dialog_text=(TextView) view;
                        //Toast.makeText(SettingsActivity.this, "Hai selezionato: "+ spinner_dialog_text.getText(), Toast.LENGTH_SHORT).show();
                        if(text1.equals("Kilometri")){
                            MainActivity.editor.putInt("metric",0);
                            MainActivity.editor.commit();}
                        else  {
                            MainActivity.editor.putInt("metric",1);
                            MainActivity.editor.commit();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });


            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Toast.makeText(this, "Recreate", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(needRefresh){
            startActivity(new Intent(this,ChangedSettingsDialogActivity.class));
        }
        needRefresh = false;
    }
}
