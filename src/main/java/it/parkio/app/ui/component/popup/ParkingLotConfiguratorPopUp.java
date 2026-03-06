package it.parkio.app.ui.component.popup;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;

import org.jxmapviewer.JXMapViewer;

import it.parkio.app.manager.ParkingLotsManager;
import it.parkio.app.model.Bounds;

public class ParkingLotConfiguratorPopUp extends JFrame {

     private final static int WIDTH = 400;
    private final static int HEIGHT = 300;

    private JSlider red;
    private JSlider green;
    private JSlider blue;
    private JPanel showCol;
    private JTextField nameFld;
    private ParkingLotsManager lotManager;
    private JXMapViewer mapViewer;
    private Bounds bounds;

    public ParkingLotConfiguratorPopUp(Bounds bounds, ParkingLotsManager lotManager, JXMapViewer mapViewer) {
        this.lotManager = lotManager;
        this.mapViewer = mapViewer;
        this.bounds = bounds;

        setSize(WIDTH, HEIGHT);
        setVisible(true);

        setLayout(new BorderLayout());
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JPanel name = new JPanel();
        JPanel rgb = new JPanel();
        JPanel rgbp = new JPanel();
        name.setLayout(new BoxLayout(name, BoxLayout.X_AXIS));
        rgb.setLayout(new BoxLayout(rgb, BoxLayout.Y_AXIS));
        rgbp.setLayout(new BoxLayout(rgb, BoxLayout.X_AXIS));

        JLabel nameLabel = new JLabel("Nome");
        nameFld = new JTextField();
        name.add(nameLabel);
        name.add(nameFld);

        JLabel colorLabel = new JLabel("Colore");
        red = new JSlider(0, 255, 100);
        red.addChangeListener(this::colorChange);
        green = new JSlider(0, 255, 100);
        green.addChangeListener(this::colorChange);
        blue = new JSlider(0, 255, 100);
        blue.addChangeListener(this::colorChange);
        showCol = new JPanel();
        showCol.setBackground(new Color(100, 100, 100));
        showCol.setPreferredSize(new Dimension(15, HEIGHT*2/5));
        rgb.add(colorLabel);
        rgb.add(red);
        rgb.add(green);
        rgb.add(blue);
        rgb.add(showCol);
        // rgbp.add(rgb);
        // rgbp.add(showCol);

        Object spacer1 = Box.createRigidArea(new Dimension(0, HEIGHT*1/5));
        Object spacer2 = Box.createRigidArea(new Dimension(0, HEIGHT*1/5));
        Object spacer3 = Box.createRigidArea(new Dimension(0, HEIGHT*1/5));

        // panel.add((Component)spacer1);
        panel.add(name);
        // panel.add((Component)spacer2);
        // panel.add(rgbp);
        panel.add(rgb);
        // panel.add((Component)spacer3);
        add(panel, BorderLayout.CENTER);

        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

        toolbar.add(Box.createHorizontalGlue());

        JButton saveBtn = new JButton("Salva");
        saveBtn.addActionListener(this::save);
        JButton abortBtn = new JButton("Annulla");
        abortBtn.addActionListener(this::abort);

        toolbar.add(saveBtn);
        toolbar.add(abortBtn);

        add(toolbar, BorderLayout.SOUTH);
    }

    private void save(ActionEvent ev)
    {
        String name = nameFld.getText();
        if (name == null) {
            return;
        }

        
        int red = this.red.getValue();
        int green = this.green.getValue();
        int blue = this.blue.getValue();
        Color c = new Color(red, green, blue);
        // if (m_red.isSelected()) {
        //     c = Color.RED;
        // }

        lotManager.createParkingLot(name, bounds, c);
        mapViewer.repaint();
        dispose();
    }

    private void abort(ActionEvent ev)
    {
        dispose();
    }

    private void colorChange(ChangeEvent ev) {
        int red = this.red.getValue();
        int green = this.green.getValue();
        int blue = this.blue.getValue();
        Color c = new Color(red, green, blue);
        showCol.setBackground(c);
    }

}
