package pi.parkidle;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by Matteo on 07/03/18.
 */

public class FragmentThree extends Fragment {
    private ImageView funzione_ricentro;
    private ImageView box_testo_ricentro;
    private ImageView tasto;



    public FragmentThree(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v3 = inflater.inflate(R.layout.fragment_three_layout,container,false);

        super.onViewCreated(v3, savedInstanceState);

        funzione_ricentro = v3.findViewById(R.id.imageView7);
        box_testo_ricentro = v3.findViewById(R.id.imageView8);
        tasto = v3.findViewById(R.id.tasto_avanti_3);
        tasto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TutorialActivity)getActivity()).switchToNextFragment3();
            }
        });

        return v3;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        funzione_ricentro.setImageDrawable(null);
        box_testo_ricentro.setImageDrawable(null);
        tasto.setImageDrawable(null);
    }
}
