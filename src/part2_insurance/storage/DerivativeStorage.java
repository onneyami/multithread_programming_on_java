package part2_insurance.storage;
import part2_insurance.model.InsuranceObligation;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public interface DerivativeStorage {
    String getName();
    void initialize(List<InsuranceObligation> initialData) throws IOException;
    void addOrUpdateAndSort(InsuranceObligation obligation) throws IOException;
    double calculateTotalCostAndSearch(double minRisk, double maxRisk, List<InsuranceObligation> foundOut) throws IOException, InterruptedException;

    default List<InsuranceObligation> readAllFromDisk(Path path) throws IOException {
        if (!Files.exists(path)) return new ArrayList<>();
        List<String> lines = Files.readAllLines(path);
        List<InsuranceObligation> list = new ArrayList<>(lines.size());
        for (String line : lines) if (!line.isBlank()) list.add(InsuranceObligation.fromCsv(line));
        return list;
    }

    default void writeAllAtomically(Path target, List<InsuranceObligation> items) throws IOException {
        Path tempFile = target.resolveSibling(target.getFileName() + ".tmp." + Thread.currentThread().threadId());
        List<String> lines = items.stream().map(InsuranceObligation::toCsv).toList();
        Files.write(tempFile, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        Files.move(tempFile, target, StandardCopyOption.REPLACE_EXISTING);
    }
}
