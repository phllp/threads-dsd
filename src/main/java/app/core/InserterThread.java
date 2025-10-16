package app.core;

import app.model.Car;
import app.model.RowSegment;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class InserterThread extends Thread {
    private final int[][] grid;
    private final SimulationState sim;
    private final CellLockGrid locks;

    private final IntSupplier maxCarsSupplier;              // spnMaxVeiculos::getValue
    private final IntSupplier minInsertMsSupplier;          // spnIntervaloMs::getValue
    private final Supplier<RowSegment> RowsegmentSupplier;  // findRandomEdgeRowSegment()
    private final List<Car> spawned = new ArrayList<>();

    private volatile boolean inserting = true;
    private volatile boolean running = true;

    // @todo trocar por um valor aleatório
    // velocidade dos carros
    private final IntSupplier carStepMsSupplier;

    public InserterThread(int[][] grid,
                          SimulationState sim,
                          CellLockGrid locks,
                          IntSupplier maxCarsSupplier,
                          IntSupplier minInsertMsSupplier,
                          IntSupplier carStepMsSupplier,
                          Supplier<RowSegment> RowsegmentSupplier) {
        this.grid = grid;
        this.sim = sim;
        this.locks = locks;
        this.maxCarsSupplier = maxCarsSupplier;
        this.minInsertMsSupplier = minInsertMsSupplier;
        this.carStepMsSupplier = carStepMsSupplier;
        this.RowsegmentSupplier = RowsegmentSupplier;
        setName("InserterThread");
        setDaemon(true);
    }

    public void stopInserting() {
        inserting = false;
    }

    public void resumeInserting() {
        inserting = true;
    }

    public void shutdown() {
        running = false;
        interrupt();
    }

    public List<Car> getSpawned() {
        return spawned;
    }

    @Override
    public void run() {
        long lastSpawn = 0;
        try {
            while (running) {
                int maxCars = Math.max(1, maxCarsSupplier.getAsInt());
                int minGap  = Math.max(1, minInsertMsSupplier.getAsInt());

                long now = System.currentTimeMillis();

                // Limite de carros ativos na simulação não atingido
                boolean carsAvailable = sim.activeCount() < maxCars;
                // Tempo mínimo de inserção satisfeito
                boolean insertionGapFulfilled = (now - lastSpawn) >= minGap;

                if (inserting && carsAvailable && insertionGapFulfilled) {
                    RowSegment seg = RowsegmentSupplier.get();
                    if (seg != null) {
                        int step = carStepMsSupplier.getAsInt();

                        Car v = new Car(
                                sim,
                                grid,
                                locks,
                                seg.getR0(),
                                seg.getC0(),
                                seg.getR1(),
                                seg.getC1(),
                                step,
                                seg.getDirection()
                        );

                        v.start();
                        synchronized (spawned) {
                            spawned.add(v);
                        }

                        lastSpawn = now;
                    }
                }

                // Sleep adicionado para evitar que o while rode freneticamente sem necessidade
                // O tempo de inclusão de um novo carro continua sendo parametrizado
                Thread.sleep(10);
            }
        } catch (InterruptedException ignored) {}
    }


}
