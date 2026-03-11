package it.parkio.app.ui.component.tooltip;

import it.parkio.app.model.ParkingSpace;
import it.parkio.app.model.ParkingSpaceStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Tooltip grafico che appare sopra uno spazio di parcheggio quando ci si passa
 * sopra con il mouse. Mostra le informazioni dello spazio in una piccola finestra
 * galleggiante con sfondo scuro semitrasparente.
 *
 * <p>Non è una finestra separata: è un JPanel che vive sovrapposto alla mappa
 * e viene mostrato/nascosto secondo necessità.</p>
 */
public class ParkingSpaceTooltipComponent extends JPanel {

    /**
     * Formattatore per le date: converte un Instant nella stringa "dd/MM/yyyy HH:mm".
     * Usa il fuso orario del computer corrente (systemDefault).
     */
    private static final DateTimeFormatter FMT = DateTimeFormatter
            .ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.systemDefault());

    /**
     * Colore di sfondo del tooltip: grigio molto scuro con trasparenza (alpha=230 su 255).
     * Il canale alpha permette di vedere leggermente la mappa sottostante.
     */
    private static final Color BG = new Color(28, 28, 35, 230);

    /** Colore del bordo del tooltip. */
    private static final Color BORDER_COL = new Color(65, 65, 80);

    /**
     * Lo spazio attualmente mostrato nel tooltip.
     * È @Nullable: vale null quando il tooltip è nascosto.
     */
    private @Nullable ParkingSpace currentSpace = null;

    /**
     * Costruttore: crea il tooltip inizialmente invisibile.
     */
    public ParkingSpaceTooltipComponent() {
        setOpaque(false); // lo sfondo viene disegnato da noi in paintComponent, non da Swing
        setLayout(new BorderLayout());
        setVisible(false); // nascosto fino alla prima chiamata di showTooltip()
    }

    /**
     * Mostra il tooltip vicino al punto indicato, con le informazioni dello spazio.
     * Se il tooltip sta già mostrando lo stesso spazio, non fa nulla.
     *
     * @param space    lo spazio di cui mostrare le informazioni
     * @param location la posizione (in pixel) dove posizionare il tooltip sulla mappa
     */
    public void showTooltip(@NotNull ParkingSpace space, Point location) {
        // Ottimizzazione: se stiamo già mostrando questo stesso spazio, non ricostuiamo tutto
        if (currentSpace == space && isVisible()) return;
        currentSpace = space;

        removeAll(); // rimuoviamo il contenuto precedente
        add(buildContent(space), BorderLayout.CENTER);

        revalidate(); // ricalcola le dimensioni in base al nuovo contenuto
        Dimension size = getPreferredSize();
        // Posizioniamo il tooltip 12px a destra e 12px sotto il cursore
        setBounds(location.x + 12, location.y + 12, size.width, size.height);
        setVisible(true);
        repaint();
    }

    /**
     * Nasconde il tooltip e dimentica lo spazio corrente.
     */
    public void hideTooltip() {
        currentSpace = null;
        setVisible(false);
    }

    /**
     * Disegna lo sfondo del tooltip: un rettangolo scuro semitrasparente con angoli arrotondati
     * e un bordo grigio sottile.
     *
     * <p>Sovrascriviamo paintComponent per avere uno sfondo personalizzato;
     * Swing altrimenti userebbe un rettangolo pieno senza arrotondamenti né trasparenza.</p>
     */
    @Override
    protected void paintComponent(@NotNull Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Sfondo semitrasparente
        g2.setColor(BG);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);

        // Bordo sottile (1px) per definire i contorni del tooltip
        g2.setColor(BORDER_COL);
        g2.setStroke(new BasicStroke(1f));
        // getWidth()-1 e getHeight()-1: sottraiamo 1px altrimenti il bordo verrebbe tagliato
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);

        g2.dispose();
    }

    /**
     * Costruisce il contenuto del tooltip in base allo stato dello spazio.
     * Mostra sempre: ID e tipo dello spazio.
     * Mostra in più, a seconda dello stato: badge colorato, targa, date.
     *
     * @param space lo spazio da rappresentare
     * @return il pannello con il contenuto del tooltip
     */
    private @NotNull JPanel buildContent(@NotNull ParkingSpace space) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(10, 14, 10, 14)); // margini interni del tooltip

        // Riga 1: "Spazio #N" in grassetto
        JLabel title = new JLabel("Spazio #" + space.getId());
        title.setFont(title.getFont().deriveFont(Font.BOLD, 13f));
        title.setForeground(new Color(220, 220, 228));
        title.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 2)));

        // Riga 2: tipo dello spazio (es. "STANDARD", "DISABLED") in grigio piccolo
        JLabel type = new JLabel(space.getType().name());
        type.setFont(type.getFont().deriveFont(Font.PLAIN, 10f));
        type.setForeground(new Color(110, 110, 125));
        type.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(type);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));

        // Sezione variabile in base allo stato: usiamo switch con pattern matching
        // (disponibile da Java 21: switch su oggetti con corrispondenza per tipo)
        switch (space.getStatus()) {

            // Caso FREE: solo il badge verde "LIBERO"
            case ParkingSpaceStatus.Free _ ->
                    panel.add(statusBadge("LIBERO", new Color(40, 167, 69)));

            // Caso OCCUPIED: badge rosso + targa + data inizio + data fine (se presente)
            case ParkingSpaceStatus.Occupied occ -> {
                panel.add(statusBadge("OCCUPATO", new Color(220, 53, 69)));
                panel.add(Box.createRigidArea(new Dimension(0, 8)));
                panel.add(row("Targa",  occ.getCarPlate()));
                panel.add(Box.createRigidArea(new Dimension(0, 4)));
                panel.add(row("Inizio", FMT.format(occ.getStart())));
                // ifPresent: esegue il blocco solo se la data di fine è presente (Optional non vuoto)
                occ.getEnd().ifPresent(end -> {
                    panel.add(Box.createRigidArea(new Dimension(0, 4)));
                    panel.add(row("Fine", FMT.format(end)));
                });
            }

            // Caso RESERVED: badge arancione + targa + data inizio + data fine (sempre presente)
            case ParkingSpaceStatus.Reserved res -> {
                panel.add(statusBadge("RISERVATO", new Color(253, 126, 20)));
                panel.add(Box.createRigidArea(new Dimension(0, 8)));
                panel.add(row("Targa",  res.getCarPlate()));
                panel.add(Box.createRigidArea(new Dimension(0, 4)));
                panel.add(row("Inizio", FMT.format(res.getStart())));
                panel.add(Box.createRigidArea(new Dimension(0, 4)));
                panel.add(row("Fine",   FMT.format(res.getEnd())));
            }

            // Stato non riconosciuto: non aggiungiamo nulla
            default -> {}
        }

        return panel;
    }

    /**
     * Crea un "badge" colorato con un pallino e un'etichetta di testo.
     * Usato per visualizzare lo stato con un colore codificato (verde=libero, rosso=occupato…).
     *
     * <p>Il badge ha uno sfondo molto trasparente dello stesso colore del testo
     * (alpha=40 su 255) per dare un effetto "pillola" leggero.</p>
     *
     * @param text  il testo dello stato (es. "LIBERO")
     * @param color il colore del badge (sfondo leggero, pallino e testo)
     * @return il pannello con il badge
     */
    private @NotNull JPanel statusBadge(String text, Color color) {
        // Pannello esterno: disegniamo lo sfondo semitrasparente manualmente
        JPanel badge = new JPanel() {
            @Override
            protected void paintComponent(@NotNull Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Stesso colore del testo ma quasi trasparente (alpha=40): effetto "pillola" sottile
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 40));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
            }
        };

        badge.setOpaque(false);
        // FlowLayout(LEFT, 6, 2): elementi allineati a sinistra, 6px di spazio orizzontale, 2px verticale
        badge.setLayout(new FlowLayout(FlowLayout.LEFT, 6, 2));
        badge.setAlignmentX(LEFT_ALIGNMENT);

        // Pallino colorato (piccolo cerchio pieno)
        JPanel dot = new JPanel() {
            @Override
            protected void paintComponent(@NotNull Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(0, 0, getWidth(), getHeight()); // cerchio che riempie il componente
                g2.dispose();
            }
        };
        dot.setOpaque(false);
        dot.setPreferredSize(new Dimension(8, 8)); // pallino 8×8 pixel

        // Etichetta testuale affiancata al pallino
        JLabel lbl = new JLabel(text);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 11f));
        lbl.setForeground(color); // stesso colore del pallino

        badge.add(dot);
        badge.add(lbl);

        return badge;
    }

    /**
     * Crea una riga "chiave → valore" usata per mostrare targa e date.
     * La chiave è a sinistra in grigio, il valore a destra in bianco.
     *
     * <p>Esempio: "Targa   AB123CD"</p>
     *
     * @param key   la chiave (es. "Targa", "Inizio", "Fine")
     * @param value il valore corrispondente
     * @return il pannello con la riga chiave-valore
     */
    private @NotNull JPanel row(String key, String value) {
        // BorderLayout con 10px di spazio orizzontale tra i due elementi
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);

        JLabel k = new JLabel(key);
        k.setFont(k.getFont().deriveFont(Font.PLAIN, 11f));
        k.setForeground(new Color(110, 110, 125)); // grigio → identifica la chiave

        JLabel v = new JLabel(value);
        v.setFont(v.getFont().deriveFont(Font.BOLD, 11f));
        v.setForeground(new Color(200, 200, 210)); // quasi bianco → il valore risalta

        row.add(k, BorderLayout.WEST);  // chiave a sinistra
        row.add(v, BorderLayout.EAST);  // valore a destra

        return row;
    }

}