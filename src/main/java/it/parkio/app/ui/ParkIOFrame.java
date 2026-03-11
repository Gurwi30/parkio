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

/**
 * Finestra principale dell'applicazione.
 *
 * <p>Si occupa di costruire il contenitore generale della UI
 * e di posizionare i componenti principali:</p>
 * <ul>
 *     <li>la mappa centrale;</li>
 *     <li>la lista dei parcheggi;</li>
 *     <li>il pannello di gestione del parcheggio selezionato.</li>
 * </ul>
 */
public class ParkIOFrame extends JFrame {

    /**
     * Logger dedicato alla finestra principale.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(ParkIOFrame.class);

    /**
     * Gestore dei parcheggi condiviso con i componenti della UI.
     */
    private final ParkingLotsManager lotsManager;

    /**
     * Costruisce la finestra principale e configura le proprietà base.
     *
     * @param lotsManager gestore dei parcheggi da mostrare e modificare
     */
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

    /**
     * Mostra la finestra portandola in primo piano.
     *
     * <p>Il trucco con {@code setAlwaysOnTop(true)} aiuta a garantire
     * che la finestra venga davvero visualizzata sopra le altre
     * al momento dell'avvio.</p>
     */
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

    /**
     * Inizializza i componenti grafici principali della finestra.
     *
     * <p>Viene usato un {@link JLayeredPane} per sovrapporre i pannelli laterali
     * alla mappa, mantenendo comunque la mappa come elemento di sfondo.</p>
     *
     * @param panel pannello principale della finestra
     */
    private void initComponents(@NotNull JPanel panel) {
        JLayeredPane layeredPane = new JLayeredPane() {
            @Override
            public void doLayout() {
                // L'ultimo componente inserito nello stack viene trattato come mappa di sfondo.
                Component map = getComponent(getComponentCount() - 1);
                map.setBounds(0, 0, getWidth(), getHeight());

                int padding = 16;
                int panelWidth = 260;
                int panelHeight = getHeight() - (padding * 2);

                // Posiziona i pannelli laterali sopra la mappa.
                getComponent(1).setBounds(padding, padding, panelWidth, panelHeight);
                getComponent(0).setBounds(getWidth() - panelWidth - padding, padding, panelWidth, panelHeight);
            }
        };

        // Aggiunge la mappa come layer di base.
        layeredPane.add(new MapComponent(lotsManager), JLayeredPane.DEFAULT_LAYER);

        // Aggiunge i pannelli overlay nei layer superiori.
        layeredPane.add(new ParkingLotsListComponent(lotsManager), JLayeredPane.PALETTE_LAYER);
        layeredPane.add(new ParkingLotManageComponent(lotsManager), JLayeredPane.PALETTE_LAYER);

        panel.add(layeredPane, BorderLayout.CENTER);
    }

}
