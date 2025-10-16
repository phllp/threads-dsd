package app.core;

public final class CellLockFactory {

    private CellLockFactory() {}

    public static CellLockGrid create(LockMode mode, int rows, int cols) {
        return switch (mode) {
            case SEMAPHORE -> new CellLockGridSemaphore(rows, cols);
            case MONITOR   -> new CellLockGridMonitor(rows, cols);
        };
    }
}