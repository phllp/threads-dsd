package app.model;

import app.model.enums.Direction;
import app.view.SimulationState;

public class Car extends Thread {
    private final SimulationState simState;
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
               int row,
               int startCol,
               int endRow,
               int endCol,
               int stepMs,
               Direction dir) {
        this.simState = simState;
        this.grid = grid;
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
        simState.onSpawn(getId(), row, col);

        try {
            while (running && !reachedEnd()) {
                Thread.sleep(Math.max(1, stepMs));
                if (!running) break;
                row += direction.dr;
                col += direction.dc;

                simState.onMove(getId(), row, col);
            }
        } catch (InterruptedException ignored) {
            // encerrando
        } finally {
            simState.onExit(getId());
        }
    }


}
