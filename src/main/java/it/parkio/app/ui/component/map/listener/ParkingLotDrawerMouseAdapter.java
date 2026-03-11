package it.parkio.app.ui.component.map.listener;

import it.parkio.app.model.Bounds;
import it.parkio.app.object.UserInputRequest;
import it.parkio.app.ui.ParkIOFrame;
import it.parkio.app.ui.component.map.painter.MapParkingDrawerPainter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;

/**
 * Gestisce il disegno interattivo di rettangoli sulla mappa usando il tasto destro.
 *
 * <p>Questa classe non decide il significato del rettangolo disegnato:
 * si limita a raccogliere l'input grafico dell'utente e a restituire
 * i bounds risultanti tramite una {@link UserInputRequest}.</p>
 */
public class ParkingLotDrawerMouseAdapter extends MouseAdapter {

    /**
     * Mappa su cui avviene il disegno.
     */
    private final JXMapViewer mapViewer;

    /**
     * Painter responsabile del rendering visivo del rettangolo in fase di disegno.
     */
    private final MapParkingDrawerPainter drawerPainter;

    /**
     * Richiesta di input attualmente attiva.
     *
     * <p>È valorizzata mentre l'utente sta tracciando un'area
     * e viene completata o annullata alla fine dell'operazione.</p>
     */
    private UserInputRequest<Optional<Bounds>> inputBoundsReq;

    /**
     * Costruttore.
     *
     * @param mapViewer     mappa su cui disegnare
     * @param drawerPainter painter che visualizza il rettangolo temporaneo
     */
    public ParkingLotDrawerMouseAdapter(JXMapViewer mapViewer, MapParkingDrawerPainter drawerPainter) {
        this.mapViewer = mapViewer;
        this.drawerPainter = drawerPainter;
    }

    /**
     * Aggiorna il rettangolo mentre l'utente trascina il mouse con il tasto destro premuto.
     *
     * @param e evento di drag
     */
    @Override
    public void mouseDragged(@NotNull MouseEvent e) {
        // Considera solo il trascinamento effettuato con il tasto destro realmente premuto.
        if ((e.getModifiersEx() & MouseEvent.BUTTON3_DOWN_MASK) == 0) return;
        if (!drawerPainter.isDrawing()) return;

        GeoPosition currentPoint = mapViewer.convertPointToGeoPosition(e.getPoint());
        drawerPainter.update(currentPoint);
        mapViewer.repaint();

        ParkIOFrame.LOGGER.debug("Updated drawerPaint curPos to {}", currentPoint);
    }

    /**
     * Conclude il disegno quando l'utente rilascia il tasto destro.
     *
     * <p>I bounds finali vengono inviati alla richiesta di input attiva.</p>
     *
     * @param e evento di rilascio del mouse
     */
    @Override
    public void mouseReleased(@NotNull MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON3) return;
        if (!drawerPainter.isDrawing()) return;

        inputBoundsReq.complete(drawerPainter.stopDrawing());
        inputBoundsReq = null;

        mapViewer.setCursor(Cursor.getDefaultCursor());
        mapViewer.repaint();
    }

    /**
     * Avvia una nuova richiesta di disegno.
     *
     * <p>Se esiste già una richiesta precedente non conclusa, viene annullata.
     * Se vengono passati dei bounds di restrizione, il disegno resterà confinato
     * all'interno di quell'area.</p>
     *
     * @param start  punto iniziale del disegno
     * @param color  colore con cui visualizzare l'area temporanea
     * @param bounds eventuali limiti entro cui vincolare il disegno
     * @return richiesta che verrà completata con i bounds finali
     */
    public UserInputRequest<Optional<Bounds>> getInputBounds(@NotNull GeoPosition start, @NotNull Color color, @Nullable Bounds bounds) {
        if (inputBoundsReq != null && !inputBoundsReq.isCompleted()) inputBoundsReq.cancel();

        inputBoundsReq = new UserInputRequest<>(this::cancelInputRequest);

        if (bounds == null) drawerPainter.startDrawing(start, color);
        else drawerPainter.startDrawing(start, color, bounds);

        mapViewer.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        mapViewer.repaint();

        return inputBoundsReq;
    }

    /**
     * Variante semplificata senza bounds di restrizione.
     *
     * @param start punto iniziale
     * @param color colore del disegno
     * @return richiesta di input attiva
     */
    public UserInputRequest<Optional<Bounds>> getInputBounds(@NotNull GeoPosition start, @NotNull Color color) {
        return getInputBounds(start, color, null);
    }

    /**
     * Annulla la richiesta corrente e ripulisce lo stato grafico associato al disegno.
     */
    private void cancelInputRequest() {
        if (inputBoundsReq != null && !inputBoundsReq.isCompleted()) {
            inputBoundsReq.cancel();
            inputBoundsReq = null;
        }

        drawerPainter.stopDrawing();

        mapViewer.setCursor(Cursor.getDefaultCursor());
        mapViewer.repaint();
    }

}