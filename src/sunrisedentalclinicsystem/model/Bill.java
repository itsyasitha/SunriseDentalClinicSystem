package sunrisedentalclinicsystem.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 *
 * @author yasit
 */
public class Bill {

    private int billId;
    private String billNumber;
    private int appointmentId;
    private int patientId;
    private int treatmentId;
    private double total;
    private String paymentStatus = "Pending";
    private LocalDate paymentDate;
    private LocalDateTime createdDate;
    
    public Bill() {
    }

    public Bill(int billId, String billNumber, int appointmentId, int patientId,
                int treatmentId, double total, LocalDateTime createdDate) {
        this.billId = billId;
        this.billNumber = billNumber;
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.treatmentId = treatmentId;
        this.total = total;
        this.createdDate = createdDate;
    }

    // Getters

    public int getBillId() {
        return billId;
    }

    public String getBillNumber() {
        return billNumber;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public int getPatientId() {
        return patientId;
    }

    public int getTreatmentId() {
        return treatmentId;
    }

    public double getTotal() {
        return total;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    // Setters

    public void setBillId(int billId) {
        this.billId = billId;
    }

    public void setBillNumber(String billNumber) {
        this.billNumber = billNumber;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public void setTreatmentId(int treatmentId) {
        this.treatmentId = treatmentId;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public String toString() {
        return "Bill{" +
                "billId=" + billId +
                ", billNumber='" + billNumber + '\'' +
                ", appointmentId=" + appointmentId +
                ", patientId=" + patientId +
                ", treatmentId=" + treatmentId +
                ", total=" + total +
                ", paymentStatus='" + paymentStatus + '\'' +
                ", paymentDate=" + paymentDate +
                ", createdDate=" + createdDate +
                '}';
    }
}
