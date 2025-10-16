package app;

import app.core.*;
import app.model.Car;
import app.model.RowSegment;
import app.view.MatrixCanvas;
import app.core.SimulationState;
import app.view.Ui;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.stage.Stage;
import utils.MatrixParser;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
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

    // Mecanismo de exclusão mútua
    private CellLockGrid cellLocks;

    private LockMode currentLockMode = LockMode.SEMAPHORE;

    @Override
    public void start(Stage stage) throws Exception {
        // Carrega a malha
        int[][] grid = loadGridFromResources("/malhas/malha-exemplo-2.txt");
        gridRef = grid;

        // Canvas de desenho
        matrixCanvas = new MatrixCanvas();
        matrixCanvas.setGrid(grid);

        Ui ui = new Ui();
        ui.buildLayout(stage, matrixCanvas);
        addActionListeners(ui);

        // cria os locks no modo selecionado inicialmente (padrão do ComboBox)
        currentLockMode = resolveLockMode(ui.getCbExclusao().getValue());
        cellLocks = CellLockFactory.create(currentLockMode, gridRef.length, gridRef[0].length);

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

        // Velocidade aleatória para cada carro/thread
        IntSupplier carStepMsSupplier =  () -> 200 + ThreadLocalRandom.current().nextInt(400);

        // Atualização do mecanismo de exclusão mútua
        ui.getCbExclusao().valueProperty().addListener((obs, oldV, newV) -> {
            LockMode selected = resolveLockMode(newV);

            // Se não mudou, não faz nada
            if (selected == currentLockMode) return;

            // encerra a simulação atual
            stopAll();

            // recria a estrutura de locks com o modo selecionado
            currentLockMode = selected;
            cellLocks = CellLockFactory.create(currentLockMode, gridRef.length, gridRef[0].length);

            // reinicia a inserção
            ensureInserterRunning(
                    ui.getSpnMaxVeiculos()::getValue,
                    ui.getSpnIntervaloMs()::getValue,
                    () -> 200 + java.util.concurrent.ThreadLocalRandom.current().nextInt(400)
            );
            // @todo validação para só resumir se já estava inserindo antes da mudança
            inserter.resumeInserting();
        });

        // Inicia a simulação
        btnIniciar.setOnAction(e -> {
            // Mapear seleção atual
            LockMode selected = resolveLockMode(ui.getCbExclusao().getValue());

            // Se há simulação em andamento e o modo mudou, reinicie para aplicar mudança
            boolean running = (inserter != null && inserter.isAlive()) || simState.activeCount() > 0;
            if (running && selected != currentLockMode) {
                stopAll();
            }

            // Se o modo mudou (ou ainda não tínhamos lock), recrie os locks
            if (selected != currentLockMode || cellLocks == null) {
                currentLockMode = selected;
                cellLocks = CellLockFactory.create(currentLockMode, gridRef.length, gridRef[0].length);
            }

            // Sobe o inserter
            ensureInserterRunning(spnMaxVeiculos::getValue, spnIntervaloMs::getValue, carStepMsSupplier);
            inserter.resumeInserting();
        });

        // Encerrar a inserção, não mata os carros que já estão rodando
        btnEncerrarInsercao.setOnAction(e -> {
            if (inserter != null) {
                inserter.stopInserting();
            }
        });

        // Finaliza toda a simulação imediatamente
        btnEncerrar.setOnAction(e -> {
            stopAll();
        });
    }

    /**
     * Instancia e inicia a thread que vai gerenciar a inserção dos carros no grid
     * @param maxCars
     * @param minInsertMs
     * @param carStep
     */
    private void ensureInserterRunning(IntSupplier maxCars, IntSupplier minInsertMs, IntSupplier carStep) {
        if (inserter == null || !inserter.isAlive()) {
            inserter = new InserterThread(
                    gridRef,
                    simState,
                    cellLocks,
                    maxCars,
                    minInsertMs, // tempo mínimo de inserção vindo da UI
                    carStep,
                    () -> RowSegment.findRandomEdgeSegment() // Mecanismo para encontrar a rota de entrada
            );
            inserter.start();
        }
    }

    /**
     * Finaliza a simulação parando a inserção e finalizando todos os carros
     */
    private void stopAll() {
        if (inserter != null) {
            List<Car> spawned = inserter.getSpawned();
            for (Car c: spawned) {
                c.requestStop();
            }
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

    /**
     * Retorna qual tipo de mecanismo foi selecionado com base no valor do ComboBox
     * @param uiValue
     * @return
     */
    private static LockMode resolveLockMode(String uiValue) {
        if (uiValue == null) return LockMode.SEMAPHORE;
        return uiValue.toLowerCase().contains("monit") ? LockMode.MONITOR : LockMode.SEMAPHORE;
    }

    public static void main(String[] args) {
        launch(args);
    }


}
