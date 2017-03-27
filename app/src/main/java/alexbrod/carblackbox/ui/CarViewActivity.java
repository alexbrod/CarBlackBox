package alexbrod.carblackbox.ui;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import alexbrod.carblackbox.R;
import alexbrod.carblackbox.bl.CarBlackBoxEngine;

public class CarViewActivity extends AppCompatActivity implements ICarBlackBoxEngineListener{

    private TextView mTvX;
    private TextView mTvY;
    private TextView mTvZ;
    private Button mBtnCarView;
    private Button mBtnMap;
    private Button mBtnDashboard;
    private CarBlackBoxEngine mCarBlackBoxEngine;
    private CarViewFragment mCarViewFragment;
    private ReportsMapFragment mReportsMapFragment;


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


        initButton(mBtnCarView, mCarViewFragment);
        initButton(mBtnMap, mReportsMapFragment);
        replaceFragment(mCarViewFragment);


    }

    @Override
    protected void onStart() {
        super.onStart();
        mCarBlackBoxEngine.bindToSensorsService(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCarBlackBoxEngine.registerToEngineEvents(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCarBlackBoxEngine.unregisterFromEngineEvents(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCarBlackBoxEngine.unbindFromSensorsService(this);
    }

    //------------------------ Engine Events--------------------------

    @Override
    public void onSuddenBreak(float acceleration) {
        mTvZ.setText(String.format("%.1f", acceleration));
        mCarViewFragment.animateSuddenBreak(acceleration);
    }

    @Override
    public void onSharpTurnLeft(float acceleration) {
        mTvX.setText(String.format("%.1f", acceleration));
        mCarViewFragment.animateSharpTurnLeft(acceleration);
    }

    @Override
    public void onSharpTurnRight(float acceleration) {
        mTvX.setText(String.format("%.1f", acceleration));
        mCarViewFragment.animateSharpTurnRight(acceleration);
    }

    @Override
    public void onSuddenAcceleration(float acceleration) {
        mTvZ.setText(String.format("%.1f", acceleration));
        mCarViewFragment.animateSuddenAcceleration(acceleration);
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
