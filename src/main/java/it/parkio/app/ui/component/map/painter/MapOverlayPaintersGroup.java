package it.parkio.app.ui.component.map.painter;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;

import java.awt.*;

public class MapOverlayPaintersGroup implements Painter<JXMapViewer> { // Raggruppa più Painter in un unico overlay

    private final Painter<JXMapViewer>[] painters; // Array di painter da eseguire in sequenza

    @SafeVarargs
    public MapOverlayPaintersGroup(Painter<JXMapViewer>... painters) { // Costruttore con numero variabile di painter
        this.painters = painters; // memorizza i painter
    }

    @Override
    public void paint(Graphics2D g, JXMapViewer object, int width, int height) { // Disegna tutti i painter sul JXMapViewer
        for (Painter<JXMapViewer> painter : painters) { // per ogni painter
            painter.paint(g, object, width, height); // esegue il paint
        }
    }

}