package alexbrod.carblackbox.bl;

import android.location.Location;

import com.google.android.gms.common.api.Status;

/**
 * Created by Alex Brod on 3/13/2017.
 */

public interface ICarBlackBoxEngineListener {

    void onSuddenBreak(float acceleration, Location location);
    void onSharpTurnLeft(float acceleration, Location location);
    void onSharpTurnRight(float acceleration, Location location);
    void onSuddenAcceleration(float acceleration, Location location);
    void onLocationResolutionRequired(Status status);
    void onSpeedChanged(int speed);
    void onLocationChanged(Location location);

    void onCarAcceleration(float acceleration);

    void onCrossedSpeedLimit(int speed, Location location);
}
