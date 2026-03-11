package it.parkio.app.ui.component.overlay;

import it.parkio.app.ParkIO;
import it.parkio.app.event.ParkingLotCreateEvent;
import it.parkio.app.event.ParkingLotDeselectEvent;
import it.parkio.app.event.ParkingLotRemoveEvent;
import it.parkio.app.event.ParkingLotSelectEvent;
import it.parkio.app.manager.ParkingLotsManager;
import it.parkio.app.model.ParkingLot;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

/**
 * Pannello laterale che mostra la lista di tutti i parcheggi esistenti.
 *
 * <p>Se non ci sono parcheggi, mostra un messaggio vuoto con le istruzioni.
 * Altrimenti mostra una lista scorrevole con una card per ogni parcheggio.</p>
 *
 * <p>Si aggiorna automaticamente grazie al sistema di eventi: quando un
 * parcheggio viene creato o rimosso altrove nell'app, questo pannello
 * lo saprà e si ridisegnerà da solo.</p>
 */
public class ParkingLotsListComponent extends JOverlayPanel {

    /** Gestore dei parcheggi: la "fonte di verità" da cui leggiamo i dati. */
    private final ParkingLotsManager lotsManager;

    /**
     * Modello dati della lista: contiene gli oggetti ParkingLot da mostrare.
     * È separato dalla parte visiva (JList) seguendo il pattern MVC di Swing.
     */
    private final DefaultListModel<ParkingLot> listModel = new DefaultListModel<>();

    /**
     * Componente visivo che mostra la lista.
     * Legge i dati da listModel e li visualizza usando ParkingLotRenderer.
     */
    private final JList<ParkingLot> list = new JList<>(listModel);

    /**
     * Pannello alternativo mostrato quando non ci sono parcheggi.
     * Contiene il messaggio "Nessun Parcheggio Trovato" e le istruzioni.
     */
    private final JPanel emptyPanel = new JPanel(new GridBagLayout());

    /**
     * Costruttore: riceve il gestore dei parcheggi, imposta il layout
     * e inizializza tutti i componenti grafici.
     *
     * @param lotsManager il gestore dei parcheggi
     */
    public ParkingLotsListComponent(ParkingLotsManager lotsManager) {
        this.lotsManager = lotsManager;

        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(15, 15, 15, 15)); // margine esterno di 15px su tutti i lati

