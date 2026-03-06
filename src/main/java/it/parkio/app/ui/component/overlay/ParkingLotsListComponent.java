package it.parkio.app.ui.component.overlay;

import it.parkio.app.manager.ParkingLotsManager;
import it.parkio.app.ui.component.map.MapComponent;
import org.jxmapviewer.JXMapViewer;
import it.parkio.app.model.ParkingLot;
import it.parkio.app.manager.Park;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ParkingLotsListComponent extends JOverlayPanel {

    private final MapComponent map;
    private final ParkingLotsManager m_lotsManager;
    private JList<ParkingLot> m_parksList;
    private JButton m_delParkBtn;
    private DefaultListModel lots;

    public ParkingLotsListComponent(MapComponent map, ParkingLotsManager lotsManager) {
        this.map = map;
        this.m_lotsManager = lotsManager;

        setLayout(new BorderLayout());
        setOpaque(false);

        initComponents();
    }

    private void initComponents() {
        // add(new JLabel("Parking Lots List Component"), BorderLayout.CENTER);

        lots = m_lotsManager.GetModel();
        m_parksList = new JList<ParkingLot>(lots);
        m_delParkBtn = new JButton("Elimina parcheggio");
        m_delParkBtn.addActionListener(this::RemovePark);
        add(m_parksList, BorderLayout.CENTER);
        add(m_delParkBtn, BorderLayout.SOUTH);
    }

    private void RemovePark(ActionEvent ev)
    {
        ParkingLot pl = m_parksList.getSelectedValue();
        if (pl != null) {
            m_lotsManager.removeParkingLot(pl);
            MapComponent.Repaint();
            // TODO: repaint
        }
    }

}
