package part2_insurance.storage;
import part2_insurance.model.InsuranceObligation;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.locks.*;

public class ReadWriteLockFileStorage implements DerivativeStorage {
    private final Path filePath;
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock(true);
    private final Lock readLock = rwLock.readLock(), writeLock = rwLock.writeLock();

    public ReadWriteLockFileStorage(Path filePath) { this.filePath = filePath; }
    @Override public String getName() { return "ReadWriteLockFile"; }

    @Override
    public void initialize(List<InsuranceObligation> initialData) throws IOException {
        writeLock.lock();
        try { writeAllAtomically(filePath, initialData); } finally { writeLock.unlock(); }
    }

    @Override
    public void addOrUpdateAndSort(InsuranceObligation obligation) throws IOException {
        writeLock.lock();
        try {
            List<InsuranceObligation> current = readAllFromDisk(filePath);
            current.removeIf(o -> o.getId().equals(obligation.getId()));
            current.add(obligation);
            current.sort(Comparator.comparingDouble(InsuranceObligation::getRiskLevel).reversed());
            writeAllAtomically(filePath, current);
        } finally { writeLock.unlock(); }
    }

    @Override
    public double calculateTotalCostAndSearch(double minRisk, double maxRisk, List<InsuranceObligation> foundOut) throws IOException, InterruptedException {
        readLock.lock();
        try {
            Thread.sleep(120);
            List<InsuranceObligation> current = readAllFromDisk(filePath);
            double total = 0;
            for (InsuranceObligation o : current) {
                total += o.getCost();
                if (o.getRiskLevel() >= minRisk && o.getRiskLevel() <= maxRisk) foundOut.add(o);
            }
            return total;
        } finally { readLock.unlock(); }
    }
}
