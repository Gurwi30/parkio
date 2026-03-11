package it.parkio.app.ui.component.overlay;

import it.parkio.app.ParkIO;
import it.parkio.app.event.ParkingLotDeselectEvent;
import it.parkio.app.event.ParkingLotSelectEvent;
import it.parkio.app.event.ParkingSpaceCreateEvent;
import it.parkio.app.event.ParkingSpaceRemoveEvent;
import it.parkio.app.manager.ParkingLotsManager;
import it.parkio.app.model.ParkingLot;
import it.parkio.app.model.ParkingSpace;
import it.parkio.app.ui.component.widget.ColorsWatchPickerComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Pannello laterale che permette di visualizzare e modificare
 * il parcheggio attualmente selezionato.
 *
 * <p>Consente di:</p>
 * <ul>
 *     <li>modificare nome e colore del parcheggio;</li>
 *     <li>vedere l'elenco degli spazi interni;</li>
 *     <li>cambiare il tipo di uno spazio;</li>
 *     <li>eliminare spazi o l'intero parcheggio.</li>
 * </ul>
 */
public class ParkingLotManageComponent extends JOverlayPanel {

    /**
     * Gestore dei parcheggi: contiene la lista di tutti i parcheggi
     * e si occupa di aggiungerli o rimuoverli.
     */
    private final ParkingLotsManager lotsManager;

    /**
     * Il parcheggio che stiamo guardando/modificando in questo momento.
     * Può essere null quando nessun parcheggio è selezionato.
     */
    private @Nullable ParkingLot parkingLot;

    // --- Componenti grafici del pannello ---

    /** Campo di testo dove l'utente scrive il nome del parcheggio. */
    private JTextField nameField;

    /** Etichetta rossa che appare se l'utente lascia il nome vuoto. */
    private JLabel nameError;

    /** Selettore visivo per scegliere il colore del parcheggio. */
    private ColorsWatchPickerComponent colorPicker;

    /** Lista (modello interno) degli spazi del parcheggio corrente. */
    private DefaultComboBoxModel<ParkingSpace> spaceModel;

    /** Menu a tendina che mostra gli spazi del parcheggio. */
    private JComboBox<ParkingSpace> spaceCombo;

    /** Etichetta "Spazi (N)" che indica quanti spazi ci sono. */
    private JLabel spacesLabel;

    /** Menu a tendina per cambiare il tipo dello spazio selezionato (es. normale, disabili…). */
    private JComboBox<ParkingSpace.Type> typeCombo;

    /** Pannello che mostra i dettagli dello spazio selezionato. */
    private JPanel spaceDetailPanel;

    /**
     * Costruttore: inizializza il pannello e si mette in ascolto degli eventi
     * dell'applicazione per aggiornarsi automaticamente.
     *
     * @param lotsManager il gestore dei parcheggi
     */
    public ParkingLotManageComponent(ParkingLotsManager lotsManager) {
        this.lotsManager = lotsManager;

        // Impostiamo il layout (BorderLayout divide il pannello in aree: nord, sud, est, ovest, centro)
        setLayout(new BorderLayout());
        // "false" significa che il pannello non ha uno sfondo opaco proprio (lo gestisce il genitore)
        setOpaque(false);

        // Quando l'utente clicca su un parcheggio sulla mappa, riceviamo un "evento"
        // e chiamiamo setParkingLot() per aggiornare il pannello
        ParkIO.EVENT_MANAGER.register(ParkingLotSelectEvent.class, event ->
                setParkingLot(event.selectedParkingLot())
        );

        // Quando viene aggiunto un nuovo spazio al parcheggio corrente,
        // lo aggiungiamo al menu a tendina e aggiorniamo il contatore
        ParkIO.EVENT_MANAGER.register(ParkingSpaceCreateEvent.class, event -> {
            if (parkingLot == null) return; // nessun parcheggio aperto, ignoriamo
            if (!parkingLot.getSpaces().contains(event.space())) return; // non appartiene a questo parcheggio
            spaceModel.addElement(event.space());
            updateSpacesLabel();
            refreshSpaceDetail();
        });

        // Quando uno spazio viene rimosso, lo togliamo dal menu a tendina
        ParkIO.EVENT_MANAGER.register(ParkingSpaceRemoveEvent.class, event -> {
            if (parkingLot == null) return;
            spaceModel.removeElement(event.removedSpace());
            updateSpacesLabel();
            refreshSpaceDetail();
        });

        // All'avvio mostriamo il pannello "vuoto" (nessun parcheggio selezionato)
        showEmpty();
    }

