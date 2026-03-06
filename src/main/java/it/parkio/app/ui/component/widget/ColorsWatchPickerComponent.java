package it.parkio.app.ui.component.widget;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class ColorsWatchPickerComponent extends JPanel {

    private static final Color[] PRESETS = {
            new Color(220,  53,  69),
            new Color(253, 126,  20),
            new Color(255, 193,   7),
            new Color( 40, 167,  69),
            new Color( 23, 162, 184),
            new Color( 13, 110, 253),
            new Color(111,  66, 193),
            new Color(232,  62, 140),
            new Color( 33,  37,  41),
            new Color(108, 117, 125),
            new Color(248, 249, 250),
            new Color(102, 204, 153)
    };

    private final Set<Consumer<Color>> listeners = new HashSet<>();

    private final int previewWidth;

    private JPanel selectedSwatch = null;
    private JPanel previewPanel = null;
    private Color selectedColor;

    public ColorsWatchPickerComponent(Color selectedColor, int previewWidth) {
        this.selectedColor = selectedColor;
        this.previewWidth = previewWidth;

        setOpaque(false);
        setLayout(new BorderLayout(10, 0));

        initComponents();
    }

    public ColorsWatchPickerComponent(int previewWidth) {
        this(PRESETS[ThreadLocalRandom.current().nextInt(PRESETS.length)], previewWidth);
    }

    private void initComponents() {
        add(buildGrid(), BorderLayout.CENTER);

        if (previewWidth > 0) {
            previewPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(selectedColor);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                    g2.dispose();
                }
            };

            previewPanel.setPreferredSize(new Dimension(previewWidth, 0));
            previewPanel.setOpaque(false);
            add(previewPanel, BorderLayout.EAST);
        }
    }

    private @NotNull JPanel buildGrid() {
        JPanel grid = new JPanel(new GridLayout(3, 4, 8, 8));
        grid.setOpaque(false);

        for (Color preset : PRESETS) {

            JPanel swatch = new JPanel() {
                @Override
                protected void paintComponent(@NotNull Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(preset);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.dispose();
                }
            };

            swatch.setOpaque(false);
            swatch.setPreferredSize(new Dimension(30, 30));
            swatch.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            swatch.setToolTipText(String.format("#%02X%02X%02X",
                    preset.getRed(), preset.getGreen(), preset.getBlue()));

            swatch.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    selectColor(preset, swatch);
                }
            });

            grid.add(swatch);

            if (preset.equals(selectedColor)) {
                selectedSwatch = swatch;
                SwingUtilities.invokeLater(() -> swatch.setBorder(new LineBorder(Color.WHITE, 2, true)));
            }
        }

        return grid;
    }

    private void selectColor(Color color, JPanel swatch) {
        selectedColor = color;

        if (selectedSwatch != null) { selectedSwatch.setBorder(null); selectedSwatch.repaint(); }

        selectedSwatch = swatch;

        swatch.setBorder(new LineBorder(Color.WHITE, 2, true));
        swatch.repaint();

        if (previewPanel != null) previewPanel.repaint();

        listeners.forEach(l -> l.accept(color));
    }

    public Color getSelectedColor() { return selectedColor; }

    public void addColorListener(Consumer<Color> listener) {
        listeners.add(listener);
    }

}