        initComponents(); // costruiamo i componenti interni
        reloadList();     // mostriamo subito la lista (o il pannello vuoto)
    }

    /**
     * Inizializza tutti i componenti grafici e registra i listener per gli eventi.
     * Viene chiamato una sola volta alla creazione del pannello.
     */
    private void initComponents() {

        // --- Configurazione della JList ---

        // Permettiamo di selezionare un solo elemento alla volta
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // -1 significa "altezza automatica" (ogni riga può avere altezza diversa)
        list.setFixedCellHeight(-1);
        list.setOpaque(false);
        list.setBorder(new EmptyBorder(5, 5, 5, 5));
        // Usiamo il nostro renderer personalizzato invece di quello standard di Swing
        list.setCellRenderer(new ParkingLotRenderer());

        // Carichiamo nella lista tutti i parcheggi già esistenti al momento dell'apertura
        listModel.addAll(lotsManager.getParkingLots());

        // Quando l'utente clicca su un elemento della lista, lanciamo un evento
        // "parcheggio selezionato" così gli altri pannelli (es. quello di modifica) si aggiornano
        list.addListSelectionListener(event -> {
            // getValueIsAdjusting() è true mentre la selezione è ancora "in corso"
            // (es. durante un drag): aspettiamo che sia definitiva prima di reagire
            if (event.getValueIsAdjusting()) return;
            if (list.getSelectedIndex() >= 0)
                ParkIO.EVENT_MANAGER.call(new ParkingLotSelectEvent(list.getSelectedValue()));
        });

        // Se qualcun altro nell'app seleziona un parcheggio (es. cliccando sulla mappa),
        // evidenziamo il corrispondente elemento nella lista
        ParkIO.EVENT_MANAGER.register(ParkingLotSelectEvent.class, event ->
                list.setSelectedValue(event.selectedParkingLot(), true) // true = scrolla per renderlo visibile
        );

        // Se la selezione viene annullata altrove (es. dopo il salvataggio),
        // deselezioniamo anche nella lista
        ParkIO.EVENT_MANAGER.register(ParkingLotDeselectEvent.class, _ ->
                list.clearSelection()
        );

        // Quando viene creato un nuovo parcheggio, lo aggiungiamo alla lista
        // e ricarichiamo (per passare dal pannello vuoto alla lista, se necessario)
        ParkIO.EVENT_MANAGER.register(ParkingLotCreateEvent.class, event -> {
            listModel.addElement(event.parkingLot());
            reloadList();
        });

        // Quando un parcheggio viene eliminato, lo rimuoviamo dalla lista
        ParkIO.EVENT_MANAGER.register(ParkingLotRemoveEvent.class, event -> {
            listModel.removeElement(event.removedParkingLot());
            reloadList();
        });

        // --- Effetto hover: cambia il cursore e colora la riga sotto il mouse ---

        // MouseMotionAdapter ci dà un listener per il movimento del mouse
        // senza dover implementare tutti i metodi dell'interfaccia MouseMotionListener
        list.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                // Troviamo quale riga della lista si trova sotto la posizione del mouse
                int index = list.locationToIndex(e.getPoint());

                // Verifichiamo anche che il punto sia effettivamente dentro i bordi della riga
                // (locationToIndex restituisce comunque l'indice più vicino, anche se fuori dalla riga)
                if (index >= 0 && list.getCellBounds(index, index).contains(e.getPoint())) {
                    // Salviamo l'indice della riga "sotto il mouse" come proprietà della lista
                    // putClientProperty è un modo generico di Swing per attaccare dati extra a un componente
                    list.putClientProperty("hoverIndex", index);
                    list.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // cursore a mano
                } else {
                    list.putClientProperty("hoverIndex", -1); // -1 = nessuna riga in hover
                    list.setCursor(Cursor.getDefaultCursor());
                }

                list.repaint(); // ridisegniamo per aggiornare il colore hover
            }
        });

        list.addMouseListener(new MouseAdapter() {
            // Quando il mouse esce dalla lista, resettiamo l'hover
            @Override
            public void mouseExited(MouseEvent e) {
                list.putClientProperty("hoverIndex", -1);
                list.setCursor(Cursor.getDefaultCursor());
                list.repaint();
            }

            // Se l'utente clicca in un'area vuota (non su una riga), deselezioniamo
            @Override
            public void mousePressed(MouseEvent e) {
                int index = list.locationToIndex(e.getPoint());
                if (index == -1 || !list.getCellBounds(index, index).contains(e.getPoint())) {
                    list.clearSelection();
                }
            }
        });

        // --- Configurazione del pannello "lista vuota" ---

        emptyPanel.setOpaque(false);

        // JTextArea invece di JLabel perché supporta il testo a capo automatico (line wrap)
        JTextArea mainLabel = new JTextArea("Nessun Parcheggio Trovato :\\");
        mainLabel.setWrapStyleWord(true); // va a capo sulle parole intere, non a metà
        mainLabel.setLineWrap(true);
        mainLabel.setEditable(false);    // solo testo, non modificabile dall'utente
        mainLabel.setOpaque(false);
        mainLabel.setFocusable(false);   // non intercetta il focus da tastiera
        mainLabel.setFont(mainLabel.getFont().deriveFont(Font.BOLD, 18f));
        mainLabel.setForeground(Color.LIGHT_GRAY);
        mainLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextArea subLabel = new JTextArea("Crea un parcheggio premendo con tasto destro sulla mappa e trascinando");
        subLabel.setWrapStyleWord(true);
        subLabel.setLineWrap(true);
        subLabel.setEditable(false);
        subLabel.setOpaque(false);
        subLabel.setFocusable(false);
        subLabel.setFont(subLabel.getFont().deriveFont(Font.PLAIN, 12f));
        subLabel.setForeground(Color.GRAY);
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // GridBagConstraints controlla dove e come viene posizionato ogni componente
        // all'interno di un pannello con GridBagLayout (il layout più flessibile di Swing)
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;    // colonna 0
        gbc.gridy = 0;    // riga 0 (mainLabel)
        gbc.insets = new Insets(0, 20, 10, 20); // spazio esterno: top, left, bottom, right
        gbc.fill = GridBagConstraints.HORIZONTAL; // il componente si allarga in orizzontale
        gbc.anchor = GridBagConstraints.CENTER;   // centrato nella cella
        gbc.weightx = 1.0; // questa colonna prende tutto lo spazio orizzontale disponibile
        emptyPanel.add(mainLabel, gbc);

        gbc.gridy = 1; // riga 1 (subLabel)
        emptyPanel.add(subLabel, gbc);
    }

    /**
     * Crea un'area scorrevole (JScrollPane) che contiene la lista dei parcheggi.
     * Viene ricreata ogni volta che si ricarica la lista.
     *
     * @return lo scroll pane configurato
     */
    private @NotNull JScrollPane createScrollPane() {
        JScrollPane scroll = new JScrollPane(list);

        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false); // il "viewport" è la finestra visibile all'interno dello scroll
        scroll.setBorder(new EmptyBorder(5, 0, 10, 0));
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // niente scroll orizzontale
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);  // scroll verticale solo se serve

        return scroll;
    }

    /**
     * Aggiorna il contenuto del pannello principale:
     * mostra il pannello "vuoto" se non ci sono parcheggi,
     * altrimenti mostra la lista scorrevole.
     *
     * <p>Viene chiamato ogni volta che la lista cambia (aggiunta o rimozione).</p>
     */
    public void reloadList() {
        removeAll(); // puliamo il pannello corrente

        if (listModel.isEmpty()) {
            add(emptyPanel, BorderLayout.CENTER); // nessun parcheggio → messaggio informativo
        } else {
            add(createScrollPane(), BorderLayout.CENTER); // ci sono parcheggi → mostriamo la lista
        }

        revalidate(); // ricalcola il layout
        repaint();    // ridisegna
    }

    /**
     * Renderer personalizzato per le card della lista parcheggi.
     *
     * <p>In Swing, un "renderer" è una classe che decide come appare ogni singola riga
     * di una JList. Viene chiamato da Swing per ogni elemento da disegnare.
     * Qui costruiamo una card con sfondo arrotondato, colori diversi per
     * stato normale, hover e selezionato.</p>
     */
    private static class ParkingLotRenderer implements ListCellRenderer<ParkingLot> {

        /**
         * Restituisce il componente grafico da usare per disegnare una riga della lista.
         * Swing chiama questo metodo per ogni elemento visibile, ogni volta che ridisegna.
         *
         * @param list         la JList che contiene l'elemento
         * @param value        il ParkingLot da visualizzare in questa riga
         * @param index        la posizione dell'elemento nella lista (0, 1, 2…)
         * @param isSelected   true se l'utente ha selezionato questa riga
         * @param cellHasFocus true se questa cella ha il focus da tastiera
         * @return il componente grafico da disegnare come riga
         */
        @Override
        public @NotNull Component getListCellRendererComponent(
                @NotNull JList<? extends ParkingLot> list,
                @NotNull ParkingLot value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
        ) {
            // Costanti di spaziatura interna alla card
            final int VERTICAL_GAP = 4;      // spazio sopra e sotto ogni card (tra le righe)
            final int INNER_PADDING_V = 20;  // padding verticale interno alla card

            // Calcoliamo la larghezza disponibile per il testo, sottraendo i margini della lista
            int listInsetH = list.getInsets().left + list.getInsets().right;
            int availableWidth = list.getWidth() - listInsetH - 30;
            if (availableWidth <= 0) availableWidth = 200; // valore di fallback se la lista è troppo stretta

            // Creiamo una JTextArea "fantasma" solo per misurare quanto spazio occupa il nome
            // del parcheggio con il testo a capo. Non verrà mai mostrata direttamente.
            JTextArea probe = new JTextArea(value.getName());
            probe.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 17f));
            probe.setWrapStyleWord(true);
            probe.setLineWrap(true);
            probe.setSize(new Dimension(availableWidth, Short.MAX_VALUE)); // altezza massima possibile

            // getPreferredSize() ci dice quanto spazio servirebbe "idealmente" al testo
            int textHeight = probe.getPreferredSize().height;
            // Calcoliamo l'altezza totale della card: gap sopra+sotto + padding + testo
            int cellHeight = VERTICAL_GAP * 2 + INNER_PADDING_V + textHeight;

            // "wrapper" è il contenitore esterno della riga: include il gap verticale tra le card
            // Sovrascriviamo getPreferredSize/getMinimumSize/getMaximumSize per forzare
            // l'altezza calcolata dinamicamente (Swing altrimenti userebbe valori di default)
            JPanel wrapper = new JPanel(new BorderLayout()) {

                @Contract(value = " -> new", pure = true)
                @Override
                public @NotNull Dimension getPreferredSize() {
                    return new Dimension(0, cellHeight);
                }

                @Contract(value = " -> new", pure = true)
                @Override
                public @NotNull Dimension getMinimumSize() {
                    return getPreferredSize();
                }

                @Contract(value = " -> new", pure = true)
                @Override
                public @NotNull Dimension getMaximumSize() {
                    return new Dimension(Integer.MAX_VALUE, cellHeight);
                }

            };

            wrapper.setOpaque(false);
            wrapper.setBorder(new EmptyBorder(VERTICAL_GAP, 0, VERTICAL_GAP, 0)); // gap tra le card

            // "panel" è la card vera e propria con lo sfondo colorato e gli angoli arrotondati.
            // Sovrascriviamo paintComponent per disegnarla noi invece di usare lo sfondo standard.
            JPanel panel = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(@NotNull Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    // Leggiamo l'indice della riga in hover (salvato da mouseMoved)
                    int hoverIndex = list.getClientProperty("hoverIndex") != null ?
                            (int) list.getClientProperty("hoverIndex") : -1;

                    boolean isHovered = hoverIndex == index;

                    // Scegliamo il colore dello sfondo in base allo stato della card:
                    // blu se selezionata, grigio chiaro se il mouse è sopra, grigio scuro altrimenti
                    if (isSelected) {
                        g2.setColor(new Color(70, 140, 230));  // blu selezione
                    } else if (isHovered) {
                        g2.setColor(new Color(75, 75, 75));    // grigio chiaro hover
                    } else {
                        g2.setColor(new Color(55, 55, 55));    // grigio scuro normale
                    }

                    // Disegniamo il rettangolo con angoli arrotondati (18px di raggio)
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                    g2.dispose();
                }
            };

            panel.setOpaque(false); // lo sfondo è gestito da paintComponent, non da Swing
            panel.setBorder(new EmptyBorder(10, 15, 10, 15)); // padding interno della card

            // JTextArea per il nome del parcheggio, con testo a capo automatico
            JTextArea text = new JTextArea(value.getName());
            text.setWrapStyleWord(true);
            text.setLineWrap(true);
            text.setEditable(false);
            text.setOpaque(false);
            text.setFocusable(false);
            text.setFont(text.getFont().deriveFont(Font.BOLD, 17f));
            text.setForeground(new Color(230, 230, 230)); // testo quasi bianco
            // Impostiamo esplicitamente le dimensioni calcolate prima (necessario per il wrap)
            text.setPreferredSize(new Dimension(availableWidth, textHeight));

            // Assembliamo la gerarchia: testo → card (panel) → wrapper esterno
            panel.add(text, BorderLayout.CENTER);
            wrapper.add(panel, BorderLayout.CENTER);

            // Restituiamo il wrapper come componente da disegnare per questa riga
            return wrapper;
        }
    }

}