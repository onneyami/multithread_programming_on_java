package part2_insurance.storage;
import part2_insurance.model.InsuranceObligation;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class SynchronizedFileStorage implements DerivativeStorage {
    private final Path filePath;
    public SynchronizedFileStorage(Path filePath) { this.filePath = filePath; }
    @Override public String getName() { return "SynchronizedFile"; }
    @Override public synchronized void initialize(List<InsuranceObligation> initialData) throws IOException { writeAllAtomically(filePath, initialData); }

    @Override
    public synchronized void addOrUpdateAndSort(InsuranceObligation obligation) throws IOException {
        List<InsuranceObligation> current = readAllFromDisk(filePath);
        current.removeIf(o -> o.getId().equals(obligation.getId()));
        current.add(obligation);
        current.sort(Comparator.comparingDouble(InsuranceObligation::getRiskLevel).reversed());
        writeAllAtomically(filePath, current);
    }

    @Override
    public synchronized double calculateTotalCostAndSearch(double minRisk, double maxRisk, List<InsuranceObligation> foundOut) throws IOException, InterruptedException {
        Thread.sleep(120);
        List<InsuranceObligation> current = readAllFromDisk(filePath);
        double total = 0;
        for (InsuranceObligation o : current) {
            total += o.getCost();
            if (o.getRiskLevel() >= minRisk && o.getRiskLevel() <= maxRisk) foundOut.add(o);
        }
        return total;
    }
}
