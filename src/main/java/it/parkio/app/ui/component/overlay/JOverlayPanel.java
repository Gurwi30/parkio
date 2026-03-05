package it.parkio.app.ui.component.overlay;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class JOverlayPanel extends JPanel { // crea pannelli semitrasparenti da mettere in sovraimpresione

    @Override
    protected void paintComponent(@NotNull Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create(); // crea copia di Graphics per non modificare l'originale
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // attiva antialiasing

        g2.setColor(new Color(30, 30, 30, 210)); // colore semitrasparente per overlay
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20); // disegna rettangolo arrotondato che riempie il pannello

        g2.dispose(); // rilascia risorse della copia Graphics
    }

}