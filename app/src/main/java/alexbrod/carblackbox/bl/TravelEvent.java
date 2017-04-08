package alexbrod.carblackbox.bl;

/**
 * Created by Alex Brod on 4/8/2017.
 */

public class TravelEvent {
    private long timeOccurred;
    private String type;
    private double value;
    private double locLat;
    private double locLong;

    public TravelEvent(long timeOccurred, String type, double value, double locLat, double locLong) {
        this.timeOccurred = timeOccurred;
        this.type = type;
        this.value = value;
        this.locLat = locLat;
        this.locLong = locLong;
    }

    public long getTimeOccurred() {
        return timeOccurred;
    }

    public void setTimeOccurred(long timeOccurred) {
        this.timeOccurred = timeOccurred;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getLocLat() {
        return locLat;
    }

    public void setLocLat(double locLat) {
        this.locLat = locLat;
    }

    public double getLocLong() {
        return locLong;
    }

    public void setLocLong(double locLong) {
        this.locLong = locLong;
    }
}
