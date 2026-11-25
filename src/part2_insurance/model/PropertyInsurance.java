package part2_insurance.model;
public class PropertyInsurance extends InsuranceObligation {
    private final String propertyAddress;
    public PropertyInsurance(String id, String holderName, double cost, double riskLevel, String propertyAddress) {
        super(id, holderName, cost, riskLevel); this.propertyAddress = propertyAddress;
    }
    @Override public String getType() { return "PROPERTY"; }
    @Override public String getExtraDetail() { return propertyAddress; }
}
