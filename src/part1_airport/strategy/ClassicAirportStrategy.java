package part1_airport.strategy;
import part1_airport.audit.SimulationStats;
import part1_airport.model.*;

public class ClassicAirportStrategy implements AirportStrategy {
    private final int maxTerminals, maxRamps;
    private final boolean[] terminalsInUse, rampsInUse;
    private int activeTerminals = 0, activeRamps = 0;

    public ClassicAirportStrategy(int maxTerminals, int maxRamps) {
        this.maxTerminals = maxTerminals; this.maxRamps = maxRamps;
        this.terminalsInUse = new boolean[maxTerminals]; this.rampsInUse = new boolean[maxRamps];
    }
    @Override public String getName() { return "Classic (synchronized/wait)"; }
    @Override public int getMaxTerminals() { return maxTerminals; }
    @Override public int getMaxRamps() { return maxRamps; }

    @Override
    public boolean processAircraft(Aircraft aircraft, long timeoutMs, SimulationStats stats) throws InterruptedException {
        BoardingChannel channel = aircraft.requiredChannel();
        int slotId;
        synchronized (this) {
            long deadline = System.currentTimeMillis() + timeoutMs;
            while (!hasFreeSlot(channel)) {
                long remaining = deadline - System.currentTimeMillis();
                if (remaining <= 0) { stats.rejectedCount.increment(); return false; }
                this.wait(remaining);
            }
            slotId = allocateSlot(channel);
            stats.currentTerminalsInUse.set(activeTerminals); stats.currentRampsInUse.set(activeRamps);
            stats.eventLog.add(new EventRecord(System.nanoTime(), aircraft.flightCode(), channel, slotId, "ACQUIRE", activeTerminals, activeRamps));
        }
        try { Thread.sleep(25); } finally {
            synchronized (this) {
                releaseSlot(channel, slotId);
                stats.currentTerminalsInUse.set(activeTerminals); stats.currentRampsInUse.set(activeRamps);
                stats.eventLog.add(new EventRecord(System.nanoTime(), aircraft.flightCode(), channel, slotId, "RELEASE", activeTerminals, activeRamps));
                this.notifyAll();
            }
        }
        stats.servedCount.increment(); stats.totalPassengersBoarded.add(aircraft.capacity());
        return true;
    }

    private boolean hasFreeSlot(BoardingChannel channel) {
        return (channel == BoardingChannel.TERMINAL) ? activeTerminals < maxTerminals : activeRamps < maxRamps;
    }
    private int allocateSlot(BoardingChannel channel) {
        boolean[] slots = (channel == BoardingChannel.TERMINAL) ? terminalsInUse : rampsInUse;
        for (int i = 0; i < slots.length; i++) {
            if (!slots[i]) {
                slots[i] = true;
                if (channel == BoardingChannel.TERMINAL) activeTerminals++; else activeRamps++;
                return i + 1;
            }
        }
        throw new IllegalStateException("No free slot");
    }
    private void releaseSlot(BoardingChannel channel, int slotId) {
        boolean[] slots = (channel == BoardingChannel.TERMINAL) ? terminalsInUse : rampsInUse;
        slots[slotId - 1] = false;
        if (channel == BoardingChannel.TERMINAL) activeTerminals--; else activeRamps--;
    }
}
