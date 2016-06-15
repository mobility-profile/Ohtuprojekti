package fi.ohtu.mobilityprofile;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.orm.SugarContext;

import java.util.ArrayList;
import java.util.List;

import fi.ohtu.mobilityprofile.ui.MyPagerAdapter;


public class MainActivity extends AppCompatActivity {

    FragmentPagerAdapter adapterViewPager;

    private CalendarConnection calendarConnection;
    private ArrayList<String> calendarEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        CalendarConnection cc = new CalendarConnection(this);
        List<String> el = new ArrayList<String>();
        el = cc.getLocations();
        for (String l : el) {
            System.out.println(l);
        }
*/
        SugarContext.init(this);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapterViewPager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }
}
