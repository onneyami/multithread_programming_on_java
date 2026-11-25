package part2_insurance;
import part2_insurance.model.*;
import part2_insurance.storage.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;

public class InsuranceLabRunner {
    public static void main(String[] args) throws Exception {
        Path workDir = Files.createTempDirectory("insurance_lab4_");
        List<InsuranceObligation> seed = List.of(
                new LifeInsurance("OBL-1", "Alice", 1200.0, 4.5, 20),
                new PropertyInsurance("OBL-2", "Bob", 3500.0, 7.8, "Main St 10"),
                new LiabilityInsurance("OBL-3", "CorpX", 8000.0, 9.1, 500000.0)
        );
        List<DerivativeStorage> storages = List.of(
                new UnsafeFileStorage(workDir.resolve("unsafe.csv")),
                new SynchronizedFileStorage(workDir.resolve("sync.csv")),
                new ReadWriteLockFileStorage(workDir.resolve("rwlock.csv"))
        );
        System.out.println("=== PART 2: READERS-WRITERS FILE STORAGE BENCHMARK ===");
        System.out.printf("%-20s | %10s | %12s | %14s | %s%n", "Strategy", "Time(ms)", "Read Ops", "Write Ops", "Data Integrity");
        System.out.println("-".repeat(85));
        for (DerivativeStorage storage : storages) runBenchmark(storage, seed, workDir);
    }

    private static void runBenchmark(DerivativeStorage storage, List<InsuranceObligation> seed, Path workDir) throws IOException, InterruptedException {
        storage.initialize(seed);
        int writerThreads = 3, readerThreads = 6, opsPerThread = 4;
        ExecutorService pool = Executors.newFixedThreadPool(writerThreads + readerThreads);
        CountDownLatch latch = new CountDownLatch(writerThreads + readerThreads);
        LongAdder readSuccess = new LongAdder(), writeSuccess = new LongAdder(), errors = new LongAdder();
        long start = System.nanoTime();

        for (int w = 0; w < writerThreads; w++) {
            final int writerId = w;
            pool.submit(() -> {
                try {
                    for (int i = 0; i < opsPerThread; i++) {
                        String id = "W" + writerId + "-" + i;
                        double risk = 2.0 + ((writerId * 3 + i) % 8);
                        storage.addOrUpdateAndSort(new PropertyInsurance(id, "Client_" + id, 1500.0 + i * 100, risk, "Addr " + i));
                        writeSuccess.increment();
                    }
                } catch (Exception e) { errors.increment(); }
                finally { latch.countDown(); }
            });
        }

        for (int r = 0; r < readerThreads; r++) {
            pool.submit(() -> {
                try {
                    for (int i = 0; i < opsPerThread; i++) {
                        List<InsuranceObligation> matches = new ArrayList<>();
                        storage.calculateTotalCostAndSearch(5.0, 9.5, matches);
                        readSuccess.increment();
                    }
                } catch (Exception e) { errors.increment(); }
                finally { latch.countDown(); }
            });
        }

        latch.await();
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);

        String fileName = switch (storage.getName()) {
            case "UnsafeFile" -> "unsafe.csv";
            case "SynchronizedFile" -> "sync.csv";
            default -> "rwlock.csv";
        };
        List<InsuranceObligation> finalRecords = storage.readAllFromDisk(workDir.resolve(fileName));
        boolean sorted = isSortedDescending(finalRecords);
        String integrity = (errors.sum() == 0 && finalRecords.size() == 15 && sorted)
                ? "OK (15 records, sorted)"
                : "CORRUPTED/LOST (records=" + finalRecords.size() + "/15, errors=" + errors.sum() + ")";

        System.out.printf("%-20s | %10d | %12d | %14d | %s%n", storage.getName(), elapsedMs, readSuccess.sum(), writeSuccess.sum(), integrity);
    }

    private static boolean isSortedDescending(List<InsuranceObligation> list) {
        for (int i = 1; i < list.size(); i++) if (list.get(i - 1).getRiskLevel() < list.get(i).getRiskLevel()) return false;
        return true;
    }
}
