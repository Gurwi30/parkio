package it.parkio.app.ui.component.overlay;

import it.parkio.app.manager.ParkingLotsManager;
import it.parkio.app.ui.component.map.MapComponent;

import javax.swing.*;
import java.awt.*;

public class ParkingLotsListComponent extends JOverlayPanel {

    private final MapComponent map; // riferimento al componente mappa a cui è associata la lista
    private final ParkingLotsManager lotsManager; // manager dei parcheggi per popolare la lista

    public ParkingLotsListComponent(MapComponent map, ParkingLotsManager lotsManager) {
        this.map = map; // assegna la mappa
        this.lotsManager = lotsManager; // assegna il manager

        setLayout(new GridLayout()); // layout a griglia per far occupare l'intero spazio a un singolo componente
        setOpaque(false); // pannello trasparente per overlay

        initComponents(); // inizializza componenti UI
    }

    private void initComponents() {
        add(new JLabel("Parking Lots List Component"), BorderLayout.CENTER); // label segnaposto
        // qui si possono aggiungere JLists, JButton o JTable per gestire e mostrare i parcheggi
    }

}