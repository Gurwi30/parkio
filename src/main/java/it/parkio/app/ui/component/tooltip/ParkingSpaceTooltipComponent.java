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

public class ParkingSpaceTooltipComponent extends JPanel {

    private static final DateTimeFormatter FMT = DateTimeFormatter
            .ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.systemDefault());

    private static final Color BG = new Color(28, 28, 35, 230);
    private static final Color BORDER_COL = new Color(65, 65, 80);

    private @Nullable ParkingSpace currentSpace = null;

    public ParkingSpaceTooltipComponent() {
        setOpaque(false);
        setLayout(new BorderLayout());
        setVisible(false);
    }

    public void showTooltip(@NotNull ParkingSpace space, Point location) {
        if (currentSpace == space && isVisible()) return;
        currentSpace = space;

        removeAll();
        add(buildContent(space), BorderLayout.CENTER);

        revalidate();
        Dimension size = getPreferredSize();
        setBounds(location.x + 12, location.y + 12, size.width, size.height);
        setVisible(true);
        repaint();
    }

    public void hideTooltip() {
        currentSpace = null;
        setVisible(false);
    }

    @Override
    protected void paintComponent(@NotNull Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(BG);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);

        g2.setColor(BORDER_COL);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);

        g2.dispose();
    }

    private @NotNull JPanel buildContent(@NotNull ParkingSpace space) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(10, 14, 10, 14));

        JLabel title = new JLabel("Spazio #" + space.getId());
        title.setFont(title.getFont().deriveFont(Font.BOLD, 13f));
        title.setForeground(new Color(220, 220, 228));
        title.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 2)));

        JLabel type = new JLabel(space.getType().name());
        type.setFont(type.getFont().deriveFont(Font.PLAIN, 10f));
        type.setForeground(new Color(110, 110, 125));
        type.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(type);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));

        switch (space.getStatus()) {
            case ParkingSpaceStatus.Free _ -> panel.add(statusBadge("LIBERO", new Color(40, 167, 69)));

            case ParkingSpaceStatus.Occupied occ -> {
                panel.add(statusBadge("OCCUPATO", new Color(220, 53, 69)));
                panel.add(Box.createRigidArea(new Dimension(0, 8)));
                panel.add(row("Targa",  occ.getCarPlate()));
                panel.add(Box.createRigidArea(new Dimension(0, 4)));
                panel.add(row("Inizio", FMT.format(occ.getStart())));
                occ.getEnd().ifPresent(end -> {
                    panel.add(Box.createRigidArea(new Dimension(0, 4)));
                    panel.add(row("Fine", FMT.format(end)));
                });
            }

            case ParkingSpaceStatus.Reserved res -> {
                panel.add(statusBadge("RISERVATO", new Color(253, 126, 20)));
                panel.add(Box.createRigidArea(new Dimension(0, 8)));
                panel.add(row("Targa",  res.getCarPlate()));
                panel.add(Box.createRigidArea(new Dimension(0, 4)));
                panel.add(row("Inizio", FMT.format(res.getStart())));
                panel.add(Box.createRigidArea(new Dimension(0, 4)));
                panel.add(row("Fine",   FMT.format(res.getEnd())));
            }

            default -> {}
        }

        return panel;
    }

    private @NotNull JPanel statusBadge(String text, Color color) {
        JPanel badge = new JPanel() {

            @Override
            protected void paintComponent(@NotNull Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 40));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
            }

        };

        badge.setOpaque(false);
        badge.setLayout(new FlowLayout(FlowLayout.LEFT, 6, 2));
        badge.setAlignmentX(LEFT_ALIGNMENT);

        JPanel dot = new JPanel() {

            @Override
            protected void paintComponent(@NotNull Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
            }

        };

        dot.setOpaque(false);
        dot.setPreferredSize(new Dimension(8, 8));

        JLabel lbl = new JLabel(text);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 11f));
        lbl.setForeground(color);

        badge.add(dot);
        badge.add(lbl);

        return badge;
    }

    private @NotNull JPanel row(String key, String value) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);

        JLabel k = new JLabel(key);
        k.setFont(k.getFont().deriveFont(Font.PLAIN, 11f));
        k.setForeground(new Color(110, 110, 125));

        JLabel v = new JLabel(value);
        v.setFont(v.getFont().deriveFont(Font.BOLD, 11f));
        v.setForeground(new Color(200, 200, 210));

        row.add(k, BorderLayout.WEST);
        row.add(v, BorderLayout.EAST);

        return row;
    }

}