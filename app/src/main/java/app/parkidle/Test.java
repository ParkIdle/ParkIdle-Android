package app.parkidle;

import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.amazonaws.http.HttpClient;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import io.predict.PredictIO;
import retrofit2.http.HTTP;

/**
 * Created by simonestaffa on 23/11/17.
 */

public class Test implements Runnable {

    private final Location mLastLocation;
    private final String event;
    private Context context;
    private Handler mHandler;

    public Test(Context context, Location location, String event){
        this.mLastLocation = location;
        this.context = context;
        this.event=event;

    }

    @Override
    public void run() {
        Looper.prepare();
        Handler handler = new Handler(Looper.getMainLooper());

        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, " Test isRunning", Toast.LENGTH_LONG).show();
            }
        });

        try {
            //URL url = new URL(PIOManager.myServerURL);
            URL url = new URL("https://requestb.in/1amp6dc1");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            //conn.setRequestProperty("USER-AGENT", "Chrome/7.0.517.41");
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setDoInput(true);

            DataOutputStream os = new DataOutputStream(conn.getOutputStream());

            JSONObject jsonParam = new JSONObject();

            try {
                jsonParam.put("UUID", "1234");
                jsonParam.put("event", event);
                jsonParam.put("time", mLastLocation.getTime());
                jsonParam.put("latitude", mLastLocation.getLatitude());
                jsonParam.put("longitude", mLastLocation.getLongitude());

                os.write(jsonParam.toString().getBytes("utf-8"));
                os.flush();
                os.close();
                int responseCode = conn.getResponseCode();
                String response = "";
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line=br.readLine()) != null) {
                        response+=line;
                    }
                }
                else {
                    response="";

                }
                Toast.makeText(context, response, Toast.LENGTH_SHORT).show();
            }catch(JSONException e){
                e.printStackTrace();
            }

            conn.disconnect();


        }catch(ProtocolException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
        Looper.loop();
    }
}
