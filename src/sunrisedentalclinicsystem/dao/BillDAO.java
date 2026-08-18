/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sunrisedentalclinicsystem.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import sunrisedentalclinicsystem.database.DBConnection;
import sunrisedentalclinicsystem.model.Bill;
import sunrisedentalclinicsystem.model.Treatment;
import sunrisedentalclinicsystem.util.AppConstants;

/**
 *
 * @author yasit
 */
public class BillDAO {

    // Get all bills, with the latest bills first.
    public List<Bill> getAllBills() {

        String sql = "SELECT bill_id, bill_number, appointment_id, patient_id, "
            + "treatment_id, total, payment_status, payment_date, created_date "
                + "FROM bills "
                + "ORDER BY bill_id DESC";

        List<Bill> bills = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet rs = preparedStatement.executeQuery()) {

            while (rs.next()) {
                bills.add(mapBill(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to retrieve bills from the database.", e);
        }

        return bills;
    }

    // Find a bill using its ID.
    public Bill findBill(int billId) {

        String sql = "SELECT bill_id, bill_number, appointment_id, patient_id, "
            + "treatment_id, total, payment_status, payment_date, created_date "
                + "FROM bills "
                + "WHERE bill_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, billId);

            try (ResultSet rs = preparedStatement.executeQuery()) {

                if (rs.next()) {
                    return mapBill(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to find bill in the database.", e);
        }

        return null;
    }

    // Find a bill using its bill number.
    public Bill findBillByNumber(String billNumber) {

        String sql = "SELECT bill_id, bill_number, appointment_id, patient_id, "
            + "treatment_id, total, payment_status, payment_date, created_date "
                + "FROM bills "
                + "WHERE bill_number = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, billNumber);

            try (ResultSet rs = preparedStatement.executeQuery()) {

                if (rs.next()) {
                    return mapBill(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to find bill by number in the database.", e);
        }

        return null;
    }

    // Search bills by bill number, patient name or bill ID.
    public List<Bill> searchBills(String keyword) {

        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllBills();
        }

        String sql = "SELECT b.bill_id, b.bill_number, b.appointment_id, b.patient_id, "
            + "b.treatment_id, b.total, b.payment_status, b.payment_date, b.created_date "
                + "FROM bills b "
                + "INNER JOIN patients p ON b.patient_id = p.patient_id "
                + "WHERE b.bill_number LIKE ? "
                + "OR CONCAT_WS(' ', p.first_name, p.last_name) LIKE ? "
                + "OR CAST(b.bill_id AS CHAR) LIKE ? "
                + "ORDER BY b.bill_id DESC";

        List<Bill> bills = new ArrayList<>();
        String likeKeyword = "%" + keyword.trim() + "%";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, likeKeyword);
            preparedStatement.setString(2, likeKeyword);
            preparedStatement.setString(3, likeKeyword);

            try (ResultSet rs = preparedStatement.executeQuery()) {

                while (rs.next()) {
                    bills.add(mapBill(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to search bills in the database.", e);
        }

        return bills;
    }

    // Get all bills for a specific patient.
    public List<Bill> getBillsByPatient(int patientId) {

        String sql = "SELECT bill_id, bill_number, appointment_id, patient_id, "
            + "treatment_id, total, payment_status, payment_date, created_date "
                + "FROM bills "
                + "WHERE patient_id = ? "
                + "ORDER BY bill_id DESC";

        List<Bill> bills = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, patientId);

            try (ResultSet rs = preparedStatement.executeQuery()) {

                while (rs.next()) {
                    bills.add(mapBill(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to retrieve bills for patient in the database.", e);
        }

        return bills;
    }

    // Get all bills for a specific appointment.
    public List<Bill> getBillsByAppointment(int appointmentId) {

        String sql = "SELECT bill_id, bill_number, appointment_id, patient_id, "
            + "treatment_id, total, payment_status, payment_date, created_date "
                + "FROM bills "
                + "WHERE appointment_id = ? "
                + "ORDER BY bill_id DESC";

        List<Bill> bills = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, appointmentId);

            try (ResultSet rs = preparedStatement.executeQuery()) {

                while (rs.next()) {
                    bills.add(mapBill(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to retrieve bills for appointment in the database.", e);
        }

        return bills;
    }

    // Calculate the total using the treatment cost and consultation fee.
    public double calculateBillTotal(Treatment treatment) {

        if (treatment == null) {
            throw new IllegalArgumentException("Treatment cannot be null");
        }

        BigDecimal treatmentCost = BigDecimal.valueOf(treatment.getCost());
        BigDecimal total = treatmentCost.add(AppConstants.CONSULTATION_FEE);

        return total.doubleValue();
    }

    // Add a new bill to the database.
    public boolean addBill(Bill bill) {

        String sql = "INSERT INTO bills "
            + "(bill_number, appointment_id, patient_id, treatment_id, total, payment_status, payment_date) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, bill.getBillNumber());
            preparedStatement.setInt(2, bill.getAppointmentId());
            preparedStatement.setInt(3, bill.getPatientId());
            preparedStatement.setInt(4, bill.getTreatmentId());
            preparedStatement.setDouble(5, bill.getTotal());
            String paymentStatus = bill.getPaymentStatus();
            if (paymentStatus == null || paymentStatus.isBlank()) {
                paymentStatus = "Pending";
            }
            preparedStatement.setString(6, paymentStatus);
            if (bill.getPaymentDate() == null) {
                preparedStatement.setNull(7, Types.DATE);
            } else {
                preparedStatement.setDate(7, Date.valueOf(bill.getPaymentDate()));
            }

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        bill.setBillId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }

            return false;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to add bill to the database.", e);
        }
    }

    // Update an existing bill.
    public boolean updateBill(Bill bill) {

        String sql = "UPDATE bills SET total = ?, payment_status = ?, payment_date = ? "
            + "WHERE bill_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setDouble(1, bill.getTotal());
            String paymentStatus = bill.getPaymentStatus();
            if (paymentStatus == null || paymentStatus.isBlank()) {
                paymentStatus = "Pending";
            }
            preparedStatement.setString(2, paymentStatus);
            if (bill.getPaymentDate() == null) {
                preparedStatement.setNull(3, Types.DATE);
            } else {
                preparedStatement.setDate(3, Date.valueOf(bill.getPaymentDate()));
            }
            preparedStatement.setInt(4, bill.getBillId());

            int affectedRows = preparedStatement.executeUpdate();

            return affectedRows > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to update bill in the database.", e);
        }
    }

    // Delete a bill using its ID.
    public boolean deleteBill(int billId) {

        String sql = "DELETE FROM bills WHERE bill_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, billId);

            int affectedRows = preparedStatement.executeUpdate();

            return affectedRows > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to delete bill from the database.", e);
        }
    }

    // Generate the next bill number for the current year.
    public String generateBillNumber() {

        String sql = "SELECT COUNT(*) AS yearly_count "
                + "FROM bills "
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
                    "Failed to generate bill number.", e);
        }

        int year = java.time.LocalDate.now().getYear();
        return String.format("BILL-%d-%04d", year, sequence);
    }

    // Convert a database row into a Bill object.
    private Bill mapBill(ResultSet rs) throws SQLException {

        Bill bill = new Bill();

        bill.setBillId(rs.getInt("bill_id"));
        bill.setBillNumber(rs.getString("bill_number"));
        bill.setAppointmentId(rs.getInt("appointment_id"));
        bill.setPatientId(rs.getInt("patient_id"));
        bill.setTreatmentId(rs.getInt("treatment_id"));
        bill.setTotal(rs.getDouble("total"));
        bill.setPaymentStatus(rs.getString("payment_status"));

        Date paymentDate = rs.getDate("payment_date");
        if (paymentDate != null) {
            bill.setPaymentDate(paymentDate.toLocalDate());
        } else {
            bill.setPaymentDate(null);
        }

        Timestamp createdDateTimestamp = rs.getTimestamp("created_date");
        if (createdDateTimestamp != null) {
            bill.setCreatedDate(createdDateTimestamp.toLocalDateTime());
        }

        return bill;
    }
}
