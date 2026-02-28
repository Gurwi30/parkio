package it.parkio.app.ui.component.map;

import it.parkio.app.manager.ParkingLotsManager;
import it.parkio.app.model.ParkingLot;
import it.parkio.app.ui.Assets;
import it.parkio.app.ui.component.svg.JSVG;
import org.jetbrains.annotations.NotNull;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class MapParkingPainter implements Painter<JXMapViewer> {

    private final ParkingLotsManager lotsManager;

    public MapParkingPainter(ParkingLotsManager lotsManager) {
        this.lotsManager = lotsManager;
    }

    @Override
    public void paint(@NotNull Graphics2D g, JXMapViewer map, int width, int height) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        lotsManager.getParkingLots().forEach(parkingLot -> drawParkingLot(g, map, parkingLot));
    }

    private void drawParkingLot(@NotNull Graphics2D g, @NotNull JXMapViewer map, @NotNull ParkingLot parkingLot) {
        Point2D topLeft = map.getTileFactory().geoToPixel(parkingLot.getBounds().top(), map.getZoom());
        Point2D bottomRight = map.getTileFactory().geoToPixel(parkingLot.getBounds().bottom(), map.getZoom());

        Rectangle viewportBounds = map.getViewportBounds();

        int x1 = (int) (topLeft.getX() - viewportBounds.getX());
        int y1 = (int) (topLeft.getY() - viewportBounds.getY());
        int x2 = (int) (bottomRight.getX() - viewportBounds.getX());
        int y2 = (int) (bottomRight.getY() - viewportBounds.getY());

        int x = Math.min(x1, x2);
        int y = Math.min(y1, y2);
        int w = Math.abs(x2 - x1);
        int h = Math.abs(y2 - y1);

        Color base = parkingLot.getColor();
        Color transparent = new Color(
                base.getRed(),
                base.getGreen(),
                base.getBlue(),
                100
        );

        g.setColor(transparent);
        g.fill(new Rectangle2D.Double(x, y, w, h));

        g.setColor(base);
        g.setStroke(new BasicStroke(2));
        g.draw(new Rectangle2D.Double(x, y, w, h));

        //drawParkingSpaces(g, map, lot, x, y, w, h);

        int iconSize = 20;

        if (parkingLot.getName() != null && !parkingLot.getName().isEmpty()) {
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            FontMetrics fm = g.getFontMetrics();
            String text = parkingLot.getName();

            int textWidth = fm.stringWidth(text);
            int textHeight = fm.getHeight();

            int textX = x + (w - textWidth) / 2;
            int totalHeight = textHeight + iconSize + 5;
            int startY = y + (h - totalHeight) / 2;
            int textY = startY + fm.getAscent();

            g.drawString(text, textX, textY);

            int iconX = x + (w - iconSize) / 2;
            int iconY = textY + 5;
            drawSVGIcon(JSVG.from(Assets.ACCESSIBILITY_ICON), g, iconSize, iconX, iconY);

        } else {
            int iconX = x + (w - iconSize) / 2;
            int iconY = y + (h - iconSize) / 2;
            drawSVGIcon(JSVG.from(Assets.ACCESSIBILITY_ICON), g, iconSize, iconX, iconY);
        }

    }

    private void drawSVGIcon(@NotNull JSVG svg, @NotNull Graphics2D g, int size, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();

        g2.translate(x, y);
        svg.setSize(size, size);
        svg.paint(g2);

        g2.dispose();
    }

}
