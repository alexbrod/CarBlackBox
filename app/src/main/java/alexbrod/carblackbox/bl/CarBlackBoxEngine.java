package alexbrod.carblackbox.bl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.api.Status;

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
    private static final String TAG = "CarBlackBoxEngine";
    private static final float BRAKE_SENSITIVITY = 5;
    private static final float TURN_ACC_SENSITIVITY = 3;
    private static final int EVENT_FILTER_THRESHOLD = 10;
    private static final long MINIMAL_BRAKE_TIME = 2000000; //micro-sec
    private static final long MINIMAL_TURN_TIME = 2000000;  //micro-sec
    private int mZNegEventFilterCounter = 0;
    private long mStartRecordTurnLeftTime = 0;
    private long mStartRecordTurnRightTime = 0;
    private long mStartRecordBrakeTime = 0;
    private int mAboveBrakeSensitivityCounter = 0;
    private int mUnderBreakSensitivityCounter = 0;
    private int mAboveTurnLeftSensitivityCounter = 0;
    private int mUnderTurnLeftSensitivityCounter = 0;
    private int mAboveTurnRightSensitivityCounter = 0;
    private int mUnderTurnRightSensitivityCounter = 0;

    private static CarBlackBoxEngine mCarBlackBoxEngine;
    private SensorsManagerService mSensorsManagerService;
    private ICarBlackBoxEngineListener mUiListener;
    private LocationManager mLocationManager;



    private CarBlackBoxEngine(){
        mSensorsManagerService = new SensorsManagerService();
    }


    public static CarBlackBoxEngine getInstance(){
        if(mCarBlackBoxEngine == null){
            mCarBlackBoxEngine = new CarBlackBoxEngine();
        }
        return mCarBlackBoxEngine;
    }



    public void bindToSensorsService(Context context){
        Intent intent = new Intent(context, SensorsManagerService.class);
        context.bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    public void unbindFromSensorsService(Context context){
        context.unbindService(this);
    }

    public void registerToEngineEvents(ICarBlackBoxEngineListener listener){
        mUiListener = listener;
        Log.w(this.getClass().getSimpleName(),"Another listener added to engine events");
    }

    public void unregisterFromEngineEvents(){
        mUiListener = null;
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
    public void onSensorsMissing() {
        Log.e(this.getClass().getSimpleName(),"One or more sensors are missing!");
    }

    @Override
    public void onXNegativeAccChange(float x, float y, float z, long timestamp) {
        Log.w(TAG,"onXNegativeAccChange: " + timestamp + " x:" + x + ",y:" + y + ",z:" + z);

        if(mStartRecordTurnLeftTime == 0){
            mStartRecordTurnLeftTime = timestamp;
        }
        if(timestamp - mStartRecordTurnLeftTime >= MINIMAL_TURN_TIME){
            if(mAboveTurnLeftSensitivityCounter >= mUnderTurnLeftSensitivityCounter){
                //TODO: use more interesting function and not a constant
                if(mUiListener != null){
                    Log.w(TAG,"Turn Left: " + timestamp + " x:" + x + ",y:" + y + ",z:" + z);
                    mUiListener.onSharpTurnLeft(20);
                }
            }
            mStartRecordTurnLeftTime = 0;
            mAboveTurnLeftSensitivityCounter = 0;
            mUnderTurnLeftSensitivityCounter = 0;
        }
        if(x < -TURN_ACC_SENSITIVITY){
            mAboveTurnLeftSensitivityCounter++;
        }else{
            mUnderTurnLeftSensitivityCounter++;
        }
    }

    @Override
    public void onXPositiveAccChange(float x, float y, float z, long timestamp) {
        Log.w(TAG,"onXPositiveAccChange: " + timestamp + " x:" + x + ",y:" + y + ",z:" + z);

        if(mStartRecordTurnRightTime == 0){
            mStartRecordTurnRightTime = timestamp;
        }
        if(timestamp - mStartRecordTurnRightTime >= MINIMAL_TURN_TIME){
            if(mAboveTurnRightSensitivityCounter >= mUnderTurnRightSensitivityCounter){
                //TODO: use more interesting function and not a constant
                Log.w(TAG,"Turn Right " + timestamp + " x:" + x + ",y:" + y + ",z:" + z);
                if(mUiListener != null) {
                    mUiListener.onSharpTurnRight(-20);
                }
            }
            mStartRecordTurnRightTime = 0;
            mAboveTurnRightSensitivityCounter = 0;
            mUnderTurnRightSensitivityCounter = 0;
        }
        if(x > TURN_ACC_SENSITIVITY){
            mAboveTurnRightSensitivityCounter++;
        }else{
            mUnderTurnRightSensitivityCounter++;
        }
    }

    @Override
    public void onYNegativeAccChange(float x, float y, float z, long timestamp) {

    }

    @Override
    public void onYPositiveAccChange(float x, float y, float z, long timestamp) {

    }

    @Override
    public void onZNegativeAccChange(float x, float y, float z, long timestamp) {
        if(mZNegEventFilterCounter == EVENT_FILTER_THRESHOLD){
            mZNegEventFilterCounter = 0;
            if(mUiListener != null) {
                mUiListener.onCarAcceleration(z);
            }
        }
        mZNegEventFilterCounter++;

    }

    @Override
    public void onZPositiveAccChange(float x, float y, float z, long timestamp) {
        //Log.w(TAG,"onZPositiveAccChange: " + timestamp + " x:" + x + ",y:" + y + ",z:" + z);

        if(mStartRecordBrakeTime == 0){
            mStartRecordBrakeTime = timestamp;
        }
        if(timestamp - mStartRecordBrakeTime >= MINIMAL_BRAKE_TIME){
            if(mAboveBrakeSensitivityCounter >= mUnderBreakSensitivityCounter){
                //TODO: use more interesting function and not a constant
                if(mUiListener != null) {
                    mUiListener.onSuddenBreak(20);
                }
            }
            mStartRecordBrakeTime = 0;
            mAboveBrakeSensitivityCounter = 0;
            mUnderBreakSensitivityCounter = 0;
        }
        if(z > BRAKE_SENSITIVITY){
            mAboveBrakeSensitivityCounter++;
        }else{
            mUnderBreakSensitivityCounter++;
        }
    }

    //-------------------------Location Management--------------------------------

    public void initiateLocationManager(Context context){
        mLocationManager = LocationManager.getInstance(context);
    }

    public void startLocationManager(){
        mLocationManager.registerToLocationManagerEvents(this);
        mLocationManager.connect();
    }

    public void stopLocationManager(){
        mLocationManager.disconnect();
        mLocationManager.unregisterFromLocationManagerEvents();
    }

    @Override
    public void onLocationResolutionRequired(Status status) {
        mUiListener.onLocationResolutionRequired(status);
    }

    @Override
    public void onSpeedChanged(int speed) {
        if(mUiListener != null) {
            mUiListener.onSpeedChanged(speed);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if(mUiListener != null) {
            mUiListener.onLocationChanged(location);
        }
    }

}
