package app.model.enums;

public enum Direction {

        UP(-1, 0, LaneCode.ESTRADA_CIMA),
        RIGHT(0, 1, LaneCode.ESTRADA_DIREITA),
        DOWN(1, 0, LaneCode.ESTRADA_BAIXO),
        LEFT(0, -1, LaneCode.ESTRADA_ESQUERDA);

        public final int dr;
        public final int dc;
        public final LaneCode laneCode;

        Direction(int dr, int dc, LaneCode laneCode) {
            this.dr = dr; this.dc = dc; this.laneCode = laneCode;
        }


}
