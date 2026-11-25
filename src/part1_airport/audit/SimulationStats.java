package part1_airport.audit;
import part1_airport.model.EventRecord;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

public class SimulationStats {
    public final LongAdder servedCount = new LongAdder();
    public final LongAdder rejectedCount = new LongAdder();
    public final LongAdder totalPassengersBoarded = new LongAdder();
    public final AtomicInteger currentTerminalsInUse = new AtomicInteger(0);
    public final AtomicInteger currentRampsInUse = new AtomicInteger(0);
    public final Queue<EventRecord> eventLog = new ConcurrentLinkedQueue<>();

    public void reset() {
        servedCount.reset(); rejectedCount.reset(); totalPassengersBoarded.reset();
        currentTerminalsInUse.set(0); currentRampsInUse.set(0); eventLog.clear();
    }
}
