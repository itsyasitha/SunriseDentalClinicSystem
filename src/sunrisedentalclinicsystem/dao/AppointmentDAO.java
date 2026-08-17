/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sunrisedentalclinicsystem.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import sunrisedentalclinicsystem.database.DBConnection;
import sunrisedentalclinicsystem.model.Appointment;

/**
 *
 * @author yasit
 */
public class AppointmentDAO {

    // Holds the appointment details shown in the appointment list.
    public static final class AppointmentSummary {

        private final int appointmentId;
        private final int dentistId;
        private final String appointmentNumber;
        private final String patientName;
        private final String dentistName;
        private final String treatmentName;
        private final LocalDate appointmentDate;
        private final LocalTime appointmentTime;
        private final String status;
        private final String notes;

        public AppointmentSummary(
                int appointmentId,
            int dentistId,
                String appointmentNumber,
                String patientName,
                String dentistName,
                String treatmentName,
                LocalDate appointmentDate,
                LocalTime appointmentTime,
                String status,
                String notes) {

            this.appointmentId = appointmentId;
            this.dentistId = dentistId;
            this.appointmentNumber = appointmentNumber;
            this.patientName = patientName;
            this.dentistName = dentistName;
            this.treatmentName = treatmentName;
            this.appointmentDate = appointmentDate;
            this.appointmentTime = appointmentTime;
            this.status = status;
            this.notes = notes;
        }

        public int getAppointmentId() {
            return appointmentId;
        }

        public int getDentistId() {
            return dentistId;
        }

        public String getAppointmentNumber() {
            return appointmentNumber;
        }

        public String getPatientName() {
            return patientName;
        }

        public String getDentistName() {
            return dentistName;
        }

        public String getTreatmentName() {
            return treatmentName;
        }

        public LocalDate getAppointmentDate() {
            return appointmentDate;
        }

        public LocalTime getAppointmentTime() {
            return appointmentTime;
        }

        public String getStatus() {
            return status;
        }

        public String getNotes() {
            return notes;
        }
    }

