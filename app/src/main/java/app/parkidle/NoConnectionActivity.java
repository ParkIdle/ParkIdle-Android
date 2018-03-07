package app.parkidle;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by misui on 06/03/2018.
 */

public class NoConnectionActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_connection);
        final Button button = findViewById(R.id.buttonNoConn);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent tutorial = new Intent(NoConnectionActivity.this,LoginActivity.class);
                tutorial.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(tutorial);
            }
        });
    }
}