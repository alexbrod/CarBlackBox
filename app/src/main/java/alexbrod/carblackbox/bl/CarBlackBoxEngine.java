package alexbrod.carblackbox.bl;

import android.animation.ValueAnimator;
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

    private static final long ACC_PEAK_DURATION = 1000; //ms
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
        Log.w("X","General: (" + x + "," + y + "," + z + ")");

        if(x >= (float)mLeftAccAnimation.getAnimatedValue() && x >= TURN_ACC_SENSITIVITY){
            Log.w("X","Left: (" + x + "," + y + "," + z + ")");
            mLeftAccAnimation = ValueAnimator.ofFloat(x,0f);
            mLeftAccAnimation.setDuration(ACC_PEAK_DURATION);
            mLeftAccAnimation.start();
            for (ICarBlackBoxEngineListener l:mUiListeners) {
                l.OnSharpTurnLeft(x, y, z);
            }
        }
        else if(x <= (float)mRightAccAnimation.getAnimatedValue() && x < -TURN_ACC_SENSITIVITY){
            Log.w("X","Right: (" + x + "," + y + "," + z + ")");
            mRightAccAnimation = ValueAnimator.ofFloat(z,0f);
            mRightAccAnimation.setDuration(ACC_PEAK_DURATION);
            mRightAccAnimation.start();
            for (ICarBlackBoxEngineListener l:mUiListeners) {
                l.OnSharpTurnRight(x, y, z);
            }
        }


    }

    @Override
    public void OnSensorYAccChanged(float x, float y, float z, long timestamp) {

    }

    @Override
    public void OnSensorZAccChanged(float x, float y, float z, long timestamp) {
        Log.w("Z","General: (" + x + "," + y + "," + z + ")");
        if(z >= (float)mForwardAccAnimation.getAnimatedValue() && z >= FORWARD_ACC_SENSITIVITY){
            Log.w("Z","Forward: (" + x + "," + y + "," + z + ")");
            mForwardAccAnimation = ValueAnimator.ofFloat(z,0f);
            mForwardAccAnimation.setDuration(ACC_PEAK_DURATION);
            mForwardAccAnimation.start();
            for (ICarBlackBoxEngineListener l:mUiListeners) {
                l.OnSuddenBreak(x, y, z);
            }
        }
        else if(z <= (float)mBackwardAccAnimation.getAnimatedValue() && z < -BACK_ACC_SENSITIVITY){
            Log.w("Z","Back: (" + x + "," + y + "," + z + ")");
            mBackwardAccAnimation = ValueAnimator.ofFloat(z,0f);
            mBackwardAccAnimation.setDuration(ACC_PEAK_DURATION);
            mBackwardAccAnimation.start();
            for (ICarBlackBoxEngineListener l:mUiListeners) {
                l.onSuddenAcceleration(x, y, z);
            }
        }
    }

}
