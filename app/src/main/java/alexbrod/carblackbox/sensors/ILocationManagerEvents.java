package alexbrod.carblackbox.sensors;

import android.location.Location;

import com.google.android.gms.common.api.Status;

/**
 * Created by Alex Brod on 3/27/2017.
 */

public interface ILocationManagerEvents {
    void onLocationResolutionRequired(Status status);
    void onSpeedChanged(int speed, Location location);
    void onLocationChanged(Location location);
}
