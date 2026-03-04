package it.parkio.app.ui.component.overlay;

import it.parkio.app.manager.ParkingLotsManager;

import javax.swing.*;
import java.awt.*;

public class ParkingLotsListComponent extends JOverlayPanel {

    private final ParkingLotsManager lotsManager;

    public ParkingLotsListComponent(ParkingLotsManager lotsManager) {
        this.lotsManager = lotsManager;

        setLayout(new GridLayout());
        setOpaque(false);

        initComponents();
    }

    private void initComponents() {
        add(new JLabel("Parking Lots List Component"), BorderLayout.CENTER);
    }

}
