package sunrisedentalclinicsystem.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import sunrisedentalclinicsystem.database.DBConnection;
import sunrisedentalclinicsystem.model.Patient;

/**
 *
 * @author yasit
 */
public class PatientDAO {

    // Get all patients, with the newest registrations first.
    public List<Patient> getAllPatients() {
        String sql = "SELECT patient_id, first_name, last_name, contact_number, address, created_date "
                + "FROM patients ORDER BY patient_id DESC";

        List<Patient> patients = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet rs = preparedStatement.executeQuery()) {

            while (rs.next()) {
                patients.add(mapPatient(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve patients from the database.", e);
        }

        return patients;
    }

    // Find a patient using their ID.
    public Patient findPatient(int patientId) {
        String sql = "SELECT patient_id, first_name, last_name, contact_number, address, created_date "
                + "FROM patients WHERE patient_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, patientId);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    return mapPatient(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find patient in the database.", e);
        }

        return null;
    }

    // Search patients by name or contact number.
    public List<Patient> searchPatients(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllPatients();
        }

        String sql = "SELECT patient_id, first_name, last_name, contact_number, address, created_date "
                + "FROM patients "
                + "WHERE first_name LIKE ? OR last_name LIKE ? OR contact_number LIKE ? "
                + "ORDER BY patient_id DESC";

        List<Patient> patients = new ArrayList<>();
        String likeKeyword = "%" + keyword.trim() + "%";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, likeKeyword);
            preparedStatement.setString(2, likeKeyword);
            preparedStatement.setString(3, likeKeyword);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    patients.add(mapPatient(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to search patients in the database.", e);
        }

        return patients;
    }

    // Add a new patient to the database.
    public boolean addPatient(Patient patient) {
        String sql = "INSERT INTO patients (first_name, last_name, contact_number, address) VALUES (?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, patient.getFirstName());
            preparedStatement.setString(2, patient.getLastName());
            preparedStatement.setString(3, patient.getContactNumber());
            preparedStatement.setString(4, patient.getAddress());

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        patient.setPatientId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }

            return false;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to add patient to the database.", e);
        }
    }

    // Update an existing patient's details.
    public boolean updatePatient(Patient patient) {
        String sql = "UPDATE patients SET first_name = ?, last_name = ?, contact_number = ?, address = ? "
                + "WHERE patient_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, patient.getFirstName());
            preparedStatement.setString(2, patient.getLastName());
            preparedStatement.setString(3, patient.getContactNumber());
            preparedStatement.setString(4, patient.getAddress());
            preparedStatement.setInt(5, patient.getPatientId());

            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update patient in the database.", e);
        }
    }

    // Delete a patient using their ID.
    public boolean deletePatient(int patientId) {
        String sql = "DELETE FROM patients WHERE patient_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, patientId);
            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete patient from the database.", e);
        }
    }

    // Get the total number of patients.
    public int countPatients() {
        String sql = "SELECT COUNT(*) AS total_patients FROM patients";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet rs = preparedStatement.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("total_patients");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to count patients in the database.", e);
        }

        return 0;
    }

    private Patient mapPatient(ResultSet rs) throws SQLException {
        Patient patient = new Patient();
        patient.setPatientId(rs.getInt("patient_id"));
        patient.setFirstName(rs.getString("first_name"));
        patient.setLastName(rs.getString("last_name"));
        patient.setContactNumber(rs.getString("contact_number"));
        patient.setAddress(rs.getString("address"));

        Timestamp createdDate = rs.getTimestamp("created_date");
        if (createdDate != null) {
            patient.setCreatedDate(createdDate.toLocalDateTime());
        }

        return patient;
    }
}
