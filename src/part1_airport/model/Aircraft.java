package part1_airport.model;
public record Aircraft(int id, String flightCode, int capacity, RangeZone rangeZone, Destination destination) {
    public BoardingChannel requiredChannel() {
        return (rangeZone == RangeZone.LONG_HAUL || capacity > 180) ? BoardingChannel.TERMINAL : BoardingChannel.RAMP;
    }
}
