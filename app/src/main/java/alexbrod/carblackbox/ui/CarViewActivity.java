package alexbrod.carblackbox.ui;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.TextView;

import alexbrod.carblackbox.R;
import alexbrod.carblackbox.bl.CarBlackBoxEngine;
import alexbrod.carblackbox.sensors.SensorsManagerService;

public class CarViewActivity extends Activity implements ICarBlackBoxEngineListener{
    private static final int ARC_MAX_RANGE = 5000;
    private static final int ARC_MIN_RANGE = 1000;
    private static final int ACC_MIN_RANGE = (int) SensorsManagerService.SENSITIVITY_LEVEL;
    private static final int ACC_MAX_RANGE = 50;
    private static final long ARC_ANIM_DURATION = 2500;
    private TextView mTvX;
    private TextView mTvY;
    private TextView mTvZ;
    private TextView mTopArc;
    private TextView mBottomArc;
    private TextView mLeftArc;
    private TextView mRightArc;
    private Drawable mTopArcDrawable;
    private Drawable mBottomArcDrawable;
    private Drawable mLeftArcDrawable;
    private Drawable mRightArcDrawable;
    private CarBlackBoxEngine carBlackBoxEngine;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_view);
        carBlackBoxEngine = CarBlackBoxEngine.getInstance();

        mTopArc = (TextView) findViewById(R.id.top_arc);
        mBottomArc = (TextView) findViewById(R.id.bottom_arc);
        mLeftArc = (TextView) findViewById(R.id.left_arc);
        mRightArc = (TextView) findViewById(R.id.right_arc);

        mTopArcDrawable = mTopArc.getBackground();
        mBottomArcDrawable = mBottomArc.getBackground();
        mLeftArcDrawable = mLeftArc.getBackground();
        mRightArcDrawable = mRightArc.getBackground();



        mTvX = (TextView)findViewById(R.id.tvX);
        mTvY = (TextView)findViewById(R.id.tvY);
        mTvZ = (TextView)findViewById(R.id.tvZ);

    }

    @Override
    protected void onStart() {
        super.onStart();
        carBlackBoxEngine.bindToSensorsService(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        carBlackBoxEngine.registerToEngineEvents(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        carBlackBoxEngine.unregisterFromEngineEvents(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        carBlackBoxEngine.unbindFromSensorsService(this);
    }

    @Override
    public void OnSuddenBreak(float x, float y, float z) {
        mTvX.setText(String.format("%.1f",x));
        mTvY.setText(String.format("%.1f",y));
        mTvZ.setText(String.format("%.1f",z));

        //scale a range [min,max] to [a,b]:
        //         (b-a)(x - min)
        //  f(x) = --------------  + a
        //           max - min

        int newZ = (ARC_MAX_RANGE - ARC_MIN_RANGE)*((int)z - ACC_MIN_RANGE)
                /(ACC_MAX_RANGE - ACC_MIN_RANGE) + ARC_MIN_RANGE;
        ObjectAnimator anim = ObjectAnimator.ofInt(mTopArcDrawable,"level",newZ , 0);
        anim.setDuration(ARC_ANIM_DURATION);
        anim.start();
    }

    @Override
    public void OnSharpTurnLeft(float x, float y, float z) {
        mTvX.setText(String.format("%.1f",x));
        mTvY.setText(String.format("%.1f",y));
        mTvZ.setText(String.format("%.1f",z));

        //scale a range [min,max] to [a,b]:
        //         (b-a)(x - min)
        //  f(x) = --------------  + a
        //           max - min

        int newX = (ARC_MAX_RANGE - ARC_MIN_RANGE)*((int)x - ACC_MIN_RANGE)
                /(ACC_MAX_RANGE - ACC_MIN_RANGE) + ARC_MIN_RANGE;
        ObjectAnimator anim = ObjectAnimator.ofInt(mLeftArcDrawable,"level",newX , 0);
        anim.setDuration(ARC_ANIM_DURATION);
        anim.start();
    }

    @Override
    public void OnSharpTurnRight(float x, float y, float z) {
        mTvX.setText(String.format("%.1f",x));
        mTvY.setText(String.format("%.1f",y));
        mTvZ.setText(String.format("%.1f",z));

        //scale a range [min,max] to [a,b]:
        //         (b-a)(x - min)
        //  f(x) = --------------  + a
        //           max - min

        int newX = (ARC_MAX_RANGE - ARC_MIN_RANGE)*((int)x + ACC_MIN_RANGE)
                /(ACC_MIN_RANGE - ACC_MAX_RANGE) + ARC_MIN_RANGE;
        ObjectAnimator anim = ObjectAnimator.ofInt(mRightArcDrawable,"level",newX , 0);
        anim.setDuration(ARC_ANIM_DURATION);
        anim.start();
    }

    @Override
    public void onSuddenAcceleration(float x, float y, float z) {
        mTvX.setText(String.format("%.1f",x));
        mTvY.setText(String.format("%.1f",y));
        mTvZ.setText(String.format("%.1f",z));

        //scale a range [min,max] to [a,b]:
        //         (b-a)(x - min)
        //  f(x) = --------------  + a
        //           max - min

        int newZ = (ARC_MAX_RANGE - ARC_MIN_RANGE)*((int)z + ACC_MIN_RANGE)
                /(ACC_MIN_RANGE - ACC_MAX_RANGE) + ARC_MIN_RANGE;
        ObjectAnimator anim = ObjectAnimator.ofInt(mBottomArcDrawable,"level",newZ , 0);
        anim.setDuration(ARC_ANIM_DURATION);
        anim.start();
    }
}
