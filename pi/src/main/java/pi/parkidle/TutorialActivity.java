package pi.parkidle;

/**
 * Created by Andrea on 29/12/2017.
 */

import android.support.v4.view.ViewPager;
        import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


public class TutorialActivity extends AppCompatActivity {
    ViewPager viewPager;
    PagerAdapter pagerAdapter;
    private static final int num_fragment = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        viewPager = findViewById(R.id.viewPager);
        pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);

    }

    public void switchToNextFragment1(){
        viewPager.setCurrentItem(1);
    }
    public void switchToNextFragment2(){
        viewPager.setCurrentItem(2);
    }
    public void switchToNextFragment3(){
        viewPager.setCurrentItem(3);
    }
    public void switchToNextFragment4(){
        viewPager.setCurrentItem(4);
    }
    public void switchToMap(){
        onBackPressed();
    }
}
