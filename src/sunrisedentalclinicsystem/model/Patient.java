package sunrisedentalclinicsystem.model;

import java.time.LocalDateTime;

/**
 *
 * @author yasit
 */
public class Patient {

    private int patientId;
    private String firstName;
    private String lastName;
    private String contactNumber;
    private String address;
    private LocalDateTime createdDate;

    public Patient() {
    }

    public Patient(int patientId, String firstName, String lastName, 
                    String contactNumber, String address, LocalDateTime createdDate) {
        this.patientId = patientId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.contactNumber = contactNumber;
        this.address = address;
        this.createdDate = createdDate;
    }

    // Getters

    public int getPatientId() {
        return patientId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public String getAddress() {
        return address;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    // Setters

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public String toString() {
        return "Patient{" +
                "patientId=" + patientId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", contactNumber='" + contactNumber + '\'' +
                ", address='" + address + '\'' +
                ", createdDate=" + createdDate +
                '}';
    }
}
