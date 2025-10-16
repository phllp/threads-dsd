package app.core;

import app.view.CarColors;
import javafx.scene.paint.Color;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Armazena e expõe o estado da simulação de forma thread-safe para a UI.
 *
 * Responsabilidades:
 * - Registrar o nascimento (spawn), movimentação (move) e saída (exit) de cada carro.
 * - Fornecer um snapshot iterável das posições para o renderer (JavaFX).
 *
 * Concorrência:
 * - Usa {@link ConcurrentHashMap} para permitir leituras e escritas simultâneas.
 * - Cada {@link CarInfo} mantém r/c como {@code volatile} para garantir visibilidade
 *   imediata entre threads (threads dos carros escrevem; thread JavaFX lê).
 */
public class SimulationState {

    /**
     * DTO de leitura/atualização da posição do carro.
     * - {@code id} e {@code color} são imutáveis após construção.
     * - {@code r} e {@code c} são {@code volatile} para garantir que a UI visualize
     *   as atualizações feitas pelas threads dos carros sem precisar de bloqueio extra.
     */
    public static final class CarInfo {
        public final long id;
        public final Color color;
        private volatile int r, c;

        CarInfo(long id, int r, int c, Color color) {
            this.id = id;
            this.r = r;
            this.c = c;
            this.color = color;

        }

        public int getR() {
            return r;
        }

        public int getC() {
            return c;
        }

        void set(int r, int c) {
            this.r = r; this.c = c;
        }
    }

    /** Mapa thread-safe id → info do carro. */
    private final ConcurrentMap<Long, CarInfo> cars = new ConcurrentHashMap<>();

    /**
     * Publica um novo carro no estado (chamado quando a thread do carro nasce).
     * A publicação no {@link ConcurrentHashMap} garante visibilidade segura do objeto {@link CarInfo}.
     */
    public void onSpawn(long id, int r, int c) {
        cars.put(
                id,
                new CarInfo(id, r, c, CarColors.colorForId(id)));
    }

    /**
     * Atualiza a posição do carro. Se o carro já foi removido, ignora silenciosamente.
     * As atualizações são visíveis imediatamente à UI graças aos campos {@code volatile}.
     */
    public void onMove (long id, int r, int c) {
        CarInfo info = cars.get(id);
        if (info != null) {
            info.set(r, c);
        };
    }

    /** Remove o carro do estado (chamado ao encerrar a thread do carro). */
    public void onExit (long id) {
        cars.remove(id);
    }

    /**
     * Quantidade de carros ativos no momento.
     */
    public int activeCount() {
        return cars.size();
    }

    /**
     * Snapshot iterável das posições
     */
    public Collection<CarInfo> snapshotPositions() {
        return cars.values();
    }


}
