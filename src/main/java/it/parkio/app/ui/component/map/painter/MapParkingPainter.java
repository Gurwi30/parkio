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

/**
 * Painter responsabile del rendering dei parcheggi e dei posti auto sulla mappa.
 *
 * <p>Disegna:</p>
 * <ul>
 *     <li>rettangolo del parcheggio;</li>
 *     <li>nome del parcheggio;</li>
 *     <li>rettangoli dei singoli spazi;</li>
 *     <li>icone speciali per posti elettrici o riservati a disabili.</li>
 * </ul>
 */
public class MapParkingPainter implements Painter<JXMapViewer> {

    /**
     * Gestore dei parcheggi da visualizzare.
     */
    private final ParkingLotsManager lotsManager;

    /**
     * Costruttore.
     *
     * @param lotsManager sorgente dei dati da disegnare
     */
    public MapParkingPainter(ParkingLotsManager lotsManager) {
        this.lotsManager = lotsManager;
    }

    /**
     * Disegna tutti i parcheggi visibili.
     *
     * @param g      contesto grafico
     * @param map    mappa di riferimento
     * @param width  larghezza disponibile
     * @param height altezza disponibile
     */
    @Override
    public void paint(@NotNull Graphics2D g, JXMapViewer map, int width, int height) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        lotsManager.getParkingLots().forEach(parkingLot -> drawParkingLot(g, map, parkingLot));
    }

    /**
     * Disegna un singolo parcheggio e tutti i suoi spazi interni.
     *
     * @param g          contesto grafico
     * @param map        mappa di riferimento
     * @param parkingLot parcheggio da disegnare
     */
    private void drawParkingLot(@NotNull Graphics2D g, @NotNull JXMapViewer map, @NotNull ParkingLot parkingLot) {
        Point2D topLeft = map.getTileFactory().geoToPixel(parkingLot.getBounds().top(), map.getZoom());
        Point2D bottomRight = map.getTileFactory().geoToPixel(parkingLot.getBounds().bottom(), map.getZoom());

        Rectangle lotRect = calculateViewportRect(topLeft, bottomRight, map.getViewportBounds());

        Color base = parkingLot.getColor();
        Color transparent = new Color(
                base.getRed(),
                base.getGreen(),
                base.getBlue(),
                100
        );

        // Disegna il riempimento semitrasparente dell'area del parcheggio.
        g.setColor(transparent);
        g.fill(new Rectangle2D.Double(lotRect.x, lotRect.y, lotRect.width, lotRect.height));

        // Disegna il bordo principale del parcheggio.
        g.setColor(base);
        g.setStroke(new BasicStroke(2));
        g.draw(new Rectangle2D.Double(lotRect.x, lotRect.y, lotRect.width, lotRect.height));

        // Disegna tutti i posti interni al parcheggio.
        parkingLot.getSpaces().forEach(space -> drawParkingSpace(g, map, space));

        // Se presente, mostra il nome al centro del rettangolo del parcheggio.
        if (parkingLot.getName() != null && !parkingLot.getName().isEmpty()) {
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            FontMetrics fm = g.getFontMetrics();
            String text = parkingLot.getName();

            int textWidth = fm.stringWidth(text);
            int textHeight = fm.getHeight();

            int textX = lotRect.x + (lotRect.width - textWidth) / 2;
            int totalHeight = textHeight + 5;
            int startY = lotRect.y + (lotRect.height - totalHeight) / 2;
            int textY = startY + fm.getAscent();

            g.drawString(text, textX, textY);
        }

    }

    /**
     * Disegna un singolo posto auto.
     *
     * <p>Il colore cambia in base allo stato:</p>
     * <ul>
     *     <li>verde = libero;</li>
     *     <li>giallo = riservato;</li>
     *     <li>rosso = occupato.</li>
     * </ul>
     *
     * @param g            contesto grafico
     * @param mapViewer    mappa di riferimento
     * @param parkingSpace spazio da disegnare
     */
    private void drawParkingSpace(@NotNull Graphics2D g, @NotNull JXMapViewer mapViewer, @NotNull ParkingSpace parkingSpace) {
        Point2D topLeft = mapViewer.getTileFactory().geoToPixel(parkingSpace.getBounds().top(), mapViewer.getZoom());
        Point2D bottomRight = mapViewer.getTileFactory().geoToPixel(parkingSpace.getBounds().bottom(), mapViewer.getZoom());

        Color color = switch (parkingSpace.getStatus()) {
            case ParkingSpaceStatus.Occupied _ -> Color.RED;
            case ParkingSpaceStatus.Reserved _ -> Color.YELLOW;
            default -> Color.GREEN;
        };

        g.setColor(color);
        g.setStroke(new BasicStroke(1));

        Rectangle spaceRect = calculateViewportRect(topLeft, bottomRight, mapViewer.getViewportBounds());

        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 150));
        g.fill(new Rectangle2D.Double(spaceRect.x, spaceRect.y, spaceRect.width, spaceRect.height));

        g.setColor(color);
        g.draw(new Rectangle2D.Double(spaceRect.x, spaceRect.y, spaceRect.width, spaceRect.height));

        // A determinati livelli di zoom mostra anche un'icona identificativa per i posti speciali.
        if (mapViewer.getZoom() <= 12 && parkingSpace.getType() != ParkingSpace.Type.NORMAL) {
            JSVG icon = parkingSpace.getType() == ParkingSpace.Type.ELECTRIC ?
                    JSVG.from(Assets.ZAP_ICON).setColor(Color.YELLOW) :
                    JSVG.from(Assets.ACCESSIBILITY_ICON).setColor(Color.BLUE);

            int iconSize = 18;

            int iconX = spaceRect.x + (spaceRect.width - iconSize) / 2;
            int iconY = spaceRect.y + (spaceRect.height - iconSize) / 2;

            drawSVGIcon(icon, g, iconSize, iconX, iconY);
        }

    }

    /**
     * Disegna un'icona SVG all'interno dell'area indicata.
     *
     * @param svg  icona da disegnare
     * @param g    contesto grafico principale
     * @param size dimensione dell'icona
     * @param x    coordinata x
     * @param y    coordinata y
     */
    private void drawSVGIcon(@NotNull JSVG svg, @NotNull Graphics2D g, int size, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();

        g2.translate(x, y);
        svg.setSize(size, size);
        svg.paint(g2);

        g2.dispose();
    }

    /**
     * Calcola un rettangolo relativo alla viewport partendo da coordinate assolute della mappa.
     *
     * @param topLeft        vertice superiore sinistro
     * @param bottomRight    vertice inferiore destro
     * @param viewportBounds area visibile corrente
     * @return rettangolo convertito nelle coordinate dello schermo
     */
    private @NotNull Rectangle calculateViewportRect(@NotNull Point2D topLeft, @NotNull Point2D bottomRight, @NotNull Rectangle viewportBounds) {
        int x1 = (int) (topLeft.getX() - viewportBounds.getX());
        int y1 = (int) (topLeft.getY() - viewportBounds.getY());
        int x2 = (int) (bottomRight.getX() - viewportBounds.getX());
        int y2 = (int) (bottomRight.getY() - viewportBounds.getY());

        int x = Math.min(x1, x2);
        int y = Math.min(y1, y2);
        int w = Math.abs(x2 - x1);
        int h = Math.abs(y2 - y1);

        return new Rectangle(x, y, w, h);
    }

}