    /**
     * Mostra un messaggio generico quando nessun parcheggio è selezionato
     * e nasconde il pannello.
     */
    private void showEmpty() {
        removeAll(); // rimuoviamo tutti i componenti visibili

        JLabel lbl = new JLabel("Seleziona un parcheggio");
        lbl.setForeground(Color.GRAY);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        add(lbl, BorderLayout.CENTER);

        setVisible(false); // nascondiamo il pannello laterale

        revalidate(); // diciamo all'interfaccia di ricalcolare il layout
        repaint();    // ridisegniamo il pannello
    }

    /**
     * Cambia il parcheggio mostrato nel pannello.
     * Se viene passato null, il pannello torna allo stato "vuoto".
     *
     * @param lot il nuovo parcheggio da mostrare, oppure null per svuotare il pannello
     */
    private void setParkingLot(@Nullable ParkingLot lot) {
        // Se stiamo già mostrando questo stesso parcheggio, non facciamo nulla
        if (this.parkingLot == lot) return;
        this.parkingLot = lot;

        removeAll(); // puliamo il pannello dai vecchi componenti

        if (lot == null) {
            showEmpty();
            return;
        }

        // Rendiamo visibile il pannello laterale (era nascosto se non c'era nulla)
        if (!isVisible()) setVisible(true);

        buildUI(lot);   // costruiamo l'interfaccia per il parcheggio scelto
        revalidate();
        repaint();
    }

    /**
     * Aggiorna l'etichetta che mostra il numero di spazi (es. "Spazi (5)").
     */
    private void updateSpacesLabel() {
        if (spacesLabel == null || parkingLot == null) return;
        spacesLabel.setText("Spazi (" + spaceModel.getSize() + ")");
    }

