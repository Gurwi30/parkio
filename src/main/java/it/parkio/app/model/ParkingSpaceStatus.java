package it.parkio.app.model;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Optional;

/**
 * Modella lo stato di un posto auto.
 *
 * <p>È una sealed interface, quindi i tipi ammessi sono chiusi e noti:
 * {@link Free}, {@link Occupied} e {@link Reserved}.</p>
 *
 * <p>Questa scelta rende il modello più sicuro:
 * il compilatore sa esattamente quali stati sono possibili
 * e gli {@code switch} diventano più chiari e robusti.</p>
 */
public sealed interface ParkingSpaceStatus {

    /**
     * Restituisce un identificatore testuale dello stato.
     *
     * @return nome logico dello stato, ad esempio {@code FREE} o {@code OCCUPIED}
     */
    String getIdentifier();

    /**
     * Factory method per ottenere lo stato "libero".
     *
     * <p>Usa un'istanza singleton, perché lo stato libero
     * non contiene dati variabili.</p>
     *
     * @return stato libero
     */
    static ParkingSpaceStatus free() {
        return Free.FREE;
    }

    /**
     * Factory method per creare uno stato "occupato" con inizio e fine opzionale.
     *
     * @param carPlate targa del veicolo
     * @param start    istante di inizio occupazione
     * @param end      eventuale fine prevista
     * @return nuovo stato occupato
     */
    @Contract(value = "_, _, _ -> new", pure = true)
    static @NotNull ParkingSpaceStatus occupied(String carPlate, Instant start, @Nullable Instant end) {
        return new ParkingSpaceStatus.Occupied(carPlate, start, end);
    }

    /**
     * Variante semplificata dello stato occupato senza fine definita.
     *
     * @param carPlate targa del veicolo
     * @param start    istante di inizio
     * @return nuovo stato occupato
     */
    @Contract(value = "_, _ -> new", pure = true)
    static @NotNull ParkingSpaceStatus occupied(String carPlate, Instant start) {
        return occupied(carPlate, start, null);
    }

    /**
     * Factory method per creare uno stato "riservato".
     *
     * @param carPlate targa associata alla prenotazione
     * @param start    inizio della prenotazione
     * @param end      fine della prenotazione
     * @return nuovo stato riservato
     */
    @Contract("_, _, _ -> new")
    static @NotNull ParkingSpaceStatus reserved(String carPlate, Instant start, Instant end) {
        return new ParkingSpaceStatus.Reserved(carPlate, start, end);
    }

    /**
     * Verifica se uno stato è libero.
     *
     * @param status stato da controllare
     * @return {@code true} se è un {@link Free}
     */
    static boolean isFree(ParkingSpaceStatus status) {
        return status instanceof Free;
    }

    /**
     * Verifica se uno stato è occupato.
     *
     * @param status stato da controllare
     * @return {@code true} se è un {@link Occupied}
     */
    static boolean isOccupied(ParkingSpaceStatus status) {
        return status instanceof Occupied;
    }

    /**
     * Verifica se uno stato è riservato.
     *
     * @param status stato da controllare
     * @return {@code true} se è un {@link Reserved}
     */
    static boolean isReserved(ParkingSpaceStatus status) {
        return status instanceof Reserved;
    }

    /**
     * Stato che rappresenta un posto libero.
     *
     * <p>Non contiene informazioni aggiuntive perché il posto
     * non è associato a nessun veicolo né intervallo temporale.</p>
     */
    final class Free implements ParkingSpaceStatus {

        /**
         * Unica istanza condivisa dello stato libero.
         */
        private static final Free FREE = new Free();

        /**
         * Costruttore privato per forzare l'uso della singleton.
         */
        private Free() {}

        /**
         * @return identificatore testuale dello stato libero
         */
        @Contract(pure = true)
        @Override
        public @NotNull String getIdentifier() {
            return "FREE";
        }

        @Override
        public String toString() {
            return "Free";
        }
    }

    /**
     * Stato che rappresenta un posto attualmente occupato.
     *
     * <p>Contiene la targa del veicolo, l'istante di inizio
     * e una eventuale data di fine.</p>
     */
    final class Occupied implements ParkingSpaceStatus {

        private final String carPlate;
        private final Instant start;
        private final @Nullable Instant end;

        /**
         * Costruisce uno stato occupato.
         *
         * @param carPlate targa del veicolo
         * @param start    inizio occupazione
         * @param end      fine opzionale
         */
        private Occupied(String carPlate, Instant start, @Nullable Instant end) {
            this.carPlate = carPlate;
            this.start = start;
            this.end = end;
        }

        /**
         * @return identificatore testuale dello stato occupato
         */
        @Contract(pure = true)
        @Override
        public @NotNull String getIdentifier() {
            return "OCCUPIED";
        }

        /**
         * @return targa del veicolo
         */
        public String getCarPlate() {
            return carPlate;
        }

        /**
         * @return istante di inizio occupazione
         */
        public Instant getStart() {
            return start;
        }

        /**
         * Restituisce la fine occupazione, se presente.
         *
         * @return eventuale istante di fine
         */
        @Contract(pure = true)
        public @NotNull Optional<Instant> getEnd() {
            return Optional.ofNullable(end);
        }

        @Override
        public String toString() {
            return String.format("Occupied { carPlate: '%s', start: %s, end: %s }", carPlate, start, end);
        }
    }

    /**
     * Stato che rappresenta un posto riservato per un certo intervallo.
     */
    final class Reserved implements ParkingSpaceStatus {

        private final String carPlate;
        private final Instant start;
        private final Instant end;

        /**
         * Costruisce uno stato riservato.
         *
         * @param carPlate targa associata alla prenotazione
         * @param start    inizio della riserva
         * @param end      fine della riserva
         */
        private Reserved(String carPlate, Instant start, Instant end) {
            this.carPlate = carPlate;
            this.start = start;
            this.end = end;
        }

        /**
         * @return identificatore testuale dello stato riservato
         */
        @Contract(pure = true)
        @Override
        public @NotNull String getIdentifier() {
            return "RESERVED";
        }

        /**
         * @return targa associata alla riserva
         */
        public String getCarPlate() {
            return carPlate;
        }

        /**
         * @return inizio della prenotazione
         */
        public Instant getStart() {
            return start;
        }

        /**
         * @return fine della prenotazione
         */
        public Instant getEnd() {
            return end;
        }

        @Override
        public String toString() {
            return String.format("Reserved { carPlate: '%s', start: %s, end: %s }", carPlate, start, end);
        }

    }

}
