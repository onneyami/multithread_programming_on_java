package part2_insurance.model;
public class LiabilityInsurance extends InsuranceObligation {
    private final double coverageLimit;
    public LiabilityInsurance(String id, String holderName, double cost, double riskLevel, double coverageLimit) {
        super(id, holderName, cost, riskLevel); this.coverageLimit = coverageLimit;
    }
    @Override public String getType() { return "LIABILITY"; }
    @Override public String getExtraDetail() { return String.valueOf(coverageLimit); }
}
