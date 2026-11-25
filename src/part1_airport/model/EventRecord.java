package part1_airport.model;
public record EventRecord(long timestampNano, String flightCode, BoardingChannel channel, int slotId, String action, int activeTerminalsSnapshot, int activeRampsSnapshot) {}
