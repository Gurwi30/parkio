package it.parkio.app.ui.component.popup;

import it.parkio.app.manager.ParkingLotsManager;
import it.parkio.app.model.Bounds;
import it.parkio.app.ui.component.widget.ColorsWatchPickerComponent;
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

public class ParkingLotConfiguratorPopUp extends JFrame {

    private static final int WIDTH = 380;
    private static final int HEIGHT = 460;

    private JTextField nameFld;
    private JLabel nameError;
    private ColorsWatchPickerComponent colorPicker;

    private final ParkingLotsManager lotManager;
    private final JXMapViewer mapViewer;
    private final Bounds bounds;

    public ParkingLotConfiguratorPopUp(Bounds bounds, ParkingLotsManager lotManager, JXMapViewer mapViewer) {
        this.lotManager = lotManager;
        this.mapViewer = mapViewer;
        this.bounds = bounds;

        setTitle("Nuovo Parcheggio");
        setSize(WIDTH, HEIGHT);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(28, 28, 32));
        root.setBorder(new EmptyBorder(24, 24, 20, 24));
        setContentPane(root);

        root.add(buildForm(), BorderLayout.CENTER);
        root.add(buildButtons(), BorderLayout.SOUTH);

        setVisible(true);
    }

    private @NotNull JPanel buildForm() {
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Nuovo Parcheggio");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setForeground(new Color(230, 230, 235));
        title.setAlignmentX(LEFT_ALIGNMENT);
        form.add(title);
        form.add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel nameLabel = new JLabel("Nome *");
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 13f));
        nameLabel.setForeground(new Color(180, 180, 190));
        nameLabel.setAlignmentX(LEFT_ALIGNMENT);
        form.add(nameLabel);
        form.add(Box.createRigidArea(new Dimension(0, 6)));

        nameFld = new JTextField();
        nameFld.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        nameFld.setPreferredSize(new Dimension(0, 38));
        nameFld.setFont(nameFld.getFont().deriveFont(14f));
        nameFld.setBackground(new Color(42, 42, 50));
        nameFld.setForeground(new Color(220, 220, 228));
        nameFld.setCaretColor(new Color(220, 220, 228));
        nameFld.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(65, 65, 78), 1, true),
                new EmptyBorder(0, 10, 0, 10)));
        nameFld.setAlignmentX(LEFT_ALIGNMENT);
        nameFld.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                nameFld.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(13, 110, 253), 1, true),
                        new EmptyBorder(0, 10, 0, 10)));
            }
            @Override public void focusLost(FocusEvent e) {
                nameFld.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(65, 65, 78), 1, true),
                        new EmptyBorder(0, 10, 0, 10)));
            }
        });

        form.add(nameFld);

        nameError = new JLabel("Il nome è obbligatorio");
        nameError.setFont(nameError.getFont().deriveFont(Font.ITALIC, 11f));
        nameError.setForeground(new Color(220, 53, 69));
        nameError.setAlignmentX(LEFT_ALIGNMENT);
        nameError.setVisible(false);
        form.add(Box.createRigidArea(new Dimension(0, 4)));
        form.add(nameError);
        form.add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel colorLabel = new JLabel("Colore");
        colorLabel.setFont(colorLabel.getFont().deriveFont(Font.BOLD, 13f));
        colorLabel.setForeground(new Color(180, 180, 190));
        colorLabel.setAlignmentX(LEFT_ALIGNMENT);
        form.add(colorLabel);
        form.add(Box.createRigidArea(new Dimension(0, 10)));

        colorPicker = new ColorsWatchPickerComponent(80);
        colorPicker.setAlignmentX(LEFT_ALIGNMENT);
        colorPicker.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        form.add(colorPicker);

        return form;
    }

    private @NotNull JPanel buildButtons() {
        JPanel bar = new JPanel(new GridLayout(1, 2, 12, 0));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(20, 0, 0, 0));

        JButton abortBtn = makeButton("Annulla", new Color(180, 40, 50), new Color(210, 55, 65));
        abortBtn.addActionListener(_ -> dispose());

        JButton createBtn = makeButton("Crea", new Color(13, 90, 210), new Color(13, 110, 253));
        createBtn.addActionListener(_ -> create());

        bar.add(abortBtn);
        bar.add(createBtn);
        return bar;
    }

    private @NotNull JButton makeButton(String text, Color base, Color hover) {
        JButton btn = new JButton(text) {
            private Color current = base;

            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        current = hover; repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e)  {
                        current = base;  repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(@NotNull Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(current);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont().deriveFont(Font.BOLD, 14f));

                FontMetrics fm = g2.getFontMetrics();

                int tx = (getWidth()  - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;

                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        btn.setFont(btn.getFont().deriveFont(Font.BOLD, 14f));
        btn.setPreferredSize(new Dimension(0, 44));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return btn;
    }

    private void create() {
        String name = nameFld.getText().trim();

        if (name.isEmpty()) {
            nameError.setVisible(true);
            nameFld.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(220, 53, 69), 1, true),
                    new EmptyBorder(0, 10, 0, 10)));

            nameFld.requestFocus();
            return;
        }

        lotManager.createParkingLot(name, bounds, colorPicker.getSelectedColor());
        mapViewer.repaint();
        dispose();
    }

}