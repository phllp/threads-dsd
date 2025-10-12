package app.model;

import app.model.enums.Direction;
import app.view.MatrixCanvas;
import javafx.application.Platform;

public class Car extends Thread {
    private final MatrixCanvas canvas;
    private final int[][] grid;
    private final int stepMs;

    private int row;
    private int col;
    private final int endRow;
    private final int endCol;

    private final Direction direction;

    private volatile boolean running = true;

    public Car(MatrixCanvas canvas, int[][] grid, int row, int startCol, int endRow, int endCol, int stepMs, Direction dir) {
        this.canvas = canvas;
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
        // todo add no slide: toda chamada que mexe em UI está dentro de Platform.runLater
        // Thread-safe JavaFX: posiciona o carro inicialmente
        Platform.runLater(() -> canvas.setCar(row, col));

        try {
            while (running && !reachedEnd()) {
                Thread.sleep(Math.max(1, stepMs));
                if (!running) break;
                row += direction.dr;
                col += direction.dc;
                final int curentRow = row;
                final int curentCol = col;
                javafx.application.Platform.runLater(() -> canvas.setCar(curentRow, curentCol));
            }
        } catch (InterruptedException ignored) {
            // encerrando
        } finally {
            // ao terminar (naturalmente ou por stop), limpa o carro
            Platform.runLater(canvas::clearCar);
        }
    }


}
