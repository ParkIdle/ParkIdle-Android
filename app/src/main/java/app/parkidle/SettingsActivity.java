package app.parkidle;

import android.content.ClipData;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

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
                        R.array.language_array, android.R.layout.simple_spinner_item);
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
                        Toast.makeText(SettingsActivity.this, "Riavvia l'app per i cambiamenti \nRestart the app to apply changes", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
        });
    }
}
