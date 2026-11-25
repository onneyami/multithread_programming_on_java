package part1_airport.strategy;

import part1_airport.audit.SimulationStats;
import part1_airport.model.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrentAirportStrategy implements AirportStrategy {
    private final int maxTerminals, maxRamps;
    private final Semaphore terminalSemaphore, rampSemaphore;
    private final BlockingQueue<Integer> availableTerminalIds, availableRampIds;
    private final ReentrantLock stateLock = new ReentrantLock();

    public ConcurrentAirportStrategy(int maxTerminals, int maxRamps) {
        this.maxTerminals = maxTerminals;
        this.maxRamps = maxRamps;
        this.terminalSemaphore = new Semaphore(maxTerminals, true);
        this.rampSemaphore = new Semaphore(maxRamps, true);
        this.availableTerminalIds = new ArrayBlockingQueue<>(maxTerminals);
        this.availableRampIds = new ArrayBlockingQueue<>(maxRamps);
        for (int i = 1; i <= maxTerminals; i++) availableTerminalIds.add(i);
        for (int i = 1; i <= maxRamps; i++) availableRampIds.add(i);
    }

    @Override public String getName() { return "Concurrent (java.util.concurrent)"; }
    @Override public int getMaxTerminals() { return maxTerminals; }
    @Override public int getMaxRamps() { return maxRamps; }

    @Override
    public boolean processAircraft(Aircraft aircraft, long timeoutMs, SimulationStats stats) throws InterruptedException {
        BoardingChannel channel = aircraft.requiredChannel();
        Semaphore sem = (channel == BoardingChannel.TERMINAL) ? terminalSemaphore : rampSemaphore;
        BlockingQueue<Integer> idQueue = (channel == BoardingChannel.TERMINAL) ? availableTerminalIds : availableRampIds;

        if (!sem.tryAcquire(timeoutMs, TimeUnit.MILLISECONDS)) {
            stats.rejectedCount.increment();
            return false;
        }

        int slotId;
        stateLock.lock();
        try {
            slotId = idQueue.take();
            int activeT = maxTerminals - availableTerminalIds.size();
            int activeR = maxRamps - availableRampIds.size();
            stats.currentTerminalsInUse.set(activeT);
            stats.currentRampsInUse.set(activeR);
            stats.eventLog.add(new EventRecord(System.nanoTime(), aircraft.flightCode(), channel, slotId, "ACQUIRE", activeT, activeR));
        } finally {
            stateLock.unlock();
        }

        try {
            Thread.sleep(25);
        } finally {
            stateLock.lock();
            try {
                idQueue.put(slotId);
                int activeT = maxTerminals - availableTerminalIds.size();
                int activeR = maxRamps - availableRampIds.size();
                stats.currentTerminalsInUse.set(activeT);
                stats.currentRampsInUse.set(activeR);
                stats.eventLog.add(new EventRecord(System.nanoTime(), aircraft.flightCode(), channel, slotId, "RELEASE", activeT, activeR));
            } finally {
                stateLock.unlock();
                sem.release();
            }
        }

        stats.servedCount.increment();
        stats.totalPassengersBoarded.add(aircraft.capacity());
        return true;
    }
}