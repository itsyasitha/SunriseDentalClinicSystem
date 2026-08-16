package sunrisedentalclinicsystem.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 *
 * @author yasit
 */
public class Appointment {

    private int appointmentId;
    private String appointmentNumber;
    private int patientId;
    private int dentistId;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private int treatmentId;
    private String status;
    private String notes;
    private LocalDateTime createdDate;
    
    public Appointment() {
    }

    public Appointment(
            int appointmentId,
            String appointmentNumber,
            int patientId,
            int dentistId,
            LocalDate appointmentDate,
            LocalTime appointmentTime,
            int treatmentId,
            String status,
            String notes,
            LocalDateTime createdDate) {

        this.appointmentId = appointmentId;
        this.appointmentNumber = appointmentNumber;
        this.patientId = patientId;
        this.dentistId = dentistId;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.treatmentId = treatmentId;
        this.status = status;
        this.notes = notes;
        this.createdDate = createdDate;
    }

    // Getters

    public int getAppointmentId() {
        return appointmentId;
    }

    public String getAppointmentNumber() {
        return appointmentNumber;
    }

    public int getPatientId() {
        return patientId;
    }

    public int getDentistId() {
        return dentistId;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public LocalTime getAppointmentTime() {
        return appointmentTime;
    }

    public int getTreatmentId() {
        return treatmentId;
    }

    public String getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    // Setters

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public void setAppointmentNumber(String appointmentNumber) {
        this.appointmentNumber = appointmentNumber;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public void setDentistId(int dentistId) {
        this.dentistId = dentistId;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public void setAppointmentTime(LocalTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public void setTreatmentId(int treatmentId) {
        this.treatmentId = treatmentId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public String toString() {
        return "Appointment{"
                + "appointmentId=" + appointmentId
                + ", appointmentNumber='" + appointmentNumber + '\''
                + ", patientId=" + patientId
                + ", dentistId=" + dentistId
                + ", appointmentDate=" + appointmentDate
                + ", appointmentTime=" + appointmentTime
                + ", treatmentId=" + treatmentId
                + ", status='" + status + '\''
                + ", notes='" + notes + '\''
                + ", createdDate=" + createdDate
                + '}';
    }
}
