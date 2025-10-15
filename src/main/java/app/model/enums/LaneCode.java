package app.model.enums;

import java.util.ArrayList;

public enum LaneCode {
    NADA(0),
    ESTRADA_CIMA(1),
    ESTRADA_DIREITA(2),
    ESTRADA_BAIXO(3),
    ESTRADA_ESQUERDA(4),
    CRUZAMENTO_CIMA(5),
    CRUZAMENTO_DIREITA(6),
    CRUZAMENTO_BAIXO(7),
    CRUZAMENTO_ESQUERDA(8),
    CRUZAMENTO_CIMA_DIREITA(9),
    CRUZAMENTO_CIMA_ESQUERDA(10),
    CRUZAMENTO_DIREITA_BAIXO(11),
    CRUZAMENTO_ESQUERDA_BAIXO(12);

    private final int codigo;

    LaneCode(int codigo) {
        this.codigo = codigo;
    }

    public int getCodigo() {
        return codigo;
    }

    public static boolean isOnCrossroad(int codigo) {
        ArrayList<Integer> crossroads = new ArrayList<>();
        crossroads.add(LaneCode.CRUZAMENTO_CIMA.getCodigo());           // 5
        crossroads.add(LaneCode.CRUZAMENTO_DIREITA.getCodigo());        // 6  <-- ADICIONE
        crossroads.add(LaneCode.CRUZAMENTO_BAIXO.getCodigo());          // 7
        crossroads.add(LaneCode.CRUZAMENTO_ESQUERDA.getCodigo());       // 8  <-- ADICIONE
        crossroads.add(LaneCode.CRUZAMENTO_CIMA_DIREITA.getCodigo());   // 9
        crossroads.add(LaneCode.CRUZAMENTO_CIMA_ESQUERDA.getCodigo());  // 10
        crossroads.add(LaneCode.CRUZAMENTO_DIREITA_BAIXO.getCodigo());  // 11
        crossroads.add(LaneCode.CRUZAMENTO_ESQUERDA_BAIXO.getCodigo()); // 12

        return crossroads.contains(codigo);
    }


}
