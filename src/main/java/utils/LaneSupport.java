package utils;

import app.model.enums.Direction;
import app.model.enums.LaneCode;

import java.util.EnumSet;

/**
 * Utilitários de apoio à malha viária.
 *
 * Responsabilidades:
 * - Dizer a direção oposta (ex.: UP ↔ DOWN) — útil para evitar retorno/U-turn.
 * - Verificar se uma célula (código da malha) suporta movimento em uma direção.
 * - Informar quais direções de saída são possíveis a partir de uma célula de cruzamento.
 *
 * Observações:
 * - O mapeamento de compatibilidade usa os códigos definidos em {@link LaneCode}.
 * - “Cruzamentos com duas vias” (9–12) permitem tanto vertical quanto horizontal.
 * - Esta classe é stateless e final: apenas funções puras/utilitárias.
 */
public final class LaneSupport {
    private LaneSupport() {}

    /**
     * Retorna a direção oposta à informada.
     * @param d direção atual
     * @return direção oposta
     */
    public static Direction opposite(Direction d) {
        return switch (d) {
            case UP -> Direction.DOWN;
            case DOWN -> Direction.UP;
            case LEFT -> Direction.RIGHT;
            case RIGHT -> Direction.LEFT;
        };
    }

    /**
     * Informa se uma célula (identificada por seu código) permite movimento na direção dada.
     *
     * Regra geral:
     * - Vias verticais (estrada/cross vertical) aceitam UP/DOWN.
     * - Vias horizontais (estrada/cross horizontal) aceitam LEFT/RIGHT.
     * - Cruzamentos 2-vias (9–12) aceitam qualquer direção.
     * - NADA (0) não permite deslocamento.
     *
     * @param code código da célula no grid (ver {@link LaneCode})
     * @param dir  direção desejada de movimento
     * @return true se a célula de destino é compatível com a direção; false caso contrário
     */
    public static boolean supportsDir(int code, Direction dir) {
        LaneCode lc = fromCode(code);
        if (lc == null) return false;

        return switch (lc) {
            // VERTICAL (cima/baixo)
            case ESTRADA_CIMA, ESTRADA_BAIXO,
                    CRUZAMENTO_CIMA, CRUZAMENTO_BAIXO
                    -> (dir == Direction.UP || dir == Direction.DOWN);

            // HORIZONTAL (esquerda/direita)
            case ESTRADA_DIREITA, ESTRADA_ESQUERDA,
                    CRUZAMENTO_DIREITA, CRUZAMENTO_ESQUERDA
                    -> (dir == Direction.LEFT || dir == Direction.RIGHT);

            // CRUZAMENTOS COM AS DUAS VIAS (permitem vertical e horizontal)
            case CRUZAMENTO_CIMA_DIREITA, CRUZAMENTO_CIMA_ESQUERDA,
                    CRUZAMENTO_DIREITA_BAIXO, CRUZAMENTO_ESQUERDA_BAIXO
                    -> true;

            case NADA -> false;
        };
    }


    /**
     * Direções de saída “possíveis” quando o carro está sobre um cruzamento.
     * <p>
     * Importante:
     * - Não considera retorno/U-turn (essa regra é aplicada no planner).
     * - Para cruzamentos 2-vias (9–12), devolve as quatro direções.
     * - Para cruzamentos 1-via (5–8), devolve apenas o conjunto coerente (vertical OU horizontal).
     *
     * @param code código da célula de cruzamento
     * @return conjunto de direções permitidas a partir desta célula
     */
    public static EnumSet<Direction> possibleExitDirsFromCross(int code) {
        EnumSet<Direction> s = EnumSet.noneOf(Direction.class);
        LaneCode lc = fromCode(code);
        if (lc == null) return s;

        switch (lc) {
            // Cruzamentos verticais (apenas UP/DOWN)
            case CRUZAMENTO_CIMA, CRUZAMENTO_BAIXO -> {
                s.add(Direction.UP);
                s.add(Direction.DOWN);
            }

            // Cruzamentos horizontais (apenas LEFT/RIGHT)
            case CRUZAMENTO_DIREITA, CRUZAMENTO_ESQUERDA -> {
                s.add(Direction.LEFT);
                s.add(Direction.RIGHT);
            }

            // Cruzamentos 2-vias (vertical + horizontal)
            case CRUZAMENTO_CIMA_DIREITA,
                    CRUZAMENTO_CIMA_ESQUERDA,
                    CRUZAMENTO_DIREITA_BAIXO,
                    CRUZAMENTO_ESQUERDA_BAIXO -> {
                s.add(Direction.UP);
                s.add(Direction.DOWN);
                s.add(Direction.LEFT);
                s.add(Direction.RIGHT);
            }

            // Não é cruzamento válido (ou NADA): retorna vazio
            default -> {}
        }
        return s;
    }

    /**
     * Converte o inteiro do grid em {@link LaneCode}.
     * @param code código bruto da célula
     * @return enum correspondente ou null se não encontrado
     */
    private static LaneCode fromCode(int code) {
        for (LaneCode lc : LaneCode.values()) if (lc.getCodigo() == code) return lc;
        return null;
    }


}
