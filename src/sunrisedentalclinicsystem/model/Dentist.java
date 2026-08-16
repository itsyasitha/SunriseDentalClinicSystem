package sunrisedentalclinicsystem.model;

/**
 *
 * @author yasit
 */
public class Dentist {

    private int dentistId;
    private int userId;
    private String specialization;

    // Joined information from users table
    private String fullName;
    
    public Dentist() {
    }
    
    public Dentist(int dentistId, int userId, String specialization) {
        this.dentistId = dentistId;
        this.userId = userId;
        this.specialization = specialization;
    }

    // Getters

    public int getDentistId() {
        return dentistId;
    }

    public int getUserId() {
        return userId;
    }

    public String getSpecialization() {
        return specialization;
    }

    public String getFullName() {
        return fullName;
    }

    // Setters

    public void setDentistId(int dentistId) {
        this.dentistId = dentistId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @Override
    public String toString() {
        return "Dentist{" +
                "dentistId=" + dentistId +
                ", userId=" + userId +
                ", specialization='" + specialization + '\'' +
                ", fullName='" + fullName + '\'' +
                '}';
    }
}
