package app;

import app.model.enums.LaneCodes;
import app.view.MatrixCanvas;
import app.view.Ui;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.stage.Stage;
import javafx.util.Duration;
import utils.MatrixParser;

import java.io.InputStream;
import java.util.ArrayList;

public class Main extends Application {

    private MatrixCanvas matrixCanvas;

    /** Armazena a malha */
    private int[][] gridRef;

    /** Testes com um carro */
    private Timeline demo;
    private int carR = -1, carC = -1;

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

                while (c < gridRef[r].length && (gridRef[r][c] == 2 || LaneCodes.isOnCrossroad(gridRef[r][c]))) c++;
                int end = c - 1;

                // achamos um segmento [start..end] de via 2
                return new int[]{ r, start, end };
            }
        }
        return null;
    }

    private void startStraightRightDemo(int stepMs) {
        stopDemo(); // garante que não tem outro rodando

        int[] seg = findFirstRightLaneSegment();
        if (seg == null) {
            System.out.println("Nenhum segmento horizontal de '2' encontrado.");
            return;
        }
        carR = seg[0];
        carC = seg[1];
        matrixCanvas.setCar(carR, carC);

        int endC = seg[2];

        System.out.println("End = " + endC);

        demo = new Timeline(new KeyFrame(Duration.millis(stepMs), e -> {
            if (carC < endC) {
                carC += 1;
                matrixCanvas.setCar(carR, carC);
            } else {
                // chegou no fim do segmento -> para a demo
                stopDemo();
            }
        }));
        demo.setCycleCount(Timeline.INDEFINITE);
        demo.play();
    }

    private void stopDemo() {
        if (demo != null) {
            demo.stop();
            demo = null;
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
