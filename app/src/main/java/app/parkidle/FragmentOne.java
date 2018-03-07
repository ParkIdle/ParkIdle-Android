package app.parkidle;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

/**
 * Created by Matteo on 07/03/18.
 */

public class FragmentOne extends android.support.v4.app.Fragment{
    public ViewPager viewPager;

    public FragmentOne(){

    }
    @Nullable

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_one_layout,container,false);

        super.onViewCreated(v, savedInstanceState);


        ImageView tasto = (ImageView)v.findViewById(R.id.tasto_avanti_1);
        tasto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TutorialActivity)getActivity()).switchToNextFragment1();
            }
        });

        return v;


    }


}