    // Get all appointments, with the latest appointments first.
    public List<Appointment> getAllAppointments() {

        String sql = "SELECT appointment_id, appointment_number, patient_id, dentist_id, "
                + "appointment_date, appointment_time, treatment_id, status, notes, created_date "
                + "FROM appointments "
                + "ORDER BY appointment_date DESC, appointment_time DESC";

        List<Appointment> appointments = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet rs = preparedStatement.executeQuery()) {

            while (rs.next()) {
                appointments.add(mapAppointment(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to retrieve appointments from the database.", e);
        }

        return appointments;
    }

    // Find an appointment using its ID.
    public Appointment findAppointment(int appointmentId) {

        String sql = "SELECT appointment_id, appointment_number, patient_id, dentist_id, "
                + "appointment_date, appointment_time, treatment_id, status, notes, created_date "
                + "FROM appointments "
                + "WHERE appointment_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, appointmentId);

            try (ResultSet rs = preparedStatement.executeQuery()) {

                if (rs.next()) {
                    return mapAppointment(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to find appointment in the database.", e);
        }

        return null;
    }

    // Search appointments using the entered keyword.
    public List<Appointment> searchAppointments(String keyword) {

        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllAppointments();
        }

        String sql = "SELECT a.appointment_id, a.appointment_number, a.patient_id, a.dentist_id, "
                + "a.appointment_date, a.appointment_time, a.treatment_id, a.status, a.notes, a.created_date "
                + "FROM appointments a "
                + "INNER JOIN patients p ON a.patient_id = p.patient_id "
                + "INNER JOIN dentists d ON a.dentist_id = d.dentist_id "
                + "INNER JOIN users u ON d.user_id = u.user_id "
                + "LEFT JOIN treatments t ON a.treatment_id = t.treatment_id "
                + "WHERE a.appointment_number LIKE ? "
                + "OR CAST(a.appointment_id AS CHAR) LIKE ? "
                + "OR CONCAT_WS(' ', p.first_name, p.last_name) LIKE ? "
                + "OR u.full_name LIKE ? "
                + "OR COALESCE(t.treatment_name, '') LIKE ? "
                + "OR DATE_FORMAT(a.appointment_date, '%d/%m/%Y') LIKE ? "
                + "OR CAST(a.appointment_date AS CHAR) LIKE ? "
                + "OR DATE_FORMAT(a.appointment_time, '%h:%i %p') LIKE ? "
                + "OR CAST(a.appointment_time AS CHAR) LIKE ? "
                + "OR a.status LIKE ? "
                + "OR COALESCE(a.notes, '') LIKE ? "
                + "ORDER BY a.appointment_date DESC, a.appointment_time DESC";

        List<Appointment> appointments = new ArrayList<>();

        String likeKeyword = "%" + keyword.trim() + "%";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            for (int i = 1; i <= 11; i++) {
                preparedStatement.setString(i, likeKeyword);
            }

            try (ResultSet rs = preparedStatement.executeQuery()) {

                while (rs.next()) {
                    appointments.add(mapAppointment(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to search appointments in the database.", e);
        }

        return appointments;
    }

    // Get appointment details with patient, dentist and treatment names.
    public List<AppointmentSummary> getAllAppointmentSummaries() {
        return queryAppointmentSummaries(null);
    }

    // Search appointment details using the entered keyword.
    public List<AppointmentSummary> searchAppointmentSummaries(String keyword) {
        return queryAppointmentSummaries(keyword);
    }

    // Get the dentist's booked time slots for the selected date.
    public Set<LocalTime> getBookedDentistSlots(int dentistId, LocalDate appointmentDate, int appointmentId) {

        String sql = "SELECT appointment_time "
                + "FROM appointments "
                + "WHERE dentist_id = ? "
                + "AND appointment_date = ? "
                + "AND status != 'Cancelled' "
                + "AND appointment_id != ?";

        Set<LocalTime> bookedSlots = new LinkedHashSet<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, dentistId);
            preparedStatement.setDate(2, Date.valueOf(appointmentDate));
            preparedStatement.setInt(3, appointmentId);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    Time time = rs.getTime("appointment_time");
                    if (time != null) {
                        bookedSlots.add(time.toLocalTime());
                    }
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to retrieve booked dentist slots.", e);
        }

        return bookedSlots;
    }

    // Generate the next appointment number for the current year.
    public String generateAppointmentNumber() {

        String sql = "SELECT COUNT(*) AS yearly_count "
                + "FROM appointments "
                + "WHERE YEAR(created_date) = YEAR(CURRENT_DATE)";

        int sequence = 1;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet rs = preparedStatement.executeQuery()) {

            if (rs.next()) {
                sequence = rs.getInt("yearly_count") + 1;
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to generate appointment number.", e);
        }

        int year = LocalDate.now().getYear();
        return String.format("APT-%d-%04d", year, sequence);
    }

    private List<AppointmentSummary> queryAppointmentSummaries(String keyword) {

        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT a.appointment_id, a.dentist_id, a.appointment_number, ")
                .append("CONCAT_WS(' ', p.first_name, p.last_name) AS patient_name, ")
                .append("u.full_name AS dentist_name, ")
                .append("COALESCE(t.treatment_name, 'Not Assigned') AS treatment_name, ")
                .append("a.appointment_date, a.appointment_time, a.status, a.notes ")
                .append("FROM appointments a ")
                .append("INNER JOIN patients p ON a.patient_id = p.patient_id ")
                .append("INNER JOIN dentists d ON a.dentist_id = d.dentist_id ")
                .append("INNER JOIN users u ON d.user_id = u.user_id ")
                .append("LEFT JOIN treatments t ON a.treatment_id = t.treatment_id ");

        if (hasKeyword) {
            sql.append("WHERE a.appointment_number LIKE ? ")
                    .append("OR CAST(a.appointment_id AS CHAR) LIKE ? ")
                    .append("OR CONCAT_WS(' ', p.first_name, p.last_name) LIKE ? ")
                    .append("OR u.full_name LIKE ? ")
                    .append("OR COALESCE(t.treatment_name, '') LIKE ? ")
                    .append("OR DATE_FORMAT(a.appointment_date, '%d/%m/%Y') LIKE ? ")
                    .append("OR CAST(a.appointment_date AS CHAR) LIKE ? ")
                    .append("OR DATE_FORMAT(a.appointment_time, '%h:%i %p') LIKE ? ")
                    .append("OR CAST(a.appointment_time AS CHAR) LIKE ? ")
                    .append("OR a.status LIKE ? ")
                    .append("OR COALESCE(a.notes, '') LIKE ? ");
        }

        sql.append("ORDER BY a.appointment_id DESC");

        List<AppointmentSummary> appointments = new ArrayList<>();
        String likeKeyword = "%" + (keyword == null ? "" : keyword.trim()) + "%";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql.toString())) {

            if (hasKeyword) {
                for (int i = 1; i <= 11; i++) {
                    preparedStatement.setString(i, likeKeyword);
                }
            }

            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    appointments.add(mapAppointmentSummary(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to retrieve appointment summaries.", e);
        }

        return appointments;
    }

    private AppointmentSummary mapAppointmentSummary(ResultSet rs) throws SQLException {

        Date appointmentDate = rs.getDate("appointment_date");
        Time appointmentTime = rs.getTime("appointment_time");

        return new AppointmentSummary(
                rs.getInt("appointment_id"),
            rs.getInt("dentist_id"),
                rs.getString("appointment_number"),
                rs.getString("patient_name"),
                rs.getString("dentist_name"),
                rs.getString("treatment_name"),
                appointmentDate == null ? null : appointmentDate.toLocalDate(),
                appointmentTime == null ? null : appointmentTime.toLocalTime(),
                rs.getString("status"),
                rs.getString("notes"));
    }

    // Get all appointments for a specific patient.
    public List<Appointment> getAppointmentsByPatient(int patientId) {

        String sql = "SELECT appointment_id, appointment_number, patient_id, dentist_id, "
                + "appointment_date, appointment_time, treatment_id, status, notes, created_date "
                + "FROM appointments "
                + "WHERE patient_id = ? "
                + "ORDER BY appointment_date DESC, appointment_time DESC";

        List<Appointment> appointments = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, patientId);

            try (ResultSet rs = preparedStatement.executeQuery()) {

                while (rs.next()) {
                    appointments.add(mapAppointment(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to retrieve patient appointments.", e);
        }

        return appointments;
    }

    // Get all appointments for a specific dentist.
    public List<Appointment> getAppointmentsByDentist(int dentistId) {

        String sql = "SELECT appointment_id, appointment_number, patient_id, dentist_id, "
                + "appointment_date, appointment_time, treatment_id, status, notes, created_date "
                + "FROM appointments "
                + "WHERE dentist_id = ? "
                + "ORDER BY appointment_date DESC, appointment_time DESC";

        List<Appointment> appointments = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, dentistId);

            try (ResultSet rs = preparedStatement.executeQuery()) {

                while (rs.next()) {
                    appointments.add(mapAppointment(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to retrieve dentist appointments.", e);
        }

        return appointments;
    }

    // Get appointments scheduled for the selected date.
    public List<Appointment> getAppointmentsByDate(LocalDate appointmentDate) {

        String sql = "SELECT appointment_id, appointment_number, patient_id, dentist_id, "
                + "appointment_date, appointment_time, treatment_id, status, notes, created_date "
                + "FROM appointments "
                + "WHERE appointment_date = ? "
                + "ORDER BY appointment_time ASC";

        List<Appointment> appointments = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setDate(1, Date.valueOf(appointmentDate));

            try (ResultSet rs = preparedStatement.executeQuery()) {

                while (rs.next()) {
                    appointments.add(mapAppointment(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to retrieve appointments for the selected date.", e);
        }

        return appointments;
    }

    // Check if the dentist is already booked at this date and time.
    public boolean hasDentistConflict(
            int dentistId,
            LocalDate appointmentDate,
            LocalTime appointmentTime,
            int appointmentId) {

        String sql = "SELECT COUNT(*) AS conflict_count "
                + "FROM appointments "
                + "WHERE dentist_id = ? "
                + "AND appointment_date = ? "
                + "AND appointment_time = ? "
                + "AND status != 'Cancelled' "
                + "AND appointment_id != ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, dentistId);
            preparedStatement.setDate(2, Date.valueOf(appointmentDate));
            preparedStatement.setTime(3, Time.valueOf(appointmentTime));
            preparedStatement.setInt(4, appointmentId);

            try (ResultSet rs = preparedStatement.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt("conflict_count") > 0;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to check dentist appointment availability.", e);
        }

        return false;
    }

    // Check if the patient already has an appointment at this date and time.
    public boolean hasPatientConflict(
            int patientId,
            LocalDate appointmentDate,
            LocalTime appointmentTime,
            int appointmentId) {

        String sql = "SELECT COUNT(*) AS conflict_count "
                + "FROM appointments "
                + "WHERE patient_id = ? "
                + "AND appointment_date = ? "
                + "AND appointment_time = ? "
                + "AND status != 'Cancelled' "
                + "AND appointment_id != ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, patientId);
            preparedStatement.setDate(2, Date.valueOf(appointmentDate));
            preparedStatement.setTime(3, Time.valueOf(appointmentTime));
            preparedStatement.setInt(4, appointmentId);

            try (ResultSet rs = preparedStatement.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt("conflict_count") > 0;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to check patient appointment availability.", e);
        }

        return false;
    }

    // Add a new appointment to the database.
    public boolean addAppointment(Appointment appointment) {

        String sql = "INSERT INTO appointments "
                + "(appointment_number, patient_id, dentist_id, appointment_date, "
                + "appointment_time, treatment_id, status, notes) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(
                    1, appointment.getAppointmentNumber());

            preparedStatement.setInt(
                    2, appointment.getPatientId());

            preparedStatement.setInt(
                    3, appointment.getDentistId());

            preparedStatement.setDate(
                    4, Date.valueOf(appointment.getAppointmentDate()));

            preparedStatement.setTime(
                    5, Time.valueOf(appointment.getAppointmentTime()));

            if (appointment.getTreatmentId() > 0) {
                preparedStatement.setInt(
                        6, appointment.getTreatmentId());
            } else {
                preparedStatement.setNull(
                        6, java.sql.Types.INTEGER);
            }

            preparedStatement.setString(
                    7, appointment.getStatus());

            preparedStatement.setString(
                    8, appointment.getNotes());

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows > 0) {

                try (ResultSet generatedKeys =
                        preparedStatement.getGeneratedKeys()) {

                    if (generatedKeys.next()) {
                        appointment.setAppointmentId(
                                generatedKeys.getInt(1));
                    }
                }

                return true;
            }

            return false;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to add appointment to the database.", e);
        }
    }

    // Update an existing appointment.
    public boolean updateAppointment(Appointment appointment) {

        String sql = "UPDATE appointments SET "
                + "patient_id = ?, "
                + "dentist_id = ?, "
                + "appointment_date = ?, "
                + "appointment_time = ?, "
                + "treatment_id = ?, "
                + "status = ?, "
                + "notes = ? "
                + "WHERE appointment_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(sql)) {

            preparedStatement.setInt(
                    1, appointment.getPatientId());

            preparedStatement.setInt(
                    2, appointment.getDentistId());

            preparedStatement.setDate(
                    3, Date.valueOf(appointment.getAppointmentDate()));

            preparedStatement.setTime(
                    4, Time.valueOf(appointment.getAppointmentTime()));

            if (appointment.getTreatmentId() > 0) {
                preparedStatement.setInt(
                        5, appointment.getTreatmentId());
            } else {
                preparedStatement.setNull(
                        5, java.sql.Types.INTEGER);
            }

            preparedStatement.setString(
                    6, appointment.getStatus());

            preparedStatement.setString(
                    7, appointment.getNotes());

            preparedStatement.setInt(
                    8, appointment.getAppointmentId());

            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to update appointment in the database.", e);
        }
    }

    // Delete an appointment using its ID.
    public boolean deleteAppointment(int appointmentId) {

        String sql = "DELETE FROM appointments WHERE appointment_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, appointmentId);

            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to delete appointment from the database.", e);
        }
    }

    // Get the total number of appointments.
    public int countAppointments() {

        String sql = "SELECT COUNT(*) AS total_appointments "
                + "FROM appointments";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(sql);
             ResultSet rs = preparedStatement.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("total_appointments");
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to count appointments in the database.", e);
        }

        return 0;
    }

    // Get the number of appointments scheduled for today.
    public int countTodayAppointments() {

        String sql = "SELECT COUNT(*) AS total_appointments "
                + "FROM appointments "
                + "WHERE appointment_date = CURRENT_DATE "
                + "AND status != 'Cancelled'";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(sql);
             ResultSet rs = preparedStatement.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("total_appointments");
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to count today's appointments.", e);
        }

        return 0;
    }

    // Convert a database row into an Appointment object.
    private Appointment mapAppointment(ResultSet rs) throws SQLException {

        Appointment appointment = new Appointment();

        appointment.setAppointmentId(
                rs.getInt("appointment_id"));

        appointment.setAppointmentNumber(
                rs.getString("appointment_number"));

        appointment.setPatientId(
                rs.getInt("patient_id"));

        appointment.setDentistId(
                rs.getInt("dentist_id"));

        Date appointmentDate =
                rs.getDate("appointment_date");

        if (appointmentDate != null) {
            appointment.setAppointmentDate(
                    appointmentDate.toLocalDate());
        }

        Time appointmentTime =
                rs.getTime("appointment_time");

        if (appointmentTime != null) {
            appointment.setAppointmentTime(
                    appointmentTime.toLocalTime());
        }

        int treatmentId =
                rs.getInt("treatment_id");

        if (!rs.wasNull()) {
            appointment.setTreatmentId(treatmentId);
        }

        appointment.setStatus(
                rs.getString("status"));

        appointment.setNotes(
                rs.getString("notes"));

        Timestamp createdDate =
                rs.getTimestamp("created_date");

        if (createdDate != null) {
            appointment.setCreatedDate(
                    createdDate.toLocalDateTime());
        }

        return appointment;
    }
}
