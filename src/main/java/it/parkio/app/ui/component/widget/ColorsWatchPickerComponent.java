package it.parkio.app.ui.component.widget;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

/**
 * Widget grafico per selezionare un colore da una griglia di 12 preset.
 *
 * <p>Mostra una griglia di 3×4 "pallini" colorati (swatch).
 * Facoltativamente, sul lato destro, mostra un rettangolo di anteprima
 * con il colore attualmente selezionato.</p>
 *
 * <p>Supporta listener: altri componenti possono registrarsi per essere
 * notificati ogni volta che l'utente cambia colore.</p>
 */
public class ColorsWatchPickerComponent extends JPanel {

    /**
     * I 12 colori preset disponibili nella griglia.
     * Sono definiti come costante statica: appartengono alla classe, non alle singole istanze.
     */
    private static final Color[] PRESETS = {
            new Color(220,  53,  69),  // rosso
            new Color(253, 126,  20),  // arancione
            new Color(255, 193,   7),  // giallo
            new Color( 40, 167,  69),  // verde
            new Color( 23, 162, 184),  // azzurro
            new Color( 13, 110, 253),  // blu
            new Color(111,  66, 193),  // viola
            new Color(232,  62, 140),  // rosa
            new Color( 33,  37,  41),  // quasi nero
            new Color(108, 117, 125),  // grigio
            new Color(248, 249, 250),  // quasi bianco
            new Color(102, 204, 153)   // verde acqua
    };

    /**
     * Insieme di funzioni da chiamare quando l'utente seleziona un colore.
     * Consumer<Color> è una funzione che riceve un Color e non restituisce nulla.
     * Set<> evita duplicati se lo stesso listener venisse registrato più volte.
     */
    private final Set<Consumer<Color>> listeners = new HashSet<>();

    /**
     * Larghezza in pixel del rettangolo di anteprima a destra.
     * Se vale 0, l'anteprima non viene mostrata.
     */
    private final int previewWidth;

    /** Il pannello "swatch" attualmente selezionato (per potergli rimuovere il bordo bianco). */
    private JPanel selectedSwatch = null;

    /** Il pannello di anteprima del colore, o null se previewWidth == 0. */
    private JPanel previewPanel = null;

    /** Il colore attualmente selezionato. */
    private Color selectedColor;

    /**
     * Costruttore completo: specifica colore iniziale e larghezza dell'anteprima.
     *
     * @param selectedColor il colore pre-selezionato all'apertura
     * @param previewWidth  larghezza del rettangolo di anteprima (0 = nessuna anteprima)
     */
    public ColorsWatchPickerComponent(Color selectedColor, int previewWidth) {
        this.selectedColor = selectedColor;
        this.previewWidth  = previewWidth;

        setOpaque(false);
        setLayout(new BorderLayout(10, 0)); // 10px di spazio tra griglia e anteprima

        initComponents();
    }

    /**
     * Costruttore senza colore iniziale: ne viene scelto uno casuale tra i preset.
     * Comodo quando non importa quale colore parta selezionato.
     *
     * @param previewWidth larghezza del rettangolo di anteprima
     */
    public ColorsWatchPickerComponent(int previewWidth) {
        // ThreadLocalRandom: generatore di numeri casuali thread-safe e performante
        // nextInt(PRESETS.length) restituisce un intero tra 0 (incluso) e PRESETS.length (escluso)
        this(PRESETS[ThreadLocalRandom.current().nextInt(PRESETS.length)], previewWidth);
    }

    /**
     * Costruisce e aggiunge la griglia dei colori e (se richiesta) l'anteprima.
     */
    private void initComponents() {
        add(buildGrid(), BorderLayout.CENTER); // griglia occupa lo spazio centrale

        if (previewWidth > 0) {
            // Pannello di anteprima: un rettangolo colorato che mostra il colore selezionato.
            // Sovrascriviamo paintComponent per disegnarlo a mano con angoli arrotondati.
            previewPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(selectedColor); // usa sempre il colore correntemente selezionato
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                    g2.dispose();
                }
            };

