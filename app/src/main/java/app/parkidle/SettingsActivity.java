package app.parkidle;

import android.content.ClipData;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import static app.parkidle.MainActivity.metric;

public class SettingsActivity extends AppCompatActivity {
    //roba per il menù
    public static SeekBar seek_bar;
    private static TextView text_view;
    private Spinner spinner_unita_misura;
    private ArrayAdapter adapter;
    private Switch switch_risparmio_batteria;
    private Button trovabutton;
    private EditText indirizzo;

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
            if(MainActivity.language==0)
                Toast.makeText(this, "La tua casa si trova a " + locality, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, "Home is in " + locality, Toast.LENGTH_SHORT).show();
            double lat=add.getLatitude();
            double longi=add.getLongitude();
            //Toast.makeText(this, (float)lat +" " + (float)longi, Toast.LENGTH_SHORT).show();

            MainActivity.editor.putString("lathouse", String.valueOf(lat));
            MainActivity.editor.putString("longhouse",  String.valueOf(longi));

        } catch (IOException e) {
            Toast.makeText(this, "Impossibile trovare luogo", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void switch_batteria(){
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
    }

    public void trovabutton(){
        indirizzo=(EditText) findViewById((R.id.indirizzo));
        trovabutton=(Button) findViewById(R.id.trovabutton);
        trovabutton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(indirizzo.getText().equals("")) {
                    Toast.makeText(SettingsActivity.this, "Inserisci un indirizzo", Toast.LENGTH_SHORT).show();
                }
                else {
                    geocoding(indirizzo.getText().toString());
                }
            }
        });
    }



    public void seekbar(){
        seek_bar=(SeekBar)findViewById(R.id.seek_Bar);
        text_view=(TextView)findViewById(R.id.text_view);
        seek_bar.setProgress(MainActivity.sharedPreferences.getInt("progressKm",50));
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

                    }
                }
        );

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView languageLabel = (TextView)findViewById(R.id.language_label);
                if(MainActivity.language == 0) languageLabel.setText("Lingua");
                else languageLabel.setText("Language");
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
                        MainActivity.editor.putInt("language",position);
                        MainActivity.editor.apply();
                        MainActivity.cambialingua();
                        Toast.makeText(SettingsActivity.this, "Riavvia l'app per i cambiamenti \nRestart the app to apply changes", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                seekbar();
                trovabutton();
                switch_batteria();
                adapter=ArrayAdapter.createFromResource(SettingsActivity.this, R.array.spinner_options,android.R.layout.simple_spinner_item);
                spinner_unita_misura=(Spinner) findViewById(R.id.unita_misura_spinner);
                spinner_unita_misura.setAdapter(adapter);
                spinner_unita_misura.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String text1 = spinner_unita_misura.getItemAtPosition(position).toString();

                        TextView spinner_dialog_text=(TextView) view;
                        Toast.makeText(SettingsActivity.this, "Hai selezionato: "+ spinner_dialog_text.getText(), Toast.LENGTH_SHORT).show();
                        if(text1.equals("Kilometri")) metric=0;
                        else metric=1;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
        });
    }
}
