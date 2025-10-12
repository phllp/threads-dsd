package app;

import app.model.RowSegment;
import app.view.InserterThread;
import app.view.MatrixCanvas;
import app.view.SimulationState;
import app.view.Ui;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.stage.Stage;
import utils.MatrixParser;

import java.io.InputStream;
import java.util.function.IntSupplier;

public class Main extends Application {

    private MatrixCanvas matrixCanvas;

    /** Armazena a malha */
    public static int[][] gridRef;

    // Estado da simulação
    private final SimulationState simState = new SimulationState();

    // Renderização
    private AnimationTimer painter;

    // Controle de execução
    private InserterThread inserter;

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

        // todo: testar com 30fps tbm
        // painter: 60fps
        painter = new AnimationTimer() {
            @Override
            public void handle(long now) {
                matrixCanvas.setCars(simState.snapshotPositions());
            }
        };
        painter.start();
    }

    private void addActionListeners(Ui ui) {
        Button btnIniciar = ui.getBtnIniciar();
        Button btnEncerrar = ui.getBtnEncerrar();
        Button btnEncerrarInsercao = ui.getBtnEncerrarInsercao();
        Spinner<Integer> spnIntervaloMs = ui.getSpnIntervaloMs();
        Spinner<Integer> spnMaxVeiculos = ui.getSpnMaxVeiculos();

        //@todo randomizar essa velocidade
        // velocidade de cada carro
        IntSupplier carStepMsSupplier = () -> 850;

        btnIniciar.setOnAction(e -> {
            ensureInserterRunning(spnMaxVeiculos::getValue, spnIntervaloMs::getValue, carStepMsSupplier);
            // começa/retoma a inserir respeitando o "intervalo mínimo"
            inserter.resumeInserting();
        });

        btnEncerrarInsercao.setOnAction(e -> {
            if (inserter != null) {
                // para só a inserção, carros ativos continuam
                inserter.stopInserting();
            }
        });

        btnEncerrar.setOnAction(e -> {
            stopAll();
        });
    }

    private void ensureInserterRunning(IntSupplier maxCars, IntSupplier minInsertMs, IntSupplier carStep) {
        if (inserter == null || !inserter.isAlive()) {
            inserter = new InserterThread(
                    gridRef,
                    simState,
                    maxCars,
                    // ESTE é o tempo mínimo de inserção vindo da UI
                    minInsertMs,
                    carStep,
                    // Rota de entrada, passada desta maneira pois vai ser encapsulada em um supplier
                    () -> RowSegment.findRandomEdgeSegment()
            );
            inserter.start();
        }
    }

    private void stopAll() {
        if (inserter != null) {
            inserter.shutdown();
            inserter = null;
        }
        matrixCanvas.clearCars();
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
