package alexbrod.carblackbox.utilities;

import java.math.BigDecimal;

/**
 * Created by Alex Brod on 3/18/2017.
 */

public class MyUtilities {
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
}
