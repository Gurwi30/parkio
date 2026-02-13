package it.parkio.app;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.util.SystemInfo;
import it.parkio.app.manager.ParkingLotsManager;
import it.parkio.app.ui.ParkIOFrame;

import javax.swing.*;

public class ParkIO {

    private static final ParkingLotsManager parkingLotsManager = new ParkingLotsManager();

    public static void main(String[] args) throws UnsupportedLookAndFeelException {
        renderUI();
    }

    private static void renderUI() throws UnsupportedLookAndFeelException {
        SwingUtilities.invokeLater(() -> {
            if (SystemInfo.isLinux || SystemInfo.isWindows) {
                FlatDarkLaf.setup();

                JFrame.setDefaultLookAndFeelDecorated(true);
                JDialog.setDefaultLookAndFeelDecorated(true);

                UIManager.put("flatlaf.menuBarEmbedded", true);
                UIManager.put("flatlaf.useWindowDecorations", true);
            }

            if (SystemInfo.isMacOS) {
                FlatDarkLaf.setup();
            }

            ParkIOFrame frame = new ParkIOFrame(parkingLotsManager);

            frame.setAlwaysOnTop(true);
            frame.setVisible(true);
            frame.toFront();
            frame.requestFocus();
            frame.setAlwaysOnTop(false);
        });
    }

}
