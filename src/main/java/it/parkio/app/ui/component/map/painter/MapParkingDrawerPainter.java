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

public class MapParkingDrawerPainter implements Painter<JXMapViewer> { // Painter per disegno interattivo di parcheggi

    private DrawMode drawMode = DrawMode.none(); // modalità di disegno corrente
    private GeoPosition currentPoint; // punto corrente durante il drag/disegno

    @Override
    public void paint(Graphics2D g, JXMapViewer mapViewer, int width, int height) { // Disegna il rettangolo di selezione
        if (!isDrawing()) return; // esce se non si sta disegnando

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // anti-aliasing

        switch (drawMode) { // gestisce diverse modalità di disegno
            case DrawMode.None _ -> {} // nessun disegno

            case DrawMode.WithOutBounds mode -> { // disegno libero senza bounds predefiniti
                Point2D startPoint = mapViewer.getTileFactory().geoToPixel(mode.startPoint(), mapViewer.getZoom()); // converti geo -> pixel
                Point2D curPoint = mapViewer.getTileFactory().geoToPixel(currentPoint, mapViewer.getZoom());

                Rectangle selection = calculateViewportRect(startPoint, curPoint, mapViewer.getViewportBounds()); // calcola rettangolo viewport

                Color color = mode.color();
                Color transparentColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 100); // colore trasparente

                g.setColor(transparentColor);
                g.fill(selection); // riempi rettangolo
                g.setColor(transparentColor.darker());
                g.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[] { 10f }, 0f)); // tratteggio
                g.draw(selection); // disegna bordo tratteggiato
            }

            case DrawMode.WithBounds mode -> { // disegno con bounds predefiniti
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

                drawRestrictionBounds(g, mapViewer, mapViewer.getViewportBounds(), mode.bounds); // disegna bounds di restrizione
            }
        }
    }

    private void drawRestrictionBounds(@NotNull Graphics2D g, @NotNull JXMapViewer mapViewer, @NotNull Rectangle viewport, @NotNull Bounds bounds) {
        // Disegna i bounds di restrizione come rettangolo semitrasparente
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

    public void startDrawing(GeoPosition startPoint, Color color) { // Inizia disegno libero
        drawMode = DrawMode.basic(startPoint, color);
        currentPoint = startPoint;
    }

    public void startDrawing(GeoPosition startPoint, Color color, Bounds bounds) { // Inizia disegno con bounds predefiniti
        drawMode = DrawMode.withBounds(startPoint, color, bounds);
        currentPoint = startPoint;
    }

    public Optional<Bounds> stopDrawing() { // Termina il disegno e restituisce bounds finali
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

    public boolean isDrawing() { // Controlla se è in corso un disegno
        return !(drawMode instanceof DrawMode.None || currentPoint == null);
    }

    public void update(GeoPosition currentPoint) { // Aggiorna la posizione corrente durante il drag
        if (drawMode instanceof DrawMode.WithBounds mode) {
            this.currentPoint = clampToBounds(currentPoint, mode.bounds()); // limita posizione ai bounds
            return;
        }

        this.currentPoint = currentPoint; // aggiornamento libero
    }

    private void reset() { // Resetta il disegno
        this.drawMode = DrawMode.none();
        this.currentPoint = null;
    }

    private @NotNull GeoPosition clampToBounds(@NotNull GeoPosition pos, @NotNull Bounds bounds) {
        // Limita la GeoPosition ai limiti dei bounds
        double minLat = Math.min(bounds.top().getLatitude(), bounds.bottom().getLatitude());
        double maxLat = Math.max(bounds.top().getLatitude(), bounds.bottom().getLatitude());
        double minLon = Math.min(bounds.top().getLongitude(), bounds.bottom().getLongitude());
        double maxLon = Math.max(bounds.top().getLongitude(), bounds.bottom().getLongitude());

        double lat = Math.max(minLat, Math.min(maxLat, pos.getLatitude()));
        double lon = Math.max(minLon, Math.min(maxLon, pos.getLongitude()));

        return new GeoPosition(lat, lon);
    }

    private @NotNull Rectangle calculateViewportRect(@NotNull Point2D start, @NotNull Point2D end, @NotNull Rectangle viewport) {
        // Calcola rettangolo da disegnare sulla viewport tra due punti
        int x = (int) (Math.min(start.getX(), end.getX()) - viewport.getX());
        int y = (int) (Math.min(start.getY(), end.getY()) - viewport.getY());
        int w = (int) Math.abs(end.getX() - start.getX());
        int h = (int) Math.abs(end.getY() - start.getY());

        return new Rectangle(x, y, w, h);
    }

    private sealed interface DrawMode { // Modalità di disegno: none, libero o con bounds

        static DrawMode none() { // nessun disegno
            return None.INSTANCE;
        }

        @Contract("_, _ -> new")
        static @NotNull DrawMode basic(GeoPosition startPoint, Color color) { // disegno base
            return new WithOutBounds(startPoint, color);
        }

        @Contract("_, _, _ -> new")
        static @NotNull DrawMode withBounds(GeoPosition startPoint, Color color, Bounds bounds) { // disegno con bounds
            return new WithBounds(startPoint, color, bounds);
        }

        record None() implements DrawMode { // Modalità nessuna
            static final None INSTANCE = new None();
        }

        record WithOutBounds(GeoPosition startPoint, Color color) implements DrawMode { // disegno libero senza restrizioni

        }

        record WithBounds(GeoPosition startPoint, Color color, Bounds bounds) implements DrawMode { } // disegno con restrizioni

    }

}