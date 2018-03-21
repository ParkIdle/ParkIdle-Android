package pi.parkidle;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by Matteo on 07/03/18.
 */

public class FragmentTwo extends android.support.v4.app.Fragment {
    private ImageView timing_parcheggi;
    private ImageView box_testo_timer;
    private ImageView tasto_avanti;

    public FragmentTwo(){

    }
    @Nullable

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        timing_parcheggi.setImageDrawable(null);
        box_testo_timer.setImageDrawable(null);
        tasto_avanti.setImageDrawable(null);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View v2 = inflater.inflate(R.layout.fragment_two_layout,container,false);
        super.onViewCreated(v2, savedInstanceState);

        /*BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(),R.drawable.timing_parcheggi , options);
        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;
        String imageType = options.outMimeType;
        Log.w("FRAGMENT TWO","IMMAGINE FUNZIONE PARCHEGGIO: " + imageHeight+ "x" +imageWidth +": Ora il tipo dell'immagine: ."+imageType);
        Log.w("FRAGMENT TWO","IMMAGINE FUNZIONE PARCHEGGIO: " + imageHeight +"x" +imageWidth +": Ora il tipo dell'immagine: ."+imageType);
        Log.w("FRAGMENT TWO" ,"IMMAGINE FUNZIONE PARCHEGGIO: " + imageHeight+"x" +imageWidth +": Ora il tipo dell'immagine: ."+imageType);
        Bitmap timing_parcheggi_decoded = decodeSampledBitmapFromResource(getResources(),R.drawable.timing_parcheggi,262,341);
        if(timing_parcheggi_decoded!= null){
            int byte_spesi = timing_parcheggi_decoded.getByteCount();
            int byte_allocati = timing_parcheggi_decoded.getAllocationByteCount();
            Log.w("FRAGMENT TWO","Immagine decodificata: Byte spesi: " + byte_spesi + " , Byte allocati : "+ byte_allocati);
        }*/

        timing_parcheggi = v2.findViewById(R.id.imageView10);
        box_testo_timer = v2.findViewById(R.id.imageView12);
        tasto_avanti = v2.findViewById(R.id.tasto_avanti_2);
        tasto_avanti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TutorialActivity)getActivity()).switchToNextFragment2();
            }
        });

        return v2;
    }
}

