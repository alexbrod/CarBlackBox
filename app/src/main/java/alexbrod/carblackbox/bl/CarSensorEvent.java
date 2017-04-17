package alexbrod.carblackbox.bl;

/**
 * Created by Alex Brod on 4/11/2017.
 */

public class CarSensorEvent {
    private long timestamp;
    private String type;
    private String axis;
    private double value;

    public CarSensorEvent(long timestamp, String type, String axis, double value) {
        this.timestamp = timestamp;
        this.type = type;
        this.axis = axis;
        this.value = value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAxis() {
        return axis;
    }

    public void setAxis(String axis) {
        this.axis = axis;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
