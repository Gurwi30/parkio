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

    private final ParkingLotsManager lotsManager; // Gestore dei parcheggi

    public MapParkingPainter(ParkingLotsManager lotsManager) {
        this.lotsManager = lotsManager;
    }

    @Override
    public void paint(@NotNull Graphics2D g, JXMapViewer map, int width, int height) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // Antialiasing

        lotsManager.getParkingLots().forEach(parkingLot -> drawParkingLot(g, map, parkingLot)); // Disegna tutti i parcheggi
    }

    private void drawParkingLot(@NotNull Graphics2D g, @NotNull JXMapViewer map, @NotNull ParkingLot parkingLot) {
        Point2D topLeft = map.getTileFactory().geoToPixel(parkingLot.getBounds().top(), map.getZoom()); // coordinate pixel top-left
        Point2D bottomRight = map.getTileFactory().geoToPixel(parkingLot.getBounds().bottom(), map.getZoom()); // coordinate pixel bottom-right

        Rectangle lotRect = calculateViewportRect(topLeft, bottomRight, map.getViewportBounds()); // rettangolo viewport

        Color base = parkingLot.getColor(); // colore principale del parcheggio
        Color transparent = new Color(
                base.getRed(),
                base.getGreen(),
                base.getBlue(),
                100 // trasparenza
        );

        g.setColor(transparent);
        g.fill(new Rectangle2D.Double(lotRect.x, lotRect.y, lotRect.width, lotRect.height)); // riempie il parcheggio

        g.setColor(base);
        g.setStroke(new BasicStroke(2)); // bordo spesso
        g.draw(new Rectangle2D.Double(lotRect.x, lotRect.y, lotRect.width, lotRect.height)); // disegna bordo

        parkingLot.getSpaces().forEach(space -> drawParkingSpace(g, map, space)); // disegna gli spazi interni

        if (parkingLot.getName() != null && !parkingLot.getName().isEmpty()) { // disegna il nome del parcheggio
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            FontMetrics fm = g.getFontMetrics();
            String text = parkingLot.getName();

            int textWidth = fm.stringWidth(text); // larghezza testo
            int textHeight = fm.getHeight(); // altezza testo

            int textX = lotRect.x + (lotRect.width - textWidth) / 2; // centra orizzontalmente
            int totalHeight = textHeight + 5; // spazio totale incluso margine
            int startY = lotRect.y + (lotRect.height - totalHeight) / 2; // posizione verticale centrata
            int textY = startY + fm.getAscent(); // baseline del testo

            g.drawString(text, textX, textY); // disegna testo
        }

    }

    private void drawParkingSpace(@NotNull Graphics2D g, @NotNull JXMapViewer mapViewer, @NotNull ParkingSpace parkingSpace) {
        Point2D topLeft = mapViewer.getTileFactory().geoToPixel(parkingSpace.getBounds().top(), mapViewer.getZoom()); // top-left pixel
        Point2D bottomRight = mapViewer.getTileFactory().geoToPixel(parkingSpace.getBounds().bottom(), mapViewer.getZoom()); // bottom-right pixel

        Color color = switch (parkingSpace.getStatus()) { // colore secondo lo stato
            case ParkingSpaceStatus.Occupied _ -> Color.RED;
            case ParkingSpaceStatus.Reserved _ -> Color.YELLOW;
            default -> Color.GREEN;
        };

        g.setColor(color);
        g.setStroke(new BasicStroke(1));

        Rectangle spaceRect = calculateViewportRect(topLeft, bottomRight, mapViewer.getViewportBounds()); // rettangolo spazio

        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 150)); // colore trasparente
        g.fill(new Rectangle2D.Double(spaceRect.x, spaceRect.y, spaceRect.width, spaceRect.height)); // riempie spazio

        g.setColor(color);
        g.draw(new Rectangle2D.Double(spaceRect.x, spaceRect.y, spaceRect.width, spaceRect.height)); // bordo spazio

        if (mapViewer.getZoom() <= 12 && parkingSpace.getType() != ParkingSpace.Type.NORMAL) { // icona solo zoom <= 12
            JSVG icon = parkingSpace.getType() == ParkingSpace.Type.ELECTRIC ?
                    JSVG.from(Assets.ZAP_ICON).setColor(Color.YELLOW) : // icona elettrica
                    JSVG.from(Assets.ACCESSIBILITY_ICON).setColor(Color.BLUE); // icona accessibilità

            int iconSize = 18; // dimensione icona

            int iconX = spaceRect.x + (spaceRect.width - iconSize) / 2; // centra icona orizzontalmente
            int iconY = spaceRect.y + (spaceRect.height - iconSize) / 2; // centra icona verticalmente

            drawSVGIcon(icon, g, iconSize, iconX, iconY); // disegna icona
        }

    }

    private void drawSVGIcon(@NotNull JSVG svg, @NotNull Graphics2D g, int size, int x, int y) { // disegna un'icona SVG
        Graphics2D g2 = (Graphics2D) g.create(); // copia Graphics

        g2.translate(x, y);
        svg.setSize(size, size); // imposta dimensione
        svg.paint(g2); // disegna

        g2.dispose(); // libera resources
    }

    private @NotNull Rectangle calculateViewportRect(@NotNull Point2D topLeft, @NotNull Point2D bottomRight, @NotNull Rectangle viewportBounds) {
        // calcola rettangolo in coordinate viewport
        int x1 = (int) (topLeft.getX() - viewportBounds.getX());
        int y1 = (int) (topLeft.getY() - viewportBounds.getY());
        int x2 = (int) (bottomRight.getX() - viewportBounds.getX());
        int y2 = (int) (bottomRight.getY() - viewportBounds.getY());

        int x = Math.min(x1, x2);
        int y = Math.min(y1, y2);
        int w = Math.abs(x2 - x1);
        int h = Math.abs(y2 - y1);

        return new Rectangle(x, y, w, h); // rettangolo viewport
    }

}