package app.model;

import app.Main;
import app.model.enums.Direction;
import app.model.enums.LaneCode;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public final class RowSegment {
    // Referencias de início e fim de um segmento (INCLUSIVOS)
    final int r0;
    final int c0;
    final int r1;
    final int c1;

    final Direction direction;

    RowSegment(int r0, int c0, int r1, int c1, Direction direction) {
        this.r0 = r0;
        this.c0 = c0;
        this.r1 = r1;
        this.c1 = c1;
        this.direction = direction;
    }

    private static boolean isLaneForwardOrX(int code, Direction direction) {
        return code == direction.laneCode.getCodigo() || LaneCode.isOnCrossroad(code);
    }

    private static int[] step(int r, int c, Direction d) {
        return new int[]{ r + d.dr, c + d.dc };
    }

    private static boolean inBounds(int r, int c, int[][] g) {
        return r >= 0 && c >= 0 && r < g.length && c < g[0].length;
    }

    /**
     * Encontra TODOS os segmentos retos válidos que começam em QUALQUER BORDA
     * na direção de entrada correspondente, e retorna UM aleatório.
     * Mantém o mesmo conceito de “percorre enquanto a célula for a pista daquela direção
     * ou um cruzamento”.
     */
    public static RowSegment findRandomEdgeSegment() {
        int[][] g = Main.gridRef;
        int rows = g.length, cols = g[0].length;
        List<RowSegment> candidates = new ArrayList<>();

        // ---- Borda superior: entrada "para baixo" (DOWN) ----
        {
            Direction d = Direction.DOWN;
            int r = 0;
            for (int c = 0; c < cols; c++) {
                if (isLaneForwardOrX(g[r][c], d)) {
                    // expande até onde dá
                    int rr = r, cc = c;
                    while (inBounds(rr, cc, g) && isLaneForwardOrX(g[rr][cc], d)) {
                        int[] nx = step(rr, cc, d);
                        rr = nx[0]; cc = nx[1];
                    }
                    // último válido é um passo antes
                    int[] back = new int[]{ rr - d.dr, cc - d.dc };
                    candidates.add(new RowSegment(r, c, back[0], back[1], d));
                }
            }
        }

        // ---- Borda inferior: entrada "para cima" (UP) ----
        {
            Direction d = Direction.UP;
            int r = rows - 1;
            for (int c = 0; c < cols; c++) {
                if (isLaneForwardOrX(g[r][c], d)) {
                    int rr = r, cc = c;
                    while (inBounds(rr, cc, g) && isLaneForwardOrX(g[rr][cc], d)) {
                        int[] nx = step(rr, cc, d);
                        rr = nx[0]; cc = nx[1];
                    }
                    int[] back = new int[]{ rr - d.dr, cc - d.dc };
                    candidates.add(new RowSegment(r, c, back[0], back[1], d));
                }
            }
        }

        // ---- Borda esquerda: entrada "para directioneita" (RIGHT) ----
        {
            Direction d = Direction.RIGHT;
            int c = 0;
            for (int r = 0; r < rows; r++) {
                if (isLaneForwardOrX(g[r][c], d)) {
                    int rr = r, cc = c;
                    while (inBounds(rr, cc, g) && isLaneForwardOrX(g[rr][cc], d)) {
                        int[] nx = step(rr, cc, d);
                        rr = nx[0]; cc = nx[1];
                    }
                    int[] back = new int[]{ rr - d.dr, cc - d.dc };
                    candidates.add(new RowSegment(r, c, back[0], back[1], d));
                }
            }
        }

        // ---- Borda directioneita: entrada "para esquerda" (LEFT) ----
        {
            Direction d = Direction.LEFT;
            int c = cols - 1;
            for (int r = 0; r < rows; r++) {
                if (isLaneForwardOrX(g[r][c], d)) {
                    int rr = r, cc = c;
                    while (inBounds(rr, cc, g) && isLaneForwardOrX(g[rr][cc], d)) {
                        int[] nx = step(rr, cc, d);
                        rr = nx[0]; cc = nx[1];
                    }
                    int[] back = new int[]{ rr - d.dr, cc - d.dc };
                    candidates.add(new RowSegment(r, c, back[0], back[1], d));
                }
            }
        }

        if (candidates.isEmpty()){
            return null;
        }

        return candidates.get(
                //todo add explicação no slide
                ThreadLocalRandom.current().nextInt(candidates.size())
        );
    }

    public int getR0() {
        return r0;
    }

    public int getC0() {
        return c0;
    }

    public int getR1() {
        return r1;
    }

    public int getC1() {
        return c1;
    }

    public Direction getDirection() {
        return direction;
    }
}
