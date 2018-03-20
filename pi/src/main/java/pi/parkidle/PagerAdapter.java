package pi.parkidle;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by Matteo on 07/03/18.
 */

public class PagerAdapter extends FragmentStatePagerAdapter {
    private static int NUMBER = 5;

    public PagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new FragmentOne();
            case 1:
                return new FragmentTwo();
            case 2:
                return new FragmentThree();
            case 3:
                return new FragmentFour();
            case 4:
                return new FragmentFive();
            default:
                return null;
        }

    }


    @Override
    public int getCount() {
        return NUMBER;
    }
}



