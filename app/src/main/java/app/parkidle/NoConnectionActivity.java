package app.parkidle;

import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by misui on 06/03/2018.
 */

public class NoConnectionActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_connection);

        OnConnectivityChangeReceiver onConnectivityChangeReceiver = new OnConnectivityChangeReceiver();
        IntentFilter filterConn = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(onConnectivityChangeReceiver,filterConn);
    }
}