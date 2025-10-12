package app.model;

import app.view.MatrixCanvas;
import javafx.application.Platform;

public class Car extends Thread {
    private final MatrixCanvas canvas;
    private final int[][] grid;
    private final int stepMs;

    private final int row;
    private int col;
    private final int endCol;

    private volatile boolean running = true;

    public Car(MatrixCanvas canvas, int[][] grid, int row, int startCol, int endCol, int stepMs) {
        this.canvas = canvas;
        this.grid = grid;
        this.row = row;
        this.col = startCol;
        this.endCol = endCol;
        this.stepMs = stepMs;
        setName("CarThread");

        // A thread pode ser interrompida junto com a aplicação
        setDaemon(true);
    }

    public void requestStop() {
        running = false;
        interrupt();
    }

    @Override
    public void run() {
        // todo add no slide: toda chamada que mexe em UI está dentro de Platform.runLater
        // Thread-safe JavaFX: posiciona o carro inicialmente
        Platform.runLater(() -> canvas.setCar(row, col));

        try {
            while (running && col <= endCol) {
                Thread.sleep(Math.max(1, stepMs));
                if (!running) break;
                col++;
                final int drawC = col;
                Platform.runLater(() -> canvas.setCar(row, drawC));
            }
        } catch (InterruptedException ignored) {
            // encerrando
        } finally {
            // ao terminar (naturalmente ou por stop), limpa o carro
            Platform.runLater(canvas::clearCar);
        }
    }


}
