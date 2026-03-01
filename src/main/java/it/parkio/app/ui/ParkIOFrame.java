package it.parkio.app.ui;

import it.parkio.app.manager.ParkingLotsManager;
import it.parkio.app.ui.component.map.MapComponent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

public class ParkIOFrame extends JFrame {

    protected static final Logger LOGGER = LoggerFactory.getLogger(ParkIOFrame.class);

    private final ParkingLotsManager lotsManager;

    public ParkIOFrame(ParkingLotsManager lotsManager) {
        this.lotsManager = lotsManager;

        setTitle("ParkIO");
        setSize(1240, 700);

        JPanel mainPanel = new JPanel(new BorderLayout());

        mainPanel.setOpaque(false);
        setContentPane(mainPanel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initComponents(mainPanel);
    }

    private void initComponents(@NotNull JPanel panel) {
        panel.add(new MapComponent(lotsManager), BorderLayout.CENTER);
    }

    public void showOnTop() {
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);
        setVisible(true);
        toFront();
        requestFocus();
        setAlwaysOnTop(false);

        revalidate();
        repaint();
    }

}
