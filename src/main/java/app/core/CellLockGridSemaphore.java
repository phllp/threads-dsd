package app.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Implementação de grade de locks por CÉLULA usando Semaphore
 */
public class CellLockGridSemaphore implements CellLockGrid {
    /** Matriz de semáforos (1 por célula). */
    private final Semaphore[][] locks;
    private final int rows, cols;

    public CellLockGridSemaphore(int rows, int cols) {
        this.rows = rows; this.cols = cols;
        this.locks = new Semaphore[rows][cols];
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                locks[r][c] = new Semaphore(1, true);
    }
    @Override
    public void acquire(int r, int c) throws InterruptedException {
        locks[r][c].acquire();
    }

    @Override
    public void release(int r, int c) {
        locks[r][c].release();
    }

    private int idOf(int r, int c) { return r * cols + c; }

    /**
     * Tenta adquirir TODOS os locks do conjunto dentro do timeout total.
     * Estratégia anti-deadlock: ordenar sempre pelo idOf(int, int).
     * Se falhar em qualquer célula, desfaz (rollback) o que já foi adquirido.
     *
     * @param cells lista de pares {r,c} a serem travados (será ORDENADA in-place)
     * @param timeoutMs tempo total máximo (ms) para obter o conjunto
     * @return true se obteve todas, false se falhou (com rollback feito)
     */
    @Override
    public boolean acquireAll(List<int[]> cells, long timeoutMs) throws InterruptedException {
        // Ordena por id global para evitar deadlock entre carros pegando conjuntos diferentes
        cells.sort(Comparator.comparingInt(p -> idOf(p[0], p[1])));

        long deadline = System.currentTimeMillis() + timeoutMs;
        List<int[]> got = new ArrayList<>();

        for (int[] p : cells) {
            long remaining = deadline - System.currentTimeMillis();
            if (remaining <= 0 || !locks[p[0]][p[1]].tryAcquire(Math.max(1, remaining), TimeUnit.MILLISECONDS)) {
                // Falhou: desfaz o que já pegou (em ordem reversa) e aborta
                for (int i = got.size() - 1; i >= 0; i--) {
                    int[] q = got.get(i);
                    locks[q[0]][q[1]].release();
                }
                return false;
            }
            got.add(p);
        }
        return true;
    }

    /**
     * Libera todos os locks do conjunto em ordem reversa da aquisição.
     */
    @Override
    public void releaseAll(List<int[]> cells) {
        for (int i = cells.size() - 1; i >= 0; i--) {
            int[] p = cells.get(i);
            release(p[0], p[1]);
        }
    }


}
