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

public class ParkingLotsListComponent extends JOverlayPanel {

    private final ParkingLotsManager lotsManager;

    private final DefaultListModel<ParkingLot> listModel = new DefaultListModel<>();
    private final JList<ParkingLot> list = new JList<>(listModel);
    private final JPanel emptyPanel = new JPanel(new GridBagLayout());

    public ParkingLotsListComponent(ParkingLotsManager lotsManager) {
        this.lotsManager = lotsManager;

        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        initComponents();
        reloadList();
    }

    private void initComponents() {
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFixedCellHeight(-1);
        list.setOpaque(false);
        list.setBorder(new EmptyBorder(5, 5, 5, 5));
        list.setCellRenderer(new ParkingLotRenderer());

        listModel.addAll(lotsManager.getParkingLots());

        list.addListSelectionListener(event -> {
            if (event.getValueIsAdjusting()) return;
            if (list.getSelectedIndex() >= 0)
                ParkIO.EVENT_MANAGER.call(new ParkingLotSelectEvent(list.getSelectedValue()));
        });

        ParkIO.EVENT_MANAGER.register(ParkingLotSelectEvent.class, event ->
                list.setSelectedValue(event.selectedParkingLot(), true)
        );

        ParkIO.EVENT_MANAGER.register(ParkingLotDeselectEvent.class, _ ->
                list.clearSelection()
        );

        ParkIO.EVENT_MANAGER.register(ParkingLotCreateEvent.class, event -> {
            listModel.addElement(event.parkingLot());
            reloadList();
        });

        ParkIO.EVENT_MANAGER.register(ParkingLotRemoveEvent.class, event -> {
            listModel.removeElement(event.removedParkingLot());
            reloadList();
        });

        list.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int index = list.locationToIndex(e.getPoint());

                if (index >= 0 && list.getCellBounds(index, index).contains(e.getPoint())) {
                    list.putClientProperty("hoverIndex", index);
                    list.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    list.putClientProperty("hoverIndex", -1);
                    list.setCursor(Cursor.getDefaultCursor());
                }

                list.repaint();
            }
        });

        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                list.putClientProperty("hoverIndex", -1);
                list.setCursor(Cursor.getDefaultCursor());
                list.repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                int index = list.locationToIndex(e.getPoint());
                if (index == -1 || !list.getCellBounds(index, index).contains(e.getPoint())) {
                    list.clearSelection();
                }
            }
        });

        emptyPanel.setOpaque(false);

        JTextArea mainLabel = new JTextArea("Nessun Parcheggio Trovato :\\");
        mainLabel.setWrapStyleWord(true);
        mainLabel.setLineWrap(true);
        mainLabel.setEditable(false);
        mainLabel.setOpaque(false);
        mainLabel.setFocusable(false);
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

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 1.0;
        emptyPanel.add(mainLabel, gbc);

        gbc.gridy = 1;
        emptyPanel.add(subLabel, gbc);
    }

    private @NotNull JScrollPane createScrollPane() {
        JScrollPane scroll = new JScrollPane(list);

        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(new EmptyBorder(5, 0, 10, 0));
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        return scroll;
    }

    public void reloadList() {
        removeAll();

        if (listModel.isEmpty()) {
            add(emptyPanel, BorderLayout.CENTER);
        } else {
            add(createScrollPane(), BorderLayout.CENTER);
        }

        revalidate();
        repaint();
    }

    private static class ParkingLotRenderer implements ListCellRenderer<ParkingLot> {

        @Override
        public @NotNull Component getListCellRendererComponent(
                @NotNull JList<? extends ParkingLot> list,
                @NotNull ParkingLot value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
        ) {
            final int VERTICAL_GAP = 4;
            final int INNER_PADDING_V = 20;

            int listInsetH = list.getInsets().left + list.getInsets().right;
            int availableWidth = list.getWidth() - listInsetH - 30;
            if (availableWidth <= 0) availableWidth = 200;

            JTextArea probe = new JTextArea(value.getName());
            probe.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 17f));
            probe.setWrapStyleWord(true);
            probe.setLineWrap(true);
            probe.setSize(new Dimension(availableWidth, Short.MAX_VALUE));

            int textHeight = probe.getPreferredSize().height;
            int cellHeight = VERTICAL_GAP * 2 + INNER_PADDING_V + textHeight;

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
            wrapper.setBorder(new EmptyBorder(VERTICAL_GAP, 0, VERTICAL_GAP, 0));

            JPanel panel = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(@NotNull Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int hoverIndex = list.getClientProperty("hoverIndex") != null ?
                            (int) list.getClientProperty("hoverIndex") : -1;

                    boolean isHovered = hoverIndex == index;

                    if (isSelected) {
                        g2.setColor(new Color(70, 140, 230));
                    } else if (isHovered) {
                        g2.setColor(new Color(75, 75, 75));
                    } else {
                        g2.setColor(new Color(55, 55, 55));
                    }

                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                    g2.dispose();
                }
            };

            panel.setOpaque(false);
            panel.setBorder(new EmptyBorder(10, 15, 10, 15));

            JTextArea text = new JTextArea(value.getName());
            text.setWrapStyleWord(true);
            text.setLineWrap(true);
            text.setEditable(false);
            text.setOpaque(false);
            text.setFocusable(false);
            text.setFont(text.getFont().deriveFont(Font.BOLD, 17f));
            text.setForeground(new Color(230, 230, 230));
            text.setPreferredSize(new Dimension(availableWidth, textHeight));

            panel.add(text, BorderLayout.CENTER);
            wrapper.add(panel, BorderLayout.CENTER);

            return wrapper;
        }
    }

}