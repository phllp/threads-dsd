package app.view;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SimulationState {
    private final ConcurrentMap<Long, int[]> carPos = new ConcurrentHashMap<>();

    public void onSpawn(long id, int r, int c) {
        carPos.put(id, new int[]{r,c});
    }

    public void onMove (long id, int r, int c) {
        carPos.put(id, new int[]{r,c});
    }

    public void onExit (long id) {
        carPos.remove(id);
    }

    public int activeCount() {
        return carPos.size();
    }

    public Collection<int[]> snapshotPositions() {
        return carPos.values();
    }


}
