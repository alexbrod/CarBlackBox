package alexbrod.carblackbox.ui;

import android.location.Location;

import com.google.android.gms.common.api.Status;

/**
 * Created by Alex Brod on 3/13/2017.
 */

public interface ICarBlackBoxEngineListener {

    void onSuddenBreak(float Acceleration);
    void onSharpTurnLeft(float Acceleration);
    void onSharpTurnRight(float Acceleration);
    void onSuddenAcceleration(float Acceleration);
    void onLocationResolutionRequired(Status status);
    void onSpeedChanged(int speed);
    void onLocationChanged(Location location);

}
