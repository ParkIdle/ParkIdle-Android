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

public class FragmentThree extends Fragment {
    public FragmentThree(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v3 = inflater.inflate(R.layout.fragment_three_layout,container,false);

        super.onViewCreated(v3, savedInstanceState);


        ImageView tasto3 = v3.findViewById(R.id.tasto_avanti_3);
        tasto3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TutorialActivity)getActivity()).switchToNextFragment3();
            }
        });

        return v3;
    }
}
