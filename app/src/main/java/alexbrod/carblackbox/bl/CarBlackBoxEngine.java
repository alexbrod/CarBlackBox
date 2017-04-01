package alexbrod.carblackbox.bl;

import android.animation.ValueAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.api.Status;

import java.util.Vector;

import alexbrod.carblackbox.sensors.ILocationManagerEvents;
import alexbrod.carblackbox.sensors.ISensorsEvents;
import alexbrod.carblackbox.sensors.LocationManager;
import alexbrod.carblackbox.sensors.SensorsManagerService;
import alexbrod.carblackbox.ui.ICarBlackBoxEngineListener;

/**
 * Created by Alex Brod on 3/13/2017.
 */

public class CarBlackBoxEngine implements ISensorsEvents, ServiceConnection,
        ILocationManagerEvents {

    private static final long ACC_PEAK_DURATION = 1000000; //micro-sec
    private static final float FORWARD_ACC_SENSITIVITY = 6;
    private static final float BACK_ACC_SENSITIVITY = 5;
    private static final float TURN_ACC_SENSITIVITY = 5;

    private static CarBlackBoxEngine carBlackBoxEngine;
    private SensorsManagerService mSensorsManagerService;
    private Vector<ICarBlackBoxEngineListener> mUiListeners;
    private ValueAnimator mLeftAccAnimation;
    private ValueAnimator mRightAccAnimation;
    private ValueAnimator mForwardAccAnimation;
    private ValueAnimator mBackwardAccAnimation;
    private float mPeakLeftAcc;
    private float mPeakRightAcc;
    private float mPeakForwardAcc;
    private float mPeakBackAcc;
    private boolean mIsMeasuringLeft = false;
    private boolean mIsMeasuringRight = false;
    private boolean mIsMeasuringForward = false;
    private boolean mIsMeasuringBack = false;
    private long mStartLeftTimestamp;
    private long mEndLeftTimestamp;
    private long mStartRightTimestamp;
    private long mEndRightTimestamp;
    private long mStartForwardTimestamp;
    private long mEndForwardTimestamp;
    private long mStartBackTimestamp;
    private long mEndBackTimestamp;
    private LocationManager locationManager;

    private CarBlackBoxEngine(){
        mSensorsManagerService = new SensorsManagerService();
        mUiListeners = new Vector<>();
        mForwardAccAnimation = ValueAnimator.ofFloat(0,0);
        mBackwardAccAnimation = ValueAnimator.ofFloat(0,0);
        mLeftAccAnimation = ValueAnimator.ofFloat(0,0);
        mRightAccAnimation = ValueAnimator.ofFloat(0,0);

    }

    public static CarBlackBoxEngine getInstance(){
        if(carBlackBoxEngine == null){
            carBlackBoxEngine = new CarBlackBoxEngine();
        }
        return carBlackBoxEngine;
    }

    public void bindToSensorsService(Context context){
        Intent intent = new Intent(context, SensorsManagerService.class);
        context.bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    public void unbindFromSensorsService(Context context){
        context.unbindService(this);
    }

    public void registerToEngineEvents(ICarBlackBoxEngineListener listener){
        mUiListeners.add(listener);
        Log.w(this.getClass().getSimpleName(),"Another listener added to engine events");
    }

    public void unregisterFromEngineEvents(ICarBlackBoxEngineListener listener){
        mUiListeners.remove(listener);
    }

    

    //-----------------------------Sensors service methods-----------------------

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        mSensorsManagerService = ((SensorsManagerService.LocalBinder)iBinder).getService();
        mSensorsManagerService.registerToSensorsEvents(this);
        Log.w(this.getClass().getSimpleName(),"Engine connected to SensorsManagerService");

    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }

    //------------------------------Sensor events methods---------------------

    @Override
    public void OnSensorsMissing() {
        Log.e(this.getClass().getSimpleName(),"One or more sensors are missing!");
    }

    @Override
    public void OnSensorXAccChanged(float x, float y, float z, long timestamp) {

        //TODO: consider to start animation on separate thread
        Log.w("X","General: " + timestamp + " (" + x + "," + y + "," + z + ")");

        if(x >= TURN_ACC_SENSITIVITY){ //check left turn start
            Log.w("X","Crossed Left: " + timestamp + " (" + x + "," + y + "," + z + ")");
            //start measuring
            if(!mIsMeasuringLeft){
                Log.w("X","Left Start measure: " + timestamp + " (" + x + "," + y + "," + z + ")");
                mStartLeftTimestamp = timestamp;
                mPeakLeftAcc = x;
                mIsMeasuringLeft = true;
            }
            //save peak acceleration
            if(x > mPeakLeftAcc){
                mPeakLeftAcc = x;
            }
            //check that the deviation is not just noise
            if(timestamp - mStartLeftTimestamp >= ACC_PEAK_DURATION){
                Log.w("X","Left Alert: " + (timestamp - mStartLeftTimestamp) + " (" + x + "," + y + "," + z + ")");
                for (ICarBlackBoxEngineListener l:mUiListeners) {
                    l.onSharpTurnLeft(mPeakLeftAcc);
                }
            }
        }else if(x < TURN_ACC_SENSITIVITY && x > 0){ //check left turn end
            if(mIsMeasuringLeft){
                Log.w("X","Left End measure: " + timestamp + " (" + x + "," + y + "," + z + ")");
                //save peak end timestamp
                mEndLeftTimestamp = timestamp;
                mIsMeasuringLeft = false;
            }
        }else if(x <= -TURN_ACC_SENSITIVITY){ //check right turn start
            Log.w("X","Crossed Right: " + timestamp + " (" + x + "," + y + "," + z + ")");
            //start measuring
            if(!mIsMeasuringRight){
                Log.w("X","Right Start measure: " + timestamp + " (" + x + "," + y + "," + z + ")");
                mStartRightTimestamp = timestamp;
                mPeakRightAcc = x;
                mIsMeasuringRight = true;
            }
            //save peak acceleration
            if(x < mPeakRightAcc){
                mPeakRightAcc = x;
            }
            //check that the deviation is not just noise
            if(timestamp - mStartRightTimestamp >= ACC_PEAK_DURATION){
                Log.w("X","Left Alert: " + (timestamp - mStartRightTimestamp) + " (" + x + "," + y + "," + z + ")");
                for (ICarBlackBoxEngineListener l:mUiListeners) {
                    l.onSharpTurnRight(mPeakRightAcc);
                }
            }
        }else if(x > -TURN_ACC_SENSITIVITY && x < 0){ //check right turn end
            if(mIsMeasuringRight){
                Log.w("X","Left End measure: " + timestamp + " (" + x + "," + y + "," + z + ")");
                //save peak end timestamp
                mEndRightTimestamp = timestamp;
                mIsMeasuringRight = false;
            }
        }


    }

    @Override
    public void OnSensorYAccChanged(float x, float y, float z, long timestamp) {

    }

    @Override
    public void OnSensorZAccChanged(float x, float y, float z, long timestamp) {
        Log.w("Z","General: " + timestamp + " (" + x + "," + y + "," + z + ")");

        if(z >= FORWARD_ACC_SENSITIVITY){ //check break start
            Log.w("Z","Crossed Break: " + timestamp + " (" + x + "," + y + "," + z + ")");
            //start measuring
            if(!mIsMeasuringForward){
                Log.w("Z","Break Start measure: " + timestamp + " (" + x + "," + y + "," + z + ")");
                mStartForwardTimestamp = timestamp;
                mPeakForwardAcc = z;
                mIsMeasuringForward = true;
            }
            //save peak acceleration
            if(z > mPeakForwardAcc){
                mPeakForwardAcc = z;
            }
            //check that the deviation is not just noise
            if(timestamp - mStartForwardTimestamp >= ACC_PEAK_DURATION){
                Log.w("Z","Break Alert: " + (timestamp - mStartForwardTimestamp) + " (" + x + "," + y + "," + z + ")");
                for (ICarBlackBoxEngineListener l:mUiListeners) {
                    l.onSuddenBreak(mPeakLeftAcc);
                }
            }
        }else if(z < FORWARD_ACC_SENSITIVITY && z > 0){ //check break end
            if(mIsMeasuringForward){
                Log.w("Z","Break End measure: " + timestamp + " (" + x + "," + y + "," + z + ")");
                //save peak end timestamp
                mEndForwardTimestamp = timestamp;
                mIsMeasuringForward = false;
            }
        }else if(z <= -BACK_ACC_SENSITIVITY){ //check acceleration start
            Log.w("Z","Crossed Acc: " + timestamp + " (" + x + "," + y + "," + z + ")");
            //start measuring
            if(!mIsMeasuringBack){
                Log.w("Z","Acc Start measure: " + timestamp + " (" + x + "," + y + "," + z + ")");
                mStartBackTimestamp = timestamp;
                mPeakBackAcc = z;
                mIsMeasuringBack = true;
            }
            //save peak acceleration
            if(z < mPeakBackAcc){
                mPeakBackAcc = z;
            }
            //check that the deviation is not just noise
            if(timestamp - mStartBackTimestamp >= ACC_PEAK_DURATION){
                Log.w("Z","Acc Alert: " + (timestamp - mStartBackTimestamp) + " (" + x + "," + y + "," + z + ")");
                for (ICarBlackBoxEngineListener l:mUiListeners) {
                    l.onSuddenAcceleration(mPeakRightAcc);
                }
            }
        }else if(z > -BACK_ACC_SENSITIVITY && z < 0){ //check acceleration end
            if(mIsMeasuringBack){
                Log.w("Z","Acc End measure: " + timestamp + " (" + x + "," + y + "," + z + ")");
                //save peak end timestamp
                mEndBackTimestamp = timestamp;
                mIsMeasuringBack = false;
            }
        }
    }

    //-------------------------Location Management--------------------------------

    public void initiateLocationManager(Context context){
        locationManager = LocationManager.getInstance(context);
    }

    public void startLocationManager(){
        locationManager.registerToLocationManagerEvents(this);
        locationManager.connect();
    }

    public void stopLocationManager(){
        locationManager.disconnect();
        locationManager.unregisterFromLocationManagerEvents();
    }

    @Override
    public void onLocationResolutionRequired(Status status) {
        for (ICarBlackBoxEngineListener l:mUiListeners) {
            l.onLocationResolutionRequired(status);
        }
    }

    @Override
    public void onSpeedChanged(int speed) {
        for (ICarBlackBoxEngineListener l:mUiListeners) {
            l.onSpeedChanged(speed);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        for (ICarBlackBoxEngineListener l:mUiListeners) {
            l.onLocationChanged(location);
        }
    }
}