            // Larghezza fissa definita dal costruttore; altezza 0 = si adatta al genitore
            previewPanel.setPreferredSize(new Dimension(previewWidth, 0));
            previewPanel.setOpaque(false);
            add(previewPanel, BorderLayout.EAST); // anteprima incollata a destra della griglia
        }
    }

    /**
     * Costruisce la griglia 3×4 di swatch (pallini colorati cliccabili).
     *
     * @return il pannello con la griglia
     */
    private @NotNull JPanel buildGrid() {
        // GridLayout(3, 4, 8, 8): 3 righe, 4 colonne, 8px di spazio orizzontale e verticale tra le celle
        JPanel grid = new JPanel(new GridLayout(3, 4, 8, 8));
        grid.setOpaque(false);

        for (Color preset : PRESETS) {

            // Per ogni colore creiamo un pannello quadrato che si disegna come cerchio arrotondato
            JPanel swatch = new JPanel() {
                @Override
                protected void paintComponent(@NotNull Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(preset);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.dispose();
                }
            };

            swatch.setOpaque(false);
            swatch.setPreferredSize(new Dimension(30, 30)); // ogni swatch è 30×30 pixel
            swatch.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            // Tooltip testuale nativo con il codice esadecimale del colore (es. "#FF5733")
            swatch.setToolTipText(String.format("#%02X%02X%02X",
                    preset.getRed(), preset.getGreen(), preset.getBlue()));

            // Al click, selezioniamo questo colore
            swatch.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    selectColor(preset, swatch);
                }
            });

            grid.add(swatch);

            // Se questo è il colore pre-selezionato, aggiungiamo subito il bordo bianco
            // invokeLater garantisce che il bordo venga applicato DOPO che Swing ha terminato
            // il layout corrente (altrimenti potrebbe essere ignorato o sovrascritto)
            if (preset.equals(selectedColor)) {
                selectedSwatch = swatch;
                SwingUtilities.invokeLater(() -> swatch.setBorder(new LineBorder(Color.WHITE, 2, true)));
            }
        }

        return grid;
    }

    /**
     * Aggiorna la selezione: rimuove il bordo dal vecchio swatch, lo aggiunge al nuovo,
     * aggiorna l'anteprima e notifica tutti i listener.
     *
     * @param color  il nuovo colore selezionato
     * @param swatch il pannello swatch cliccato
     */
    private void selectColor(Color color, JPanel swatch) {
        selectedColor = color;

        // Rimuoviamo il bordo bianco dal precedente swatch selezionato
        if (selectedSwatch != null) {
            selectedSwatch.setBorder(null);
            selectedSwatch.repaint();
        }

        // Aggiorniamo il riferimento al swatch selezionato e aggiungiamo il bordo bianco
        selectedSwatch = swatch;
        swatch.setBorder(new LineBorder(Color.WHITE, 2, true));
        swatch.repaint();

        // Se l'anteprima esiste, la ridisegniamo con il nuovo colore
        if (previewPanel != null) previewPanel.repaint();

        // Notifichiamo tutti i listener registrati passando il nuovo colore
        // forEach con lambda: per ogni Consumer<Color> l, chiamiamo l.accept(color)
        listeners.forEach(l -> l.accept(color));
    }

    /**
     * Restituisce il colore attualmente selezionato.
     *
     * @return il colore selezionato
     */
    public Color getSelectedColor() { return selectedColor; }

    /**
     * Registra un listener da chiamare ogni volta che il colore cambia.
     * Il listener è una funzione che riceve il nuovo colore come parametro.
     *
     * <p>Esempio di utilizzo:</p>
     * <pre>
     *     picker.addColorListener(color -> System.out.println("Colore scelto: " + color));
     * </pre>
     *
     * @param listener la funzione da chiamare al cambio colore
     */
    public void addColorListener(Consumer<Color> listener) {
        listeners.add(listener);
    }

}