package alexbrod.carblackbox.ui;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cardiomood.android.controls.gauge.SpeedometerGauge;

import alexbrod.carblackbox.R;


public class DashboardFragment extends Fragment {

    private static final double MAX_SPEED = 150;
    private static final double SPEED_MAJOR_STEP = 30;
    private static final int SPEED_MINOR_STEP = 10;
    private static final double SPEED_GREEN_RANGE = 110;
    private static final double SPEED_YELLOW_RANGE = 130;
    private static final double MAX_ACCELERATION = 25;
    private static final double ACC_MAJOR_STEP = 5;
    private static final int ACC_MINOR_STEP = 1;
    private static final double ACC_GREEN_RANGE = 12;
    private static final double ACC_YELLOW_RANGE = 20;


    private SpeedometerGauge mSpeedometer;
    private SpeedometerGauge mAccelerometer;



    public DashboardFragment() {
        // Required empty public constructor
    }

    public static DashboardFragment newInstance() {
        DashboardFragment fragment = new DashboardFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.fragment_dashboard_layout, container, false);

        mSpeedometer = (SpeedometerGauge) v.findViewById(R.id.speedometer);
        initSpeedometer();

        mAccelerometer = (SpeedometerGauge) v.findViewById(R.id.accelerometer);
        initAccelerometer();
        return v;
    }

    private void initAccelerometer() {
        mAccelerometer.setLabelConverter(new SpeedometerGauge.LabelConverter() {
            @Override
            public String getLabelFor(double progress, double maxProgress) {
                return String.valueOf((int) Math.round(progress));
            }
        });

        mAccelerometer.setMaxSpeed(MAX_ACCELERATION);
        mAccelerometer.setMajorTickStep(ACC_MAJOR_STEP);
        mAccelerometer.setMinorTicks(ACC_MINOR_STEP);
        mAccelerometer.addColoredRange(0, ACC_GREEN_RANGE, Color.GREEN);
        mAccelerometer.addColoredRange(ACC_GREEN_RANGE, ACC_YELLOW_RANGE, Color.YELLOW);
        mAccelerometer.addColoredRange(ACC_YELLOW_RANGE, MAX_ACCELERATION, Color.RED);
        mAccelerometer.setLabelTextSize(25);
    }

    private void initSpeedometer(){

        mSpeedometer.setLabelConverter(new SpeedometerGauge.LabelConverter() {
            @Override
            public String getLabelFor(double progress, double maxProgress) {
                return String.valueOf((int) Math.round(progress));
            }
        });

        mSpeedometer.setMaxSpeed(MAX_SPEED);
        mSpeedometer.setMajorTickStep(SPEED_MAJOR_STEP);
        mSpeedometer.setMinorTicks(SPEED_MINOR_STEP);
        mSpeedometer.addColoredRange(0, SPEED_GREEN_RANGE, Color.GREEN);
        mSpeedometer.addColoredRange(SPEED_GREEN_RANGE, SPEED_YELLOW_RANGE, Color.YELLOW);
        mSpeedometer.addColoredRange(SPEED_YELLOW_RANGE, MAX_SPEED, Color.RED);
        mSpeedometer.setLabelTextSize(25);
    }


    public void updateSpeedometer(double speed) {
        mSpeedometer.setSpeed(speed,true);
    }

    public void updateAccelerometer(double acceleration) {
        mAccelerometer.setSpeed(Math.abs(acceleration),100,0);
        //TODO:Make acceleration go back to zero
    }
}
