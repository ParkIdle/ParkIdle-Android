package app.parkidle;

import android.content.ClipData;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import static app.parkidle.MainActivity.metric;

public class SettingsActivity extends AppCompatActivity {
    //roba per il menù
    public static SeekBar seek_bar;
    private static TextView text_view;
    private Spinner spinner_unita_misura;
    private ArrayAdapter adapter;
    private Switch switch_risparmio_batteria;

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
