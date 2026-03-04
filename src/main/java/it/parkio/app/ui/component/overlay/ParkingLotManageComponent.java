package it.parkio.app.ui.component.overlay;

import it.parkio.app.model.ParkingLot;

import javax.swing.*;
import java.awt.*;

public class ParkingLotManageComponent extends JOverlayPanel {

    private final ParkingLot parkingLot;

    public ParkingLotManageComponent(ParkingLot parkingLot) {
        this.parkingLot = parkingLot;

        setLayout(new GridLayout());
        setOpaque(false);

        initComponents();
    }

    private void initComponents() {
        add(new JLabel("Parking Lots Manage Component"), BorderLayout.CENTER);
    }

}
