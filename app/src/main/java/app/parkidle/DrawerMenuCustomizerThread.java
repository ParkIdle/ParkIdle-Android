package app.parkidle;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.NavigationView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by simonestaffa on 16/12/17.
 */

public class DrawerMenuCustomizerThread implements Runnable {

    private View drawer;
    private Bitmap imageBitmap;
    private final String TAG = "DrawerMenuCustomizerThread";

    public DrawerMenuCustomizerThread(View drawer){
        this.drawer = drawer;
    }

    @Override
    public void run() {
        // Moves the current Thread into the background
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        // getting Google Profile Image
        ImageView profile_img = (ImageView)drawer.findViewById(R.id.menu_photo);
        String image_uri = LoginActivity.getGoogleAccount().getPhotoUrl().toString();
        profile_img.setImageBitmap(getImageBitmap(image_uri));

        // Display Name nel Menu lateralte
        TextView display_name = drawer.findViewById(R.id.menu_display_name);
        display_name.setText(LoginActivity.getGoogleAccount().getDisplayName());

        // Email nel Menu laterale
        TextView email = drawer.findViewById(R.id.menu_email);
        email.setText(LoginActivity.getGoogleAccount().getEmail());
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
