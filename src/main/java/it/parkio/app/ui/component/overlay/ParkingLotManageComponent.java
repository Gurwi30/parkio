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

public class ParkingLotManageComponent extends JOverlayPanel {

    private final ParkingLotsManager lotsManager;
    private @Nullable ParkingLot parkingLot;

    private JTextField nameField;
    private JLabel nameError;
    private ColorsWatchPickerComponent colorPicker;
    private DefaultComboBoxModel<ParkingSpace> spaceModel;
    private JComboBox<ParkingSpace> spaceCombo;
    private JLabel spacesLabel;

    private JComboBox<ParkingSpace.Type> typeCombo;
    private JPanel spaceDetailPanel;

    public ParkingLotManageComponent(ParkingLotsManager lotsManager) {
        this.lotsManager = lotsManager;

        setLayout(new BorderLayout());
        setOpaque(false);

        ParkIO.EVENT_MANAGER.register(ParkingLotSelectEvent.class, event ->
                setParkingLot(event.selectedParkingLot())
        );

        ParkIO.EVENT_MANAGER.register(ParkingSpaceCreateEvent.class, event -> {
            if (parkingLot == null) return;
            if (!parkingLot.getSpaces().contains(event.space())) return;
            spaceModel.addElement(event.space());
            updateSpacesLabel();
            refreshSpaceDetail();
        });

        ParkIO.EVENT_MANAGER.register(ParkingSpaceRemoveEvent.class, event -> {
            if (parkingLot == null) return;
            spaceModel.removeElement(event.removedSpace());
            updateSpacesLabel();
            refreshSpaceDetail();
        });

        showEmpty();
    }

    private void showEmpty() {
        removeAll();

        JLabel lbl = new JLabel("Seleziona un parcheggio");
        lbl.setForeground(Color.GRAY);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        add(lbl, BorderLayout.CENTER);

        setVisible(false);

        revalidate();
        repaint();
    }

    private void setParkingLot(@Nullable ParkingLot lot) {
        if (this.parkingLot == lot) return;
        this.parkingLot = lot;

        removeAll();

        if (lot == null) {
            showEmpty();
            return;
        }

        if (!isVisible()) setVisible(true);

        buildUI(lot);
        revalidate();
        repaint();
    }

    private void updateSpacesLabel() {
        if (spacesLabel == null || parkingLot == null) return;
        spacesLabel.setText("Spazi (" + spaceModel.getSize() + ")");
    }

    private void buildUI(@NotNull ParkingLot lot) {
        JPanel root = new JPanel();
        root.setOpaque(false);
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(new EmptyBorder(16, 16, 12, 16));

        root.add(buildTopForm(lot));
        root.add(buildSpacesPanel(lot));
        root.add(Box.createRigidArea(new Dimension(0, 12)));
        root.add(buildActionButtons(lot));

        JScrollPane scroll = new JScrollPane(root);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        add(scroll, BorderLayout.CENTER);
    }

