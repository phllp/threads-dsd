package app.core;

import java.util.*;

public class CellLockGridMonitor implements CellLockGrid {
    private static final class Cell {
        private boolean occupied = false;

        synchronized boolean tryAcquireUntil(long deadlineMs) throws InterruptedException {
            long remaining;
            while (occupied && (remaining = deadlineMs - System.currentTimeMillis()) > 0) {
                wait(Math.max(1, remaining));
            }
            if (!occupied) {
                occupied = true;
                return true;
            }
            return false; // timeout
        }

        synchronized void acquire() throws InterruptedException {
            while (occupied) wait();
            occupied = true;
        }

        synchronized void release() {
            if (!occupied) {
                // proteção best-effort contra over-release
                return;
            }
            occupied = false;
            notifyAll();
        }
    }

    private final Cell[][] cells;
    private final int rows, cols;

    public CellLockGridMonitor(int rows, int cols) {
        this.rows = rows; this.cols = cols;
        this.cells = new Cell[rows][cols];
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                cells[r][c] = new Cell();
    }

    private int idOf(int r, int c) { return r * cols + c; }

    @Override
    public void acquire(int r, int c) throws InterruptedException {
        cells[r][c].acquire();
    }

    @Override
    public void release(int r, int c) {
        cells[r][c].release();
    }

    @Override
    public boolean acquireAll(List<int[]> list, long timeoutMs) throws InterruptedException {
        // Ordena por id global fixo para evitar deadlock
        list.sort(Comparator.comparingInt(p -> idOf(p[0], p[1])));

        long deadline = System.currentTimeMillis() + timeoutMs;
        List<int[]> got = new ArrayList<>();

        for (int[] p : list) {
            Cell cell = cells[p[0]][p[1]];
            if (!cell.tryAcquireUntil(deadline)) {
                // rollback
                for (int i = got.size() - 1; i >= 0; i--) {
                    int[] q = got.get(i);
                    cells[q[0]][q[1]].release();
                }
                return false;
            }
            got.add(p);
        }
        return true;
    }

    @Override
    public void releaseAll(List<int[]> list) {
        for (int i = list.size() - 1; i >= 0; i--) {
            int[] p = list.get(i);
            cells[p[0]][p[1]].release();
        }
    }


}
