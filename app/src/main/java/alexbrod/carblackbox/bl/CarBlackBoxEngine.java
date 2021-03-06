package alexbrod.carblackbox.bl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.api.Status;

import java.util.ArrayList;
import java.util.Calendar;

import alexbrod.carblackbox.db.DbManager;
import alexbrod.carblackbox.sensors.CarLocationManager;
import alexbrod.carblackbox.sensors.ILocationManagerEvents;
import alexbrod.carblackbox.sensors.ISensorsEvents;
import alexbrod.carblackbox.sensors.SensorsManagerService;

import static alexbrod.carblackbox.utilities.MyUtilities.SHARP_TURN;
import static alexbrod.carblackbox.utilities.MyUtilities.SPEEDING;
import static alexbrod.carblackbox.utilities.MyUtilities.SUDDEN_BRAKE;

/**
 * Created by Alex Brod on 3/13/2017.
 */

public class CarBlackBoxEngine implements ISensorsEvents, ServiceConnection,
        ILocationManagerEvents {
    private static final String TAG = "CarBlackBoxEngine";
    private static final float BRAKE_SENSITIVITY = 5;
    private static final float TURN_ACC_SENSITIVITY = 3;
    private static final long MINIMAL_BRAKE_TIME = 3000; //milli-sec
    private static final long MINIMAL_TURN_TIME = 3000;  //milli-sec
    private static final long MINIMAL_SPEED_TIME = 5000;  //milli-sec;
    private static final int EVENT_FILTER_THRESHOLD = 3;
    private static final int SPEED_LIMIT = 60;
    private int mZNegEventFilterCounter = 0;
    private long mStartRecordTurnLeftTime = 0;
    private long mStartRecordTurnRightTime = 0;
    private long mStartRecordBrakeTime = 0;
    private long mStartRecordSpeedTime = 0;
    private int mAboveSpeedLimitCounter = 0;
    private int mUnderSpeedLimitCounter = 0;
    private int mAboveBrakeSensitivityCounter = 0;
    private int mUnderBreakSensitivityCounter = 0;
    private int mAboveTurnLeftSensitivityCounter = 0;
    private int mUnderTurnLeftSensitivityCounter = 0;
    private int mAboveTurnRightSensitivityCounter = 0;
    private int mUnderTurnRightSensitivityCounter = 0;
    private long mTravelStartTime;

    private static CarBlackBoxEngine mCarBlackBoxEngine;
    private SensorsManagerService mSensorsManagerService;
    private ICarBlackBoxEngineListener mEngineEventsListener;
    private CarLocationManager mCarLocationManager;
    private DbManager mDbManager;
    private int XPCounterFilter = 0;
    private int XNCounterFilter = 0;
    private int ZPCounterFilter = 0;


    private CarBlackBoxEngine(){
        mSensorsManagerService = new SensorsManagerService();
    }


    public static CarBlackBoxEngine getInstance(){
        if(mCarBlackBoxEngine == null){
            mCarBlackBoxEngine = new CarBlackBoxEngine();
        }
        return mCarBlackBoxEngine;
    }


    public void connectDb(Context context){
        mDbManager = DbManager.getInstance(context);
        mTravelStartTime = Calendar.getInstance().getTimeInMillis();
        mDbManager.addTravel(mTravelStartTime,-1);
        Log.i(TAG,"Travel added: " + mTravelStartTime);
    }

    public void disconnectDb(){
        int rowUpdated;
        rowUpdated = mDbManager.updateTravelEndTime(mTravelStartTime,
                Calendar.getInstance().getTimeInMillis());
        Log.i(TAG,"Travel: " + mTravelStartTime + " updated " + rowUpdated );
        mDbManager.close();
    }

    public void bindToSensorsService(Context context){
        Intent intent = new Intent(context, SensorsManagerService.class);
        context.bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    public void unbindFromSensorsService(Context context){
        context.unbindService(this);
    }

    public void registerToEngineEvents(ICarBlackBoxEngineListener listener){
        mEngineEventsListener = listener;
        Log.i(TAG,"Listener added to engine events");
    }

    public void unregisterFromEngineEvents(){
        mEngineEventsListener = null;
    }

    public ArrayList<TravelEvent> getCurrentTravelEvents(){
        return mDbManager.getEventsByTravel(mTravelStartTime);
    }

    //-----------------------------Sensors service methods-----------------------

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        mSensorsManagerService = ((SensorsManagerService.LocalBinder)iBinder).getService();
        mSensorsManagerService.registerToSensorsEvents(this);
        Log.i(TAG,"Engine connected to SensorsManagerService");

    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }

    //------------------------------Sensor events methods---------------------

    @Override
    public void onSensorsMissing() {
        Log.e(TAG,"One or more sensors are missing!");
    }

    @Override
    public void onXNegativeAccChange(float x, float y, float z, long timestamp) {
        if(XNCounterFilter == EVENT_FILTER_THRESHOLD){
            Log.i(TAG,"onXNegativeAccChange: " + timestamp + " x:" + x + ",y:" + y + ",z:" + z);
            mDbManager.addSensorEvent(new CarSensorEvent(timestamp,
                    "accelerometer",
                    "x_negative",
                    x));
            XNCounterFilter = 0;
        }
        XNCounterFilter++;

        if(mStartRecordTurnLeftTime == 0){
            mStartRecordTurnLeftTime = timestamp;
        }
        if(timestamp - mStartRecordTurnLeftTime >= MINIMAL_TURN_TIME){
            if(mAboveTurnLeftSensitivityCounter > mUnderTurnLeftSensitivityCounter){
                mDbManager.addTravelEvent(mTravelStartTime,
                        SHARP_TURN,
                        timestamp,
                        -1,
                        mCarLocationManager.getLastKnownLocation().getLatitude(),
                        mCarLocationManager.getLastKnownLocation().getLongitude());
                Log.i(TAG,"Add TravelEvent " + SHARP_TURN + " time " + timestamp);
                //TODO: use more interesting function and not a constant
                if(mEngineEventsListener != null){
                    Log.i(TAG,"Turn Left: " + timestamp + " x:" + x + ",y:" + y + ",z:" + z);
                    mEngineEventsListener.onSharpTurnLeft(20, mCarLocationManager.getLastKnownLocation());
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
        if(XPCounterFilter == EVENT_FILTER_THRESHOLD){
            Log.i(TAG,"onXPositiveAccChange: " + timestamp + " x:" + x + ",y:" + y + ",z:" + z);
            mDbManager.addSensorEvent(new CarSensorEvent(timestamp,
                    "accelerometer",
                    "x_positive",
                    x));
            XPCounterFilter = 0;
        }
        XPCounterFilter++;

        if(mStartRecordTurnRightTime == 0){
            mStartRecordTurnRightTime = timestamp;
        }
        if(timestamp - mStartRecordTurnRightTime >= MINIMAL_TURN_TIME){
            if(mAboveTurnRightSensitivityCounter > mUnderTurnRightSensitivityCounter){
                mDbManager.addTravelEvent(mTravelStartTime,
                        SHARP_TURN,
                        timestamp,
                        -1,
                        mCarLocationManager.getLastKnownLocation().getLatitude(),
                        mCarLocationManager.getLastKnownLocation().getLongitude());
                Log.i(TAG,"Add TravelEvent " + SHARP_TURN + " time " + timestamp);
                //TODO: use more interesting function and not a constant
                if(mEngineEventsListener != null) {
                    Log.i(TAG,"Turn Right " + timestamp + " x:" + x + ",y:" + y + ",z:" + z);
                    mEngineEventsListener.onSharpTurnRight(-20, mCarLocationManager.getLastKnownLocation());
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
            if(mEngineEventsListener != null) {
                mEngineEventsListener.onCarAcceleration(z);
            }
        }
        mZNegEventFilterCounter++;

    }

    @Override
    public void onZPositiveAccChange(float x, float y, float z, long timestamp) {

        if(ZPCounterFilter == EVENT_FILTER_THRESHOLD){
            Log.i(TAG,"onZPositiveAccChange: " + timestamp + " x:" + x + ",y:" + y + ",z:" + z);
            mDbManager.addSensorEvent(new CarSensorEvent(timestamp,
                    "accelerometer",
                    "z_positive",
                    z));
            ZPCounterFilter = 0;
        }
        ZPCounterFilter++;

        if(mStartRecordBrakeTime == 0){
            mStartRecordBrakeTime = timestamp;
        }
        if(timestamp - mStartRecordBrakeTime >= MINIMAL_BRAKE_TIME){
            if(mAboveBrakeSensitivityCounter > mUnderBreakSensitivityCounter){
                mDbManager.addTravelEvent(mTravelStartTime,
                        SUDDEN_BRAKE,
                        timestamp,
                        -1,
                        mCarLocationManager.getLastKnownLocation().getLatitude(),
                        mCarLocationManager.getLastKnownLocation().getLongitude());
                Log.i(TAG,"Add TravelEvent " + SUDDEN_BRAKE + " time " + timestamp);
                //TODO: use more interesting function and not a constant
                if(mEngineEventsListener != null) {
                    mEngineEventsListener.onSuddenBreak(20, mCarLocationManager.getLastKnownLocation());
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
        mCarLocationManager = CarLocationManager.getInstance(context);
    }

    public void startLocationManager(){
        mCarLocationManager.registerToLocationManagerEvents(this);
        mCarLocationManager.connect();
    }

    public void stopLocationManager(){
        mCarLocationManager.disconnect();
        mCarLocationManager.unregisterFromLocationManagerEvents();
    }

    @Override
    public void onLocationResolutionRequired(Status status) {
        mEngineEventsListener.onLocationResolutionRequired(status);
    }

    @Override
    public void onSpeedChanged(int speed, Location location) {
        mDbManager.addSensorEvent(new CarSensorEvent(location.getTime(),
                "GPS",
                "z_negative",
                speed));
        if(mStartRecordSpeedTime == 0){
            mStartRecordSpeedTime = location.getTime();
        }
        if(location.getTime() - mStartRecordSpeedTime >= MINIMAL_SPEED_TIME){
            if(mAboveSpeedLimitCounter >= mUnderSpeedLimitCounter){
                mDbManager.addTravelEvent(mTravelStartTime,
                        SPEEDING,
                        location.getTime(),
                        -1,
                        mCarLocationManager.getLastKnownLocation().getLatitude(),
                        mCarLocationManager.getLastKnownLocation().getLongitude());
                Log.w(TAG,"Add TravelEvent " + SPEEDING + " time " + location.getTime());
                if(mEngineEventsListener != null){
                    mEngineEventsListener.onCrossedSpeedLimit(speed, location);
                }
            }
            mStartRecordSpeedTime = 0;
            mAboveSpeedLimitCounter = 0;
            mUnderSpeedLimitCounter = 0;
        }
        if(speed > SPEED_LIMIT){
            mAboveSpeedLimitCounter++;
        }else{
            mUnderSpeedLimitCounter++;
        }

        if(mEngineEventsListener != null) {
            mEngineEventsListener.onSpeedChanged(speed);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if(mEngineEventsListener != null) {
            mEngineEventsListener.onLocationChanged(location);
        }
    }

}
