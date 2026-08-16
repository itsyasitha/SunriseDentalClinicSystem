package sunrisedentalclinicsystem.model;

/**
 *
 * @author yasit
 */
public class Treatment {

    private int treatmentId;
    private String treatmentName;
    private double cost;

    public Treatment() {
    }

    public Treatment(int treatmentId, String treatmentName, double cost) {
        this.treatmentId = treatmentId;
        this.treatmentName = treatmentName;
        this.cost = cost;
    }

    // Getters

    public int getTreatmentId() {
        return treatmentId;
    }

    public String getTreatmentName() {
        return treatmentName;
    }

    public double getCost() {
        return cost;
    }

    // Setters

    public void setTreatmentId(int treatmentId) {
        this.treatmentId = treatmentId;
    }

    public void setTreatmentName(String treatmentName) {
        this.treatmentName = treatmentName;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    @Override
    public String toString() {
        return "Treatment{" +
                "treatmentId=" + treatmentId +
                ", treatmentName='" + treatmentName + '\'' +
                ", cost=" + cost +
                '}';
    }
}
