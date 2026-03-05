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

public class ParkingLotDrawerMouseAdapter extends MouseAdapter { // Gestisce input di disegno dei parcheggi con mouse

    private final JXMapViewer mapViewer; // Mappa su cui disegnare
    private final MapParkingDrawerPainter drawerPainter; // Painter che gestisce il disegno interattivo

    private UserInputRequest<Optional<Bounds>> inputBoundsReq; // Riferimento alla richiesta di input corrente

    public ParkingLotDrawerMouseAdapter(JXMapViewer mapViewer, MapParkingDrawerPainter drawerPainter) {
        this.mapViewer = mapViewer; // inizializza mappa
        this.drawerPainter = drawerPainter; // inizializza painter
    }

    @Override
    public void mouseDragged(@NotNull MouseEvent e) { // Evento trascinamento mouse
        if ((e.getModifiersEx() & MouseEvent.BUTTON3_DOWN_MASK) == 0) return; // solo tasto destro
        if (!drawerPainter.isDrawing()) return; // esce se non si sta disegnando

        GeoPosition currentPoint = mapViewer.convertPointToGeoPosition(e.getPoint()); // converte punto pixel -> GeoPosition
        drawerPainter.update(currentPoint); // aggiorna disegno
        mapViewer.repaint(); // ridisegna mappa

        ParkIOFrame.LOGGER.debug("Updated drawerPaint curPos to {}", currentPoint); // log debug
    }

    @Override
    public void mouseReleased(@NotNull MouseEvent e) { // Evento rilascio mouse
        if (e.getButton() != MouseEvent.BUTTON3) return; // solo tasto destro
        if (!drawerPainter.isDrawing()) return; // esce se non si stava disegnando

        inputBoundsReq.complete(drawerPainter.stopDrawing()); // completa la richiesta con i bounds disegnati
        inputBoundsReq = null; // resetta la richiesta

        mapViewer.repaint(); // ridisegna mappa
    }

    public UserInputRequest<Optional<Bounds>> getInputBounds(@NotNull GeoPosition start, @NotNull Color color, @Nullable Bounds bounds) {
        // Crea nuova richiesta di input per bounds parcheggio
        if (inputBoundsReq != null && !inputBoundsReq.isCompleted()) inputBoundsReq.cancel(); // cancella richiesta precedente se attiva

        inputBoundsReq = new UserInputRequest<>(this::cancelInputRequest); // crea nuova richiesta con azione cancel

        if (bounds == null) drawerPainter.startDrawing(start, color); // inizia disegno libero
        else drawerPainter.startDrawing(start, color, bounds); // inizia disegno predefinito (bounds esistente)

        mapViewer.repaint(); // ridisegna mappa

        ParkIOFrame.LOGGER.debug("Started drawing"); // log debug

        return inputBoundsReq; // restituisce la richiesta per callback
    }

    public UserInputRequest<Optional<Bounds>> getInputBounds(@NotNull GeoPosition start, @NotNull Color color) {
        return getInputBounds(start, color, null); // overload senza bounds iniziale
    }

    private void cancelInputRequest() { // Cancella richiesta input attiva
        if (inputBoundsReq != null && !inputBoundsReq.isCompleted()) {
            inputBoundsReq.cancel(); // cancella la richiesta
            inputBoundsReq = null; // resetta riferimento
        }

        drawerPainter.stopDrawing(); // ferma disegno
        mapViewer.repaint(); // ridisegna mappa
    }

}