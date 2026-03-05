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
        Runtime.getRuntime().addShutdownHook(new Thread(() -> { // DEFINENDO ISTRUZIONI DA ESEGUIRE A CHIUSURA PROGRAMMA
            LOGGER.info("Shutting down ParkIO...");

            try {
                PARKING_LOTS_MANAGER.save(DATA_FILE); // SALVANDO DATI PARCHEGGI
            } catch (IOException e) {
                LOGGER.error("Error saving parking lots to file", e);
            }
        }));
    }

    private static void redirectLogs() {
        System.setOut(new PrintStream(System.out) { // REINDIRIZZANDO LOG MESSAGGI AL LOGGER CUSTOM
            @Override
            public void println(String x) {
                LOGGER.info(x);
            }
        });

        System.setErr(new PrintStream(System.err) { // REINDIRIZZANDO LOG ERRORI AL LOGGER CUSTOM
            @Override
            public void println(String x) {
                LOGGER.error(x);
            }
        });

        SLF4JBridgeHandler.removeHandlersForRootLogger(); // RIMOSSI LOGGER PREDEFINITI
        SLF4JBridgeHandler.install(); // IMPOSTANDO SLF4J COME LOGGER PREDEFINITO
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Contract(" -> new")
    private static @NotNull ParkingLotsManager initParkingLotsManager() { // CREANDO IL MANAGER
        try {
            if (!DATA_FILE.exists()) { // CREANDO IL FILE DATA_FILE SE NON ESISTE
                if (DATA_FILE.getParentFile() != null) DATA_FILE.getParentFile().mkdirs();
                DATA_FILE.createNewFile();
            }

            return ParkingLotsManager.load(DATA_FILE); // CREANDO IL MANAGER
        } catch (IOException e) {
            LOGGER.error("Error loading parking lots from file", e);
            LOGGER.warn("Falling back to empty parking lots manager");

            return new ParkingLotsManager(); // CREANDO MANAGER VUOTO IN CASO DI ERRORI DURANTE LA CREAZIONE E LETTURA DEL DATA_FILE
        }
    }

    private static void renderUI() {
        LOGGER.info("Starting ParkIO UI...");

        SwingUtilities.invokeLater(() -> { // CREANDO FINESTRA INTERFACCIA QUANDO SWING E' PRONTO
            initFlatLafTheme(); // IMPOSTANDO TEMA

            ParkIOFrame frame = new ParkIOFrame(PARKING_LOTS_MANAGER);
            frame.showOnTop(); // MOSTRANDO FINESTRA IN ALTO
        });
    }

    private static void initFlatLafTheme() {
        LOGGER.info("Setting up FlatLaf theme...");

        if (SystemInfo.isMacOS) {
            FlatMacDarkLaf.setup(); // IMPOSTANDO TEMA PER MAC
            return;
        }

        FlatDarkLaf.setup(); // IMPOSTANDO TEMA PER WINDOWS
    }

}
