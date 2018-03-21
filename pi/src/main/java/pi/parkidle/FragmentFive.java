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
    private ImageView testo_finale;
    private ImageView tasto_mappa;

    public FragmentFive(){

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        testo_finale.setImageDrawable(null);
        tasto_mappa.setImageDrawable(null);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v5 = inflater.inflate(R.layout.fragment_five_layout,container,false);

        super.onViewCreated(v5, savedInstanceState);

        testo_finale = v5.findViewById(R.id.imageView18);

        tasto_mappa = v5.findViewById(R.id.tasto_mappa);
        tasto_mappa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TutorialActivity)getActivity()).switchToMap();
            }
        });

        return v5;
    }
}
