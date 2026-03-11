package it.parkio.app.ui.component.map.listener;

import it.parkio.app.ParkIO;
import it.parkio.app.event.ParkingLotSelectEvent;
import it.parkio.app.manager.ParkingLotsManager;
import it.parkio.app.model.ParkingLot;
import it.parkio.app.model.ParkingSpace;
import it.parkio.app.ui.component.popup.ParkingLotConfiguratorPopUp;
import it.parkio.app.ui.component.popup.ParkingSpaceDetailPopUp;
import it.parkio.app.ui.component.tooltip.ParkingSpaceTooltipComponent;
import org.jetbrains.annotations.NotNull;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;

/**
 * Ascoltatore principale degli eventi del mouse sulla mappa.
 * Coordina tutte le interazioni dell'utente: click sui parcheggi, click sugli spazi,
 * disegno di nuovi parcheggi/spazi, e visualizzazione del tooltip.
 *
 * <p>Estende MouseAdapter invece di implementare MouseListener: MouseAdapter è una
 * classe astratta che fornisce implementazioni vuote di tutti i metodi dell'interfaccia,
 * così sovrascriviamo solo quelli che ci interessano (mousePressed e mouseMoved).</p>
 */
public class MapListener extends MouseAdapter {

    /** La mappa su cui stiamo ascoltando gli eventi. */
    private final JXMapViewer mapViewer;

    /**
     * Gestore del disegno di nuovi parcheggi/spazi tramite drag del mouse.
     * Viene delegato a questo oggetto quando l'utente clicca col tasto destro.
     */
    private final ParkingLotDrawerMouseAdapter drawerMouseAdapter;

    /** Gestore dei parcheggi: da qui otteniamo la lista di tutti i parcheggi esistenti. */
    private final ParkingLotsManager lotsManager;

    /**
     * Tooltip che appare quando il mouse passa sopra uno spazio.
     * È un JPanel sovrapposto alla mappa (non una finestra separata).
     */
    private final ParkingSpaceTooltipComponent parkingSpaceTooltip = new ParkingSpaceTooltipComponent();

    /**
     * Costruttore: riceve i collaboratori, aggiunge il tooltip alla mappa
     * e imposta il layout a null (necessario per posizionare il tooltip
     * con coordinate assolute tramite setBounds).
     *
     * @param mapViewer           la mappa
     * @param drawerMouseAdapter  il gestore del disegno
     * @param lotsManager         il gestore dei parcheggi
     */
    public MapListener(@NotNull JXMapViewer mapViewer, ParkingLotDrawerMouseAdapter drawerMouseAdapter, ParkingLotsManager lotsManager) {
        this.mapViewer = mapViewer;
        this.drawerMouseAdapter = drawerMouseAdapter;
        this.lotsManager = lotsManager;

        // null layout: i componenti figli vengono posizionati con setBounds(x, y, w, h)
        // invece di essere gestiti automaticamente da un LayoutManager
        mapViewer.setLayout(null);
        // Aggiungiamo il tooltip come figlio della mappa così galleggia sopra di essa
        mapViewer.add(parkingSpaceTooltip);
    }

    /**
     * Chiamato quando l'utente preme un tasto del mouse sulla mappa.
     *
     * <p><b>Tasto sinistro (BUTTON1):</b></p>
     * <ul>
     *   <li>Se il click è su uno spazio → apre il pop-up di dettaglio dello spazio.</li>
     *   <li>Se il click è su un parcheggio (ma non su uno spazio) → seleziona il parcheggio.</li>
     *   <li>Se il click è nel vuoto → ripristina il cursore normale.</li>
     * </ul>
     *
     * <p><b>Tasto destro (BUTTON3):</b></p>
     * <ul>
     *   <li>Se il click è dentro un parcheggio → avvia il disegno di un nuovo spazio.</li>
     *   <li>Se il click è nel vuoto → avvia il disegno di un nuovo parcheggio.</li>
     * </ul>
     */
    @Override
    public void mousePressed(@NotNull MouseEvent e) {
        // Convertiamo il punto in pixel (coordinate schermo) in coordinate geografiche
        // (latitudine/longitudine) per poter confrontarlo con i bounds dei parcheggi
        GeoPosition position = mapViewer.convertPointToGeoPosition(e.getPoint());

        // --- Tasto sinistro ---
        if (e.getButton() == MouseEvent.BUTTON1) {
            // Cerchiamo se il click è dentro un parcheggio
            // ifPresentOrElse: esegue il primo blocco se l'Optional ha un valore,
            // il secondo blocco (il ramo "else") se è vuoto
            getParkingLot(position).ifPresentOrElse(
                    parkingLot -> {
                        // Siamo dentro un parcheggio: cerchiamo se siamo anche dentro uno spazio
                        getParkingSpace(parkingLot, position)
                                .ifPresentOrElse(
                                        // Click su uno spazio → apriamo il pop-up di dettaglio
                                        space -> new ParkingSpaceDetailPopUp(mapViewer, space),
                                        // Click sul parcheggio ma non su uno spazio → selezioniamo il parcheggio
                                        () -> ParkIO.EVENT_MANAGER.call(new ParkingLotSelectEvent(parkingLot))
                                );

                        // In entrambi i casi, siamo su un'area cliccabile → cursore a mano
                        mapViewer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    },

                    // Click nel vuoto (fuori da qualsiasi parcheggio)
                    () -> {
                        // Se il cursore era già a mano (da un hover precedente), lo resettiamo
                        if (mapViewer.getCursor().getType() == Cursor.HAND_CURSOR) {
                            mapViewer.setCursor(Cursor.getDefaultCursor());
                        }
                    }
            );
        }

        // --- Tasto destro ---
        if (e.getButton() == MouseEvent.BUTTON3) {
            // Usiamo stream().filter().findFirst() per cercare il primo parcheggio
            // che contiene la posizione del click
            lotsManager.getParkingLots().stream()
                    .filter(lot -> lot.getBounds().contains(position))
                    .findFirst()
                    .ifPresentOrElse(
                            // Click dentro un parcheggio → disegniamo un nuovo spazio al suo interno
                            // Il colore suggerito per il disegno è il colore del parcheggio reso più scuro
                            lot -> drawerMouseAdapter.getInputBounds(position, lot.getColor().darker().darker(), lot.getBounds())
                                    // onInput: callback chiamata quando l'utente finisce di disegnare
                                    // bounds è un Optional<Bounds>: se presente (disegno completato), aggiungiamo lo spazio
                                    .onInput(bounds -> bounds.ifPresent(b -> lot.addParkingSpace(b, ParkingSpace.Type.NORMAL))),

                            // Click nel vuoto → disegniamo un nuovo parcheggio (colore ciano di default)
                            () -> drawerMouseAdapter.getInputBounds(position, Color.CYAN).onInput(bounds ->
                                    // Se l'utente ha completato il disegno (non annullato), apriamo il configuratore
                                    bounds.ifPresent(b -> new ParkingLotConfiguratorPopUp(b, lotsManager, mapViewer))
                            )
                    );
        }
    }

