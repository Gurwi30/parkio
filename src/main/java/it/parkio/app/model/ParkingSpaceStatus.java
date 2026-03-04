package it.parkio.app.model;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Optional;

public sealed interface ParkingSpaceStatus {

    String getIdentifier();

    static ParkingSpaceStatus free() {
        return Free.FREE;
    }

    @Contract(value = "_, _, _ -> new", pure = true)
    static @NotNull ParkingSpaceStatus occupied(String carPlate, Instant start, @Nullable Instant end) {
        return new ParkingSpaceStatus.Occupied(carPlate, start, end);
    }

    @Contract(value = "_, _ -> new", pure = true)
    static @NotNull ParkingSpaceStatus occupied(String carPlate, Instant start) {
        return occupied(carPlate, start, null);
    }

    @Contract("_, _, _ -> new")
    static @NotNull ParkingSpaceStatus reserved(String carPlate, Instant start, Instant end) {
        return new ParkingSpaceStatus.Reserved(carPlate, start, end);
    }

    static boolean isFree(ParkingSpaceStatus status) {
        return status instanceof Free;
    }

    static boolean isOccupied(ParkingSpaceStatus status) {
        return status instanceof Occupied;
    }

    static boolean isReserved(ParkingSpaceStatus status) {
        return status instanceof Reserved;
    }

    final class Free implements ParkingSpaceStatus {

        private static final Free FREE = new Free();

        private Free() {}

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

    final class Occupied implements ParkingSpaceStatus {

        private final String carPlate;
        private final Instant start;
        private final @Nullable Instant end;

        private Occupied(String carPlate, Instant start, @Nullable Instant end) {
            this.carPlate = carPlate;
            this.start = start;
            this.end = end;
        }

        @Contract(pure = true)
        @Override
        public @NotNull String getIdentifier() {
            return "OCCUPIED";
        }

        public String getCarPlate() {
            return carPlate;
        }

        public Instant getStart() {
            return start;
        }

        @Contract(pure = true)
        public @NotNull Optional<Instant> getEnd() {
            return Optional.ofNullable(end);
        }

        @Override
        public String toString() {
            return String.format("Occupied { carPlate: '%s', start: %s, end: %s }", carPlate, start, end);
        }
    }

    final class Reserved implements ParkingSpaceStatus {

        private final String carPlate;
        private final Instant start;
        private final Instant end;

        private Reserved(String carPlate, Instant start, Instant end) {
            this.carPlate = carPlate;
            this.start = start;
            this.end = end;
        }

        @Contract(pure = true)
        @Override
        public @NotNull String getIdentifier() {
            return "RESERVED";
        }

        public String getCarPlate() {
            return carPlate;
        }

        public Instant getStart() {
            return start;
        }

        public Instant getEnd() {
            return end;
        }

        @Override
        public String toString() {
            return String.format("Reserved { carPlate: '%s', start: %s, end: %s }", carPlate, start, end);
        }

    }

}
