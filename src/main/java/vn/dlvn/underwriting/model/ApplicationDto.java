package vn.dlvn.underwriting.model;

public class ApplicationDto {
    private String applicantName;
    private int age;
    private String disease;
    private String policyType;
    private double coverageAmount;
    
    public ApplicationDto() {
    }
    
    public ApplicationDto(String applicantName, int age, String disease, String policyType, double coverageAmount) {
        this.applicantName = applicantName;
        this.age = age;
        this.disease = disease;
        this.policyType = policyType;
        this.coverageAmount = coverageAmount;
    }
    
    // Getters and Setters
    public String getApplicantName() {
        return applicantName;
    }
    
    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }
    
    public int getAge() {
        return age;
    }
    
    public void setAge(int age) {
        this.age = age;
    }
    
    public String getDisease() {
        return disease;
    }
    
    public void setDisease(String disease) {
        this.disease = disease;
    }
    
    public String getPolicyType() {
        return policyType;
    }
    
    public void setPolicyType(String policyType) {
        this.policyType = policyType;
    }
    
    public double getCoverageAmount() {
        return coverageAmount;
    }
    
    public void setCoverageAmount(double coverageAmount) {
        this.coverageAmount = coverageAmount;
    }
    
    @Override
    public String toString() {
        return "ApplicationDto{" +
                "applicantName='" + applicantName + '\'' +
                ", age=" + age +
                ", disease='" + disease + '\'' +
                ", policyType='" + policyType + '\'' +
                ", coverageAmount=" + coverageAmount +
                '}';
    }
}
