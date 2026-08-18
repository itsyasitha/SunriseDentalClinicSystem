/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sunrisedentalclinicsystem.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import sunrisedentalclinicsystem.database.DBConnection;
import sunrisedentalclinicsystem.model.Treatment;

/**
 *
 * @author yasit
 */
public class TreatmentDAO {
  
    // Get all treatments, with the newest entries first.
    public List<Treatment> getAllTreatments() {

        String sql = "SELECT treatment_id, treatment_name, cost "
                + "FROM treatments "
                + "ORDER BY treatment_id DESC";

        List<Treatment> treatments = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(sql);
             ResultSet rs = preparedStatement.executeQuery()) {

            while (rs.next()) {
                treatments.add(mapTreatment(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to retrieve treatments from the database.", e);
        }

        return treatments;
    }

    // Find a treatment using its ID.
    public Treatment findTreatment(int treatmentId) {

        String sql = "SELECT treatment_id, treatment_name, cost "
                + "FROM treatments "
                + "WHERE treatment_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, treatmentId);

            try (ResultSet rs = preparedStatement.executeQuery()) {

                if (rs.next()) {
                    return mapTreatment(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to find treatment in the database.", e);
        }

        return null;
    }

    // Search treatments by name, ID or cost.
    public List<Treatment> searchTreatments(String keyword) {

        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllTreatments();
        }

        String trimmedKeyword = keyword.trim();
        StringBuilder sql = new StringBuilder(
                "SELECT treatment_id, treatment_name, cost "
                + "FROM treatments "
                + "WHERE LOWER(treatment_name) LIKE ?");

        List<Treatment> treatments = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();

        parameters.add("%" + trimmedKeyword.toLowerCase() + "%");

        Integer treatmentId = parseInteger(trimmedKeyword);
        if (treatmentId != null) {
            sql.append(" OR treatment_id = ?");
            parameters.add(treatmentId);
        }

        BigDecimal cost = parseBigDecimal(trimmedKeyword);
        if (cost != null) {
            sql.append(" OR cost = ?");
            parameters.add(cost);
        }

        sql.append(" ORDER BY treatment_id DESC");

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(sql.toString())) {

            for (int i = 0; i < parameters.size(); i++) {
                Object parameter = parameters.get(i);
                int index = i + 1;

                if (parameter instanceof Integer integerValue) {
                    preparedStatement.setInt(index, integerValue);
                } else if (parameter instanceof BigDecimal bigDecimalValue) {
                    preparedStatement.setBigDecimal(index, bigDecimalValue);
                } else {
                    preparedStatement.setString(index, String.valueOf(parameter));
                }
            }

            try (ResultSet rs = preparedStatement.executeQuery()) {

                while (rs.next()) {
                    treatments.add(mapTreatment(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to search treatments in the database.", e);
        }

        return treatments;
    }

    private Integer parseInteger(String value) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private BigDecimal parseBigDecimal(String value) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Add a new treatment to the database.
    public boolean addTreatment(Treatment treatment) {

        String sql = "INSERT INTO treatments "
                + "(treatment_name, cost) "
                + "VALUES (?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(sql)) {

            preparedStatement.setString(
                    1, treatment.getTreatmentName());

            preparedStatement.setDouble(
                    2, treatment.getCost());

            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to add treatment to the database.", e);
        }
    }

    // Update an existing treatment.
    public boolean updateTreatment(Treatment treatment) {

        String sql = "UPDATE treatments SET "
                + "treatment_name = ?, "
                + "cost = ? "
                + "WHERE treatment_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(sql)) {

            preparedStatement.setString(
                    1, treatment.getTreatmentName());

            preparedStatement.setDouble(
                    2, treatment.getCost());

            preparedStatement.setInt(
                    3, treatment.getTreatmentId());

            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to update treatment in the database.", e);
        }
    }

    // Delete a treatment using its ID.
    public boolean deleteTreatment(int treatmentId) {

        String sql = "DELETE FROM treatments WHERE treatment_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, treatmentId);

            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to delete treatment from the database.", e);
        }
    }

    // Convert a database row into a Treatment object.
    private Treatment mapTreatment(ResultSet rs) throws SQLException {

        Treatment treatment = new Treatment();

        treatment.setTreatmentId(
                rs.getInt("treatment_id"));

        treatment.setTreatmentName(
                rs.getString("treatment_name"));

        treatment.setCost(
                rs.getDouble("cost"));

        return treatment;
    }  
}
