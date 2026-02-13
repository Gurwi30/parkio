package it.parkio.app.ui;

import it.parkio.app.manager.ParkingLotsManager;
import it.parkio.app.ui.component.map.MapComponent;
import it.parkio.app.ui.component.windowcontrol.WindowControlComponent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class ParkIOFrame extends JFrame {

    public static final int CORNER_RADIUS = 12;

    private final ParkingLotsManager lotsManager;

    public ParkIOFrame(ParkingLotsManager lotsManager) {
        this.lotsManager = lotsManager;

        setTitle("ParkIO");
        setSize(800, 600);

        setUndecorated(true);
        setBackground(new Color(0,0,0,0));
        setShape(new RoundRectangle2D.Double(0,0, getWidth(), getHeight(),CORNER_RADIUS,CORNER_RADIUS));

        JPanel mainPanel = new JPanel(new BorderLayout());

        mainPanel.setOpaque(false);
        setContentPane(mainPanel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initComponents(mainPanel);
    }

    private void initComponents(@NotNull JPanel panel) {
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(panel.getSize());

        MapComponent map = new MapComponent();
        map.setBounds(0, 0, getWidth(), getHeight());
        layeredPane.add(map, JLayeredPane.DEFAULT_LAYER);

        WindowControlComponent toolbar = new WindowControlComponent(this);
        toolbar.setBounds(0, 0, getWidth(), toolbar.getPreferredSize().height);
        layeredPane.add(toolbar, JLayeredPane.PALETTE_LAYER);

        panel.add(layeredPane, BorderLayout.CENTER);
    }

}
