package it.parkio.app;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.util.SystemInfo;
import it.parkio.app.manager.ParkingLotsManager;
import it.parkio.app.ui.ParkIOFrame;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

public class ParkIO {

    public static final Logger LOGGER = LoggerFactory.getLogger(ParkIO.class);
    public static final File DATA_FILE = new File("parking_lots.json");
    private static final ParkingLotsManager PARKING_LOTS_MANAGER = initParkingLotsManager();

    public static void main(String[] args) {
        initHooks();
        redirectLogs();
        renderUI();
    }

    private static void initHooks() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutting down ParkIO...");

            try {
                PARKING_LOTS_MANAGER.save(DATA_FILE);
            } catch (IOException e) {
                LOGGER.error("Error saving parking lots to file", e);
            }
        }));
    }

    private static void redirectLogs() {
        System.setOut(new PrintStream(System.out) {
            @Override
            public void println(String x) {
                LOGGER.info(x);
            }
        });

        System.setErr(new PrintStream(System.err) {
            @Override
            public void println(String x) {
                LOGGER.error(x);
            }
        });

        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Contract(" -> new")
    private static @NotNull ParkingLotsManager initParkingLotsManager() {
        try {
            if (!DATA_FILE.exists()) {
                if (DATA_FILE.getParentFile() != null) DATA_FILE.getParentFile().mkdirs();
                DATA_FILE.createNewFile();
            }

            return ParkingLotsManager.load(DATA_FILE);
        } catch (IOException e) {
            LOGGER.error("Error loading parking lots from file", e);
            LOGGER.warn("Falling back to empty parking lots manager");

            return new ParkingLotsManager();
        }
    }

    private static void renderUI() {
        LOGGER.info("Starting ParkIO UI...");

        SwingUtilities.invokeLater(() -> {
            initFlatLafTheme();

            ParkIOFrame frame = new ParkIOFrame(PARKING_LOTS_MANAGER);
            frame.showOnTop();

        });
    }

    private static void initFlatLafTheme() {
        LOGGER.info("Setting up FlatLaf theme...");

        if (SystemInfo.isMacOS) {
            FlatMacDarkLaf.setup();
            return;
        }

        FlatDarkLaf.setup();

        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);

        UIManager.put("flatlaf.menuBarEmbedded", true);
        UIManager.put("flatlaf.useWindowDecorations", true);
    }

}
