package it.parkio.app.ui.component.map.painter;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;

import java.awt.*;

/**
 * Raggruppa più painter in un unico painter composito.
 *
 * <p>È utile quando si vuole disegnare più livelli grafici sulla mappa
 * mantenendo il codice separato per responsabilità:
 * ad esempio un painter per i parcheggi reali
 * e un altro per il rettangolo temporaneo di disegno.</p>
 */
public class MapOverlayPaintersGroup implements Painter<JXMapViewer> {

    /**
     * Elenco dei painter da eseguire in ordine.
     *
     * <p>L'ordine è importante: i painter successivi possono sovrapporsi
     * a quelli disegnati prima.</p>
     */
    private final Painter<JXMapViewer>[] painters;

    /**
     * Costruttore variadico che accetta un numero arbitrario di painter.
     *
     * @param painters painter da comporre insieme
     */
    @SafeVarargs
    public MapOverlayPaintersGroup(Painter<JXMapViewer>... painters) {
        this.painters = painters;
    }

    /**
     * Disegna tutti i painter in sequenza sullo stesso contesto grafico.
     *
     * @param g      contesto grafico
     * @param object mappa su cui disegnare
     * @param width  larghezza disponibile
     * @param height altezza disponibile
     */
    @Override
    public void paint(Graphics2D g, JXMapViewer object, int width, int height) {
        for (Painter<JXMapViewer> painter : painters) {
            painter.paint(g, object, width, height);
        }
    }

}