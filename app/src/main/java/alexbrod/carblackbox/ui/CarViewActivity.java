package alexbrod.carblackbox.ui;

import android.app.Activity;
import android.graphics.drawable.shapes.ArcShape;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import alexbrod.carblackbox.R;
import alexbrod.carblackbox.bl.CarBlackBoxEngine;

import static android.R.attr.x;
import static android.R.attr.y;

public class CarViewActivity extends Activity implements ICarBlackBoxEngineListener{
    private TextView mTvX;
    private TextView mTvY;
    private TextView mTvZ;
    private TextView mTopArc;
    private TextView mButtomArc;
    private TextView mLeftArc;
    private TextView mRightArc;
    private CarBlackBoxEngine carBlackBoxEngine;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_view);
        carBlackBoxEngine = CarBlackBoxEngine.getInstance();

        mTopArc = (TextView) findViewById(R.id.top_arc);
        mButtomArc = (TextView) findViewById(R.id.bottom_arc);
        mLeftArc = (TextView) findViewById(R.id.left_arc);
        mRightArc = (TextView) findViewById(R.id.right_arc);

        mTopArc.getBackground().setLevel(5000);
        mButtomArc.getBackground().setLevel(5000);
        mLeftArc.getBackground().setLevel(5000);
        mRightArc.getBackground().setLevel(5000);

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
    public void OnAccelerationChanged(float x, float y, float z) {
        mTvX.setText(String.format("%f",x));
        mTvY.setText(String.format("%f",y));
        mTvZ.setText(String.format("%f",z));
    }
}