    /**
     * Costruisce tutta l'interfaccia grafica del pannello per il parcheggio dato.
     * Il contenuto viene inserito in un'area scorrevole (JScrollPane).
     *
     * @param lot il parcheggio di cui costruire l'interfaccia
     */
    private void buildUI(@NotNull ParkingLot lot) {
        // JPanel è un contenitore generico; BoxLayout impila i figli in verticale (Y_AXIS)
        JPanel root = new JPanel();
        root.setOpaque(false);
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(new EmptyBorder(16, 16, 12, 16)); // margine interno: top, left, bottom, right

        root.add(buildTopForm(lot));        // sezione nome + colore
        root.add(buildSpacesPanel(lot));    // sezione elenco spazi
        root.add(Box.createRigidArea(new Dimension(0, 12))); // spazio vuoto di 12 pixel
        root.add(buildActionButtons(lot));  // bottoni "Elimina" e "Salva ed Esci"

        // JScrollPane aggiunge una barra di scorrimento verticale se il contenuto è troppo lungo
        JScrollPane scroll = new JScrollPane(root);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER); // niente scroll orizzontale
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED); // scroll verticale solo se serve

        add(scroll, BorderLayout.CENTER);
    }

    /**
     * Costruisce la sezione in cima con il campo del nome e il selettore colore.
     *
     * @param lot il parcheggio corrente
     * @return il pannello con il form
     */
    private @NotNull JPanel buildTopForm(@NotNull ParkingLot lot) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Mostra l'ID del parcheggio (es. "ID #3") — non modificabile
        JLabel idLabel = new JLabel("ID #" + lot.getId());
        idLabel.setFont(idLabel.getFont().deriveFont(Font.BOLD, 11f));
        idLabel.setForeground(new Color(120, 120, 135));
        idLabel.setAlignmentX(LEFT_ALIGNMENT); // allineato a sinistra
        panel.add(idLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));

        // Etichetta "Nome *" (l'asterisco indica che è obbligatorio)
        JLabel nameLabel = new JLabel("Nome *");
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 12f));
        nameLabel.setForeground(new Color(180, 180, 190));
        nameLabel.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(nameLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 4)));

        // Campo di testo già compilato con il nome attuale del parcheggio
        nameField = styledTextField(lot.getName());
        panel.add(nameField);

        // Messaggio d'errore (invisibile per default, diventa rosso se il nome è vuoto)
        nameError = new JLabel("Il nome è obbligatorio");
        nameError.setFont(nameError.getFont().deriveFont(Font.ITALIC, 11f));
        nameError.setForeground(new Color(0, 0, 0, 0)); // alpha=0 → completamente trasparente
        nameError.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(Box.createRigidArea(new Dimension(0, 3)));
        panel.add(nameError);

        panel.add(Box.createRigidArea(new Dimension(0, 12)));

        // Etichetta "Colore"
        JLabel colorLabel = new JLabel("Colore");
        colorLabel.setFont(colorLabel.getFont().deriveFont(Font.BOLD, 12f));
        colorLabel.setForeground(new Color(180, 180, 190));
        colorLabel.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(colorLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 6)));

        // Selettore colore: mostra una serie di "pallini" colorati da cliccare
        colorPicker = new ColorsWatchPickerComponent(lot.getColor(), 60);
        colorPicker.setAlignmentX(LEFT_ALIGNMENT);
        colorPicker.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        panel.add(colorPicker);

        panel.add(Box.createRigidArea(new Dimension(0, 14)));

        // Linea orizzontale di separazione tra il form e la sezione spazi
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(60, 60, 70));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(sep);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));

        return panel;
    }

    /**
     * Crea un campo di testo con lo stile scuro dell'applicazione.
     * Cambia il bordo quando il campo è attivo (focus) per dare un feedback visivo.
     *
     * @param value il testo iniziale da mostrare nel campo
     * @return il campo di testo già stilizzato
     */
    private @NotNull JTextField styledTextField(String value) {
        JTextField tf = new JTextField(value);
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        tf.setPreferredSize(new Dimension(0, 34));
        tf.setFont(tf.getFont().deriveFont(13f));
        tf.setBackground(new Color(42, 42, 50));       // sfondo grigio scuro
        tf.setForeground(new Color(220, 220, 228));    // testo quasi bianco
        tf.setCaretColor(new Color(220, 220, 228));    // cursore lampeggiante dello stesso colore del testo
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(65, 65, 78), 1, true),  // bordo grigio di default
                new EmptyBorder(0, 8, 0, 8)));                   // padding interno sinistro/destro
        tf.setAlignmentX(LEFT_ALIGNMENT);

        // FocusAdapter gestisce gli eventi di "entrata" e "uscita" dal campo
        tf.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                // L'utente ha cliccato sul campo: bordo blu per evidenziarlo
                tf.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(13, 110, 253), 1, true),
                        new EmptyBorder(0, 8, 0, 8)));
            }

            @Override
            public void focusLost(FocusEvent e) {
                // L'utente ha cliccato altrove: torniamo al bordo grigio
                tf.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(65, 65, 78), 1, true),
                        new EmptyBorder(0, 8, 0, 8)));
            }
        });

        return tf;
    }

    /**
     * Costruisce la sezione che mostra l'elenco degli spazi del parcheggio,
     * con la possibilità di selezionarne uno e rimuoverlo.
     *
     * @param lot il parcheggio corrente
     * @return il pannello con la lista degli spazi
     */
    private @NotNull JPanel buildSpacesPanel(@NotNull ParkingLot lot) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Etichetta con il contatore degli spazi (es. "Spazi (4)")
        spacesLabel = new JLabel("Spazi (" + lot.getSpaces().size() + ")");
        spacesLabel.setFont(spacesLabel.getFont().deriveFont(Font.BOLD, 12f));
        spacesLabel.setForeground(new Color(180, 180, 190));
        spacesLabel.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(spacesLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 6)));

        // DefaultComboBoxModel è la "lista dati" che alimenta il menu a tendina
        spaceModel = new DefaultComboBoxModel<>();
        lot.getSpaces().forEach(spaceModel::addElement); // aggiungiamo tutti gli spazi esistenti

        // Menu a tendina che mostra gli spazi; usa SpaceListRenderer per formattare ogni riga
        spaceCombo = new JComboBox<>(spaceModel);
        spaceCombo.setRenderer(new SpaceListRenderer());
        spaceCombo.setPreferredSize(new Dimension(0, 26));
        spaceCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        spaceCombo.setAlignmentX(LEFT_ALIGNMENT);
        styleCombo(spaceCombo);

        // Bottone "✕" per rimuovere lo spazio selezionato
        // È una classe anonima che sovrascrive paintComponent per disegnare un bottone personalizzato
        JButton removeSpaceBtn = new JButton("✕") {
            // Colore corrente del bottone (cambia quando ci passiamo sopra con il mouse)
            private Color current = new Color(180, 40, 50);

            {
                // Blocco di inizializzazione: aggiungiamo il listener per l'effetto hover
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        current = new Color(210, 55, 65); // rosso più chiaro quando il mouse è sopra
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        current = new Color(180, 40, 50); // torniamo al rosso scuro
                        repaint();
                    }
                });
            }

            /**
             * Sovrascriviamo il disegno del bottone per avere uno stile personalizzato
             * invece del classico aspetto grigio di sistema.
             */
            @Override
            protected void paintComponent(@NotNull Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                // ANTIALIAS: rende i bordi arrotondati morbidi, senza effetto "scalettato"
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(current);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8); // rettangolo con angoli arrotondati
                g2.setColor(Color.WHITE);
                g2.setFont(getFont().deriveFont(Font.BOLD, 12f));
                FontMetrics fm = g2.getFontMetrics();
                // Calcoliamo la posizione per centrare il testo "✕" nel bottone
                int tx = (getWidth()  - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), tx, ty);
                g2.dispose(); // liberiamo le risorse grafiche
            }
        };

        removeSpaceBtn.setPreferredSize(new Dimension(26, 26));
        removeSpaceBtn.setMinimumSize(new Dimension(26, 26));
        removeSpaceBtn.setMaximumSize(new Dimension(26, 26));
        removeSpaceBtn.setContentAreaFilled(false); // disabilitiamo il riempimento standard
        removeSpaceBtn.setBorderPainted(false);     // niente bordo standard
        removeSpaceBtn.setFocusPainted(false);      // niente indicatore di focus
        removeSpaceBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // cursore a mano

        // Azione al click sul bottone "✕": chiede conferma e poi rimuove lo spazio
        removeSpaceBtn.addActionListener(_ -> {
            ParkingSpace selected = (ParkingSpace) spaceCombo.getSelectedItem();
            if (selected == null || parkingLot == null) return;
            // JOptionPane apre una finestra di dialogo per chiedere conferma
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Rimuovere lo spazio #" + selected.getId() + "?",
                    "Conferma", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                parkingLot.removeSpace(selected.getId());
                // Lanciamo un evento così gli altri componenti sanno che lo spazio è stato rimosso
                ParkIO.EVENT_MANAGER.call(new ParkingSpaceRemoveEvent(selected));
            }
        });

        // Riga orizzontale con il menu a tendina + bottone rimozione affiancati
        JPanel comboRow = new JPanel(new BorderLayout(6, 0));
        comboRow.setOpaque(false);
        comboRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        comboRow.setAlignmentX(LEFT_ALIGNMENT);
        comboRow.add(spaceCombo, BorderLayout.CENTER); // menu occupa tutto lo spazio al centro
        comboRow.add(removeSpaceBtn, BorderLayout.EAST); // bottone incollato a destra
        panel.add(comboRow);

        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Pannello dettaglio spazio (mostra il tipo): si aggiorna quando cambia la selezione
        spaceDetailPanel = new JPanel();
        spaceDetailPanel.setOpaque(false);
        spaceDetailPanel.setLayout(new BoxLayout(spaceDetailPanel, BoxLayout.Y_AXIS));
        spaceDetailPanel.setAlignmentX(LEFT_ALIGNMENT);
        refreshSpaceDetail();

        // Ogni volta che l'utente sceglie un altro spazio dal menu, aggiorniamo il dettaglio
        spaceCombo.addActionListener(_ -> refreshSpaceDetail());

        panel.add(spaceDetailPanel);

        return panel;
    }

    /**
     * Aggiorna il pannello del dettaglio mostrando le proprietà dello spazio
     * attualmente selezionato nel menu a tendina.
     * Se non c'è nessuno spazio selezionato, svuota il pannello.
     */
    private void refreshSpaceDetail() {
        spaceDetailPanel.removeAll(); // puliamo il pannello prima di ricostruirlo

        ParkingSpace space = (ParkingSpace) spaceCombo.getSelectedItem();
        if (space == null) {
            spaceDetailPanel.revalidate();
            spaceDetailPanel.repaint();
            return;
        }

        // Etichetta "Tipo"
        JLabel typeLabel = new JLabel("Tipo");
        typeLabel.setFont(typeLabel.getFont().deriveFont(Font.BOLD, 11f));
        typeLabel.setForeground(new Color(160, 160, 175));
        typeLabel.setAlignmentX(LEFT_ALIGNMENT);
        spaceDetailPanel.add(typeLabel);
        spaceDetailPanel.add(Box.createRigidArea(new Dimension(0, 4)));

        // Menu a tendina con tutti i tipi disponibili (es. STANDARD, DISABILI, ELETTRICO…)
        typeCombo = new JComboBox<>(ParkingSpace.Type.values());
        typeCombo.setSelectedItem(space.getType()); // selezioniamo il tipo corrente dello spazio
        typeCombo.setPreferredSize(new Dimension(0, 26));
        typeCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        typeCombo.setAlignmentX(LEFT_ALIGNMENT);
        styleCombo(typeCombo);
        // Quando l'utente cambia il tipo, aggiorniamo subito lo spazio nel modello dati
        typeCombo.addActionListener(_ -> space.setType((ParkingSpace.Type) typeCombo.getSelectedItem()));
        spaceDetailPanel.add(typeCombo);

        spaceDetailPanel.revalidate();
        spaceDetailPanel.repaint();
    }

    /**
     * Applica lo stile scuro dell'applicazione a un qualsiasi menu a tendina (JComboBox).
     * Il metodo è generico (<E>) per funzionare con qualunque tipo di elementi.
     *
     * @param combo il menu a tendina da stilizzare
     */
    private <E> void styleCombo(@NotNull JComboBox<E> combo) {
        combo.setBackground(new Color(42, 42, 50));
        combo.setForeground(new Color(220, 220, 228));
        combo.setBorder(new LineBorder(new Color(65, 65, 78), 1, true));
        combo.setFocusable(false); // non mostriamo l'indicatore di focus
    }

    /**
     * Costruisce la barra con i due bottoni principali in fondo al pannello:
     * "Elimina" (rosso) e "Salva ed Esci" (blu).
     *
     * @param lot il parcheggio corrente
     * @return il pannello con i bottoni
     */
    private @NotNull JPanel buildActionButtons(@NotNull ParkingLot lot) {
        // GridLayout(1, 2, 10, 0) → 1 riga, 2 colonne, 10px di spazio orizzontale tra le celle
        JPanel bar = new JPanel(new GridLayout(1, 2, 10, 0));
        bar.setOpaque(false);
        bar.setAlignmentX(LEFT_ALIGNMENT);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // Bottone "Elimina": rosso, chiede conferma prima di cancellare il parcheggio
        JButton deleteBtn = makeButton("Elimina", new Color(160, 35, 45), new Color(200, 50, 60));
        deleteBtn.addActionListener(_ -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Eliminare il parcheggio \"" + lot.getName() + "\"?",
                    "Conferma eliminazione", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                lotsManager.removeParkingLot(lot.getId()); // rimuoviamo dal gestore
                setParkingLot(null);                       // svuotiamo il pannello
            }
        });

        // Bottone "Salva ed Esci": blu, salva le modifiche e chiude il pannello
        JButton saveBtn = makeButton("Salva ed Esci", new Color(13, 90, 210), new Color(13, 110, 253));
        saveBtn.addActionListener(_ -> {
            String name = nameField.getText().trim(); // leggiamo il testo rimuovendo spazi iniziali/finali

            // Validazione: il nome non può essere vuoto
            if (name.isEmpty()) {
                nameError.setForeground(new Color(220, 53, 69)); // rendiamo visibile il messaggio d'errore
                nameField.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(220, 53, 69), 1, true), // bordo rosso
                        new EmptyBorder(0, 8, 0, 8)));

                nameField.requestFocus(); // spostiamo il cursore nel campo del nome
                return; // usciamo senza salvare
            }

            // Salviamo nome e colore nel modello dati del parcheggio
            lot.setName(name);
            lot.setColor(colorPicker.getSelectedColor());

            // Chiudiamo il pannello e notifichiamo che nessun parcheggio è più selezionato
            setParkingLot(null);
            ParkIO.EVENT_MANAGER.call(new ParkingLotDeselectEvent());
        });

        bar.add(deleteBtn);
        bar.add(saveBtn);
        return bar;
    }

    /**
     * Crea un bottone stilizzato con sfondo colorato e effetto hover (cambio colore al passaggio del mouse).
     * Dimensioni standard: altezza 40px.
     *
     * @param text  il testo da mostrare sul bottone
     * @param base  il colore normale del bottone
     * @param hover il colore quando il mouse ci passa sopra
     * @return il bottone creato
     */
    private @NotNull JButton makeButton(String text, Color base, Color hover) {
        return makeButtonInternal(text, base, hover, 13f, 40);
    }

    /**
     * Implementazione interna per creare bottoni personalizzati.
     * Sovrascrive il disegno standard di Swing per ottenere angoli arrotondati
     * e colori personalizzati senza dipendere dallo stile del sistema operativo.
     *
     * @param text     testo del bottone
     * @param base     colore normale
     * @param hover    colore al passaggio del mouse
     * @param fontSize dimensione del font
     * @param height   altezza in pixel
     * @return il bottone creato
     */
    private @NotNull JButton makeButtonInternal(String text, Color base, Color hover, float fontSize, int height) {
        JButton btn = new JButton(text) {
            // Teniamo traccia del colore corrente (normale o hover)
            private Color current = base;

            {
                // Blocco di inizializzazione: gestiamo il cambio colore al passaggio del mouse
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        current = hover; // mouse sopra → colore più chiaro
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e)  {
                        current = base; // mouse via → colore normale
                        repaint();
                    }
                });
            }

            /**
             * Disegniamo il bottone manualmente invece di usare l'aspetto di sistema.
             * Questo ci dà il pieno controllo su forma, colore e testo.
             */
            @Override
            protected void paintComponent(@NotNull Graphics g) {
                // Graphics2D è la versione "avanzata" di Graphics, con più funzionalità
                Graphics2D g2 = (Graphics2D) g.create();

                // Antialiasing: bordi morbidi senza "scalettatura"
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(current);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10); // sfondo con angoli arrotondati
                g2.setColor(Color.WHITE); // testo bianco
                g2.setFont(getFont().deriveFont(Font.BOLD, fontSize));

                // FontMetrics ci fornisce le misure del testo per centrarlo perfettamente
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth()  - fm.stringWidth(getText())) / 2; // centro orizzontale
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2; // centro verticale

                g2.drawString(getText(), tx, ty);
                g2.dispose(); // importante: liberiamo sempre il contesto grafico
            }
        };

        btn.setFont(btn.getFont().deriveFont(Font.BOLD, fontSize));
        btn.setPreferredSize(new Dimension(0, height));
        btn.setContentAreaFilled(false); // disabilitiamo il riempimento Swing standard
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // cursore a mano al passaggio

        return btn;
    }

    /**
     * Renderer personalizzato per le righe del menu a tendina degli spazi.
     * Decide come viene visualizzato ogni elemento nella lista.
     *
     * <p>Senza questo renderer, la JComboBox mostrerebbe solo il risultato di
     * {@code toString()} sull'oggetto ParkingSpace, che non sarebbe leggibile.</p>
     */
    private static class SpaceListRenderer extends DefaultListCellRenderer {

        /**
         * Chiamato automaticamente da Swing per ogni elemento della lista
         * quando deve essere disegnato.
         *
         * @param list         la lista in cui si trova l'elemento
         * @param value        l'oggetto da visualizzare (nel nostro caso un ParkingSpace)
         * @param index        l'indice dell'elemento nella lista
         * @param isSelected   true se l'elemento è selezionato
         * @param cellHasFocus true se la cella ha il focus della tastiera
         */
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            // Prima chiamiamo il renderer di default per avere il comportamento base
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            // Se l'oggetto è effettivamente uno spazio di parcheggio, formattiamo il testo
            // es. "Spazio #2  [DISABILI]  — libero"
            if (value instanceof ParkingSpace ps)
                setText("Spazio #" + ps.getId() + "  [" + ps.getType() + "]  — " + ps.getStatus().getIdentifier());

            // Colore di sfondo: blu se selezionato, grigio scuro altrimenti
            setBackground(isSelected ? new Color(13, 90, 210) : new Color(42, 42, 50));
            setForeground(new Color(220, 220, 228)); // testo quasi bianco
            setBorder(new EmptyBorder(2, 8, 2, 8));  // piccolo padding interno
            return this;
        }
    }

}