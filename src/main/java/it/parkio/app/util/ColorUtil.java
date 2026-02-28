package it.parkio.app.util;

import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class ColorUtil {

    private ColorUtil() {
        throw new IllegalStateException("Can't instantiate utility class");
    }

    public static @NotNull String toHexString(@NotNull Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

}
