package utils;

import app.model.enums.Direction;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Planejador determinístico/leve para travessia de cruzamentos.
 *
 * Escopo:
 * - Cruzamentos 2x2 (códigos {9,10,11,12}): usa templates internos e
 *   ANEXA a 1ª célula fora do cruzamento para garantir que o carro não pare dentro.
 * - Regras de segurança: aplica proibição de “primeiro par” por CÓDIGO
 *   (evita sequências que travam/voltam imediatamente).
 *
 *   entra por CIMA    (entryDir=DOWN):     12->10  proibido
 *   entra pela ESQUERDA (entryDir=RIGHT):  11->12  proibido
 *   entra por BAIXO   (entryDir=UP):       9->11   proibido
 *   entra pela DIREITA (entryDir=LEFT):    10->9   proibido
 *
 * - Cruzamentos não-2x2: fallback simples (1 célula + saída).
 *
 * Observações:
 * - O método {@link #plan} é a única porta de entrada; o restante são helpers internos.
 * - O retorno {@link Path} traz a lista de células a serem reservadas (internas + 1 fora)
 *   e a direção de saída a ser adotada ao término da travessia.
 */
public final class CrossPlanner {
    private CrossPlanner() {}

    /**
     * Planejador principal chamado pelo {@code Car.run()} quando detecta cruzamento.
     * Tenta detectar um cluster 2x2 e aplicar templates; caso contrário, usa fallback simples.
     *
     * @param grid     matriz da malha
     * @param rCross   linha da 1ª célula de cruzamento que o carro pretende entrar
     * @param cCross   coluna da 1ª célula de cruzamento que o carro pretende entrar
     * @param entryDir direção de entrada (direção do carro ao aproximar-se do cruzamento)
     * @return {@link Path} com células internas + 1ª fora e direção de saída; ou {@link Path#empty()} se inviável
     */
    public static Path plan(int[][] grid, int rCross, int cCross, Direction entryDir) {
        int[] tl = find2x2TopLeft(grid, rCross, cCross);
        if (tl == null) {
            return planSimple(grid, rCross, cCross, entryDir);
        }
        return plan2x2(grid, rCross, cCross, entryDir, tl[0], tl[1]);
    }

    /**
     * Planeja a travessia para um cluster 2x2 de cruzamento ({9,10,11,12} em alguma ordem).
     *
     * Fluxo:
     * 1) Mapeia as 4 células do bloco 2x2 (TL/TR/BL/BR).
     * 2) Identifica a célula de entrada (primeira interna).
     * 3) Gera candidatos de saída (exclui “voltar”) e embaralha (variação).
     * 4) Monta o caminho interno por template e filtra pelo “primeiro par proibido”.
     * 5) Valida e anexa a 1ª célula FORA do cruzamento na direção de saída.
     *
     * @param g        grid
     * @param r        linha da célula interna pela qual o carro vai entrar
     * @param c        coluna da célula interna pela qual o carro vai entrar
     * @param entryDir direção de entrada
     * @param tlr      linha do top-left do bloco 2x2
     * @param tlc      coluna do top-left do bloco 2x2
     * @return {@link Path} pronto para reserva; ou {@link Path#empty()}
     */
    private static Path plan2x2(int[][] g, int r, int c, Direction entryDir, int tlr, int tlc) {
        // Mapa das 4 células do cluster (posicionais)
        int[][] cells = {
                {tlr, tlc},         // 0: TL  (esperado 12)
                {tlr, tlc + 1},     // 1: TR  (esperado 10)
                {tlr + 1, tlc},     // 2: BL  (esperado 11)
                {tlr + 1, tlc + 1}  // 3: BR  (esperado 9)
        };

        // Índice da célula pela qual o carro ENTRA primeiro no 2x2
        int entryIdx = idxOf(cells, r, c);

        // Saídas candidatas (não permitir voltar)
        List<Direction> candidates = new ArrayList<>(List.of(Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT));
        candidates.remove(LaneSupport.opposite(entryDir));
        Collections.shuffle(candidates, ThreadLocalRandom.current());

        for (Direction exit : candidates) {
            // Caminho INTERNO (somente dentro do 2x2)
            List<int[]> internal = internalPathTemplate(entryDir, entryIdx, exit, cells);
            if (internal.isEmpty()) continue;

            // ====== FILTRO DOS "PARES INICIAIS PROIBIDOS" POR CÓDIGO ======
            if (internal.size() >= 2) {
                int[] a = internal.get(0);
                int[] b = internal.get(1);
                int codeA = g[a[0]][a[1]];
                int codeB = g[b[0]][b[1]];
                if (isForbiddenFirstPairByCode(entryDir, codeA, codeB)) {
                    // viola a regra -> tenta outro exit
                    continue;
                }
            }
            // ===============================================================

            // Célula “fora” do cruzamento (primeira após a borda na direção de saída)
            int[] last = internal.get(internal.size() - 1);
            int outR = last[0] + exit.dirRow;
            int outC = last[1] + exit.dirCol;
            if (!inside(g, outR, outC)) continue;
            if (!LaneSupport.supportsDir(g[outR][outC], exit)) continue;

            // Caminho a reservar = todas internas + primeira fora
            List<int[]> reserve = new ArrayList<>(internal);
            reserve.add(new int[]{outR, outC});
            return new Path(reserve, exit);
        }

        return Path.empty();
    }

    /**
     * Templates internos do cluster 2×2.
     *
     * Convenção de índices:
     *
     * * 0: TL(12)   1: TR(10)
     * 2: BL(11)   3: BR(9)
     *
     * Observação:
     * - As sequências retornadas representam o CAMINHO INTERNO (sem incluir a célula atual antes do cruzamento)
     * - O filtro de pares proibidos por código é aplicado posteriormente.
     *
     * @param entryDir direção de entrada no 2x2
     * @param entryIdx índice da primeira célula interna (0..3)
     * @param exit     direção de saída desejada (já sem o “voltar”)
     * @param cells    mapeamento [0..3] → coordenadas (r,c) do 2x2
     * @return lista de células internas na ordem de travessia; vazia se impossível
     */
    private static List<int[]> internalPathTemplate(Direction entryDir, int entryIdx, Direction exit, int[][] cells) {
        int[] TL = cells[0], TR = cells[1], BL = cells[2], BR = cells[3];

        // === ENTRANDO PELA DIREITA (entryDir = LEFT) ===
        // Caso superior já existia (primeira interna: TR=10)
        if (entryDir == Direction.LEFT && entryIdx == 1) {
            return switch (exit) {
                case LEFT  -> List.of(TR, TL);     // reto p/ esquerda: 10 -> 12
                case UP    -> List.of(TR);         // sair acima via 10
                case DOWN  -> List.of(TR, TL, BL); // 10 -> 12 -> 11
                default    -> List.of();           // RIGHT (voltar) proibido
            };
        }
        // NOVO: caso inferior (primeira interna: BR=9)
        if (entryDir == Direction.LEFT && entryIdx == 3) {
            return switch (exit) {
                case LEFT  -> List.of(BR, BL);     // reto p/ esquerda: 9 -> 11
                case UP    -> List.of(BR, TR);     // virar p/ cima: 9 -> 10
                case DOWN  -> List.of(BR);         // sair p/ baixo via 9
                default    -> List.of();           // RIGHT proibido
            };
        }

        // === ENTRANDO PELA ESQUERDA (entryDir = RIGHT) ===
        // Caso superior já existia (primeira interna: TL=12)
        if (entryDir == Direction.RIGHT && entryIdx == 0) {
            return switch (exit) {
                case RIGHT -> List.of(TL, TR); // reto
                case UP    -> List.of(TL, TR); // sair p/ cima via TR
                case DOWN  -> List.of(TL, BL); // sair p/ baixo via BL
                default    -> List.of();
            };
        }
        // NOVO: caso inferior (primeira interna: BL=11)
        if (entryDir == Direction.RIGHT && entryIdx == 2) {
            return switch (exit) {
                case RIGHT -> List.of(BL, BR); // reto: 11 -> 9
                case UP    -> List.of(BL, TL); // virar p/ cima: 11 -> 12
                case DOWN  -> List.of(BL);     // sair p/ baixo via 11
                default    -> List.of();
            };
        }

        // === ENTRANDO POR CIMA (entryDir = DOWN) — já cobria TL(12) e TR(10) ===
        if (entryDir == Direction.DOWN && (entryIdx == 0 || entryIdx == 1)) {
            if (entryIdx == 1) { // TR=10
                return switch (exit) {
                    case DOWN -> List.of(TR, BR);
                    case LEFT -> List.of(TR, TL);
                    case RIGHT-> List.of(TR);
                    default  -> List.of();
                };
            } else { // TL=12
                return switch (exit) {
                    case DOWN -> List.of(TL, BL);
                    case RIGHT-> List.of(TL, TR);
                    case LEFT -> List.of(TL);
                    default  -> List.of();
                };
            }
        }

        // === ENTRANDO POR BAIXO (entryDir = UP) — já cobria BL(11) e BR(9) ===
        if (entryDir == Direction.UP && (entryIdx == 2 || entryIdx == 3)) {
            if (entryIdx == 2) { // BL=11
                return switch (exit) {
                    case UP    -> List.of(BL, TL);
                    case RIGHT -> List.of(BL, BR);
                    case LEFT  -> List.of(BL);
                    default    -> List.of();
                };
            } else { // BR=9
                return switch (exit) {
                    case UP    -> List.of(BR, TR);
                    case LEFT  -> List.of(BR, BL);
                    case RIGHT -> List.of(BR);
                    default    -> List.of();
                };
            }
        }

        return List.of();
    }

// ======= PARES INICIAIS PROIBIDOS POR CÓDIGO =======
     /**
     *  cima(DOWN):     12->10
     *  direita(LEFT):  11->12
     *  baixo(UP):      9->11
     *  esquerda(RIGHT):10->9
     *
     * Verifica se os dois primeiros códigos internos (primeiro passo → segundo passo)
     * infringem a regra de proibição, considerando o lado de ENTRADA.
     *
     * @param entryDir   direção de entrada (define a regra aplicada)
     * @param firstCode  código da 1ª célula interna percorrida
     * @param secondCode código da 2ª célula interna percorrida
     * @return true se a sequência é proibida para este lado de entrada
     */
    private static boolean isForbiddenFirstPairByCode(Direction entryDir, int firstCode, int secondCode) {
        return switch (entryDir) {
            case DOWN  -> (firstCode == 12 && secondCode == 10); // entra por CIMA: 12->10 proibido
            case RIGHT -> (firstCode == 11 && secondCode == 12); // entra pela ESQUERDA: 11->12 proibido
            case UP    -> (firstCode == 9  && secondCode == 11); // entra por BAIXO: 9->11 proibido
            case LEFT  -> (firstCode == 10 && secondCode == 9);  // entra pela DIREITA: 10->9 proibido
        };
    }

    /* ===================== FALLBACK SIMPLES ===================== */

    /**
     * Planejamento mínimo para células de cruzamento que não formam 2x2.
     * Escolhe uma saída possível (sem “voltar”) e anexa a 1ª célula fora.
     *
     * @param grid     malha
     * @param rCross   linha da célula de cruzamento
     * @param cCross   coluna da célula de cruzamento
     * @param entryDir direção de entrada
     * @return {@link Path} simples; ou {@link Path#empty()}
     */
    public static Path planSimple(int[][] grid, int rCross, int cCross, Direction entryDir) {
        var exits = LaneSupport.possibleExitDirsFromCross(grid[rCross][cCross]);
        exits.remove(LaneSupport.opposite(entryDir));

        List<Direction> feasible = new ArrayList<>();
        for (Direction d : exits) {
            int nr = rCross + d.dirRow, nc = cCross + d.dirCol;
            if (inside(grid, nr, nc) && LaneSupport.supportsDir(grid[nr][nc], d)) {
                feasible.add(d);
            }
        }
        if (feasible.isEmpty()) return Path.empty();

        Direction chosen = feasible.get(ThreadLocalRandom.current().nextInt(feasible.size()));
        List<int[]> cells = new ArrayList<>(2);
        cells.add(new int[]{rCross, cCross});
        cells.add(new int[]{rCross + chosen.dirRow, cCross + chosen.dirCol});
        return new Path(cells, chosen);
    }

    /* ===================== HELPERS ===================== */

    /**
     * Encontra de forma robusta o top-left (TL) de um bloco 2×2 de cruzamento que contenha (r,c).
     * Testa os 4 offsets possíveis: (0,0), (-1,0), (0,-1), (-1,-1).
     *
     * @param g grid
     * @param r linha de referência
     * @param c coluna de referência
     * @return par {tlr, tlc} ou {@code null} se não for um 2×2 válido (9,10,11,12)
     */
    private static int[] find2x2TopLeft(int[][] g, int r, int c) {
        int[] ro = {0, -1};
        int[] co = {0, -1};
        for (int dr : ro) for (int dc : co) {
            int tlr = r + dr, tlc = c + dc;
            if (!inside(g, tlr, tlc) || !inside(g, tlr + 1, tlc + 1)) continue;
            Set<Integer> s = new HashSet<>();
            int[][] corners = {{tlr, tlc}, {tlr, tlc + 1}, {tlr + 1, tlc}, {tlr + 1, tlc + 1}};
            boolean allCross = true;
            for (int[] p : corners) {
                int code = g[p[0]][p[1]];
                if (!isCrossCode(code)) { allCross = false; break; }
                s.add(code);
            }
            if (allCross && s.containsAll(Set.of(9, 10, 11, 12))) {
                return new int[]{tlr, tlc};
            }
        }
        return null;
    }

    /**
     * Retorna o índice [0..3] (TL/TR/BL/BR) das coordenadas (r,c) no array {@code cs},
     * ou -1 se (r,c) não pertence ao conjunto.
     */
    private static int idxOf(int[][] cs, int r, int c) {
        for (int i = 0; i < cs.length; i++) if (cs[i][0] == r && cs[i][1] == c) return i;
        return -1;
    }

    /**
     * True se o código for um dos cruzamentos 2-vias (9..12).
     */
    private static boolean isCrossCode(int code) {
        return code == 9 || code == 10 || code == 11 || code == 12;
    }

    /**
     * Checagem de limites do grid.
     */
    private static boolean inside(int[][] g, int r, int c) {
        return r >= 0 && c >= 0 && r < g.length && c < g[0].length;
    }

    /* ===================== DTO ===================== */

    /**
     * Resultado do planejamento:
     * - {@code cells}: sequência de células a reservar (todas internas + 1ª fora).
     * - {@code exitDir}: direção que o carro deve adotar ao concluir a travessia.
     *
     * Contratos:
     * - A lista {@code cells} NÃO inclui a célula atual do carro (antes de entrar).
     * - O chamador deve reservar todas as células antes de começar a avançar.
     */
    public static final class Path {
        public final List<int[]> cells; // TODAS internas + primeira fora
        public final Direction exitDir;
        public Path(List<int[]> cells, Direction exitDir) { this.cells = cells; this.exitDir = exitDir; }
        public static Path empty() { return new Path(List.of(), null); }
        public boolean isEmpty() { return cells == null || cells.isEmpty(); }
    }


}
