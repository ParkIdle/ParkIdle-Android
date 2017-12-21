package app.parkidle;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Process;
import android.support.design.widget.NavigationView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by simonestaffa on 16/12/17.
 */

public class DrawerMenuCustomizerThread implements Runnable {

    private ImageView profile_img;
    private TextView display_name;
    private TextView email;
    private Bitmap imageBitmap;
    private final String TAG = "DrawerMenuCustomizerThread";

    public DrawerMenuCustomizerThread(ImageView profile_img, TextView display_name, TextView email){
        this.profile_img = profile_img;
        this.display_name = display_name;
        this.email = email;
    }

    @Override
    public void run() {

            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            // getting Facebook Profile Image

            String image_uri = LoginActivity.getUser().getPhotoUrl().toString();
            if(image_uri.contains(".jpg") || image_uri.contains(".png"))
                profile_img.setImageBitmap(getImageBitmap(image_uri));

            // Display Name nel Menu laterale
            display_name.setText(LoginActivity.getUser().getDisplayName());

            // Email nel Menu laterale
            email.setText(LoginActivity.getUser().getEmail());

    }

    private Bitmap getImageBitmap(String url) {
        Bitmap bm = null;
        try {
            URL aURL = new URL(url);
            URLConnection conn = aURL.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            bm = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();
        } catch (IOException e) {
            Log.e(TAG, "Error getting bitmap", e);
        }
        return bm;
    }

}
