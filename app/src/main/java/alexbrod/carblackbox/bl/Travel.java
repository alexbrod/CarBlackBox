package alexbrod.carblackbox.bl;

/**
 * Created by Alex Brod on 4/8/2017.
 */

public class Travel {
    private long startTime;
    private long endTime;

    public Travel(long startTime, long endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
}
