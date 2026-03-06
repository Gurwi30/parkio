package it.parkio.app.ui.component.overlay;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class JOverlayPanel extends JPanel {

    public JOverlayPanel() {
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
    }

    @Override
    protected void paintComponent(@NotNull Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(30, 30, 30, 210));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

        g2.dispose();
    }

}
