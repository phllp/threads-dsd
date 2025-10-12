package app.model;

import app.core.CellLockGridSemaphore;
import app.model.enums.Direction;
import app.view.SimulationState;

public class Car extends Thread {
    private final SimulationState simState;
    private final CellLockGridSemaphore locks;
    private final int[][] grid;
    private final int stepMs;

    private int row;
    private int col;
    private final int endRow;
    private final int endCol;

    private final Direction direction;

    private volatile boolean running = true;

    public Car(SimulationState simState,
               int[][] grid,
               CellLockGridSemaphore locks,
               int row,
               int startCol,
               int endRow,
               int endCol,
               int stepMs,
               Direction dir) {
        this.simState = simState;
        this.grid = grid;
        this.locks = locks;
        this.row = row;
        this.col = startCol;
        this.endRow = endRow;
        this.endCol = endCol;
        this.stepMs = stepMs;
        this.direction = dir;
        setName("CarThread");

        // A thread pode ser interrompida junto com a aplicação
        setDaemon(true);
    }

    public void requestStop() {
        running = false;
        interrupt();
    }

    private boolean reachedEnd() {
        return row == endRow && col == endCol;
    }

    @Override
    public void run() {
        try {
            // Segura a célula inicial (bloqueante).
            locks.acquire(row, col);

            simState.onSpawn(getId(), row, col);

            while (running && !reachedEnd()) {
                // 1. Calcula próxima célula na direção
                int nextRow = row + direction.dr;
                int nextCol = col + direction.dc;

                // 2. Tenta adquirir a célula à frente — bloqueia se ocupada
                locks.acquire(nextRow, nextCol);

                // 3. Move: atualiza posição e libera a anterior
                int previousRow = row;
                int previousCol = col;

                row = nextRow;
                col = nextCol;

                simState.onMove(getId(), row, col);
                locks.release(previousRow, previousCol);

                //4. Velocidade do carro definida com o sleep
                Thread.sleep(Math.max(1, stepMs));
            }
        } catch (InterruptedException ignored) {
            // encerrando
        } finally {
            try {
                locks.release(row, col);
            } catch (Exception ignored) {}
            simState.onExit(getId());
        }
    }


}
