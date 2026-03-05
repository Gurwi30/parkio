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

public class ParkIOFrame extends JFrame { // finestra principale dell'app ParkIO

    public static final Logger LOGGER = LoggerFactory.getLogger(ParkIOFrame.class); // logger statico

    private final ParkingLotsManager lotsManager; // manager dei parcheggi

    public ParkIOFrame(ParkingLotsManager lotsManager) {
        this.lotsManager = lotsManager;

        setTitle("ParkIO"); // titolo finestra
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/assets/logo.png"))); // icona finestra
        setSize(1240, 700); // dimensioni iniziali

        JPanel mainPanel = new JPanel(new BorderLayout()); // pannello principale con BorderLayout
        mainPanel.setOpaque(false); // trasparente
        setContentPane(mainPanel); // setta il contenuto della JFrame

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // chiusura finestra termina app

        initComponents(mainPanel); // inizializza componenti
    }

    public void showOnTop() { // mostra la finestra al centro e in primo piano
        setLocationRelativeTo(null); // centra sullo schermo
        setAlwaysOnTop(true); // temporaneamente sempre in primo piano
        setVisible(true); // mostra finestra
        toFront(); // porta davanti
        requestFocus(); // richiede focus
        setAlwaysOnTop(false); // ripristina comportamento normale

        revalidate(); // aggiorna layout
        repaint(); // ridisegna
    }

    private void initComponents(@NotNull JPanel panel) { // aggiunge componenti alla finestra
        JLayeredPane layeredPane = new JLayeredPane() { // pannello a livelli per sovrapporre mappe e overlay
            @Override
            public void doLayout() { // layout dinamico dei componenti
                Component map = getComponent(getComponentCount() - 1); // ultimo componente = mappa
                map.setBounds(0, 0, getWidth(), getHeight()); // mappa occupa tutto il layered pane

                int padding = 16; // padding dai bordi
                int panelWidth = 260; // larghezza overlay
                int panelHeight = getHeight() - (padding * 2); // altezza overlay

                getComponent(1).setBounds(padding, padding, panelWidth, panelHeight); // lista parcheggi
                getComponent(0).setBounds(getWidth() - panelWidth - padding, padding, panelWidth, panelHeight); // gestione parcheggio
            }
        };

        MapComponent mapComponent = new MapComponent(lotsManager); // crea componente mappa
        layeredPane.add(mapComponent, JLayeredPane.DEFAULT_LAYER); // aggiunge mappa come layer base

        layeredPane.add(new ParkingLotsListComponent(mapComponent, lotsManager), JLayeredPane.PALETTE_LAYER); // overlay lista parcheggi
        layeredPane.add(new ParkingLotManageComponent(null), JLayeredPane.PALETTE_LAYER); // overlay gestione parcheggio

        panel.add(layeredPane, BorderLayout.CENTER); // aggiunge layeredPane al pannello principale
    }

}