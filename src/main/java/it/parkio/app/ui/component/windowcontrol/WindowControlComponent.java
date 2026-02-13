package it.parkio.app.ui.component.windowcontrol;

import it.parkio.app.ui.ParkIOFrame;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.atomic.AtomicReference;

public class WindowControlComponent extends JPanel {

    private static final int TITLE_BAR_HEIGHT = 30;

    public WindowControlComponent(@NotNull JFrame root) {
        setLayout(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        setOpaque(false);
        setPreferredSize(new Dimension(root.getWidth(), TITLE_BAR_HEIGHT));

        add(initButton(new Color(255, 189, 46), _ -> {
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window instanceof JFrame) ((JFrame) window).setState(JFrame.ICONIFIED);
        }));

        add(initButton(new Color(39, 201, 63), _ -> {
            Window window = SwingUtilities.getWindowAncestor(this);

            if (window instanceof JFrame frame) {
                frame.setExtendedState(
                        frame.getExtendedState() == JFrame.MAXIMIZED_BOTH ? JFrame.NORMAL : JFrame.MAXIMIZED_BOTH
                );
            }
        }));

        add(initButton(new Color(255, 95, 86), _ -> {
            Window window = SwingUtilities.getWindowAncestor(this);
            window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
        }));

        initListeners();
    }

    @Contract("_, _ -> new")
    private @NotNull WindowControlButton initButton(Color color, ActionListener listener) {
        WindowControlButton windowControlButton = new WindowControlButton(color);
        windowControlButton.addActionListener(listener);

        return windowControlButton;
    }

    private void initListeners() {
        AtomicReference<Point> initialClick = new AtomicReference<>();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                requestFocus();
            }

            @Override
            public void mousePressed(MouseEvent event) {
                initialClick.set(event.getPoint());
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent event) {
                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(WindowControlComponent.this);
                if (frame == null) return;

                int thisX = frame.getLocation().x;
                int thisY = frame.getLocation().y;

                int xMoved = event.getX() - initialClick.get().x;
                int yMoved = event.getY() - initialClick.get().y;

                int X = thisX + xMoved;
                int Y = thisY + yMoved;

                frame.setLocation(X, Y);
            }
        });
    }

}
