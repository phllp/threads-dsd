package app.model;

import app.core.CellLockGrid;
import app.core.CellLockGridSemaphore;
import app.model.enums.Direction;
import app.core.SimulationState;
import app.model.enums.LaneCode;
import utils.CrossPlanner;

import java.util.ArrayList;
import java.util.List;

public class Car extends Thread {
    private final SimulationState simState;
    private final CellLockGrid locks;
    private final int[][] grid;
    private final int stepMs;

    private int row;
    private int col;
    private final int endRow;
    private final int endCol;

    private Direction direction;

    private volatile boolean running = true;

    public Car(SimulationState simState,
               int[][] grid,
               CellLockGrid locks,
               int row,
               int startCol,
               int endRow,
               int endCol,
               int stepMs,
               Direction dir) {
        this.simState = simState;
        this.grid = grid;
        this.locks = locks;
        this.row = row;
        this.col = startCol;
        this.endRow = endRow;
        this.endCol = endCol;
        this.stepMs = stepMs;
        this.direction = dir;
        setName("RelampagoMarquinhos-" + getId());

        // A thread pode ser interrompida junto com a aplicação
        setDaemon(true);
    }

    public void requestStop() {
        running = false;
        interrupt();
    }

    private boolean reachedEnd() {
        return row == endRow && col == endCol;
    }

    @Override
    public void run() {
        try {
            // Bloqueia a célula inicial para garantir exclusão mútua desde o spawn
            locks.acquire(row, col);
            // Registra o carro no estado para saber cor, posição, etc
            simState.onSpawn(getId(), row, col);

            // [LOOP PRINCIPAL] – executa enquanto a thread estiver ativa e a rota não terminou
            while (running && !reachedEnd()) {
                // Calcula próxima célula na direção
                int nextRow = row + direction.dirRow;
                int nextCol = col + direction.dirCol;

                // Continua dentro da malha?
                if (nextRow < 0 || nextCol < 0 || nextRow >= grid.length || nextCol >= grid[0].length) {
                    // saiu da malha
                    break;
                }

                // Armazena o código da próxima célula do grid
                int nextCode = grid[nextRow][nextCol];

                boolean stillGoingSameWay = nextCode == direction.laneCode.getCodigo();
                // Se a próxima célula pertence ao mesmo “tipo de via” na direção atual
                if (stillGoingSameWay) {
                    // Continua na mesma direção: adquire próximo, atualiza posição, libera a anterior
                    locks.acquire(nextRow, nextCol);
                    int previousRow = row;
                    int previousCol = col;

                    row = nextRow;
                    col = nextCol;

                    // Notifica a UI/estado para redesenho com a nova posição
                    simState.onMove(getId(), row, col);

                    // Libera a célula que ficou para trás
                    locks.release(previousRow, previousCol);
                    //Controle da velocidade
                    Thread.sleep(Math.max(1, stepMs));

                    // Cruzamento detectado
                } else if (LaneCode.isOnCrossroad(nextCode)) {
                    // 2.1) Planejamento: define caminho interno pelo cruzamento + célula de saída.
                    //      - escolhe saída antes de entrar
                    //      - aplica regras de pares proibidos
                    //      - inclui a 1ª célula fora (evita “parar em cima” do cruzamento)
                    var plan = CrossPlanner.plan(grid, nextRow, nextCol, direction);

                    if (plan.isEmpty()) {
                        // Sem rota viável agora — reavaliar no próximo ciclo
                        Thread.sleep(Math.max(1, stepMs));
                        continue;
                    }

                    // Faz uma cópia: acquireAll pode ordenar a lista para prevenir deadlocks
                    List<int[]> path = new ArrayList<>(plan.cells);

                    // 2) Tenta reservar todas as células do caminho (cruzamento + 1 após a saída)
                    //    não inclui a célula atual pois ela já está travada
                    long timeout = 200; //@todo parametrizar via UI
                    if (!locks.acquireAll(new ArrayList<>(plan.cells), timeout)) {
                        // Não conseguiu reservar agora — tenta depois
                        Thread.sleep(Math.max(1, stepMs));
                        continue;
                    }

                    // Índice da última célula efetivamente ocupada do 'path' (para rollback seguro)
                    int progressed = -1;

                    // Guarda a célula ocupada no momento (será liberada a cada avanço).
                    int prevR = row;
                    int prevC = col;


                    try {
                        // 2.3) Travessia do cruzamento:
                        //      anda célula-a-célula liberando sempre a anterior, mantendo
                        //      reservadas as futuras (garantindo que não bloqueia o cruzamento)
                        for (int i = 0; i < path.size(); i++) {
                            int[] step = path.get(i);
                            int tr = step[0], tc = step[1];

                            row = tr;
                            col = tc;

                            // Atualiza posição para renderização
                            simState.onMove(getId(), row, col);

                            // Libera só a célula anterior
                            locks.release(prevR, prevC);

                            prevR = row;
                            prevC = col;
                            progressed = i;

                            Thread.sleep(Math.max(1, stepMs));
                        }

                        // 2.4) Ao concluir o trajeto interno, o carro já está fora do cruzamento:
                        //      atualiza a direção para a direção de saída planejada.
                        direction = plan.exitDir;

                        // Observação: prevR e prevC continuam bloqueados (célula atual)
                        // Ela permanece sob posse deste carro até a próxima iteração do loop, quando outro passo for decidido
                    } finally {
                        // 2.5) Limpeza defensiva:
                        // Se houve interrupção/exceção dentro do for, libera SOMENTE as células
                        // futuras ainda reservadas e não utilizadas. Evita “over-release”.
                        for (int i = progressed + 1; i < path.size(); i++) {
                            int[] p = path.get(i);
                            locks.release(p[0], p[1]);
                        }
                    }

                }  else {
                    // Continua na mesma direção: adquire próximo, atualiza posição, libera a anterior
                    locks.acquire(nextRow, nextCol);
                    int previousRow = row;
                    int previousCol = col;

                    row = nextRow;
                    col = nextCol;

                    // Notifica a UI/estado para redesenho com a nova posição
                    simState.onMove(getId(), row, col);


                    direction = Direction.getDirectionFromLaneCode(nextCode);

                    // Libera a célula que ficou para trás
                    locks.release(previousRow, previousCol);
                    //Controle da velocidade
                    Thread.sleep(Math.max(1, stepMs));
                }

            }
        } catch (InterruptedException ignored) {
            // encerrando
        } finally {
            // [SHUTDOWN] – Libera a célula atual e remove do estado
            try {
                locks.release(row, col);
            } catch (Exception ignored) {}
            simState.onExit(getId());
        }
    }


}
