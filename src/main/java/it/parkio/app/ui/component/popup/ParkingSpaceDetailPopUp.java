package it.parkio.app.ui.component.popup;

import it.parkio.app.manager.ParkingSpaceScheduler;
import it.parkio.app.model.ParkingSpace;
import it.parkio.app.model.ParkingSpaceStatus;
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

public class ParkingSpaceDetailPopUp extends JFrame {

    private static final int WIDTH  = 360;
    private static final DateTimeFormatter FMT = DateTimeFormatter
            .ofPattern("dd/MM/yyyy HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private final JXMapViewer mapViewer;
    private final ParkingSpace space;

    private JComboBox<String> statusCombo;
    private JTextField plateField;
    private JTextField startField;
    private JTextField endField;
    private JLabel plateError;
    private JLabel startError;
    private JLabel endError;
    private JPanel detailPanel;

    public ParkingSpaceDetailPopUp(JXMapViewer mapViewer, @NotNull ParkingSpace space) {
        this.mapViewer = mapViewer;
        this.space = space;

        setTitle("Spazio #" + space.getId());
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(28, 28, 32));
        root.setBorder(new EmptyBorder(24, 24, 20, 24));
        setContentPane(root);

        root.add(buildForm(), BorderLayout.CENTER);
        root.add(buildButtons(), BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(WIDTH, getHeight()));
        setVisible(true);
    }

