package it.parkio.app.util;

import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * Classe di utilità per operazioni legate ai colori.
 *
 * <p>Attualmente contiene un solo metodo, ma centralizzare questa logica
 * evita duplicazioni e rende il codice più leggibile nei punti
 * in cui i colori devono essere convertiti in formato testuale.</p>
 */
public class ColorUtil {

    /**
     * Costruttore privato per impedire l'istanziazione della classe utility.
     *
     * <p>Una classe utility contiene solo metodi statici
     * e non ha senso crearne oggetti.</p>
     */
    private ColorUtil() {
        throw new IllegalStateException("Can't instantiate utility class");
    }

    /**
     * Converte un oggetto {@link Color} in una stringa esadecimale
     * nel formato {@code #rrggbb}.
     *
     * <p>Questo formato è comodo per salvare il colore nel file JSON
     * e poi ricostruirlo facilmente con {@link Color#decode(String)}.</p>
     *
     * @param color colore da convertire
     * @return rappresentazione esadecimale del colore
     */
    public static @NotNull String toHexString(@NotNull Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

}