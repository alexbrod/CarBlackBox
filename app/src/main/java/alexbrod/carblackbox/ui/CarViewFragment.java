package alexbrod.carblackbox.ui;


import android.animation.ObjectAnimator;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import alexbrod.carblackbox.R;
import alexbrod.carblackbox.bl.CarBlackBoxEngine;
import alexbrod.carblackbox.sensors.SensorsManagerService;


public class CarViewFragment extends Fragment  {
    //TODO: Pass those constants as parameters to the fragment
    private static final int ARC_MAX_RANGE = 5000;
    private static final int ARC_MIN_RANGE = 1000;
    private static final int ACC_MIN_RANGE = (int) SensorsManagerService.SENSITIVITY_LEVEL;
    private static final int ACC_MAX_RANGE = 50;
    private static final long ARC_ANIM_DURATION = 2500;
    private Drawable mTopArcDrawable;
    private Drawable mBottomArcDrawable;
    private Drawable mLeftArcDrawable;
    private Drawable mRightArcDrawable;
    private TextView mTopArc;
    private TextView mBottomArc;
    private TextView mLeftArc;
    private TextView mRightArc;
    private CarBlackBoxEngine carBlackBoxEngine;


    public CarViewFragment() {
        // Required empty public constructor
        carBlackBoxEngine = CarBlackBoxEngine.getInstance();
    }

    public static CarViewFragment newInstance() {
        CarViewFragment fragment = new CarViewFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_car_layout, container, false);
        mTopArc = (TextView) v.findViewById(R.id.top_arc);
        mBottomArc = (TextView) v.findViewById(R.id.bottom_arc);
        mLeftArc = (TextView) v.findViewById(R.id.left_arc);
        mRightArc = (TextView)v.findViewById(R.id.right_arc);

        mTopArcDrawable = mTopArc.getBackground();
        mBottomArcDrawable = mBottomArc.getBackground();
        mLeftArcDrawable = mLeftArc.getBackground();
        mRightArcDrawable = mRightArc.getBackground();
        return v;
    }

    @Override
    public void onPause(){
        super.onPause();

    }

    @Override
    public void onResume(){
        super.onResume();
    }

    //------------------------ Engine Events--------------------------


    public void animateSuddenBreak(float acceleration) {
        //scale a range [min,max] to [a,b]:
        //         (b-a)(x - min)
        //  f(x) = --------------  + a
        //           max - min

        int newZ = (ARC_MAX_RANGE - ARC_MIN_RANGE)*((int)acceleration - ACC_MIN_RANGE)
                /(ACC_MAX_RANGE - ACC_MIN_RANGE) + ARC_MIN_RANGE;
        ObjectAnimator anim = ObjectAnimator.ofInt(mTopArcDrawable,"level",newZ , 0);
        anim.setDuration(ARC_ANIM_DURATION);
        anim.start();
    }

    public void animateSharpTurnLeft(float acceleration) {
        //scale a range [min,max] to [a,b]:
        //         (b-a)(x - min)
        //  f(x) = --------------  + a
        //           max - min

        int newX = (ARC_MAX_RANGE - ARC_MIN_RANGE)*((int) acceleration - ACC_MIN_RANGE)
                /(ACC_MAX_RANGE - ACC_MIN_RANGE) + ARC_MIN_RANGE;
        ObjectAnimator anim = ObjectAnimator.ofInt(mLeftArcDrawable,"level",newX , 0);
        anim.setDuration(ARC_ANIM_DURATION);
        anim.start();
    }

    public void animateSharpTurnRight(float acceleration) {
        //scale a range [min,max] to [a,b]:
        //         (b-a)(x - min)
        //  f(x) = --------------  + a
        //           max - min

        int newX = (ARC_MAX_RANGE - ARC_MIN_RANGE)*((int) acceleration + ACC_MIN_RANGE)
                /(ACC_MIN_RANGE - ACC_MAX_RANGE) + ARC_MIN_RANGE;
        ObjectAnimator anim = ObjectAnimator.ofInt(mRightArcDrawable,"level",newX , 0);
        anim.setDuration(ARC_ANIM_DURATION);
        anim.start();
    }

    public void animateSuddenAcceleration(float acceleration) {
        //scale a range [min,max] to [a,b]:
        //         (b-a)(x - min)
        //  f(x) = --------------  + a
        //           max - min

        int newZ = (ARC_MAX_RANGE - ARC_MIN_RANGE)*((int)acceleration + ACC_MIN_RANGE)
                /(ACC_MIN_RANGE - ACC_MAX_RANGE) + ARC_MIN_RANGE;
        ObjectAnimator anim = ObjectAnimator.ofInt(mBottomArcDrawable,"level",newZ , 0);
        anim.setDuration(ARC_ANIM_DURATION);
        anim.start();
    }
}
