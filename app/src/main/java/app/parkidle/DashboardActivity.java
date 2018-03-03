package app.parkidle;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.QuickContactBadge;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.Dash;

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

        //ranking dei parcheggi
        ProgressBar progressBar= findViewById(R.id.progressBarParking);
        TextView parcheggicount= findViewById(R.id.parkdone);
        RatingBar ratingbar= findViewById(R.id.ratingBar);

        progressBar.setProgress(MainActivity.sharedPreferences.getInt("parcheggiorank",0));
        parcheggicount.setText("Parcheggi segnalati: "+ MainActivity.sharedPreferences.getInt("parcheggiorank",0) );
        ratingbar.setRating(0);
        ratingbar.setClickable(false);

        if(MainActivity.sharedPreferences.getInt("parcheggiorank",0) > 10)
            ratingbar.setRating(1);
        else if(MainActivity.sharedPreferences.getInt("parcheggiorank",0) > 20)
            ratingbar.setRating(2);
        else if(MainActivity.sharedPreferences.getInt("parcheggiorank",0) > 50)
            ratingbar.setRating(3);
        else if (MainActivity.sharedPreferences.getInt("parcheggiorank",0) > 90)
            ratingbar.setRating(4);
        else if(MainActivity.sharedPreferences.getInt("parcheggiorank",0) > 110)
            ratingbar.setRating(5);


        userBadge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(DashboardActivity.this, FullScreenImage.class);
                DashboardActivity.this.startActivity(i);
            }
        });

        if(MainActivity.language == 0) Toast.makeText(this, "Work in progress\nQui potrai personalizzare il tuo profilo", Toast.LENGTH_SHORT).show();
        else Toast.makeText(this, "Work in progress\nHere you can customize your profile", Toast.LENGTH_SHORT).show();

    }








}
