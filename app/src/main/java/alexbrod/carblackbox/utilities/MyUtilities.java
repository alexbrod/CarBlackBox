package alexbrod.carblackbox.utilities;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Alex Brod on 3/18/2017.
 */

public class MyUtilities {

    //-----------------------Global constants --------------------------------
    public static final String SPEEDING = "speeding";
    public static final String SUDDEN_BRAKE = "sudden_brake";
    public static final String SHARP_TURN = "sharp_turn";


    /**
     * Round to certain number of decimals
     *
     * @param d
     * @param decimalPlace
     * @return
     */
    public static float roundUp(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

    public static String formatDateTime(long milliSeconds) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm");
        return formatter.format(new Date(milliSeconds));
    }
}


