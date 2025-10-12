package app.view;

import javafx.scene.paint.Color;

public final class CarColors {
    private CarColors() {}
    private static final double PHI = 0.61803398875;

    /** Cor estável por id, boa visibilidade geral */
    public static Color colorForId(long id) {
        double h = (id * PHI) % 1.0;       // matiz variado
        double s = 0.80;                   // saturado, mas não gritante
        double b = 0.88;                   // claro o bastante p/ se destacar
        return Color.hsb(h * 360.0, s, b);
    }
}
