package it.parkio.app.ui.component.popup;

import org.jxmapviewer.JXMapViewer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.plaf.ProgressBarUI;

import java.awt.event.ActionEvent;

import com.google.gson.*;
import java.awt.*;
import java.util.*;

import it.parkio.app.manager.Park;
import it.parkio.app.manager.ParkingLotsManager;
import it.parkio.app.model.ParkingLot;
import it.parkio.app.model.Bounds;

public class LotDetails extends JFrame 
{
    // private ParkingLot m_lot;
    // private ParkingLotsManager m_lotManager;
    // private JXMapViewer m_mapViewer;


    private final static int WIDTH = 400;
    private final static int HEIGHT = 300;

    private JSlider m_red;
    private JSlider m_green;
    private JSlider m_blue;
    private JPanel m_showCol;
    private JTextField m_nameFld;
    private ParkingLotsManager m_lotManager;
    private JXMapViewer m_mapViewer;
    private Bounds m_b;

    public LotDetails(Bounds b, ParkingLotsManager lotManager, JXMapViewer mapViewer)
    {
        m_lotManager = lotManager;
        m_mapViewer = mapViewer;
        m_b = b;
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
        m_nameFld = new JTextField();
        name.add(nameLabel);
        name.add(m_nameFld);

        JLabel colorLabel = new JLabel("Colore");
        m_red = new JSlider(0, 255, 100);
        m_red.addChangeListener(this::ColorChange);
        m_green = new JSlider(0, 255, 100);
        m_green.addChangeListener(this::ColorChange);
        m_blue = new JSlider(0, 255, 100);
        m_blue.addChangeListener(this::ColorChange);
        m_showCol = new JPanel();
        m_showCol.setBackground(new Color(100, 100, 100));
        m_showCol.setPreferredSize(new Dimension(15, HEIGHT*2/5));
        rgb.add(colorLabel);
        rgb.add(m_red);
        rgb.add(m_green);
        rgb.add(m_blue);
        rgb.add(m_showCol);
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
        saveBtn.addActionListener(this::Save);
        JButton abortBtn = new JButton("Annulla");
        abortBtn.addActionListener(this::Abort);

        toolbar.add(saveBtn);
        toolbar.add(abortBtn);

        add(toolbar, BorderLayout.SOUTH);
    }

    private void Save(ActionEvent ev)
    {
        String name = m_nameFld.getText();
        if (name == null) {
            return;
        }

        
        int red = m_red.getValue();
        int green = m_green.getValue();
        int blue = m_blue.getValue();
        Color c = new Color(red, green, blue);
        // if (m_red.isSelected()) {
        //     c = Color.RED;
        // }

        m_lotManager.createParkingLot(name, m_b, c);
        m_mapViewer.repaint();
        dispose();
    }

    private void Abort(ActionEvent ev)
    {
        dispose();
    }

    private void ColorChange(ChangeEvent ev)
    {
        int red = m_red.getValue();
        int green = m_green.getValue();
        int blue = m_blue.getValue();
        Color c = new Color(red, green, blue);
        m_showCol.setBackground(c);
    }
}
