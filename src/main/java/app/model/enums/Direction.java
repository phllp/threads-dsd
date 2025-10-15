package app.model.enums;

public enum Direction {
        /**
         * Mover para a direita  = +1 coluna
         * Mover para a esquerda = -1 coluna
         * Mover para baixo      = +1 linha
         * Mover para cima       = -1 linha
         */
        UP(-1, 0, LaneCode.ESTRADA_CIMA),
        RIGHT(0, 1, LaneCode.ESTRADA_DIREITA),
        DOWN(1, 0, LaneCode.ESTRADA_BAIXO),
        LEFT(0, -1, LaneCode.ESTRADA_ESQUERDA);

        public final int dirRow;
        public final int dirCol;
        public final LaneCode laneCode;

        Direction(int dirRow, int dirCol, LaneCode laneCode) {
            this.dirRow = dirRow;
            this.dirCol = dirCol;
            this.laneCode = laneCode;
        }


}
