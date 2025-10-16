package app.core;

import java.util.*;

/**
 * Implementação de grade de locks por CÉLULA usando Monitor (synchronized + wait/notifyAll).
 */
public class CellLockGridMonitor implements CellLockGrid {

    /**
     * Monitor de uma célula (um por posição do grid).
     * Encapsula o estado {@code occupied} e as operações de acquire/release/tryAcquireUntil.
     *
     * Observações de implementação:
     * - {@code synchronized} protege o acesso a {@code occupied} e coordena as esperas.
     * - Usamos {@code notifyAll()} (e não {@code notify()}) para evitar “perda” de sinal em cenários
     *   com múltiplos esperantes; quem acorda reavalia a condição em um laço (padrão monitor).
     * - O método {@code tryAcquireUntil} implementa um timeout baseado em **deadline absoluto**
     *   (soma das esperas), tolerando despertares espúrios.
     */
    private static final class Cell {
        private boolean occupied = false;

        /**
         * Tenta adquirir antes do deadlineMs. Retorna true se conseguiu, false por timeout.
         * Usa laço while para reaplicar a condição (padrão monitor) e lida com despertares espúrios.
         */
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

        /** Adquire bloqueando até ficar livre (sem timeout). */
        synchronized void acquire() throws InterruptedException {
            while (occupied) wait();
            occupied = true;
        }

        /**
         * Libera a célula e acorda todos os esperantes.
         * Faz uma proteção best-effort contra over-release (ignora se já está livre).
         */
        synchronized void release() {
            if (!occupied) {
                // proteção best-effort contra over-release
                return;
            }
            occupied = false;
            notifyAll();
        }
    }

    /** Matriz de monitores (1 por célula). */
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

    /**
     * Tenta adquirir TODOS os locks do conjunto dentro do timeout total.
     * Estratégia anti-deadlock: ordenar sempre pelo idOf(int, int).
     * Se falhar em qualquer célula, desfaz (rollback) o que já foi adquirido.
     *
     * Implementação do timeout:
     * - Calculamos um deadline = agora + timeoutMs
     * - Para cada célula, aguardamos até o deadline, reutilizando o saldo de tempo.
     *
     * @param list lista de pares {r,c} a serem travados (será ORDENADA in-place)
     * @param timeoutMs tempo total máximo (ms) para obter o conjunto
     * @return true se obteve todas, false se falhou (com rollback feito)
     */
    @Override
    public boolean acquireAll(List<int[]> list, long timeoutMs) throws InterruptedException {
        // Ordena por id global fixo para evitar deadlock
        list.sort(Comparator.comparingInt(p -> idOf(p[0], p[1])));

        long deadline = System.currentTimeMillis() + timeoutMs;
        List<int[]> got = new ArrayList<>();

        for (int[] p : list) {
            Cell cell = cells[p[0]][p[1]];
            if (!cell.tryAcquireUntil(deadline)) {
                // rollbac
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
