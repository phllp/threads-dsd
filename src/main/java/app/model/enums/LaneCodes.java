package app.model.enums;

import java.util.ArrayList;

public enum LaneCodes {
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

    LaneCodes(int codigo) {
        this.codigo = codigo;
    }

    public int getCodigo() {
        return codigo;
    }

    public static boolean isOnCrossroad(int codigo) {
        ArrayList<Integer> crossroads = new ArrayList<>();
        crossroads.add(LaneCodes.CRUZAMENTO_CIMA.getCodigo());
        crossroads.add(LaneCodes.CRUZAMENTO_BAIXO.getCodigo());
        crossroads.add(LaneCodes.CRUZAMENTO_CIMA_DIREITA.getCodigo());
        crossroads.add(LaneCodes.CRUZAMENTO_ESQUERDA_BAIXO.getCodigo());
        crossroads.add(LaneCodes.CRUZAMENTO_DIREITA_BAIXO.getCodigo());
        crossroads.add(LaneCodes.CRUZAMENTO_CIMA_ESQUERDA.getCodigo());

        return crossroads.contains(codigo);
    }


}
