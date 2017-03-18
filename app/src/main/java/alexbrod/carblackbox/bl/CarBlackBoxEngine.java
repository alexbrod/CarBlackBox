package alexbrod.carblackbox.bl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import java.util.Vector;

import alexbrod.carblackbox.sensors.ISensorsEvents;
import alexbrod.carblackbox.sensors.SensorsManagerService;
import alexbrod.carblackbox.ui.ICarBlackBoxEngineListener;

/**
 * Created by Alex Brod on 3/13/2017.
 */

public class CarBlackBoxEngine implements ISensorsEvents, ServiceConnection {

    private static CarBlackBoxEngine carBlackBoxEngine;
    private SensorsManagerService mSensorsManagerService;
    private Vector<ICarBlackBoxEngineListener> mUiListeners;

    private CarBlackBoxEngine(){
        mSensorsManagerService = new SensorsManagerService();
        mUiListeners = new Vector<>();

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
    public void OnSensorChanged(float x, float y, float z) {
        for (ICarBlackBoxEngineListener l:mUiListeners) {
            l.OnAccelerationChanged(x, y, z);
        }
    }

}
