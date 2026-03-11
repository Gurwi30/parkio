package it.parkio.app.ui.component.map.painter;

import it.parkio.app.model.Bounds;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Optional;

/**
 * Painter che mostra visivamente il rettangolo in fase di disegno sulla mappa.
 *
 * <p>Viene usato quando l'utente sta creando un nuovo parcheggio
 * o un nuovo posto auto trascinando il mouse.</p>
 */
public class MapParkingDrawerPainter implements Painter<JXMapViewer> {

    /**
     * Modalità di disegno corrente.
     *
     * <p>Può essere assente, libera o vincolata a bounds preesistenti.</p>
     */
    private DrawMode drawMode = DrawMode.none();

    /**
     * Punto corrente raggiunto durante il drag.
     */
    private GeoPosition currentPoint;

    /**
     * Disegna il rettangolo temporaneo di selezione, se un disegno è in corso.
     *
     * @param g         contesto grafico
     * @param mapViewer mappa di riferimento
     * @param width     larghezza disponibile
     * @param height    altezza disponibile
     */
    @Override
    public void paint(Graphics2D g, JXMapViewer mapViewer, int width, int height) {
        if (!isDrawing()) return;

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        switch (drawMode) {
            case DrawMode.None _ -> {
                // Nessun disegno attivo: non viene renderizzato nulla.
            }

            case DrawMode.WithOutBounds mode -> {
                Point2D startPoint = mapViewer.getTileFactory().geoToPixel(mode.startPoint(), mapViewer.getZoom());
                Point2D curPoint = mapViewer.getTileFactory().geoToPixel(currentPoint, mapViewer.getZoom());

                Rectangle selection = calculateViewportRect(startPoint, curPoint, mapViewer.getViewportBounds());

                Color color = mode.color();
                Color transparentColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 100);

                // Riempimento semitrasparente dell'area selezionata.
                g.setColor(transparentColor);
                g.fill(selection);

                // Bordo tratteggiato per far capire che il rettangolo è temporaneo.
                g.setColor(transparentColor.darker());
                g.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[] { 10f }, 0f));
                g.draw(selection);
            }

            case DrawMode.WithBounds mode -> {
                Point2D startPoint = mapViewer.getTileFactory().geoToPixel(mode.startPoint(), mapViewer.getZoom());
                Point2D curPoint = mapViewer.getTileFactory().geoToPixel(currentPoint, mapViewer.getZoom());

                Rectangle selection = calculateViewportRect(startPoint, curPoint, mapViewer.getViewportBounds());

                Color color = mode.color();
                Color transparentColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 100);

                g.setColor(transparentColor);
                g.fill(selection);

                g.setColor(transparentColor.darker());
                g.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[] { 10.0f }, 0.0f));
                g.draw(selection);

                // Disegna anche i limiti entro cui il rettangolo può essere tracciato.
                drawRestrictionBounds(g, mapViewer, mapViewer.getViewportBounds(), mode.bounds);
            }
        }
    }

    /**
     * Disegna un rettangolo di riferimento che rappresenta i bounds massimi consentiti.
     *
     * @param g         contesto grafico
     * @param mapViewer mappa di riferimento
     * @param viewport  area visibile corrente
     * @param bounds    limiti geografici da mostrare
     */
    private void drawRestrictionBounds(@NotNull Graphics2D g, @NotNull JXMapViewer mapViewer, @NotNull Rectangle viewport, @NotNull Bounds bounds) {
        Point2D topLeft = mapViewer.getTileFactory().geoToPixel(bounds.top(), mapViewer.getZoom());
        Point2D bottomRight = mapViewer.getTileFactory().geoToPixel(bounds.bottom(), mapViewer.getZoom());

        int x = (int) (Math.min(topLeft.getX(), bottomRight.getX()) - viewport.getX());
        int y = (int) (Math.min(topLeft.getY(), bottomRight.getY()) - viewport.getY());
        int w = (int) Math.abs(bottomRight.getX() - topLeft.getX());
        int h = (int) Math.abs(bottomRight.getY() - topLeft.getY());

        g.setColor(new Color(255, 100, 100, 50));
        g.setStroke(new BasicStroke(1));
        g.drawRect(x, y, w, h);
    }

    /**
     * Avvia un disegno libero senza vincoli.
     *
     * @param startPoint punto iniziale geografico
     * @param color      colore del rettangolo temporaneo
     */
    public void startDrawing(GeoPosition startPoint, Color color) {
        drawMode = DrawMode.basic(startPoint, color);
        currentPoint = startPoint;
    }

    /**
     * Avvia un disegno vincolato entro bounds specifici.
     *
     * @param startPoint punto iniziale
     * @param color      colore del disegno
     * @param bounds     area massima consentita
     */
    public void startDrawing(GeoPosition startPoint, Color color, Bounds bounds) {
        drawMode = DrawMode.withBounds(startPoint, color, bounds);
        currentPoint = startPoint;
    }

    /**
     * Termina il disegno e restituisce i bounds finali, se validi.
     *
     * @return eventuali bounds risultanti
     */
    public Optional<Bounds> stopDrawing() {
        if (drawMode instanceof DrawMode.None || currentPoint == null) return Optional.empty();

        GeoPosition startPos = switch (drawMode) {
            case DrawMode.WithOutBounds mode -> mode.startPoint;
            case DrawMode.WithBounds mode -> mode.startPoint;
            default -> null;
        };

        if (startPos == null) return Optional.empty();

        Bounds bounds = new Bounds(startPos, currentPoint);
        reset();

        return Optional.of(bounds);
    }

    /**
     * Indica se un disegno è attualmente in corso.
     *
     * @return {@code true} se il painter sta gestendo un rettangolo attivo
     */
    public boolean isDrawing() {
        return !(drawMode instanceof DrawMode.None || currentPoint == null);
    }

    /**
     * Aggiorna il punto corrente del disegno.
     *
     * <p>Se il disegno è vincolato a bounds, il punto viene "bloccato"
     * automaticamente entro i limiti consentiti.</p>
     *
     * @param currentPoint nuova posizione corrente
     */
    public void update(GeoPosition currentPoint) {
        if (drawMode instanceof DrawMode.WithBounds mode) {
            this.currentPoint = clampToBounds(currentPoint, mode.bounds());
            return;
        }

        this.currentPoint = currentPoint;
    }

    /**
     * Ripristina lo stato del painter alla modalità inattiva.
     */
    private void reset() {
        this.drawMode = DrawMode.none();
        this.currentPoint = null;
    }

    /**
     * Limita una posizione geografica all'interno dei bounds forniti.
     *
     * @param pos    posizione da limitare
     * @param bounds limiti geografici consentiti
     * @return posizione eventualmente corretta
     */
    private @NotNull GeoPosition clampToBounds(@NotNull GeoPosition pos, @NotNull Bounds bounds) {
        double minLat = Math.min(bounds.top().getLatitude(), bounds.bottom().getLatitude());
        double maxLat = Math.max(bounds.top().getLatitude(), bounds.bottom().getLatitude());
        double minLon = Math.min(bounds.top().getLongitude(), bounds.bottom().getLongitude());
        double maxLon = Math.max(bounds.top().getLongitude(), bounds.bottom().getLongitude());

        double lat = Math.max(minLat, Math.min(maxLat, pos.getLatitude()));
        double lon = Math.max(minLon, Math.min(maxLon, pos.getLongitude()));

        return new GeoPosition(lat, lon);
    }

    /**
     * Converte due punti assoluti della mappa in un rettangolo relativo alla viewport visibile.
     *
     * @param start    punto iniziale in pixel assoluti
     * @param end      punto finale in pixel assoluti
     * @param viewport viewport attuale della mappa
     * @return rettangolo da disegnare sullo schermo
     */
    private @NotNull Rectangle calculateViewportRect(@NotNull Point2D start, @NotNull Point2D end, @NotNull Rectangle viewport) {
        int x = (int) (Math.min(start.getX(), end.getX()) - viewport.getX());
        int y = (int) (Math.min(start.getY(), end.getY()) - viewport.getY());
        int w = (int) Math.abs(end.getX() - start.getX());
        int h = (int) Math.abs(end.getY() - start.getY());

        return new Rectangle(x, y, w, h);
    }

    /**
     * Modello interno che rappresenta lo stato del disegno corrente.
     *
     * <p>Usare una sealed interface qui evita flag sparsi
     * e rende il codice del painter più espressivo.</p>
     */
    private sealed interface DrawMode {

        /**
         * Modalità inattiva.
         *
         * @return oggetto che rappresenta "nessun disegno"
         */
        static DrawMode none() {
            return None.INSTANCE;
        }

        /**
         * Modalità di disegno libero.
         *
         * @param startPoint punto iniziale
         * @param color      colore del rettangolo
         * @return nuova modalità libera
         */
        @Contract("_, _ -> new")
        static @NotNull DrawMode basic(GeoPosition startPoint, Color color) {
            return new WithOutBounds(startPoint, color);
        }

        /**
         * Modalità di disegno vincolata a bounds.
         *
         * @param startPoint punto iniziale
         * @param color      colore del rettangolo
         * @param bounds     limiti consentiti
         * @return nuova modalità vincolata
         */
        @Contract("_, _, _ -> new")
        static @NotNull DrawMode withBounds(GeoPosition startPoint, Color color, Bounds bounds) {
            return new WithBounds(startPoint, color, bounds);
        }

        /**
         * Modalità senza disegno attivo.
         */
        record None() implements DrawMode {
            static final None INSTANCE = new None();
        }

        /**
         * Modalità di disegno libero, senza limiti.
         *
         * @param startPoint punto iniziale del drag
         * @param color      colore del rettangolo
         */
        record WithOutBounds(GeoPosition startPoint, Color color) implements DrawMode {

        }

        /**
         * Modalità di disegno vincolata entro un'area predefinita.
         *
         * @param startPoint punto iniziale del drag
         * @param color      colore del rettangolo
         * @param bounds     limiti massimi consentiti
         */
        record WithBounds(GeoPosition startPoint, Color color, Bounds bounds) implements DrawMode { }

    }

}