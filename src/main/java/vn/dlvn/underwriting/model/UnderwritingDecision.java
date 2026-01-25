package vn.dlvn.underwriting.model;

public enum UnderwritingDecision {
    AUTO_APPROVE("AUTO_APPROVE"),
    AUTO_REJECT("AUTO_REJECT"),
    MANUAL_REVIEW("MANUAL_REVIEW"),
    REQUEST_DOC("REQUEST_DOC");
    
    private final String value;
    
    UnderwritingDecision(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static UnderwritingDecision fromValue(String value) {
        for (UnderwritingDecision decision : UnderwritingDecision.values()) {
            if (decision.value.equals(value)) {
                return decision;
            }
        }
        throw new IllegalArgumentException("Unknown decision: " + value);
    }
}
