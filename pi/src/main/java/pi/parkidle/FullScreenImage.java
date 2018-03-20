package pi.parkidle;

/**
 * Created by Alessio on 03/03/2018.
 */




import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;


    public class FullScreenImage  extends AppCompatActivity {

        private Uri mImageUri;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_fullscreenimg);

            ImageView fullScreenImageView = findViewById(R.id.imageView2);
            if(MainActivity.getImageBitmap(LoginActivity.getUser().getPhotoUrl().toString())!= null)
                fullScreenImageView.setImageBitmap(MainActivity.getImageBitmap(LoginActivity.getUser().getPhotoUrl().toString()));
        }
    }