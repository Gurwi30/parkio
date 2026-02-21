package it.parkio.app.util;

import java.awt.*;

public class ColorUtil {

    private ColorUtil() {
        throw new IllegalStateException("Can't instantiate utility class");
    }

    public static String toHexString(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

}
