package app.core;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class CellLockGridSemaphore {
    private final Semaphore[][] locks;
    private final int rows, cols;

    public CellLockGridSemaphore(int rows, int cols) {
        this.rows = rows; this.cols = cols;
        this.locks = new Semaphore[rows][cols];
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                locks[r][c] = new Semaphore(1, true);
    }

    public void acquire(int r, int c) throws InterruptedException {
        locks[r][c].acquire();
    }


    public void release(int r, int c) {
        locks[r][c].release();
    }
}
