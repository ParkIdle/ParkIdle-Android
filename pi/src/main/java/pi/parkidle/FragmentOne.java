package pi.parkidle;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by Matteo on 07/03/18.
 */

public class FragmentOne extends android.support.v4.app.Fragment{
    public ViewPager viewPager;



    public FragmentOne(){

    }
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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        /*String info_funzione_parcheggio = "IMMAGINE FUNZIONE PARCHEGGIO: 886x940: Ora il tipo dell'immagine: .image/png";
        String immagine_codificata = "FRAGMENT ONE: BYTE USATI CON DECODIFICA: 22962272 , BYTE ALLOCATI: 22962272";
        //183.437 byte*/


        View v = inflater.inflate(R.layout.fragment_one_layout,container,false);
        super.onViewCreated(v, savedInstanceState);

        /*ImageView funzione_parcheggio = v.findViewById(R.id.imageView4);


        //Bitmap funzione_park_decoded = decodeSampledBitmapFromResource(getResources(),R.drawable.funzione_parcheggio,302,451);
        Bitmap funzione_park_decoded = decodeSampledBitmapFromResource(getResources(),R.drawable.funzione_parcheggio,302,451);
        if(funzione_park_decoded!=null) {
            funzione_parcheggio.setImageBitmap(funzione_park_decoded);
            int byte_spesi = funzione_park_decoded.getByteCount();
            int byte_allocati = funzione_park_decoded.getAllocationByteCount();

            Log.w("FRAGMENT ONE","BYTE USATI CON DECODIFICA: "+byte_spesi+" , BYTE ALLOCATI: "+byte_allocati);

        }*/
        /*BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(),R.drawable.tasto_avanti,options);
        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;
        String imageType = options.outMimeType;
        Log.w("FRAGMENT ONE","IMMAGINE FUNZIONE PARCHEGGIO: " + imageHeight+ "x" +imageWidth +": Ora il tipo dell'immagine: ."+imageType);
        Log.w("FRAGMENT ONE","IMMAGINE FUNZIONE PARCHEGGIO: " + imageHeight +"x" +imageWidth +": Ora il tipo dell'immagine: ."+imageType);
        Log.w("FRAGMENT ONE","IMMAGINE FUNZIONE PARCHEGGIO: " + imageHeight+"x" +imageWidth +": Ora il tipo dell'immagine: ."+imageType);
        Bitmap funzione_parcheggio = decodeSampledBitmapFromResource(getResources(),R.drawable.tasto_avanti,302,451);
        if(funzione_parcheggio != null){
            int byte_spesi = funzione_parcheggio.getByteCount();
            int byte_allocati = funzione_parcheggio.getAllocationByteCount();

            Log.w("FRAGMENT ONE","BYTE USATI CON DECODIFICA: "+byte_spesi+" , BYTE ALLOCATI: "+byte_allocati);
        }*/



        ImageView tasto = v.findViewById(R.id.tasto_avanti_1);
        tasto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TutorialActivity)getActivity()).switchToNextFragment1();
            }
        });

        return v;


    }


}

