package app.view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Collection;
import java.util.List;

/**
 * Classe responsável por renderizar a malha e seu conteúdo
 */
public class MatrixCanvas extends Canvas {

    private int[][] grid;
    private double padding = 20; // margem ao redor

    // Abordagem para N carros, cara item vai ser uma lista [row, col]
    private Collection<int[]> cars = List.of();

    public MatrixCanvas() {
        // tamanho preferido inicial
        setWidth(900);
        setHeight(600);

        // Redesenha quando o tamanho do canvas muda
        widthProperty().addListener((obs, o, n) -> redraw());
        heightProperty().addListener((obs, o, n) -> redraw());
    }

    public void setGrid(int[][] grid) {
        this.grid = grid;
        redraw();
    }

    public void redraw() {
        GraphicsContext g = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        // fundo
        g.setFill(Color.web("#1e1e1e"));
        g.fillRect(0, 0, w, h);

        // Se uma matriz não for informada não há nada para redesenhar
        if (grid == null || grid.length == 0 || grid[0].length == 0) return;

        int rows = grid.length;
        int cols = grid[0].length;

        double drawableW = w - 2 * padding;
        double drawableH = h - 2 * padding;

        // Define células quadradas que caibam na tela
        double cellSize = Math.floor(Math.min(drawableW / cols, drawableH / rows));
        double startX = (w - (cellSize * cols)) / 2.0;
        double startY = (h - (cellSize * rows)) / 2.0;

        // Desenha as celulas
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int code = grid[r][c];

                //Pinta a célula de acordo com o seu código
                g.setFill(colorFor(code));
                double x = startX + c * cellSize;
                double y = startY + r * cellSize;
                g.fillRect(x, y, cellSize, cellSize);
            }
        }

        // Desenha as linhas da grade
        g.setStroke(Color.gray(0.25));
        g.setLineWidth(1.0);
        for (int r = 0; r <= rows; r++) {
            double y = startY + r * cellSize + 0.5;
            g.strokeLine(startX, y, startX + cols * cellSize, y);
        }
        for (int c = 0; c <= cols; c++) {
            double x = startX + c * cellSize + 0.5;
            g.strokeLine(x, startY, x, startY + rows * cellSize);
        }

        // desenha N carros
        for (int[] p : cars) {
            drawCar(g, startX, startY, cellSize, p[0], p[1]);
        }
    }

    private void drawCar(GraphicsContext g, double startX, double startY, double cellSize, int r, int c) {
        double x = startX + c * cellSize, y = startY + r * cellSize;
        double d = Math.max(3.0, cellSize * 0.45);
        g.setFill(Color.WHITE);
        g.fillOval(x + (cellSize - d)/2.0, y + (cellSize - d)/2.0, d, d);
    }

    /**
     * Mapeamento simples de cores para códigos 0..12
     */
    private Color colorFor(int code) {
        return switch (code) {
            case 0 -> Color.web("#2b2b2b"); // nada
            case 1 -> Color.web("#4FC3F7"); // estrada cima
            case 2 -> Color.web("#81C784"); // direita
            case 3 -> Color.web("#FFB74D"); // baixo
            case 4 -> Color.web("#BA68C8"); // esquerda
            case 5 -> Color.web("#29B6F6"); // cruzamento cima
            case 6 -> Color.web("#66BB6A"); // cruzamento direita
            case 7 -> Color.web("#FFA726"); // cruzamento baixo
            case 8 -> Color.web("#AB47BC"); // cruzamento esquerda
            case 9 -> Color.web("#26C6DA"); // cruzamento cima+direita
            case 10 -> Color.web("#5C6BC0"); // cima+esquerda
            case 11 -> Color.web("#26A69A"); // direita+baixo
            case 12 -> Color.web("#EF5350"); // baixo+esquerda
            default -> Color.DARKGRAY;
        };
    }

    /**
     * Define os carros que vão estar rodando na simulação
     * @param cars
     */
    public void setCars(Collection<int[]> cars) {
        this.cars = (cars == null ? List.of() : cars);
        redraw();
    }

    public void clearCars() {
        this.cars = List.of();
        redraw();
    }


}
