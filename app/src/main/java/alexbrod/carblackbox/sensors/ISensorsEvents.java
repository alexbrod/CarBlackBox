package alexbrod.carblackbox.sensors;

/**
 * Created by Alex Brod on 3/10/2017.
 */

public interface ISensorsEvents {

    void OnSensorsMissing();

    void OnSensorChanged(float x, float y, float z);
}