    private @NotNull JPanel buildForm() {
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Spazio #" + space.getId());
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        title.setForeground(new Color(230, 230, 235));
        title.setAlignmentX(LEFT_ALIGNMENT);
        form.add(title);
        form.add(Box.createRigidArea(new Dimension(0, 4)));

        JLabel typeLbl = new JLabel(space.getType().name());
        typeLbl.setFont(typeLbl.getFont().deriveFont(Font.PLAIN, 11f));
        typeLbl.setForeground(new Color(120, 120, 135));
        typeLbl.setAlignmentX(LEFT_ALIGNMENT);
        form.add(typeLbl);
        form.add(Box.createRigidArea(new Dimension(0, 18)));

        JLabel statusLabel = new JLabel("Stato");
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 12f));
        statusLabel.setForeground(new Color(180, 180, 190));
        statusLabel.setAlignmentX(LEFT_ALIGNMENT);
        form.add(statusLabel);
        form.add(Box.createRigidArea(new Dimension(0, 5)));

        statusCombo = new JComboBox<>(new String[]{"FREE", "OCCUPIED", "RESERVED"});
        statusCombo.setSelectedItem(space.getStatus().getIdentifier());
        statusCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        statusCombo.setAlignmentX(LEFT_ALIGNMENT);
        styleCombo(statusCombo);
        form.add(statusCombo);
        form.add(Box.createRigidArea(new Dimension(0, 14)));

        detailPanel = new JPanel();
        detailPanel.setOpaque(false);
        detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));
        detailPanel.setAlignmentX(LEFT_ALIGNMENT);
        form.add(detailPanel);

        buildDetailFields();
        statusCombo.addActionListener(_ -> buildDetailFields());

        return form;
    }

    private void buildDetailFields() {
        detailPanel.removeAll();

        String selected = (String) statusCombo.getSelectedItem();

        if ("FREE".equals(selected)) {
            detailPanel.revalidate();
            detailPanel.repaint();
            pack();
            return;
        }

        boolean isOccupied  = "OCCUPIED".equals(selected);
        boolean isReserved  = "RESERVED".equals(selected);

        String existingPlate = null;
        String existingStart = null;
        String existingEnd = null;

        if (isOccupied && space.getStatus() instanceof ParkingSpaceStatus.Occupied occ) {
            existingPlate = occ.getCarPlate();
            existingStart = FMT.format(occ.getStart());
            existingEnd   = occ.getEnd().map(FMT::format).orElse("");
        } else if (isReserved && space.getStatus() instanceof ParkingSpaceStatus.Reserved res) {
            existingPlate = res.getCarPlate();
            existingStart = FMT.format(res.getStart());
            existingEnd   = FMT.format(res.getEnd());
        }

        JLabel plateLabel = styledLabel("Targa");
        detailPanel.add(plateLabel);
        detailPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        plateField = styledTextField(existingPlate != null ? existingPlate : "");
        detailPanel.add(plateField);
        plateError = errorLabel("La targa è obbligatoria");
        detailPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        detailPanel.add(plateError);
        detailPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JLabel startLabel = styledLabel("Inizio  (dd/MM/yyyy HH:mm:ss)");
        detailPanel.add(startLabel);
        detailPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        startField = styledTextField(existingStart != null ? existingStart : FMT.format(Instant.now()));
        detailPanel.add(startField);
        startError = errorLabel("Formato data non valido");
        detailPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        detailPanel.add(startError);
        detailPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JLabel endLabel = styledLabel(isReserved ? "Fine  (dd/MM/yyyy HH:mm:ss)" : "Fine  (dd/MM/yyyy HH:mm:ss, opzionale)");
        detailPanel.add(endLabel);
        detailPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        endField = styledTextField(existingEnd != null ? existingEnd : "");
        detailPanel.add(endField);
        endError = errorLabel("Formato data non valido");
        detailPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        detailPanel.add(endError);

        detailPanel.revalidate();
        detailPanel.repaint();
        pack();
    }

    private @NotNull JPanel buildButtons() {
        JPanel bar = new JPanel(new GridLayout(1, 2, 12, 0));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(20, 0, 0, 0));

        JButton cancelBtn = makeButton("Annulla", new Color(180, 40, 50), new Color(210, 55, 65));
        cancelBtn.addActionListener(_ -> dispose());

        JButton saveBtn = makeButton("Salva", new Color(13, 90, 210), new Color(13, 110, 253));
        saveBtn.addActionListener(_ -> trySave());

        bar.add(cancelBtn);
        bar.add(saveBtn);
        return bar;
    }

    private void trySave() {
        String selected = (String) statusCombo.getSelectedItem();

        if ("FREE".equals(selected)) {
            space.updateStatus(ParkingSpaceStatus.free());
            dispose();
            return;
        }

        boolean valid = true;

        String plate = plateField.getText().trim();
        if (plate.isEmpty()) {
            plateError.setForeground(new Color(220, 53, 69));
            valid = false;
        } else {
            plateError.setForeground(new Color(0, 0, 0, 0));
        }

        Instant start = null;
        try {
            start = FMT.parse(startField.getText().trim(), Instant::from);
            startError.setForeground(new Color(0, 0, 0, 0));
        } catch (Exception e) {
            startError.setForeground(new Color(220, 53, 69));
            valid = false;
        }

        Instant end = null;
        String endText = endField.getText().trim();
        boolean endRequired = "RESERVED".equals(selected);

        if (!endText.isEmpty()) {
            try {
                end = FMT.parse(endText, Instant::from);
                endError.setForeground(new Color(0, 0, 0, 0));
            } catch (Exception e) {
                endError.setForeground(new Color(220, 53, 69));
                valid = false;
            }
        } else if (endRequired) {
            endError.setForeground(new Color(220, 53, 69));
            valid = false;
        } else {
            endError.setForeground(new Color(0, 0, 0, 0));
        }

        if (!valid) return;

        if ("OCCUPIED".equals(selected)) {
            space.updateStatus(end != null
                    ? ParkingSpaceStatus.occupied(plate, start, end)
                    : ParkingSpaceStatus.occupied(plate, start));
        } else {
            space.updateStatus(ParkingSpaceStatus.reserved(plate, start, end));
        }

        ParkingSpaceScheduler.schedule(space, mapViewer);

        mapViewer.repaint();
        dispose();
    }

    private @NotNull JLabel styledLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 11f));
        lbl.setForeground(new Color(160, 160, 175));
        lbl.setAlignmentX(LEFT_ALIGNMENT);

        return lbl;
    }

    private @NotNull JLabel errorLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(lbl.getFont().deriveFont(Font.ITALIC, 11f));
        lbl.setForeground(new Color(0, 0, 0, 0));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    private @NotNull JTextField styledTextField(String value) {
        JTextField tf = new JTextField(value);
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        tf.setPreferredSize(new Dimension(WIDTH - 48, 32));
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
                        new LineBorder(new Color(13, 110, 253), 1, true),
                        new EmptyBorder(0, 8, 0, 8)));
            }
            @Override
            public void focusLost(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(65, 65, 78), 1, true),
                        new EmptyBorder(0, 8, 0, 8)));
            }

        });

        return tf;
    }

    private void styleCombo(@NotNull JComboBox<?> combo) {
        combo.setBackground(new Color(42, 42, 50));
        combo.setForeground(new Color(220, 220, 228));
        combo.setBorder(new LineBorder(new Color(65, 65, 78), 1, true));
        combo.setFocusable(false);
    }

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