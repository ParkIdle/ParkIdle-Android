package app.parkidle.helper;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import app.parkidle.R;

/**
 * Created by Matteo on 07/03/18.
 */

public class FragmentThree extends android.support.v4.app.Fragment {
    public FragmentThree(){

    }
    @Nullable

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_three_layout,container,false);
    }
}
