package pi.parkidle;

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

public class FragmentFive extends Fragment {
    public FragmentFive(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v5 = inflater.inflate(R.layout.fragment_five_layout,container,false);

        super.onViewCreated(v5, savedInstanceState);


        ImageView tasto5 = v5.findViewById(R.id.tasto_mappa);
        tasto5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TutorialActivity)getActivity()).switchToMap();
            }
        });

        return v5;
    }
}
