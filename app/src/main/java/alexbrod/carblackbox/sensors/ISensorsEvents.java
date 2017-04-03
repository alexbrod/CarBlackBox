package alexbrod.carblackbox.sensors;

/**
 * Created by Alex Brod on 3/10/2017.
 */

public interface ISensorsEvents {

    void onSensorsMissing();

    void onXNegativeAccChange(float x, float y, float z, long timestamp);
    void onYNegativeAccChange(float x, float y, float z, long timestamp);
    void onZNegativeAccChange(float x, float y, float z, long timestamp);
    void onXPositiveAccChange(float x, float y, float z, long timestamp);
    void onYPositiveAccChange(float x, float y, float z, long timestamp);
    void onZPositiveAccChange(float x, float y, float z, long timestamp);
}