    /**
     * Chiamato continuamente mentre l'utente muove il mouse sulla mappa (senza premere tasti).
     *
     * <p>Gestisce due comportamenti:</p>
     * <ul>
     *   <li>Aggiorna il cursore (mano sopra i parcheggi, freccia nel vuoto).</li>
     *   <li>Mostra/nasconde il tooltip quando il mouse passa sopra uno spazio.</li>
     * </ul>
     */
    @Override
    public void mouseMoved(@NotNull MouseEvent e) {
        GeoPosition position = mapViewer.convertPointToGeoPosition(e.getPoint());

        getParkingLot(position).ifPresentOrElse(
                parkingLot -> {
                    // Siamo sopra un parcheggio: controlliamo se anche sopra uno spazio
                    getParkingSpace(parkingLot, position)
                            .ifPresentOrElse(
                                    // Mouse sopra uno spazio → mostriamo il tooltip in quella posizione
                                    space -> parkingSpaceTooltip.showTooltip(space, e.getPoint()),
                                    // Mouse sopra il parcheggio ma non su uno spazio → nascondiamo il tooltip
                                    () -> {
                                        if (parkingSpaceTooltip.isVisible()) parkingSpaceTooltip.hideTooltip();
                                    }
                            );

                    // In ogni caso, sopra un parcheggio il cursore è a mano
                    mapViewer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                },

                // Mouse nel vuoto: nascondiamo il tooltip e resettiamo il cursore
                () -> {
                    if (parkingSpaceTooltip.isVisible()) parkingSpaceTooltip.hideTooltip();

                    if (mapViewer.getCursor().getType() == Cursor.HAND_CURSOR) {
                        mapViewer.setCursor(Cursor.getDefaultCursor());
                    }
                }
        );
    }

    /**
     * Cerca il parcheggio che contiene la posizione geografica indicata.
     *
     * <p>Usa l'API Stream di Java: prende la lista di tutti i parcheggi,
     * la filtra tenendo solo quelli i cui bounds contengono il punto,
     * e restituisce il primo trovato (o Optional.empty() se nessuno corrisponde).</p>
     *
     * @param clickedPosition la posizione geografica da cercare
     * @return Optional con il parcheggio trovato, o vuoto se il punto è nel vuoto
     */
    private @NotNull Optional<ParkingLot> getParkingLot(@NotNull GeoPosition clickedPosition) {
        return lotsManager.getParkingLots().stream()
                .filter(lot -> lot.getBounds().contains(clickedPosition))
                .findFirst();
    }

    /**
     * Cerca lo spazio di parcheggio (all'interno di un parcheggio dato) che contiene
     * la posizione geografica indicata.
     *
     * <p>Stessa logica di {@link #getParkingLot}: stream → filter → findFirst.</p>
     *
     * @param parkingLot      il parcheggio in cui cercare
     * @param clickedPosition la posizione geografica da cercare
     * @return Optional con lo spazio trovato, o vuoto se nessuno spazio contiene il punto
     */
    private @NotNull Optional<ParkingSpace> getParkingSpace(@NotNull ParkingLot parkingLot, @NotNull GeoPosition clickedPosition) {
        return parkingLot.getSpaces().stream()
                .filter(lot -> lot.getBounds().contains(clickedPosition))
                .findFirst();
    }

}