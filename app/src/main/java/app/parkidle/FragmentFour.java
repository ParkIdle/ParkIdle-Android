package app.parkidle;

import android.os.Bundle;
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

public class FragmentFour extends Fragment {
    public FragmentFour(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v4 = inflater.inflate(R.layout.fragment_four_layout,container,false);

        super.onViewCreated(v4, savedInstanceState);


        ImageView tasto2 = (ImageView)v4.findViewById(R.id.tasto_avanti_4);
        tasto2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TutorialActivity)getActivity()).switchToNextFragment4();
            }
        });

        return v4;
    }
}
