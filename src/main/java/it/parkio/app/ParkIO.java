package it.parkio.app;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.util.SystemInfo;

import it.parkio.app.manager.EventManager;
import it.parkio.app.manager.ParkingLotsManager;
import it.parkio.app.scheduler.ParkingSpaceScheduler;
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

/**
 * Classe di avvio principale dell'applicazione ParkIO.
 *
 * <p>Coordina l'inizializzazione globale del programma:</p>
 * <ul>
 *     <li>prepara i gestori condivisi;</li>
 *     <li>configura salvataggio e chiusura pulita;</li>
 *     <li>reindirizza i log;</li>
 *     <li>avvia l'interfaccia grafica Swing.</li>
 * </ul>
 */
public class ParkIO {

    /**
     * Logger principale dell'applicazione.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(ParkIO.class);

    /**
     * File usato per la persistenza dei parcheggi.
     */
    public static final File DATA_FILE = new File("parking_lots.json");

    /**
     * Gestore globale dei parcheggi, inizializzato all'avvio.
     */
    private static final ParkingLotsManager PARKING_LOTS_MANAGER = initParkingLotsManager();

    /**
     * Event manager globale usato per la comunicazione interna tra componenti.
     */
    public static final EventManager EVENT_MANAGER = new EventManager();

    /**
     * Entry point dell'applicazione.
     *
     * @param args argomenti da riga di comando, attualmente non utilizzati
     */
    public static void main(String[] args) {
        initHooks();
        redirectLogs();
        renderUI();
    }

    /**
     * Registra gli hook di spegnimento dell'applicazione.
     *
     * <p>Quando il processo termina, vengono eseguite operazioni importanti:
     * arresto dello scheduler e salvataggio dei dati su disco.</p>
     */
    private static void initHooks() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutting down ParkIO...");

            // Interrompe tutti i task temporizzati pendenti.
            ParkingSpaceScheduler.shutdown();

            try {
                // Salva lo stato corrente dei parcheggi prima dell'uscita.
                PARKING_LOTS_MANAGER.save(DATA_FILE);
            } catch (IOException e) {
                LOGGER.error("Error saving parking lots to file", e);
            }
        }));
    }

    /**
     * Reindirizza l'output standard e di errore verso il sistema di logging.
     *
     * <p>In questo modo anche eventuali stampe con {@code System.out.println}
     * e {@code System.err.println} finiscono nei log applicativi.</p>
     */
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

        // Reindirizza anche i log JUL verso SLF4J per avere un unico sistema centralizzato.
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    /**
     * Inizializza il gestore dei parcheggi leggendo il file dati.
     *
     * <p>Se il file non esiste, viene creato. Se il caricamento fallisce,
     * l'applicazione continua comunque usando un manager vuoto,
     * così la UI resta utilizzabile.</p>
     *
     * @return manager inizializzato o vuoto in caso di errore
     */
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

    /**
     * Avvia l'interfaccia grafica.
     *
     * <p>L'inizializzazione della UI avviene sull'Event Dispatch Thread di Swing,
     * come richiesto dalle buone pratiche del framework.</p>
     */
    private static void renderUI() {
        LOGGER.info("Starting ParkIO UI...");

        SwingUtilities.invokeLater(() -> {
            initFlatLafTheme();

            ParkIOFrame frame = new ParkIOFrame(PARKING_LOTS_MANAGER);
            frame.showOnTop();
        });
    }

    /**
     * Inizializza il tema grafico FlatLaf più adatto alla piattaforma corrente.
     *
     * <p>Su macOS usa il tema dedicato, sugli altri sistemi usa il tema dark standard.</p>
     */
    private static void initFlatLafTheme() {
        LOGGER.info("Setting up FlatLaf theme...");

        if (SystemInfo.isMacOS) {
            FlatMacDarkLaf.setup();
            return;
        }

        FlatDarkLaf.setup();
    }

}
