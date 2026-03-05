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

    public static final Logger LOGGER = LoggerFactory.getLogger(ParkIO.class); // LOGGER SLF4J
    public static final File DATA_FILE = new File("parking_lots.json"); // FILE DATI PARCHEGGI
    private static final ParkingLotsManager PARKING_LOTS_MANAGER = initParkingLotsManager(); // INIT MANAGER

    public static void main(String[] args) {
        initHooks(); // HOOK DI SHUTDOWN
        redirectLogs(); // REINDIRIZZO LOG STDOUT/ERR
        renderUI(); // MOSTRA INTERFACCIA
    }

    private static void initHooks() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> { // ESECUZIONE ALLA CHIUSURA PROGRAMMA
            LOGGER.info("Shutting down ParkIO...");

            try {
                PARKING_LOTS_MANAGER.save(DATA_FILE); // SALVATAGGIO DATI PARCHEGGI
            } catch (IOException e) {
                LOGGER.error("Error saving parking lots to file", e); // ERRORE SALVATAGGIO
            }
        }));
    }

    private static void redirectLogs() {
        System.setOut(new PrintStream(System.out) { // REINDIRIZZAMENTO STDOUT
            @Override
            public void println(String x) {
                LOGGER.info(x);
            }
        });

        System.setErr(new PrintStream(System.err) { // REINDIRIZZAMENTO STDERR
            @Override
            public void println(String x) {
                LOGGER.error(x);
            }
        });

        SLF4JBridgeHandler.removeHandlersForRootLogger(); // RIMUOVE HANDLER PREDEFINITI
        SLF4JBridgeHandler.install(); // INSTALLA SLF4J COME LOGGER PREDEFINITO
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Contract(" -> new")
    private static @NotNull ParkingLotsManager initParkingLotsManager() { // INIZIALIZZA MANAGER PARCHEGGI
        try {
            if (!DATA_FILE.exists()) { // CREA FILE SE NON ESISTE
                if (DATA_FILE.getParentFile() != null) DATA_FILE.getParentFile().mkdirs(); // CREA CARTELLE PADRE
                DATA_FILE.createNewFile(); // CREA FILE
            }

            return ParkingLotsManager.load(DATA_FILE); // CARICA MANAGER DAL FILE
        } catch (IOException e) {
            LOGGER.error("Error loading parking lots from file", e); // ERRORE LETTURA FILE
            LOGGER.warn("Falling back to empty parking lots manager");

            return new ParkingLotsManager(); // RITORNA MANAGER VUOTO IN CASO DI ERRORE
        }
    }

    private static void renderUI() {
        LOGGER.info("Starting ParkIO UI...");

        SwingUtilities.invokeLater(() -> { // AVVIO UI SUL THREAD SWING
            initFlatLafTheme(); // IMPOSTA TEMA

            ParkIOFrame frame = new ParkIOFrame(PARKING_LOTS_MANAGER); // CREAZIONE FRAME PRINCIPALE
            frame.showOnTop(); // MOSTRA FINESTRA IN PRIMO PIANO
        });
    }

    private static void initFlatLafTheme() {
        LOGGER.info("Setting up FlatLaf theme...");

        if (SystemInfo.isMacOS) {
            FlatMacDarkLaf.setup(); // TEMA MAC
            return;
        }

        FlatDarkLaf.setup(); // TEMA WINDOWS/ALTRO
    }

}