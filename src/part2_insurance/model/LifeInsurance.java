package part2_insurance.model;
public class LifeInsurance extends InsuranceObligation {
    private final int termYears;
    public LifeInsurance(String id, String holderName, double cost, double riskLevel, int termYears) {
        super(id, holderName, cost, riskLevel); this.termYears = termYears;
    }
    @Override public String getType() { return "LIFE"; }
    @Override public String getExtraDetail() { return String.valueOf(termYears); }
}
