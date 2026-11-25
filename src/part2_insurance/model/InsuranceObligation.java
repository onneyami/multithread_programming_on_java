package part2_insurance.model;

public abstract class InsuranceObligation {
    private final String id, holderName;
    private double cost, riskLevel;

    protected InsuranceObligation(String id, String holderName, double cost, double riskLevel) {
        this.id = id; this.holderName = holderName; this.cost = cost; this.riskLevel = riskLevel;
    }
    public abstract String getType();
    public abstract String getExtraDetail();
    public String getId() { return id; }
    public String getHolderName() { return holderName; }
    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }
    public double getRiskLevel() { return riskLevel; }
    public void setRiskLevel(double riskLevel) { this.riskLevel = riskLevel; }

    public String toCsv() {
        return String.join(";", getType(), id, holderName, String.valueOf(cost), String.valueOf(riskLevel), getExtraDetail());
    }

    public static InsuranceObligation fromCsv(String line) {
        String[] parts = line.split(";");
        if (parts.length != 6) throw new IllegalArgumentException("Corrupted CSV line: " + line);
        double cost = Double.parseDouble(parts[3]), risk = Double.parseDouble(parts[4]);
        return switch (parts[0]) {
            case "LIFE" -> new LifeInsurance(parts[1], parts[2], cost, risk, Integer.parseInt(parts[5]));
            case "PROPERTY" -> new PropertyInsurance(parts[1], parts[2], cost, risk, parts[5]);
            case "LIABILITY" -> new LiabilityInsurance(parts[1], parts[2], cost, risk, Double.parseDouble(parts[5]));
            default -> throw new IllegalArgumentException("Unknown insurance type: " + parts[0]);
        };
    }
}
