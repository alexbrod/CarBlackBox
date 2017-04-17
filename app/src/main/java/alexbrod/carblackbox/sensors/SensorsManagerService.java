package alexbrod.carblackbox.sensors;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Date;

import alexbrod.carblackbox.utilities.MyUtilities;

/**
 * Created by Alex Brod on 3/10/2017.
 */

public class SensorsManagerService extends Service implements SensorEventListener {
    public static final float SENSITIVITY_LEVEL = 1; //Acceleration
    private static int X = 0;
    private static int Y = 1;
    private static int Z = 2;
    private final IBinder mIBinder = new LocalBinder();
    private ISensorsEvents mSensorsEventsListener;
    private SensorManager mSensorManager;
    private Sensor mLinearAccelerationSensor;

    //TODO: create seperate thread to process sensor data
    //TODO: address application with broadcast messages

    public class LocalBinder extends Binder {
        public SensorsManagerService getService() {
            return SensorsManagerService.this;
        }


    }

    //------------------------- Service methods ----------------------

    public void onCreate(){
        super.onCreate();
        try {
            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            mLinearAccelerationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            mSensorManager.registerListener(this, mLinearAccelerationSensor, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(this.getClass().getSimpleName(), "Registered SensorsManagerService to Sensor Event");
        }catch (NullPointerException e){
            Log.e(this.getClass().getSimpleName(), "One or more sensors are not available");
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(this.getClass().getSimpleName(),"In onBind");

        return mIBinder;
    }



    @Override
    public boolean onUnbind(Intent intent){
        Log.d(this.getClass().getSimpleName(),"In onUnBind");
        super.onUnbind(intent);
        mSensorManager.unregisterListener(this);
        return false;
    }


    //------------------------- Sensors methods ----------------------


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float x = MyUtilities.roundUp(sensorEvent.values[X],1);
        float y = MyUtilities.roundUp(sensorEvent.values[Y],1);
        float z = MyUtilities.roundUp(sensorEvent.values[Z],1);
        //convert timestamp from micro to milli
        long timestamp = sensorUptimeToTimeInMilli(sensorEvent.timestamp);
        if(x < -SENSITIVITY_LEVEL){
            mSensorsEventsListener.onXNegativeAccChange(x,y,z,timestamp);
        }else if(x > SENSITIVITY_LEVEL){
            mSensorsEventsListener.onXPositiveAccChange(x,y,z,timestamp);
        }
        if(z < -SENSITIVITY_LEVEL){
            mSensorsEventsListener.onZNegativeAccChange(x,y,z,timestamp);
        }else if(z > SENSITIVITY_LEVEL){
            mSensorsEventsListener.onZPositiveAccChange(x,y,z,timestamp);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        Log.d(this.getClass().getSimpleName(),"accuracy changed");

    }

    public void registerToSensorsEvents(ISensorsEvents sensorsEventsListener){
        if (mSensorsEventsListener == sensorsEventsListener) {
            return;
        }
        mSensorsEventsListener = sensorsEventsListener;
        Log.d(this.getClass().getSimpleName(),"Registered Engine to SensorsManagerService events");
    }

    private long sensorUptimeToTimeInMilli(long uptimeInNano){
        return (new Date()).getTime()
                + (uptimeInNano - System.nanoTime()) / 1000000L;
    }
}
