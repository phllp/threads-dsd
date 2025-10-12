package app;

import app.view.MatrixCanvas;
import app.view.Ui;
import javafx.application.Application;
import javafx.stage.Stage;
import utils.MatrixParser;

import java.io.InputStream;

public class Main extends Application {

    private MatrixCanvas matrixCanvas;

    /** Armazena a malha */
    private int[][] gridRef;

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
