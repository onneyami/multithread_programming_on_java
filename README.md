
# Laboratory Work №4: Multithreaded Programming in Java (Variant 11)

A Java SE multithreading project exploring JVM monitors, `java.util.concurrent` synchronizers, lock-free atomic counters, Virtual Threads, and the Readers–Writers file synchronization pattern—built strictly with the Java SE standard library.

## Project Structure

```text
src/
├── part1_airport/
│   ├── model/        # Aircraft, Destination, RangeZone, BoardingChannel, EventRecord
│   ├── strategy/     # AirportStrategy + Unsafe, Classic (synchronized/wait), Concurrent
│   ├── audit/        # SimulationStats (LongAdder/AtomicInteger) & InvariantAuditor
│   └── AirportLabRunner.java
└── part2_insurance/
    ├── model/        # InsuranceObligation hierarchy (Life, Property, Liability)
    ├── storage/      # DerivativeStorage + UnsafeFile, SynchronizedFile, ReadWriteLockFile
    └── InsuranceLabRunner.java
```

## Part 1: Airport Boarding Simulation (Variant 11)

* **Domain:** Aircraft of varying passenger capacities and range zones (`SHORT_HAUL`,`MEDIUM_HAUL`,`LONG_HAUL`) arrive at an airport with finite **Terminals** and  **Boarding Ramps** .
* **Strategies:**
  1. `UnsafeAirportStrategy`: Unsynchronized `ArrayList` access demonstrating check-then-act race conditions and double-occupancy invariant violations.
  2. `ClassicAirportStrategy`: Pure JVM monitors (`synchronized`,`wait()`,`notifyAll()`).
  3. `ConcurrentAirportStrategy`: Fair `Semaphore` admission control,`ArrayBlockingQueue` slot tracking, and `ReentrantLock` event logging.
* **Observability & Validation:** Live console dashboard updated every 250 ms via `ScheduledExecutorService`, post-run `InvariantAuditor`, and a 1,500-aircraft benchmark comparing Platform Threads vs. Java 21+ Virtual Threads.

## Part 2: Synchronized File Storage — Insurance Derivative (Variant 11)

* **Domain:** Polymorphic `InsuranceObligation` hierarchy serialized manually to CSV using `java.nio.file` atomic temporary file replacement (`Files.move`).
* **Readers–Writers Benchmark:** 3 writer threads (adding/updating policies and sorting by descending risk level) + 6 reader threads (calculating total derivative value and filtering by risk range with simulated I/O latency), proving ~5x read speedup with `ReentrantReadWriteLock` over method-level `synchronized`.

```Shell
# Compile all sources
javac -d out $(find src -name "*.java")

# Run Part 1 (Airport Simulation + Virtual Threads Benchmark)
java -cp out part1_airport.AirportLabRunner

# Run Part 2 (Insurance Derivative Readers-Writers Benchmark)
java -cp out part2_insurance.InsuranceLabRunner
```
