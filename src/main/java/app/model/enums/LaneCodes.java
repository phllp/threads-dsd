package app.model.enums;

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


}