    private @NotNull JPanel buildTopForm(@NotNull ParkingLot lot) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel idLabel = new JLabel("ID #" + lot.getId());
        idLabel.setFont(idLabel.getFont().deriveFont(Font.BOLD, 11f));
        idLabel.setForeground(new Color(120, 120, 135));
        idLabel.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(idLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));

        JLabel nameLabel = new JLabel("Nome *");
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 12f));
        nameLabel.setForeground(new Color(180, 180, 190));
        nameLabel.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(nameLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 4)));

        nameField = styledTextField(lot.getName());
        panel.add(nameField);

        nameError = new JLabel("Il nome è obbligatorio");
        nameError.setFont(nameError.getFont().deriveFont(Font.ITALIC, 11f));
        nameError.setForeground(new Color(0, 0, 0, 0));
        nameError.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(Box.createRigidArea(new Dimension(0, 3)));
        panel.add(nameError);

        panel.add(Box.createRigidArea(new Dimension(0, 12)));

        JLabel colorLabel = new JLabel("Colore");
        colorLabel.setFont(colorLabel.getFont().deriveFont(Font.BOLD, 12f));
        colorLabel.setForeground(new Color(180, 180, 190));
        colorLabel.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(colorLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 6)));

        colorPicker = new ColorsWatchPickerComponent(lot.getColor(), 60);
        colorPicker.setAlignmentX(LEFT_ALIGNMENT);
        colorPicker.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        panel.add(colorPicker);

        panel.add(Box.createRigidArea(new Dimension(0, 14)));

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(60, 60, 70));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(sep);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));

        return panel;
    }

    private @NotNull JTextField styledTextField(String value) {
        JTextField tf = new JTextField(value);
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        tf.setPreferredSize(new Dimension(0, 34));
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

    private @NotNull JPanel buildSpacesPanel(@NotNull ParkingLot lot) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        spacesLabel = new JLabel("Spazi (" + lot.getSpaces().size() + ")");
        spacesLabel.setFont(spacesLabel.getFont().deriveFont(Font.BOLD, 12f));
        spacesLabel.setForeground(new Color(180, 180, 190));
        spacesLabel.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(spacesLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 6)));

        spaceModel = new DefaultComboBoxModel<>();
        lot.getSpaces().forEach(spaceModel::addElement);

        spaceCombo = new JComboBox<>(spaceModel);
        spaceCombo.setRenderer(new SpaceListRenderer());
        spaceCombo.setPreferredSize(new Dimension(0, 26));
        spaceCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        spaceCombo.setAlignmentX(LEFT_ALIGNMENT);
        styleCombo(spaceCombo);

        JButton removeSpaceBtn = new JButton("✕") {
            private Color current = new Color(180, 40, 50);

            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        current = new Color(210, 55, 65);
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        current = new Color(180, 40, 50);
                        repaint();
                    }
                });
            }

            @Override protected void paintComponent(@NotNull Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(current);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont().deriveFont(Font.BOLD, 12f));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth()  - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };

        removeSpaceBtn.setPreferredSize(new Dimension(26, 26));
        removeSpaceBtn.setMinimumSize(new Dimension(26, 26));
        removeSpaceBtn.setMaximumSize(new Dimension(26, 26));
        removeSpaceBtn.setContentAreaFilled(false);
        removeSpaceBtn.setBorderPainted(false);
        removeSpaceBtn.setFocusPainted(false);
        removeSpaceBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        removeSpaceBtn.addActionListener(_ -> {
            ParkingSpace selected = (ParkingSpace) spaceCombo.getSelectedItem();
            if (selected == null || parkingLot == null) return;
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Rimuovere lo spazio #" + selected.getId() + "?",
                    "Conferma", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                parkingLot.removeSpace(selected.getId());
                ParkIO.EVENT_MANAGER.call(new ParkingSpaceRemoveEvent(selected));
            }
        });

        JPanel comboRow = new JPanel(new BorderLayout(6, 0));
        comboRow.setOpaque(false);
        comboRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        comboRow.setAlignmentX(LEFT_ALIGNMENT);
        comboRow.add(spaceCombo, BorderLayout.CENTER);
        comboRow.add(removeSpaceBtn, BorderLayout.EAST);
        panel.add(comboRow);

        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        spaceDetailPanel = new JPanel();
        spaceDetailPanel.setOpaque(false);
        spaceDetailPanel.setLayout(new BoxLayout(spaceDetailPanel, BoxLayout.Y_AXIS));
        spaceDetailPanel.setAlignmentX(LEFT_ALIGNMENT);
        refreshSpaceDetail();

        spaceCombo.addActionListener(_ -> refreshSpaceDetail());

        panel.add(spaceDetailPanel);

        return panel;
    }

    private void refreshSpaceDetail() {
        spaceDetailPanel.removeAll();

        ParkingSpace space = (ParkingSpace) spaceCombo.getSelectedItem();
        if (space == null) {
            spaceDetailPanel.revalidate();
            spaceDetailPanel.repaint();
            return;
        }

        JLabel typeLabel = new JLabel("Tipo");
        typeLabel.setFont(typeLabel.getFont().deriveFont(Font.BOLD, 11f));
        typeLabel.setForeground(new Color(160, 160, 175));
        typeLabel.setAlignmentX(LEFT_ALIGNMENT);
        spaceDetailPanel.add(typeLabel);
        spaceDetailPanel.add(Box.createRigidArea(new Dimension(0, 4)));

        typeCombo = new JComboBox<>(ParkingSpace.Type.values());
        typeCombo.setSelectedItem(space.getType());
        typeCombo.setPreferredSize(new Dimension(0, 26));
        typeCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        typeCombo.setAlignmentX(LEFT_ALIGNMENT);
        styleCombo(typeCombo);
        typeCombo.addActionListener(_ -> space.setType((ParkingSpace.Type) typeCombo.getSelectedItem()));
        spaceDetailPanel.add(typeCombo);

        spaceDetailPanel.revalidate();
        spaceDetailPanel.repaint();
    }

    private <E> void styleCombo(@NotNull JComboBox<E> combo) {
        combo.setBackground(new Color(42, 42, 50));
        combo.setForeground(new Color(220, 220, 228));
        combo.setBorder(new LineBorder(new Color(65, 65, 78), 1, true));
        combo.setFocusable(false);
    }

    private @NotNull JPanel buildActionButtons(@NotNull ParkingLot lot) {
        JPanel bar = new JPanel(new GridLayout(1, 2, 10, 0));
        bar.setOpaque(false);
        bar.setAlignmentX(LEFT_ALIGNMENT);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JButton deleteBtn = makeButton("Elimina", new Color(160, 35, 45), new Color(200, 50, 60));
        deleteBtn.addActionListener(_ -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Eliminare il parcheggio \"" + lot.getName() + "\"?",
                    "Conferma eliminazione", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                lotsManager.removeParkingLot(lot.getId());
                setParkingLot(null);
            }
        });

        JButton saveBtn = makeButton("Salva ed Esci", new Color(13, 90, 210), new Color(13, 110, 253));
        saveBtn.addActionListener(_ -> {
            String name = nameField.getText().trim();

            if (name.isEmpty()) {
                nameError.setForeground(new Color(220, 53, 69));
                nameField.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(220, 53, 69), 1, true),
                        new EmptyBorder(0, 8, 0, 8)));

                nameField.requestFocus();
                return;
            }

            lot.setName(name);
            lot.setColor(colorPicker.getSelectedColor());

            setParkingLot(null);
            ParkIO.EVENT_MANAGER.call(new ParkingLotDeselectEvent());
        });

        bar.add(deleteBtn);
        bar.add(saveBtn);
        return bar;
    }

    private @NotNull JButton makeButton(String text, Color base, Color hover) {
        return makeButtonInternal(text, base, hover, 13f, 40);
    }

    private @NotNull JButton makeButtonInternal(String text, Color base, Color hover, float fontSize, int height) {
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
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont().deriveFont(Font.BOLD, fontSize));

                FontMetrics fm = g2.getFontMetrics();

                int tx = (getWidth()  - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;

                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };

        btn.setFont(btn.getFont().deriveFont(Font.BOLD, fontSize));
        btn.setPreferredSize(new Dimension(0, height));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return btn;
    }

    private static class SpaceListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof ParkingSpace ps)
                setText("Spazio #" + ps.getId() + "  [" + ps.getType() + "]  — " + ps.getStatus().getIdentifier());

            setBackground(isSelected ? new Color(13, 90, 210) : new Color(42, 42, 50));
            setForeground(new Color(220, 220, 228));
            setBorder(new EmptyBorder(2, 8, 2, 8));
            return this;
        }
    }

}