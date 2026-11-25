package part1_airport.strategy;
import part1_airport.audit.SimulationStats;
import part1_airport.model.Aircraft;

public interface AirportStrategy {
    String getName();
    boolean processAircraft(Aircraft aircraft, long timeoutMs, SimulationStats stats) throws InterruptedException;
    int getMaxTerminals();
    int getMaxRamps();
}
