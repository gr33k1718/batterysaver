package application.com.batterysaver;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * The Utils class contains various utility methods
 */
public class Utils {

    /**
     * Round a given number to a set number of decimal places
     *
     * @param value  the value to round
     * @param places the decimal place
     * @return
     */
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
