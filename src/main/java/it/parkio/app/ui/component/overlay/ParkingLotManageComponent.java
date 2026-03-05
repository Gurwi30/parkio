package it.parkio.app.ui.component.overlay;

import it.parkio.app.model.ParkingLot;

import javax.swing.*;
import java.awt.*;

public class ParkingLotManageComponent extends JOverlayPanel {

    private final ParkingLot parkingLot; // riferimento al parcheggio gestito dal pannello

    public ParkingLotManageComponent(ParkingLot parkingLot) {
        this.parkingLot = parkingLot; // assegna il parcheggio passato

        setLayout(new GridLayout()); // imposta layout a griglia (un singolo componente occupa tutto)
        setOpaque(false); // pannello trasparente per vedere overlay sotto

        initComponents(); // inizializza componenti UI
    }

    private void initComponents() {
        add(new JLabel("Parking Lots Manage Component"), BorderLayout.CENTER); // label segnaposto
    }

}