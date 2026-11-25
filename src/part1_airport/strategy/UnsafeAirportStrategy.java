package part1_airport.strategy;
import part1_airport.audit.SimulationStats;
import part1_airport.model.*;
import java.util.ArrayList;
import java.util.List;

public class UnsafeAirportStrategy implements AirportStrategy {
    private final int maxTerminals, maxRamps;
    private final List<Integer> occupiedTerminals = new ArrayList<>();
    private final List<Integer> occupiedRamps = new ArrayList<>();

    public UnsafeAirportStrategy(int maxTerminals, int maxRamps) { this.maxTerminals = maxTerminals; this.maxRamps = maxRamps; }
    @Override public String getName() { return "Unsafe (No Sync)"; }
    @Override public int getMaxTerminals() { return maxTerminals; }
    @Override public int getMaxRamps() { return maxRamps; }

    @Override
    public boolean processAircraft(Aircraft aircraft, long timeoutMs, SimulationStats stats) throws InterruptedException {
        BoardingChannel channel = aircraft.requiredChannel();
        List<Integer> pool = (channel == BoardingChannel.TERMINAL) ? occupiedTerminals : occupiedRamps;
        int limit = (channel == BoardingChannel.TERMINAL) ? maxTerminals : maxRamps;
        if (pool.size() >= limit) {
            Thread.sleep(10);
            if (pool.size() >= limit) { stats.rejectedCount.increment(); return false; }
        }
        int slotId = pool.size() + 1;
        pool.add(slotId);
        int activeT = occupiedTerminals.size(), activeR = occupiedRamps.size();
        stats.currentTerminalsInUse.set(activeT); stats.currentRampsInUse.set(activeR);
        stats.eventLog.add(new EventRecord(System.nanoTime(), aircraft.flightCode(), channel, slotId, "ACQUIRE", activeT, activeR));
        Thread.sleep(25);
        pool.remove(Integer.valueOf(slotId));
        stats.currentTerminalsInUse.set(occupiedTerminals.size()); stats.currentRampsInUse.set(occupiedRamps.size());
        stats.eventLog.add(new EventRecord(System.nanoTime(), aircraft.flightCode(), channel, slotId, "RELEASE", occupiedTerminals.size(), occupiedRamps.size()));
        stats.servedCount.increment();
        stats.totalPassengersBoarded.add(aircraft.capacity());
        return true;
    }
}
