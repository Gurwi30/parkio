package it.parkio.app.ui.component.popup;

import it.parkio.app.model.ParkingSpace;
import it.parkio.app.model.ParkingSpaceStatus;
import it.parkio.app.scheduler.ParkingSpaceScheduler;
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
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Finestra pop-up che mostra e permette di modificare lo stato di uno spazio di parcheggio.
 *
 * <p>Lo stato può essere:</p>
 * <ul>
 *   <li><b>FREE</b> – libero: nessun campo aggiuntivo richiesto.</li>
 *   <li><b>OCCUPIED</b> – occupato: richiede targa, orario di inizio e (opzionale) orario di fine.</li>
 *   <li><b>RESERVED</b> – riservato: richiede targa, orario di inizio e orario di fine (obbligatorio).</li>
 * </ul>
 *
 * <p>Il form si aggiorna dinamicamente ogni volta che l'utente cambia lo stato nel menu a tendina.</p>
 */
public class ParkingSpaceDetailPopUp extends JFrame {

    /** Larghezza minima della finestra in pixel. */
    private static final int WIDTH = 360;

    /**
     * Formattatore per le date: converte tra testo ("dd/MM/yyyy HH:mm:ss") e oggetti Instant.
     * ZoneId.systemDefault() usa il fuso orario del computer dove gira l'app.
     */
    private static final DateTimeFormatter FMT = DateTimeFormatter
            .ofPattern("dd/MM/yyyy HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    /** La mappa, da ridisegnare dopo il salvataggio per aggiornare il colore dello spazio. */
    private final JXMapViewer mapViewer;

    /** Lo spazio di parcheggio che stiamo modificando. */
    private final ParkingSpace space;

    // --- Componenti del form (dichiarati come campi per poterli leggere in trySave()) ---

    /** Menu a tendina per scegliere lo stato: FREE, OCCUPIED o RESERVED. */
    private JComboBox<String> statusCombo;

    /** Campo testo per la targa del veicolo. */
    private JTextField plateField;

    /** Campo testo per la data/ora di inizio occupazione o prenotazione. */
    private JTextField startField;

    /** Campo testo per la data/ora di fine (opzionale per OCCUPIED, obbligatoria per RESERVED). */
    private JTextField endField;

    /** Messaggi d'errore per ciascun campo (invisibili finché non fallisce la validazione). */
    private JLabel plateError;
    private JLabel startError;
    private JLabel endError;

    /** Pannello dinamico che contiene i campi targa/inizio/fine: viene svuotato e ricostruito
     *  ogni volta che l'utente cambia lo stato nel menu a tendina. */
    private JPanel detailPanel;

    /**
     * Costruttore: riceve lo spazio da modificare, costruisce l'interfaccia e la mostra.
     *
     * @param mapViewer la mappa da aggiornare dopo il salvataggio
     * @param space     lo spazio di parcheggio da visualizzare/modificare
     */
    public ParkingSpaceDetailPopUp(JXMapViewer mapViewer, @NotNull ParkingSpace space) {
        this.mapViewer = mapViewer;
        this.space = space;

        setTitle("Spazio #" + space.getId());
        setResizable(false);
        setLocationRelativeTo(null);       // centra la finestra nello schermo
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // chiudendo si libera la memoria

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(28, 28, 32));
        root.setBorder(new EmptyBorder(24, 24, 20, 24));
        setContentPane(root);

        root.add(buildForm(),    BorderLayout.CENTER);
        root.add(buildButtons(), BorderLayout.SOUTH);

        // pack() ridimensiona la finestra in modo che si adatti esattamente al contenuto,
        // invece di usare dimensioni fisse come nel pop-up del parcheggio
        pack();
        setMinimumSize(new Dimension(WIDTH, getHeight())); // imponiamo una larghezza minima
        setVisible(true);
    }

