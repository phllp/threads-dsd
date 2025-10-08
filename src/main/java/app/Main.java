package app;

import app.view.MatrixCanvas;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import utils.MatrixParser;

import java.io.InputStream;

public class Main extends Application {

    // Componentes da barra
    private Spinner<Integer> spnMaxVeiculos;
    private Spinner<Integer> spnIntervaloMs;
    private ComboBox<String> cbExclusao;

    private MatrixCanvas matrixCanvas;

    @Override
    public void start(Stage stage) throws Exception {
        // Carrega a malha
        int[][] grid = loadGridFromResources("/malhas/malha-exemplo-2.txt");

        // Canvas de desenho
        matrixCanvas = new MatrixCanvas();
        matrixCanvas.setGrid(grid);

        BorderPane root = new BorderPane();
        root.setTop(buildToolbar());
        root.setCenter(matrixCanvas);
        BorderPane.setAlignment(matrixCanvas, Pos.CENTER);
        BorderPane.setMargin(matrixCanvas, new Insets(8));

        Scene scene = new Scene(root, 1000, 700);
        stage.setTitle("Simulador de Tráfego — UI Básica");
        stage.setScene(scene);
        stage.show();

        // Redesenha responsivamente
        root.widthProperty().addListener((obs, o, n) -> matrixCanvas.redraw());
        root.heightProperty().addListener((obs, o, n) -> matrixCanvas.redraw());
    }

    private ToolBar buildToolbar() {
        // Limite de veículos simultâneos
        spnMaxVeiculos = new Spinner<>(1, 1000, 50, 1);
        spnMaxVeiculos.setEditable(true);
        spnMaxVeiculos.setPrefWidth(100);

        // Intervalo de inserção
        spnIntervaloMs = new Spinner<>(50, 60_000, 500, 50);
        spnIntervaloMs.setEditable(true);
        spnIntervaloMs.setPrefWidth(120);

        // Mecanismo de exclusão mútua
        cbExclusao = new ComboBox<>();
        cbExclusao.getItems().addAll("Semáforo", "Monitor");
        cbExclusao.getSelectionModel().selectFirst();

        Button btnIniciar = new Button("Iniciar simulação");
        Button btnEncerrarInsercao = new Button("Encerrar inserção");
        Button btnEncerrar = new Button("Encerrar simulação");

        Label lbMax = new Label("Limite veículos:");
        Label lbInt = new Label("Intervalo (ms):");
        Label lbExc = new Label("Exclusão mútua:");

        ToolBar tb = new ToolBar(
                lbMax, spnMaxVeiculos,
                new Separator(),
                lbInt, spnIntervaloMs,
                new Separator(),
                lbExc, cbExclusao,
                new Separator(),
                btnIniciar,
                btnEncerrarInsercao,
                btnEncerrar
        );
        tb.setPadding(new Insets(6));
        return tb;
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
