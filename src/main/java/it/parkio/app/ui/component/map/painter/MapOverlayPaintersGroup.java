package it.parkio.app.ui.component.map.painter;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;

import java.awt.*;

public class MapOverlayPaintersGroup implements Painter<JXMapViewer> {

    private final Painter<JXMapViewer>[] painters;

    @SafeVarargs
    public MapOverlayPaintersGroup(Painter<JXMapViewer>... painters) {
        this.painters = painters;
    }

    @Override
    public void paint(Graphics2D g, JXMapViewer object, int width, int height) {
        for (Painter<JXMapViewer> painter : painters) {
            painter.paint(g, object, width, height);
        }
    }

}
