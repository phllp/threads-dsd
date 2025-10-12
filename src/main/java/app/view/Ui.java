package app.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Ui {

    // Componentes da barra
    private Spinner<Integer> spnMaxVeiculos;
    private Spinner<Integer> spnIntervaloMs;
    private ComboBox<String> cbExclusao;

    private Button btnIniciar;
    private Button btnEncerrarInsercao;
    private Button btnEncerrar;

    public BorderPane buildLayout(Stage stage, MatrixCanvas matrixCanvas) {
        BorderPane root = new BorderPane();
        root.setTop(this.buildToolbar());

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

        return root;
    }

    public ToolBar buildToolbar() {
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

        btnIniciar = new Button("Iniciar simulação");
        btnEncerrarInsercao = new Button("Encerrar inserção");
        btnEncerrar = new Button("Encerrar simulação");

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

    public Button getBtnIniciar() {
        return btnIniciar;
    }

    public Button getBtnEncerrarInsercao() {
        return btnEncerrarInsercao;
    }

    public Button getBtnEncerrar() {
        return btnEncerrar;
    }

    public Spinner<Integer> getSpnMaxVeiculos() {
        return spnMaxVeiculos;
    }

    public Spinner<Integer> getSpnIntervaloMs() {
        return spnIntervaloMs;
    }

    public ComboBox<String> getCbExclusao() {
        return cbExclusao;
    }
}
