package alexbrod.carblackbox.ui;

import android.content.IntentSender;
import android.location.Location;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import alexbrod.carblackbox.R;
import alexbrod.carblackbox.bl.CarBlackBoxEngine;

import static alexbrod.carblackbox.utilities.MyUtilities.SHARP_TURN;
import static alexbrod.carblackbox.utilities.MyUtilities.SPEEDING;
import static alexbrod.carblackbox.utilities.MyUtilities.SUDDEN_BRAKE;

public class CarViewActivity extends AppCompatActivity implements ICarBlackBoxEngineListener,
        ReportsMapFragment.IMapFragmentEvents{

    private static final String TAG = "CarViewActivity";
    private TextView mTvX;
    private TextView mTvY;
    private TextView mTvZ;
    private Button mBtnCarView;
    private Button mBtnMap;
    private Button mBtnDashboard;
    private CarBlackBoxEngine mCarBlackBoxEngine;
    private CarViewFragment mCarViewFragment;
    private ReportsMapFragment mReportsMapFragment;
    private DashboardFragment mDashboardFragment;
    private boolean mMapFragmetReady;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_view);
        mCarBlackBoxEngine = CarBlackBoxEngine.getInstance();

        mTvX = (TextView)findViewById(R.id.tvX);
        mTvY = (TextView)findViewById(R.id.tvY);
        mTvZ = (TextView)findViewById(R.id.tvZ);

        mBtnCarView = (Button)findViewById(R.id.btnCarView);
        mBtnMap = (Button)findViewById(R.id.btnMapView);
        mBtnDashboard = (Button)findViewById(R.id.btnDashboardView);


        mCarViewFragment = CarViewFragment.newInstance();
        mReportsMapFragment = ReportsMapFragment.newInstance();
        mDashboardFragment = DashboardFragment.newInstance();

        mReportsMapFragment.registerToMapFragmentEvents(this);

        initButton(mBtnCarView, mCarViewFragment);
        initButton(mBtnMap, mReportsMapFragment);
        initButton(mBtnDashboard,mDashboardFragment);

        replaceFragment(mCarViewFragment);
        mCarBlackBoxEngine.connectDb(this);
        mCarBlackBoxEngine.initiateLocationManager(this);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mCarBlackBoxEngine.bindToSensorsService(this);
        mCarBlackBoxEngine.startLocationManager();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG,"onResume");
        mCarBlackBoxEngine.registerToEngineEvents(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG,"onPause");
        mCarBlackBoxEngine.unregisterFromEngineEvents();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"onDestroy");
        mCarBlackBoxEngine.unbindFromSensorsService(this);
        mCarBlackBoxEngine.stopLocationManager();
        mCarBlackBoxEngine.disconnectDb();
    }

    //------------------------ Engine Events--------------------------

    @Override
    public void onSuddenBreak(float acceleration, Location location) {
        mTvZ.setText(String.format("%.1f", acceleration));
        mCarViewFragment.animateSuddenBreak(acceleration);
        mReportsMapFragment.addReport(SUDDEN_BRAKE, acceleration, location);
    }

    @Override
    public void onSharpTurnLeft(float acceleration, Location location) {
        mTvX.setText(String.format("%.1f", acceleration));
        mCarViewFragment.animateSharpTurnLeft(acceleration);
        mReportsMapFragment.addReport(SHARP_TURN, acceleration, location);
    }

    @Override
    public void onSharpTurnRight(float acceleration, Location location) {
        mTvX.setText(String.format("%.1f", acceleration));
        mCarViewFragment.animateSharpTurnRight(acceleration);
        mReportsMapFragment.addReport(SHARP_TURN, acceleration, location);
    }

    @Override
    public void onSuddenAcceleration(float acceleration, Location location) {
        mTvZ.setText(String.format("%.1f", acceleration));
        mCarViewFragment.animateSuddenAcceleration(acceleration);

    }

    @Override
    public void onLocationResolutionRequired(Status status) {
        try {
            // Show the dialog by calling startResolutionForResult(),
            // and check the result in onActivityResult().
            status.startResolutionForResult(
                    this,
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED);
        } catch (IntentSender.SendIntentException e) {
            Log.e(this.getClass().getSimpleName(),"Location resolution error");
        }
    }

    @Override
    public void onSpeedChanged(int speed) {
        mTvY.setText(String.format("%d",speed));
        if(mDashboardFragment.isVisible()){
            mDashboardFragment.updateSpeedometer(speed);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mReportsMapFragment.updateMapCameraView(location);
    }

    @Override
    public void onCarAcceleration(float acceleration) {
        if(mDashboardFragment.isVisible()) {
            mDashboardFragment.updateAccelerometer(acceleration);
        }
    }

    @Override
    public void onCrossedSpeedLimit(int speed, Location location) {
        mReportsMapFragment.addReport(SPEEDING, speed, location);
    }

    //------------------------ Fragment Management ------------------------

    private void replaceFragment(Fragment fragment){
        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {
            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment
                            ,fragment.getClass().getSimpleName()).commit();
        }
        else{
            Log.e(getClass().getSimpleName(),"No fragment container");
        }
    }

    @Override
    public void onMapFragmentReady() {
        mReportsMapFragment.showSavedEventsOnMap(mCarBlackBoxEngine.getCurrentTravelEvents());
    }
    //------------------------ Button Init -----------------------------

    public void initButton(Button b, final Fragment fragment){
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CarViewActivity.this.replaceFragment(fragment);
            }
        });
    }


}
