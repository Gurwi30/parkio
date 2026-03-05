package it.parkio.app.ui;

import it.parkio.app.manager.ParkingLotsManager;
import it.parkio.app.ui.component.map.MapComponent;
import it.parkio.app.ui.component.overlay.ParkingLotManageComponent;
import it.parkio.app.ui.component.overlay.ParkingLotsListComponent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

public class ParkIOFrame extends JFrame {

    public static final Logger LOGGER = LoggerFactory.getLogger(ParkIOFrame.class);

    private final ParkingLotsManager lotsManager;

    public ParkIOFrame(ParkingLotsManager lotsManager) {
        this.lotsManager = lotsManager;

        setTitle("ParkIO");
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/assets/logo.png")));
        setSize(1240, 700);

        JPanel mainPanel = new JPanel(new BorderLayout());

        mainPanel.setOpaque(false);
        setContentPane(mainPanel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initComponents(mainPanel);
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

    private void initComponents(@NotNull JPanel panel) {
        JLayeredPane layeredPane = new JLayeredPane() {
            @Override
            public void doLayout() {
                Component map = getComponent(getComponentCount() - 1);
                map.setBounds(0, 0, getWidth(), getHeight());

                int padding = 16;
                int panelWidth = 260;
                int panelHeight = getHeight() - (padding * 2);

                getComponent(1).setBounds(padding, padding, panelWidth, panelHeight);
                getComponent(0).setBounds(getWidth() - panelWidth - padding, padding, panelWidth, panelHeight);
            }
        };

        layeredPane.add(new MapComponent(lotsManager), JLayeredPane.DEFAULT_LAYER);

        layeredPane.add(new ParkingLotsListComponent(lotsManager), JLayeredPane.PALETTE_LAYER);
        layeredPane.add(new ParkingLotManageComponent(null), JLayeredPane.PALETTE_LAYER);

        panel.add(layeredPane, BorderLayout.CENTER);
    }

}
