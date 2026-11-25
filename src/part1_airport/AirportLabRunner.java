package part1_airport;
import part1_airport.audit.*;
import part1_airport.model.*;
import part1_airport.strategy.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class AirportLabRunner {
    private static final int MAX_TERMINALS = 3, MAX_RAMPS = 4, AIRCRAFT_COUNT = 80;
    private static final long TIMEOUT_MS = 180;
    public record SummaryRow(String strategy, long timeMs, long served, long rejected, long passengers, String audit) {}

    public static void main(String[] args) throws InterruptedException {
        List<Aircraft> fleet = generateFleet(AIRCRAFT_COUNT);
        List<AirportStrategy> strategies = List.of(
                new UnsafeAirportStrategy(MAX_TERMINALS, MAX_RAMPS),
                new ClassicAirportStrategy(MAX_TERMINALS, MAX_RAMPS),
                new ConcurrentAirportStrategy(MAX_TERMINALS, MAX_RAMPS)
        );
        List<SummaryRow> results = new ArrayList<>();
        for (AirportStrategy strategy : strategies) results.add(runSimulation(strategy, fleet, false));

        List<Aircraft> heavyFleet = generateFleet(1500);
        SummaryRow platformHeavy = runSimulation(new ConcurrentAirportStrategy(15, 20), heavyFleet, false);
        SummaryRow virtualHeavy = runSimulation(new ConcurrentAirportStrategy(15, 20), heavyFleet, true);
        printSummaryTable(results, platformHeavy, virtualHeavy);
    }

    private static SummaryRow runSimulation(AirportStrategy strategy, List<Aircraft> fleet, boolean useVirtualThreads) throws InterruptedException {
        SimulationStats stats = new SimulationStats();
        AtomicBoolean running = new AtomicBoolean(true);
        ScheduledExecutorService dashboardExecutor = Executors.newSingleThreadScheduledExecutor();
        dashboardExecutor.scheduleAtFixedRate(() -> {
            if (running.get()) {
                System.out.printf("\r[%s] Terminals: %d/%d | Ramps: %d/%d | Served: %d | Rejected: %d   ",
                        strategy.getName(), stats.currentTerminalsInUse.get(), strategy.getMaxTerminals(),
                        stats.currentRampsInUse.get(), strategy.getMaxRamps(), stats.servedCount.sum(), stats.rejectedCount.sum());
            }
        }, 0, 250, TimeUnit.MILLISECONDS);

        ExecutorService workerPool = useVirtualThreads ? Executors.newVirtualThreadPerTaskExecutor() : Executors.newFixedThreadPool(16);
        long startTime = System.nanoTime();
        CountDownLatch latch = new CountDownLatch(fleet.size());
        for (Aircraft plane : fleet) {
            workerPool.submit(() -> {
                try { strategy.processAircraft(plane, TIMEOUT_MS, stats); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                finally { latch.countDown(); }
            });
        }
        latch.await();
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        running.set(false);
        shutdownPool(workerPool); shutdownPool(dashboardExecutor);
        System.out.println();

        String auditResult = InvariantAuditor.validate(stats, strategy.getMaxTerminals(), strategy.getMaxRamps());
        String label = useVirtualThreads ? strategy.getName() + " [VirtualThreads]" : strategy.getName();
        return new SummaryRow(label, elapsedMs, stats.servedCount.sum(), stats.rejectedCount.sum(), stats.totalPassengersBoarded.sum(), auditResult);
    }

    private static void shutdownPool(ExecutorService pool) throws InterruptedException {
        pool.shutdown();
        if (!pool.awaitTermination(5, TimeUnit.SECONDS)) pool.shutdownNow();
    }

    private static List<Aircraft> generateFleet(int count) {
        Random rnd = new Random(42);
        RangeZone[] zones = RangeZone.values(); Destination[] dests = Destination.values();
        List<Aircraft> list = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            RangeZone zone = zones[rnd.nextInt(zones.length)];
            int capacity = (zone == RangeZone.LONG_HAUL) ? 200 + rnd.nextInt(150) : 70 + rnd.nextInt(100);
            list.add(new Aircraft(i, "FL-" + (1000 + i), capacity, zone, dests[rnd.nextInt(dests.length)]));
        }
        return list;
    }

    private static void printSummaryTable(List<SummaryRow> rows, SummaryRow platformHeavy, SummaryRow virtualHeavy) {
        System.out.println("\n=== PART 1: AIRPORT SIMULATION SUMMARY ===");
        System.out.printf("%-38s | %8s | %7s | %8s | %10s | %s%n", "Strategy", "Time(ms)", "Served", "Rejected", "Passengers", "Invariant Audit");
        System.out.println("-".repeat(115));
        for (SummaryRow r : rows) {
            System.out.printf("%-38s | %8d | %7d | %8d | %10d | %s%n", r.strategy(), r.timeMs(), r.served(), r.rejected(), r.passengers(), r.audit());
        }
        System.out.println("-".repeat(115));
        System.out.println("HIGH LOAD (1500 aircraft) PLATFORM vs VIRTUAL THREADS:");
        System.out.printf("%-38s | %8d | %7d | %8d | %10d | %s%n", platformHeavy.strategy(), platformHeavy.timeMs(), platformHeavy.served(), platformHeavy.rejected(), platformHeavy.passengers(), platformHeavy.audit());
        System.out.printf("%-38s | %8d | %7d | %8d | %10d | %s%n", virtualHeavy.strategy(), virtualHeavy.timeMs(), virtualHeavy.served(), virtualHeavy.rejected(), virtualHeavy.passengers(), virtualHeavy.audit());
    }
}
