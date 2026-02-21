package it.parkio.app.ui.component.windowcontrol;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class WindowControlButton extends JButton {

    private final Color baseColor;
    private float hoverProgress = 0f;

    public WindowControlButton(Color color) {
        this.baseColor = color;

        setPreferredSize(new Dimension(20, 20));
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);

        initHoverAnimation();
    }

    private void initHoverAnimation() {
        Timer hoverTimer = new Timer(15, event -> {
            if (hoverProgress < 1.0f) {
                hoverProgress = Math.min(1.0f, hoverProgress + 0.1f);
                repaint();
            }

            repaint();
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hoverProgress = 0f;
                hoverTimer.start();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hoverProgress = 0f;
                hoverTimer.stop();

                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(@NotNull Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int size = (int) (15 + 5 * hoverProgress);
        int x = (getWidth() - size) / 2;
        int y = (getHeight() - size) / 2;

        if (hoverProgress > 0f) {
            g2.setColor(baseColor.brighter());
            g2.fillOval(x - 2, y - 2, size + 4, size + 4);
        }

        g2.setColor(baseColor);
        g2.fillOval(x, y, size, size);

        g2.dispose();
    }

}