    /**
     * Costruisce la parte superiore del form: titolo, tipo dello spazio,
     * menu a tendina dello stato e il pannello dinamico dei dettagli.
     *
     * @return il pannello con il form
     */
    private @NotNull JPanel buildForm() {
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        // Titolo "Spazio #N"
        JLabel title = new JLabel("Spazio #" + space.getId());
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        title.setForeground(new Color(230, 230, 235));
        title.setAlignmentX(LEFT_ALIGNMENT);
        form.add(title);
        form.add(Box.createRigidArea(new Dimension(0, 4)));

        // Sottotitolo con il tipo dello spazio (es. "STANDARD", "DISABLED"…)
        JLabel typeLbl = new JLabel(space.getType().name());
        typeLbl.setFont(typeLbl.getFont().deriveFont(Font.PLAIN, 11f));
        typeLbl.setForeground(new Color(120, 120, 135));
        typeLbl.setAlignmentX(LEFT_ALIGNMENT);
        form.add(typeLbl);
        form.add(Box.createRigidArea(new Dimension(0, 18)));

        // Etichetta "Stato"
        JLabel statusLabel = new JLabel("Stato");
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 12f));
        statusLabel.setForeground(new Color(180, 180, 190));
        statusLabel.setAlignmentX(LEFT_ALIGNMENT);
        form.add(statusLabel);
        form.add(Box.createRigidArea(new Dimension(0, 5)));

        // Menu a tendina con i tre stati possibili
        statusCombo = new JComboBox<>(new String[]{"FREE", "OCCUPIED", "RESERVED"});
        // Selezioniamo lo stato attuale dello spazio (getIdentifier() restituisce la stringa "FREE" ecc.)
        statusCombo.setSelectedItem(space.getStatus().getIdentifier());
        statusCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        statusCombo.setAlignmentX(LEFT_ALIGNMENT);
        styleCombo(statusCombo);
        form.add(statusCombo);
        form.add(Box.createRigidArea(new Dimension(0, 14)));

        // Pannello dinamico: viene ricostruito ogni volta che cambia la selezione nel menu
        detailPanel = new JPanel();
        detailPanel.setOpaque(false);
        detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));
        detailPanel.setAlignmentX(LEFT_ALIGNMENT);
        form.add(detailPanel);

        // Costruiamo subito i campi per lo stato corrente, poi aggiungiamo il listener
        buildDetailFields();
        // Ogni volta che l'utente cambia stato nel menu, ricostuiamo i campi
        statusCombo.addActionListener(_ -> buildDetailFields());

        return form;
    }

    /**
     * Ricostruisce i campi dinamici (targa, inizio, fine) in base allo stato selezionato.
     *
     * <p>Se lo stato è FREE, i campi vengono rimossi (non servono).
     * Se è OCCUPIED o RESERVED, vengono aggiunti e pre-compilati con i dati esistenti,
     * se lo spazio aveva già quei dati.</p>
     */
    private void buildDetailFields() {
        detailPanel.removeAll(); // puliamo i vecchi campi prima di ricostruire

        String selected = (String) statusCombo.getSelectedItem();

        // Se lo stato è FREE non servono campi aggiuntivi
        if ("FREE".equals(selected)) {
            detailPanel.revalidate();
            detailPanel.repaint();
            pack(); // ridimensioniamo la finestra (ora è più piccola)
            return;
        }

        boolean isOccupied = "OCCUPIED".equals(selected);
        boolean isReserved = "RESERVED".equals(selected);

        // Valori pre-esistenti: li estraiamo dallo stato attuale se il tipo corrisponde
        String existingPlate = null;
        String existingStart = null;
        String existingEnd   = null;

        // "instanceof ... occ" è il pattern matching di Java: se lo stato è di tipo Occupied,
        // lo assegniamo direttamente alla variabile "occ" per usarlo subito
        if (isOccupied && space.getStatus() instanceof ParkingSpaceStatus.Occupied occ) {
            existingPlate = occ.getCarPlate();
            existingStart = FMT.format(occ.getStart()); // convertiamo Instant → stringa
            // occ.getEnd() restituisce un Optional: se presente formattiamo, altrimenti stringa vuota
            existingEnd = occ.getEnd().map(FMT::format).orElse("");
        } else if (isReserved && space.getStatus() instanceof ParkingSpaceStatus.Reserved res) {
            existingPlate = res.getCarPlate();
            existingStart = FMT.format(res.getStart());
            existingEnd   = FMT.format(res.getEnd()); // per RESERVED la fine è sempre presente
        }

        // --- Campo Targa ---
        JLabel plateLabel = styledLabel("Targa");
        detailPanel.add(plateLabel);
        detailPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        // Pre-compiliamo con la targa esistente, o campo vuoto se è un nuovo inserimento
        plateField = styledTextField(existingPlate != null ? existingPlate : "");
        detailPanel.add(plateField);
        plateError = errorLabel("La targa è obbligatoria");
        detailPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        detailPanel.add(plateError);
        detailPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // --- Campo Data Inizio ---
        JLabel startLabel = styledLabel("Inizio  (dd/MM/yyyy HH:mm:ss)");
        detailPanel.add(startLabel);
        detailPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        // Pre-compiliamo con la data esistente, o con l'ora attuale se è un nuovo inserimento
        startField = styledTextField(existingStart != null ? existingStart : FMT.format(Instant.now()));
        detailPanel.add(startField);
        startError = errorLabel("Formato data non valido");
        detailPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        detailPanel.add(startError);
        detailPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // --- Campo Data Fine ---
        // L'etichetta segnala se è opzionale (OCCUPIED) o obbligatoria (RESERVED)
        JLabel endLabel = styledLabel(isReserved
                ? "Fine  (dd/MM/yyyy HH:mm:ss)"
                : "Fine  (dd/MM/yyyy HH:mm:ss, opzionale)");
        detailPanel.add(endLabel);
        detailPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        endField = styledTextField(existingEnd != null ? existingEnd : "");
        detailPanel.add(endField);
        endError = errorLabel("Formato data non valido");
        detailPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        detailPanel.add(endError);

        detailPanel.revalidate();
        detailPanel.repaint();
        pack(); // ridimensioniamo la finestra per contenere i nuovi campi
    }

    /**
     * Costruisce la barra con i bottoni "Annulla" e "Salva".
     *
     * @return il pannello con i bottoni
     */
    private @NotNull JPanel buildButtons() {
        JPanel bar = new JPanel(new GridLayout(1, 2, 12, 0));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(20, 0, 0, 0));

        JButton cancelBtn = makeButton("Annulla", new Color(180, 40, 50), new Color(210, 55, 65));
        cancelBtn.addActionListener(_ -> dispose()); // chiude senza salvare

        JButton saveBtn = makeButton("Salva", new Color(13, 90, 210), new Color(13, 110, 253));
        saveBtn.addActionListener(_ -> trySave()); // tenta il salvataggio con validazione

        bar.add(cancelBtn);
        bar.add(saveBtn);
        return bar;
    }

    /**
     * Valida i campi inseriti dall'utente e, se tutto è corretto, aggiorna lo stato dello spazio.
     *
     * <p>Per ogni campo non valido mostriamo il messaggio d'errore corrispondente
     * e non salviamo finché non sono tutti corretti.</p>
     */
    private void trySave() {
        String selected = (String) statusCombo.getSelectedItem();

        // Caso semplice: stato FREE → nessun campo da validare
        if ("FREE".equals(selected)) {
            space.updateStatus(ParkingSpaceStatus.free());
            dispose();
            return;
        }

        // Flag che diventa false se anche solo un campo è invalido
        boolean valid = true;

        // --- Validazione targa ---
        String plate = plateField.getText().trim();
        if (plate.isEmpty()) {
            // Rendiamo visibile il messaggio d'errore (era trasparente)
            plateError.setForeground(new Color(220, 53, 69));
            valid = false;
        } else {
            plateError.setForeground(new Color(0, 0, 0, 0)); // alpha=0 → trasparente (nascosto)
        }

        // --- Validazione data inizio ---
        Instant start = null;
        try {
            // FMT.parse() prova a convertire la stringa in un Instant
            // Se il formato è sbagliato, lancia un'eccezione che gestiamo nel catch
            start = FMT.parse(startField.getText().trim(), Instant::from);
            startError.setForeground(new Color(0, 0, 0, 0)); // nessun errore
        } catch (Exception e) {
            startError.setForeground(new Color(220, 53, 69)); // data non valida → errore rosso
            valid = false;
        }

        // --- Validazione data fine ---
        Instant end = null;
        String endText = endField.getText().trim();
        // Per RESERVED la data di fine è obbligatoria, per OCCUPIED è facoltativa
        boolean endRequired = "RESERVED".equals(selected);

        if (!endText.isEmpty()) {
            // L'utente ha scritto qualcosa: proviamo a interpretarlo come data
            try {
                end = FMT.parse(endText, Instant::from);
                endError.setForeground(new Color(0, 0, 0, 0));
            } catch (Exception e) {
                endError.setForeground(new Color(220, 53, 69));
                valid = false;
            }
        } else if (endRequired) {
            // Campo vuoto ma obbligatorio (stato RESERVED)
            endError.setForeground(new Color(220, 53, 69));
            valid = false;
        } else {
            // Campo vuoto ma facoltativo (stato OCCUPIED): nessun errore
            endError.setForeground(new Color(0, 0, 0, 0));
        }

        // Se c'è almeno un errore, non salviamo e lasciamo i messaggi rossi visibili
        if (!valid) return;

        // Tutti i campi sono validi: aggiorniamo lo stato dello spazio nel modello dati
        if ("OCCUPIED".equals(selected)) {
            // Se l'utente ha inserito anche la fine, la includiamo; altrimenti usiamo il costruttore senza fine
            space.updateStatus(end != null
                    ? ParkingSpaceStatus.occupied(plate, start, end)
                    : ParkingSpaceStatus.occupied(plate, start));
        } else {
            // RESERVED richiede sempre la fine (già validata sopra)
            space.updateStatus(ParkingSpaceStatus.reserved(plate, start, end));
        }

        // Programmiamo eventuali azioni future sullo spazio (es. liberarlo automaticamente alla scadenza)
        ParkingSpaceScheduler.schedule(space, mapViewer);

        mapViewer.repaint(); // aggiorniamo la mappa per riflettere il nuovo stato
        dispose();
    }

    /**
     * Crea un'etichetta con lo stile standard usato per i titoli dei campi del form.
     *
     * @param text il testo dell'etichetta
     * @return l'etichetta stilizzata
     */
    private @NotNull JLabel styledLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 11f));
        lbl.setForeground(new Color(160, 160, 175));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    /**
     * Crea un'etichetta d'errore, inizialmente invisibile (colore completamente trasparente).
     * Viene resa visibile impostando un colore rosso quando la validazione fallisce.
     *
     * @param text il testo del messaggio d'errore
     * @return l'etichetta d'errore
     */
    private @NotNull JLabel errorLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(lbl.getFont().deriveFont(Font.ITALIC, 11f));
        lbl.setForeground(new Color(0, 0, 0, 0)); // alpha=0 → completamente trasparente
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    /**
     * Crea un campo di testo con lo stile scuro dell'applicazione,
     * con cambio di bordo al focus (blu quando attivo, grigio quando non attivo).
     *
     * @param value il testo iniziale da pre-compilare nel campo
     * @return il campo di testo stilizzato
     */
    private @NotNull JTextField styledTextField(String value) {
        JTextField tf = new JTextField(value);
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        tf.setPreferredSize(new Dimension(WIDTH - 48, 32)); // larghezza calcolata dai margini della finestra
        tf.setFont(tf.getFont().deriveFont(13f));
        tf.setBackground(new Color(42, 42, 50));
        tf.setForeground(new Color(220, 220, 228));
        tf.setCaretColor(new Color(220, 220, 228));
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(65, 65, 78), 1, true),
                new EmptyBorder(0, 8, 0, 8)));
        tf.setAlignmentX(LEFT_ALIGNMENT);
        tf.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(13, 110, 253), 1, true), // bordo blu → campo attivo
                        new EmptyBorder(0, 8, 0, 8)));
            }
            @Override
            public void focusLost(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(65, 65, 78), 1, true), // bordo grigio → campo non attivo
                        new EmptyBorder(0, 8, 0, 8)));
            }
        });
        return tf;
    }

    /**
     * Applica lo stile scuro dell'applicazione a un menu a tendina (JComboBox).
     *
     * @param combo il menu a tendina da stilizzare
     */
    private void styleCombo(@NotNull JComboBox<?> combo) {
        combo.setBackground(new Color(42, 42, 50));
        combo.setForeground(new Color(220, 220, 228));
        combo.setBorder(new LineBorder(new Color(65, 65, 78), 1, true));
        combo.setFocusable(false); // non mostriamo l'indicatore di focus da tastiera
    }

    /**
     * Crea un bottone personalizzato con sfondo colorato e effetto hover.
     * Identico nella struttura a quello degli altri pop-up dell'applicazione.
     *
     * @param text  il testo del bottone
     * @param base  il colore normale
     * @param hover il colore al passaggio del mouse
     * @return il bottone pronto all'uso
     */
    private @NotNull JButton makeButton(String text, Color base, Color hover) {
        JButton btn = new JButton(text) {
            private Color current = base;

            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        current = hover;
                        repaint();
                    }
                    @Override
                    public void mouseExited(MouseEvent e)  {
                        current = base;
                        repaint();
                    }
                });
            }

            @Override protected void paintComponent(@NotNull Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(current);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont().deriveFont(Font.BOLD, 13f));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth()  - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        btn.setFont(btn.getFont().deriveFont(Font.BOLD, 13f));
        btn.setPreferredSize(new Dimension(0, 40));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return btn;
    }

}