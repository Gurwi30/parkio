package it.parkio.app.util;

import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class ColorUtil {

    private ColorUtil() { // costruttore privato per impedire istanziazioni della classe utility
        throw new IllegalStateException("Can't instantiate utility class");
    }

    public static @NotNull String toHexString(@NotNull Color color) { // converte un oggetto Color in stringa esadecimale #rrggbb
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()); // formatta i valori RGB in esadecimale
    }

}