package it.parkio.app.ui.component.overlay;

import it.parkio.app.ParkIO;
import it.parkio.app.event.ParkingLotSelectEvent;
import it.parkio.app.model.ParkingLot;
import it.parkio.app.ui.ParkIOFrame;

import javax.swing.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class ParkingLotManageComponent extends JOverlayPanel {

    private @NotNull ParkingLot parkingLot;

    public ParkingLotManageComponent() {
        setLayout(new GridLayout());
        setOpaque(false);

        ParkIO.EVENT_MANAGER.register(ParkingLotSelectEvent.class, event -> {
            setParkingLot(event.selectedParkingLot());
            ParkIOFrame.LOGGER.debug("ParkingLotSelectEvent called!");
        });

        initComponents();
    }

    private void initComponents() {
        if (parkingLot == null) {
            add(new JLabel("Parking Lots Manage Component"), BorderLayout.CENTER);
            return;
        }

        add(new JLabel(parkingLot.getName()));
    }

    private void setParkingLot(@Nullable ParkingLot parkingLot) {
        this.parkingLot = parkingLot;

        removeAll();
        initComponents();
        revalidate();
        repaint();
    }

}
