package alexbrod.carblackbox.ui;

import android.location.Location;

import com.google.android.gms.common.api.Status;

/**
 * Created by Alex Brod on 3/13/2017.
 */

public interface ICarBlackBoxEngineListener {

    void onSuddenBreak(float acceleration);
    void onSharpTurnLeft(float acceleration);
    void onSharpTurnRight(float acceleration);
    void onSuddenAcceleration(float acceleration);
    void onLocationResolutionRequired(Status status);
    void onSpeedChanged(int speed);
    void onLocationChanged(Location location);

    void onCarAcceleration(float acceleration);
}
