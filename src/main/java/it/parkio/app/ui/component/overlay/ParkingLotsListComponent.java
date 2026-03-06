package it.parkio.app.ui.component.overlay;

import it.parkio.app.manager.ParkingLotsManager;
import it.parkio.app.model.ParkingLot;
import it.parkio.app.ui.component.map.MapComponent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class ParkingLotsListComponent extends JOverlayPanel {

    private final MapComponent map;
    private final ParkingLotsManager lotsManager;

    private final DefaultListModel<ParkingLot> listModel = new DefaultListModel<>();
    private final JList<ParkingLot> list = new JList<>(listModel);
    private final JPanel emptyPanel = new JPanel(new GridBagLayout());

    public ParkingLotsListComponent(MapComponent map, ParkingLotsManager lotsManager) {
        this.map = map;
        this.lotsManager = lotsManager;

        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        initComponents();
        reload();
    }

    private void initComponents() {
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFixedCellHeight(60);
        list.setOpaque(false);
        list.setBorder(new EmptyBorder(5,5,5,5));
        list.setCellRenderer(new ParkingLotRenderer());

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
        });

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(new EmptyBorder(5,0,10,0));

        add(scroll, BorderLayout.CENTER);

        emptyPanel.setOpaque(false);
        JLabel mainLabel = new JLabel("Nessun Parcheggio Trovato :\\", SwingConstants.CENTER);
        mainLabel.setFont(mainLabel.getFont().deriveFont(Font.BOLD, 18f));
        mainLabel.setForeground(Color.LIGHT_GRAY);

        JLabel subLabel = new JLabel("Crea un parcheggio premendo con tasto destro sulla mappa e trascinando", SwingConstants.CENTER);
        subLabel.setFont(subLabel.getFont().deriveFont(Font.PLAIN, 12f));
        subLabel.setForeground(Color.GRAY);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0,0,10,0);
        emptyPanel.add(mainLabel, gbc);

        gbc.gridy = 1;
        emptyPanel.add(subLabel, gbc);
    }

    public void reload() {
        listModel.clear();
        for (ParkingLot lot : lotsManager.getParkingLots()) {
            listModel.addElement(lot);
        }

        removeAll();
        if (lotsManager.getParkingLots().isEmpty()) {
            add(emptyPanel, BorderLayout.CENTER);
        } else {
            JScrollPane scroll = new JScrollPane(list);
            scroll.setBorder(null);
            scroll.setOpaque(false);
            scroll.getViewport().setOpaque(false);
            scroll.setBorder(new EmptyBorder(5,0,10,0));
            add(scroll, BorderLayout.CENTER);
        }
        revalidate();
        repaint();
    }

    private static class ParkingLotRenderer implements ListCellRenderer<ParkingLot> {

        @Override
        public @NotNull Component getListCellRendererComponent(
                JList<? extends ParkingLot> list,
                @NotNull ParkingLot value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
        ) {
            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.setOpaque(false);
            wrapper.setBorder(new EmptyBorder(4,0,4,0));

            JPanel panel = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(@NotNull Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int hoverIndex = list.getClientProperty("hoverIndex") != null ?
                            (int) list.getClientProperty("hoverIndex") : -1;
                    boolean isHovered = hoverIndex == index;

                    if (isSelected) {
                        g2.setColor(new Color(70,140,230));
                    } else if (isHovered) {
                        g2.setColor(new Color(75,75,75));
                    } else {
                        g2.setColor(new Color(55,55,55));
                    }

                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                    g2.dispose();
                }
            };

            panel.setOpaque(false);
            panel.setBorder(new EmptyBorder(10,15,10,15));

            JLabel label = new JLabel(value.getName(), SwingConstants.CENTER);
            label.setFont(label.getFont().deriveFont(Font.BOLD, 17f));
            label.setForeground(new Color(230,230,230));

            panel.add(label, BorderLayout.CENTER);
            wrapper.add(panel, BorderLayout.CENTER);

            return wrapper;
        }
    }

}