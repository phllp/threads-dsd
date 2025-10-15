package app.core;

import app.view.CarColors;
import javafx.scene.paint.Color;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SimulationState {

    public static final class CarInfo {
        public final long id;
        public final Color color;
        private volatile int r, c;
        CarInfo(long id, int r, int c, Color color) {
            this.id = id; this.r = r; this.c = c; this.color = color;
        }
        public int getR() { return r; }
        public int getC() { return c; }
        void set(int r, int c) { this.r = r; this.c = c; }
    }

    private final ConcurrentMap<Long, CarInfo> cars = new ConcurrentHashMap<>();

    public void onSpawn(long id, int r, int c) {
        cars.put(id, new CarInfo(id, r, c, CarColors.colorForId(id)));
    }

    public void onMove (long id, int r, int c) {
        CarInfo info = cars.get(id);
        if (info != null) {
            info.set(r, c);
        };
    }

    public void onExit (long id) {
        cars.remove(id);
    }

    public int activeCount() {
        return cars.size();
    }

    public Collection<CarInfo> snapshotPositions() {
        return cars.values();
    }


}
