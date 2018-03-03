package app.parkidle;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.QuickContactBadge;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by misu on 04/01/18.
 */

public class DashboardActivity extends AppCompatActivity{

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private TextView userText;
    private ImageView userBadge;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        userText = findViewById(R.id.userText);
        userText.setText(LoginActivity.getUser().getDisplayName());
        userBadge = findViewById(R.id.userBadge);
        userBadge.setImageBitmap(MainActivity.getImageBitmap(LoginActivity.getUser().getPhotoUrl().toString()));

        ProgressBar progressBar= findViewById(R.id.progressBarParking);
        TextView parcheggicount= findViewById(R.id.parkdone);
        progressBar.setProgress(MainActivity.sharedPreferences.getInt("parcheggiorank",0));
        parcheggicount.setText("Parcheggi segnalati: "+ MainActivity.sharedPreferences.getInt("parcheggiorank",0) );


        if(MainActivity.language == 0) Toast.makeText(this, "Work in progress\nQui potrai personalizzare il tuo profilo", Toast.LENGTH_SHORT).show();
        else Toast.makeText(this, "Work in progress\nHere you can customize your profile", Toast.LENGTH_SHORT).show();
    }








}
