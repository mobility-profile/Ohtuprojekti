package fi.ohtu.mobilityprofile;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.PorterDuff;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.Toast;

import com.orm.SugarContext;

import com.google.android.gms.drive.Drive;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.List;

import fi.ohtu.mobilityprofile.domain.TransportMode;
import fi.ohtu.mobilityprofile.suggestions.locationHistory.PlaceRecorder;
import fi.ohtu.mobilityprofile.util.PermissionManager;
import fi.ohtu.mobilityprofile.util.ProfileBackup;
import fi.ohtu.mobilityprofile.util.SecurityCheck;
import fi.ohtu.mobilityprofile.ui.MyPagerAdapter;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final String SHARED_PREFERENCES = "fi.ohtu.mobilityprofile";
    public static final String CONFLICT_APPS = "conflictApps";
    public static final String TAG = "Mobility Profile";

    private static Activity activity;
    private static Context context;

    private TabLayout tabLayout;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SugarContext.init(this);

        activity = this;
        context = this.getApplicationContext();

        isGPSPermissionOn();
        setViewPagerAndTabs();
        checkSecurity();
        createTransportModes();
        googleApiClientBuild();

        // new ProfileBackup().handleBackup("import");
        // mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        context = getApplicationContext();
        // showMessage("Connected to Google Drive");

        if (tabLayout.getSelectedTabPosition() == 0) {
            tabLayout.getTabAt(0).getIcon().setColorFilter(ContextCompat.getColor(this, R.color.color_orange), PorterDuff.Mode.SRC_IN);
            tabLayout.getTabAt(1).getIcon().setColorFilter(ContextCompat.getColor(this, R.color.color_primary_dark), PorterDuff.Mode.SRC_IN);
            tabLayout.getTabAt(2).getIcon().setColorFilter(ContextCompat.getColor(this, R.color.color_primary_dark), PorterDuff.Mode.SRC_IN);
        } else if (tabLayout.getSelectedTabPosition() == 1) {
            tabLayout.getTabAt(0).getIcon().setColorFilter(ContextCompat.getColor(this, R.color.color_primary_dark), PorterDuff.Mode.SRC_IN);
            tabLayout.getTabAt(1).getIcon().setColorFilter(ContextCompat.getColor(this, R.color.color_orange), PorterDuff.Mode.SRC_IN);
            tabLayout.getTabAt(2).getIcon().setColorFilter(ContextCompat.getColor(this, R.color.color_primary_dark), PorterDuff.Mode.SRC_IN);
        } else {
            tabLayout.getTabAt(0).getIcon().setColorFilter(ContextCompat.getColor(this, R.color.color_primary_dark), PorterDuff.Mode.SRC_IN);
            tabLayout.getTabAt(1).getIcon().setColorFilter(ContextCompat.getColor(this, R.color.color_primary_dark), PorterDuff.Mode.SRC_IN);
            tabLayout.getTabAt(2).getIcon().setColorFilter(ContextCompat.getColor(this, R.color.color_orange), PorterDuff.Mode.SRC_IN);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            mGoogleApiClient.connect();
            // showMessage("Connected to Google Drive");
        }
    }

    private void isGPSPermissionOn() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String gps = sharedPref.getString("gps", "Not Available");

        if (PermissionManager.permissionToFineLocation(getApplicationContext()) && gps.equals("true") && !isLocationServiceRunning()) {
            Intent intent = new Intent(this, PlaceRecorder.class);
            startService(intent);
        }
    }

    private void setViewPagerAndTabs() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
        tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(activity, R.color.color_white));

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_action_logo_orange).setContentDescription(R.string.app_name);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_action_globe).setContentDescription(R.string.your_places_title);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_action_gear).setContentDescription(R.string.settings_title);

        tabLayout.setOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                super.onTabSelected(tab);
                int tabIconColor = ContextCompat.getColor(activity, R.color.color_orange);
                tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                super.onTabUnselected(tab);
                int tabIconColor = ContextCompat.getColor(activity, R.color.color_primary_dark);
                tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                super.onTabReselected(tab);
            }
        });
    }

    /**
     * Checks if there are any security problems with other applications. Check
     * {@link SecurityCheck} for more information.
     */
    private void checkSecurity() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);

        boolean securityOk = true;

        // If this is the first time the application is run or security check is not ok, we should
        // run the security check.
        if (sharedPreferences.getBoolean("firstrun", true) || !SecurityCheck.securityCheckOk(sharedPreferences)) {
            securityOk = SecurityCheck.doSecurityCheck(sharedPreferences);
            sharedPreferences.edit().putBoolean("firstrun", false).apply();
        }

        if (!securityOk) {
            processSecurityConflicts(SecurityCheck.getConflictInfo());
        }
    }

    private void createTransportModes() {
        if (TransportMode.count(TransportMode.class) == 0) {
            Log.i("MainActivity", "creating transport modes");

            TransportMode walking = new TransportMode("walking");
            walking.save();
            TransportMode bike = new TransportMode("bike");
            bike.save();
            TransportMode car = new TransportMode("car");
            car.save();
            TransportMode bus = new TransportMode("bus");
            bus.save();
            TransportMode metro = new TransportMode("metro");
            metro.save();
            TransportMode tram = new TransportMode("tram");
            tram.save();
            TransportMode train = new TransportMode("train");
            train.save();
            TransportMode boat = new TransportMode("boat");
            boat.save();
            TransportMode plane = new TransportMode("plane");
            plane.save();
        }
    }

    private void googleApiClientBuild() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addScope(Drive.SCOPE_APPFOLDER) // required for App Folder sample
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
    }

    /**
     * Informs the user about security problems.
     *
     * @param conflictApps List of applications that are causing the problems.
     */
    private void processSecurityConflicts(List<PackageInfo> conflictApps) {
        ArrayList<String> appNames = new ArrayList<>();

        for (PackageInfo packageInfo : conflictApps) {
            appNames.add(getPackageManager().getApplicationLabel(packageInfo.applicationInfo).toString());
        }

        Intent intent = new Intent(this, SecurityProblemActivity.class);
        intent.putStringArrayListExtra(CONFLICT_APPS, appNames);
        startActivity(intent);
    }


    /*GoogleApiClient*/

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "GoogleApiClient connected");
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended");
    }

    /**
     * Called when mGoogleApiClient is trying to connect but failed.
     * Handle result.getResolution() if there is a resolution is
     * available.
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this, result.getErrorCode(), 0).show();
            return;
        }
        try {
            result.startResolutionForResult(this, 1);
        } catch (SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }

    /**
     * Getter for the GoogleApiClient.
     */
    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    /**
     * Shows a toast message.
     */
    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Checks if PlaceRecorder is running.
     * @see PlaceRecorder
     * @return true/false
     */
    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (PlaceRecorder.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static void setContext(Context context) {
        MainActivity.context = context;
    }

    public static Context getContext() {
        return context;
    }

    public static Activity getActivity() {
        return activity;
    }

}
