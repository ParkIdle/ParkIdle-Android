package app.parkidle;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by Matteo on 07/03/18.
 */

public class FragmentTwo extends android.support.v4.app.Fragment {
    public FragmentTwo(){

    }
    @Nullable

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View v2 = inflater.inflate(R.layout.fragment_two_layout,container,false);

        super.onViewCreated(v2, savedInstanceState);


        ImageView tasto2 = (ImageView)v2.findViewById(R.id.tasto_avanti_2);
        tasto2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TutorialActivity)getActivity()).switchToNextFragment2();
            }
        });

        return v2;
    }
}

