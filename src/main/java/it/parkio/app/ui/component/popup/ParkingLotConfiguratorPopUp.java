package it.parkio.app.ui.component.popup;

import it.parkio.app.manager.ParkingLotsManager;
import it.parkio.app.model.Bounds;
import it.parkio.app.ui.component.widget.ColorsWatchPickerComponent;
import org.jetbrains.annotations.NotNull;
import org.jxmapviewer.JXMapViewer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Finestra pop-up che appare quando l'utente disegna un nuovo parcheggio sulla mappa.
 * Permette di assegnare un nome e un colore al parcheggio prima di crearlo.
 *
 * <p>Estende JFrame: è una finestra indipendente (non un pannello dentro un'altra finestra).</p>
 */
public class ParkingLotConfiguratorPopUp extends JFrame {

    /** Larghezza fissa della finestra in pixel. */
    private static final int WIDTH  = 380;
    /** Altezza fissa della finestra in pixel. */
    private static final int HEIGHT = 460;

    /** Campo di testo dove l'utente scrive il nome del parcheggio. */
    private JTextField nameFld;

    /** Etichetta rossa "Il nome è obbligatorio", nascosta finché non serve. */
    private JLabel nameError;

    /** Selettore visivo per scegliere il colore del parcheggio. */
    private ColorsWatchPickerComponent colorPicker;

    /** Gestore dei parcheggi: verrà chiamato per creare effettivamente il parcheggio. */
    private final ParkingLotsManager lotManager;

    /** La mappa: dopo la creazione verrà ridisegnata per mostrare il nuovo parcheggio. */
    private final JXMapViewer mapViewer;

    /**
     * I confini geografici del parcheggio che l'utente ha appena disegnato sulla mappa
     * (angolo nord-ovest e sud-est, per esempio).
     */
    private final Bounds bounds;

    /**
     * Costruttore: riceve i dati del parcheggio da creare, imposta la finestra
     * e la rende subito visibile.
     *
     * @param bounds     i confini geografici disegnati sulla mappa
     * @param lotManager il gestore dei parcheggi
     * @param mapViewer  la mappa su cui ridisegnare dopo la creazione
     */
    public ParkingLotConfiguratorPopUp(Bounds bounds, ParkingLotsManager lotManager, JXMapViewer mapViewer) {
        this.lotManager = lotManager;
        this.mapViewer  = mapViewer;
        this.bounds     = bounds;

        setTitle("Nuovo Parcheggio");
        setSize(WIDTH, HEIGHT);
        setResizable(false);               // l'utente non può ridimensionare la finestra
        setLocationRelativeTo(null);       // centra la finestra nello schermo
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // chiudendo la finestra, la distrugge (libera memoria)

        // Pannello radice con sfondo scuro e margini interni
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(28, 28, 32));
        root.setBorder(new EmptyBorder(24, 24, 20, 24));
        setContentPane(root); // sostituiamo il pannello di default della finestra con il nostro

        root.add(buildForm(),    BorderLayout.CENTER); // form con nome e colore al centro
        root.add(buildButtons(), BorderLayout.SOUTH);  // bottoni in basso

        setVisible(true); // rendiamo visibile la finestra
    }

    /**
     * Costruisce il form con il campo nome e il selettore colore.
     *
     * @return il pannello con il form
     */
    private @NotNull JPanel buildForm() {
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS)); // elementi impilati in verticale

        // Titolo grande in cima alla finestra
        JLabel title = new JLabel("Nuovo Parcheggio");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setForeground(new Color(230, 230, 235));
        title.setAlignmentX(LEFT_ALIGNMENT);
        form.add(title);
        form.add(Box.createRigidArea(new Dimension(0, 20))); // spazio vuoto di 20px

        // Etichetta "Nome *"
        JLabel nameLabel = new JLabel("Nome *");
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 13f));
        nameLabel.setForeground(new Color(180, 180, 190));
        nameLabel.setAlignmentX(LEFT_ALIGNMENT);
        form.add(nameLabel);
        form.add(Box.createRigidArea(new Dimension(0, 6)));

        // Campo di testo per il nome, con stile scuro
        nameFld = new JTextField();
        nameFld.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        nameFld.setPreferredSize(new Dimension(0, 38));
        nameFld.setFont(nameFld.getFont().deriveFont(14f));
        nameFld.setBackground(new Color(42, 42, 50));
        nameFld.setForeground(new Color(220, 220, 228));
        nameFld.setCaretColor(new Color(220, 220, 228)); // colore del cursore lampeggiante
        nameFld.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(65, 65, 78), 1, true),
                new EmptyBorder(0, 10, 0, 10)));
        nameFld.setAlignmentX(LEFT_ALIGNMENT);

        // Cambiamo il bordo del campo quando riceve/perde il focus (feedback visivo per l'utente)
        nameFld.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                // Campo attivo → bordo blu
                nameFld.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(13, 110, 253), 1, true),
                        new EmptyBorder(0, 10, 0, 10)));
            }
            @Override public void focusLost(FocusEvent e) {
                // Campo non attivo → bordo grigio
                nameFld.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(65, 65, 78), 1, true),
                        new EmptyBorder(0, 10, 0, 10)));
            }
        });
        form.add(nameFld);

        // Messaggio d'errore, nascosto di default (setVisible(false))
        nameError = new JLabel("Il nome è obbligatorio");
        nameError.setFont(nameError.getFont().deriveFont(Font.ITALIC, 11f));
        nameError.setForeground(new Color(220, 53, 69)); // rosso
        nameError.setAlignmentX(LEFT_ALIGNMENT);
        nameError.setVisible(false); // invisibile finché non si tenta di salvare senza nome
        form.add(Box.createRigidArea(new Dimension(0, 4)));
        form.add(nameError);
        form.add(Box.createRigidArea(new Dimension(0, 20)));

        // Etichetta "Colore"
        JLabel colorLabel = new JLabel("Colore");
        colorLabel.setFont(colorLabel.getFont().deriveFont(Font.BOLD, 13f));
        colorLabel.setForeground(new Color(180, 180, 190));
        colorLabel.setAlignmentX(LEFT_ALIGNMENT);
        form.add(colorLabel);
        form.add(Box.createRigidArea(new Dimension(0, 10)));

        // Selettore colore senza colore pre-selezionato (costruttore con solo la dimensione dei pallini)
        colorPicker = new ColorsWatchPickerComponent(80);
        colorPicker.setAlignmentX(LEFT_ALIGNMENT);
        colorPicker.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        form.add(colorPicker);

        return form;
    }

    /**
     * Costruisce la barra con i due bottoni "Annulla" e "Crea".
     *
     * @return il pannello con i bottoni
     */
    private @NotNull JPanel buildButtons() {
        // GridLayout(1, 2, 12, 0) → 1 riga, 2 colonne, 12px di spazio orizzontale tra i bottoni
        JPanel bar = new JPanel(new GridLayout(1, 2, 12, 0));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(20, 0, 0, 0)); // spazio sopra i bottoni

        // "Annulla" → chiude la finestra senza fare nulla (dispose() distrugge la finestra)
        JButton abortBtn = makeButton("Annulla", new Color(180, 40, 50), new Color(210, 55, 65));
        abortBtn.addActionListener(_ -> dispose());

        // "Crea" → chiama create() per validare e creare il parcheggio
        JButton createBtn = makeButton("Crea", new Color(13, 90, 210), new Color(13, 110, 253));
        createBtn.addActionListener(_ -> create());

        bar.add(abortBtn);
        bar.add(createBtn);
        return bar;
    }

    /**
     * Crea un bottone personalizzato con sfondo colorato e effetto hover.
     * Il disegno è interamente manuale (sovrascrittura di paintComponent)
     * per ottenere angoli arrotondati e colori che non dipendono dallo stile del sistema operativo.
     *
     * @param text  il testo del bottone
     * @param base  il colore normale
     * @param hover il colore quando il mouse ci passa sopra
     * @return il bottone pronto all'uso
     */
    private @NotNull JButton makeButton(String text, Color base, Color hover) {
        JButton btn = new JButton(text) {
            // Colore attuale: parte da "base" e cambia durante l'hover
            private Color current = base;

            {
                // Blocco di inizializzazione anonimo: eseguito alla creazione dell'oggetto
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        current = hover; repaint(); // mouse sopra → colore più chiaro
                    }
                    @Override
                    public void mouseExited(MouseEvent e)  {
                        current = base;  repaint(); // mouse via → colore normale
                    }
                });
            }

            /** Disegniamo il bottone a mano invece di usare l'aspetto nativo di sistema. */
            @Override
            protected void paintComponent(@NotNull Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(current);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12); // sfondo con angoli arrotondati
                g2.setColor(Color.WHITE);
                g2.setFont(getFont().deriveFont(Font.BOLD, 14f));
                FontMetrics fm = g2.getFontMetrics();
                // Calcoliamo le coordinate per centrare il testo nel bottone
                int tx = (getWidth()  - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), tx, ty);
                g2.dispose(); // liberiamo sempre il contesto grafico dopo l'uso
            }
        };
        btn.setFont(btn.getFont().deriveFont(Font.BOLD, 14f));
        btn.setPreferredSize(new Dimension(0, 44));
        btn.setContentAreaFilled(false); // disabilitiamo il riempimento standard di Swing
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return btn;
    }

    /**
     * Chiamato quando l'utente preme "Crea".
     * Valida il nome, crea il parcheggio tramite il gestore e chiude la finestra.
     */
    private void create() {
        // trim() rimuove gli spazi iniziali e finali (es. "  Parcheggio  " → "Parcheggio")
        String name = nameFld.getText().trim();

        // Validazione: il nome non può essere vuoto
        if (name.isEmpty()) {
            nameError.setVisible(true); // mostriamo il messaggio d'errore
            nameFld.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(220, 53, 69), 1, true), // bordo rosso
                    new EmptyBorder(0, 10, 0, 10)));
            nameFld.requestFocus(); // spostiamo il cursore nel campo del nome
            return; // usciamo senza creare
        }

        // Tutto ok: creiamo il parcheggio con nome, confini geografici e colore scelto
        lotManager.createParkingLot(name, bounds, colorPicker.getSelectedColor());
        mapViewer.repaint(); // aggiorniamo la mappa per mostrare il nuovo parcheggio
        dispose();           // chiudiamo e distruggiamo questa finestra
    }

}