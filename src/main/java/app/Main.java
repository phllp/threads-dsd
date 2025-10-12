package app;

import app.model.Car;
import app.model.enums.LaneCode;
import app.view.MatrixCanvas;
import app.view.Ui;
import javafx.application.Application;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.stage.Stage;
import utils.MatrixParser;

import java.io.InputStream;

public class Main extends Application {

    private MatrixCanvas matrixCanvas;

    /** Armazena a malha */
    private int[][] gridRef;

    /** Testes com uma thread */
    private Car carThread;

    @Override
    public void start(Stage stage) throws Exception {
        // Carrega a malha
        int[][] grid = loadGridFromResources("/malhas/malha-exemplo-1.txt");
        this.gridRef = grid;

        // Canvas de desenho
        matrixCanvas = new MatrixCanvas();
        matrixCanvas.setGrid(grid);

        Ui ui = new Ui();
        ui.buildLayout(stage, matrixCanvas);
        addActionListeners(ui);
    }

    private void addActionListeners(Ui ui) {
        Button btnIniciar = ui.getBtnIniciar();
        Button btnEncerrar = ui.getBtnEncerrar();
        Spinner<Integer> spnIntervaloMs = ui.getSpnIntervaloMs();

        btnIniciar.setOnAction(event -> {
            startStraightRightDemo(spnIntervaloMs.getValue());
        });

        btnEncerrar.setOnAction(e -> {
            stopDemo();
            matrixCanvas.clearCar();
        });
    }

    /**
     * Encontra a primeira entrada na parte da malha,
     * para movimentar da esquerda para a direita
     * @return
     */
    private int[] findFirstRightLaneSegment() {
        for (int r = 0; r < gridRef.length; r++) {
            int c = 0;
            while (c < gridRef[r].length) {
                // pula até achar um '2'
                while (c < gridRef[r].length && gridRef[r][c] != 2) c++;
                if (c >= gridRef[r].length) break;

                int start = c;

                while (c < gridRef[r].length && (gridRef[r][c] == 2 || LaneCode.isOnCrossroad(gridRef[r][c]))) c++;
                int end = c - 1;

                // achamos um segmento [start..end] de via 2
                return new int[]{ r, start, end };
            }
        }
        return null;
    }

    private synchronized void startStraightRightDemo(int stepMs) {
        stopDemo(); // garante que não tem outro rodando

        int[] seg = findFirstRightLaneSegment();
        if (seg == null) {
            System.out.println("Nenhum segmento horizontal de '2' encontrado.");
            return;
        }

        int row = seg[0];
        int startCol = seg[1];
        int endCol = seg[2];

        carThread = new Car(matrixCanvas, gridRef, row, startCol, endCol, stepMs);
        carThread.start();
   }

    private synchronized void stopDemo() {
        if (carThread != null) {
            carThread.requestStop();
            carThread = null;
        }
    }


    private int[][] loadGridFromResources(String resourcePath) throws Exception {
        try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
            if (in == null)
                throw new IllegalStateException("Recurso não encontrado: " + resourcePath);
            return MatrixParser.readMatrix(in);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }


}
