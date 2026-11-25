package part1_airport.audit;
import part1_airport.model.BoardingChannel;
import part1_airport.model.EventRecord;
import java.util.HashSet;
import java.util.Set;

public class InvariantAuditor {
    public static String validate(SimulationStats stats, int maxTerminals, int maxRamps) {
        Set<Integer> busyTerminals = new HashSet<>();
        Set<Integer> busyRamps = new HashSet<>();
        int violations = 0;
        String firstReason = "";
        for (EventRecord e : stats.eventLog) {
            if (e.activeTerminalsSnapshot() < 0 || e.activeTerminalsSnapshot() > maxTerminals) {
                violations++;
                if (firstReason.isEmpty()) firstReason = "Terminals out of bounds: " + e.activeTerminalsSnapshot();
            }
            if (e.activeRampsSnapshot() < 0 || e.activeRampsSnapshot() > maxRamps) {
                violations++;
                if (firstReason.isEmpty()) firstReason = "Ramps out of bounds: " + e.activeRampsSnapshot();
            }
            Set<Integer> targetSet = (e.channel() == BoardingChannel.TERMINAL) ? busyTerminals : busyRamps;
            if ("ACQUIRE".equals(e.action())) {
                if (!targetSet.add(e.slotId())) {
                    violations++;
                    if (firstReason.isEmpty()) firstReason = "Double occupancy on " + e.channel() + " #" + e.slotId();
                }
            } else if ("RELEASE".equals(e.action())) {
                targetSet.remove(e.slotId());
            }
        }
        return (violations == 0) ? "OK (0 violations)" : "FAILED (" + violations + " violations: " + firstReason + ")";
    }
}
