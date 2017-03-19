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

import alexbrod.carblackbox.utilities.MyUtilities;

/**
 * Created by Alex Brod on 3/10/2017.
 */

public class SensorsManagerService extends Service implements SensorEventListener {
    public static final float SENSITIVITY_LEVEL = 4;
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
            mSensorManager.registerListener(this, mLinearAccelerationSensor, SensorManager.SENSOR_DELAY_GAME); // around 20ms
            Log.w(this.getClass().getSimpleName(), "Registered SensorsManagerService to Sensor Event");
        }catch (NullPointerException e){
            Log.w(this.getClass().getSimpleName(), "One or more sensors are not available");
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.w(this.getClass().getSimpleName(),"In onBind");

        return mIBinder;
    }



    @Override
    public boolean onUnbind(Intent intent){
        Log.w(this.getClass().getSimpleName(),"In onUnBind");
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
        if(Math.abs(x) > SENSITIVITY_LEVEL){
            mSensorsEventsListener.OnSensorXAccChanged(x,y,z,sensorEvent.timestamp);
        }
        if(Math.abs(z) > SENSITIVITY_LEVEL){
            mSensorsEventsListener.OnSensorZAccChanged(x,y,z,sensorEvent.timestamp);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        Log.w(this.getClass().getSimpleName(),"accuracy changed");

    }

    public void registerToSensorsEvents(ISensorsEvents sensorsEventsListener){
        if (mSensorsEventsListener == sensorsEventsListener) {
            return;
        }
        mSensorsEventsListener = sensorsEventsListener;
        Log.w(this.getClass().getSimpleName(),"Registered Engine to SensorsManagerService events");
    }
}
