package it.parkio.app.ui.component.map.painter;

import it.parkio.app.manager.ParkingLotsManager;
import it.parkio.app.model.ParkingLot;
import it.parkio.app.model.ParkingSpace;
import it.parkio.app.model.ParkingSpaceStatus;
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

        Coordinates coordinates = calculateCoordinates(topLeft, bottomRight, map.getViewportBounds());

        Color base = parkingLot.getColor();
        Color transparent = new Color(
                base.getRed(),
                base.getGreen(),
                base.getBlue(),
                100
        );

        g.setColor(transparent);
        g.fill(new Rectangle2D.Double(coordinates.x, coordinates.y, coordinates.width, coordinates.height));

        g.setColor(base);
        g.setStroke(new BasicStroke(2));
        g.draw(new Rectangle2D.Double(coordinates.x, coordinates.y, coordinates.width, coordinates.height));

        parkingLot.getSpaces().forEach(space -> drawParkingSpace(g, map, space));

        if (parkingLot.getName() != null && !parkingLot.getName().isEmpty()) {
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            FontMetrics fm = g.getFontMetrics();
            String text = parkingLot.getName();

            int textWidth = fm.stringWidth(text);
            int textHeight = fm.getHeight();

            int textX = coordinates.x + (coordinates.width - textWidth) / 2;
            int totalHeight = textHeight + 5;
            int startY = coordinates.y + (coordinates.height - totalHeight) / 2;
            int textY = startY + fm.getAscent();

            g.drawString(text, textX, textY);
        }

    }

    private void drawParkingSpace(@NotNull Graphics2D g, @NotNull JXMapViewer map, @NotNull ParkingSpace parkingSpace) {
        Point2D topLeft = map.getTileFactory().geoToPixel(parkingSpace.getBounds().top(), map.getZoom());
        Point2D bottomRight = map.getTileFactory().geoToPixel(parkingSpace.getBounds().bottom(), map.getZoom());

        Color color = switch (parkingSpace.getStatus()) {
            case ParkingSpaceStatus.Occupied _ -> Color.RED;
            case ParkingSpaceStatus.Reserved _ -> Color.YELLOW;
            default -> Color.GREEN;
        };

        g.setColor(color);
        g.setStroke(new BasicStroke(1));

        Coordinates coordinates = calculateCoordinates(topLeft, bottomRight, map.getViewportBounds());

        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 150));

        g.fill(new Rectangle2D.Double(coordinates.x, coordinates.y, coordinates.width, coordinates.height));

        g.setColor(color);
        g.draw(new Rectangle2D.Double(coordinates.x, coordinates.y, coordinates.width, coordinates.height));

        if (parkingSpace.getType() != ParkingSpace.Type.NORMAL) {
            JSVG icon = parkingSpace.getType() == ParkingSpace.Type.ELECTRIC ?
                    JSVG.from(Assets.ZAP_ICON).setColor(Color.YELLOW) :
                    JSVG.from(Assets.ACCESSIBILITY_ICON).setColor(Color.BLUE);

            int iconSize = 20;

            int iconX = coordinates.x + (coordinates.width - iconSize) / 2;
            int iconY = coordinates.y + (coordinates.height - iconSize) / 2;

            drawSVGIcon(icon, g, iconSize, iconX, iconY);
        }

    }

    private void drawSVGIcon(@NotNull JSVG svg, @NotNull Graphics2D g, int size, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();

        g2.translate(x, y);
        svg.setSize(size, size);
        svg.paint(g2);

        g2.dispose();
    }

    private @NotNull Coordinates calculateCoordinates(@NotNull Point2D topLeft, @NotNull Point2D bottomRight, @NotNull Rectangle viewportBounds) {
        int x1 = (int) (topLeft.getX() - viewportBounds.getX());
        int y1 = (int) (topLeft.getY() - viewportBounds.getY());
        int x2 = (int) (bottomRight.getX() - viewportBounds.getX());
        int y2 = (int) (bottomRight.getY() - viewportBounds.getY());

        int x = Math.min(x1, x2);
        int y = Math.min(y1, y2);
        int w = Math.abs(x2 - x1);
        int h = Math.abs(y2 - y1);

        return new Coordinates(x, y, w, h);
    }

    private record Coordinates(int x, int y, int width, int height) {

    }

}
