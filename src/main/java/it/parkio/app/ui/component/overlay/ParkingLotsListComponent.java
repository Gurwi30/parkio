package it.parkio.app.ui.component.overlay;

import it.parkio.app.manager.ParkingLotsManager;
import it.parkio.app.ui.component.map.MapComponent;
import org.jxmapviewer.JXMapViewer;

import javax.swing.*;
import java.awt.*;

public class ParkingLotsListComponent extends JOverlayPanel {

    private final MapComponent map;
    private final ParkingLotsManager lotsManager;

    public ParkingLotsListComponent(MapComponent map, ParkingLotsManager lotsManager) {
        this.map = map;
        this.lotsManager = lotsManager;

        setLayout(new GridLayout());
        setOpaque(false);

        initComponents();
    }

    private void initComponents() {
        add(new JLabel("Parking Lots List Component"), BorderLayout.CENTER);
    }

}
