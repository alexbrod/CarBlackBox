package alexbrod.carblackbox.sensors;

/**
 * Created by Alex Brod on 3/10/2017.
 */

public interface ISensorsEvents {

    void OnSensorsMissing();

    void OnSensorXAccChanged(float x, float y, float z, long timestamp);
    void OnSensorYAccChanged(float x, float y, float z, long timestamp);
    void OnSensorZAccChanged(float x, float y, float z, long timestamp);
}
