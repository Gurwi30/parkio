package it.parkio.app.manager;

import it.parkio.app.model.Bounds;
import it.parkio.app.model.ParkingLot;
import it.parkio.app.model.ParkingSpace;
import it.parkio.app.model.ParkingSpaceStatus;
import org.junit.jupiter.api.Test;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.*;
import java.io.File;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ParkingLotsManagerTest {

    @Test
    void save() {
        ParkingLotsManager manager = new ParkingLotsManager();
        ParkingLot test = manager.createParkingLot("test", new Bounds(new GeoPosition(0, 0), new GeoPosition(1, 1)), Color.CYAN);

        test.addParkingSpace(new Bounds(new GeoPosition(0.5, 0.5), new GeoPosition(0.6, 0.6)));
        ParkingSpace parkingSpace = test.addParkingSpace(new Bounds(new GeoPosition(0.7, 0.7), new GeoPosition(0.8, 0.8)));

        parkingSpace.updateStatus(ParkingSpaceStatus.occupied("CG123IT", Instant.now()));
        assertDoesNotThrow(() -> manager.save(new File("parkinglots.json")));
    }

    @Test
    void load() {
        ParkingLotsManager manager = assertDoesNotThrow(() -> ParkingLotsManager.load(new File("parkinglots.json")));
        manager.getParkingLots().forEach(lot -> {
            System.out.println(lot);
            lot.getSpaces().forEach(System.out::println);
        });
    }

}