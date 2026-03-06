package it.parkio.app.scheduler;

import it.parkio.app.model.ParkingSpace;
import it.parkio.app.model.ParkingSpaceStatus;
import org.jetbrains.annotations.NotNull;
import org.jxmapviewer.JXMapViewer;

import javax.swing.*;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.*;

public class ParkingSpaceScheduler {

    private static final ScheduledExecutorService SCHEDULER =
            Executors.newScheduledThreadPool(1, r -> {
                Thread t = new Thread(r, "parking-space-scheduler");
                t.setDaemon(true);
                return t;
            });

    private static final Map<Integer, ScheduledFuture<?>> pending = new ConcurrentHashMap<>();

    private ParkingSpaceScheduler() {}

    public static void schedule(@NotNull ParkingSpace space, @NotNull JXMapViewer mapViewer) {
        cancel(space);

        switch (space.getStatus()) {

            case ParkingSpaceStatus.Reserved res -> {
                long msUntilStart = msUntil(res.getStart());
                long msUntilEnd   = msUntil(res.getEnd());

                if (msUntilEnd <= 0) {
                    applyAndRepaint(space, ParkingSpaceStatus.free(), mapViewer);
                    return;
                }

                if (msUntilStart <= 0) {
                    applyAndRepaint(space,
                            ParkingSpaceStatus.occupied(res.getCarPlate(), res.getStart(), res.getEnd()),
                            mapViewer);

                    scheduleRelease(space, msUntilEnd, mapViewer);
                } else {
                    ScheduledFuture<?> toOccupied = SCHEDULER.schedule(() -> {
                        applyAndRepaint(space,
                                ParkingSpaceStatus.occupied(res.getCarPlate(), res.getStart(), res.getEnd()),
                                mapViewer);

                        scheduleRelease(space, msUntil(res.getEnd()), mapViewer);
                    }, msUntilStart, TimeUnit.MILLISECONDS);

                    pending.put(space.getId(), toOccupied);
                }
            }

            case ParkingSpaceStatus.Occupied occ -> occ.getEnd().ifPresent(end -> {
                long msUntilEnd = msUntil(end);
                if (msUntilEnd <= 0) {
                    applyAndRepaint(space, ParkingSpaceStatus.free(), mapViewer);
                    return;
                }

                scheduleRelease(space, msUntilEnd, mapViewer);
            });

            default -> {}
        }
    }

    public static void cancel(@NotNull ParkingSpace space) {
        ScheduledFuture<?> existing = pending.remove(space.getId());
        if (existing != null) existing.cancel(false);
    }

    public static void shutdown() {
        SCHEDULER.shutdownNow();
    }

    private static void scheduleRelease(@NotNull ParkingSpace space, long delayMs, @NotNull JXMapViewer mapViewer) {
        ScheduledFuture<?> toFree = SCHEDULER.schedule(() -> applyAndRepaint(space, ParkingSpaceStatus.free(), mapViewer), delayMs, TimeUnit.MILLISECONDS);
        pending.put(space.getId(), toFree);
    }

    private static void applyAndRepaint(@NotNull ParkingSpace space,
                                        @NotNull ParkingSpaceStatus status,
                                        @NotNull JXMapViewer mapViewer) {
        space.updateStatus(status);
        SwingUtilities.invokeLater(mapViewer::repaint);
    }

    private static long msUntil(@NotNull Instant instant) {
        return instant.toEpochMilli() - Instant.now().toEpochMilli();
    }

}