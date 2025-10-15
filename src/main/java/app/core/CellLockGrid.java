package app.core;

import java.util.List;

public interface CellLockGrid {
    void acquire(int r, int c) throws InterruptedException;
    void release(int r, int c);

    /**
     * Tenta adquirir todos os locks das células em "cells".
     * Quem implementar deve ORDENAR por um id global estável para evitar deadlock.
     * @return true se conseguiu todos no prazo; false caso contrário (devendo reverter o que pegou).
     */
    boolean acquireAll(List<int[]> cells, long timeoutMs) throws InterruptedException;

    void releaseAll(List<int[]> cells);
}
