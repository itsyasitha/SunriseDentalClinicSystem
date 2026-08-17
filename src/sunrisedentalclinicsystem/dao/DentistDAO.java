/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sunrisedentalclinicsystem.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import sunrisedentalclinicsystem.database.DBConnection;
import sunrisedentalclinicsystem.model.Dentist;

/**
 *
 * @author yasit
 */
public class DentistDAO {

    // Get all active dentists.
    public List<Dentist> getAllDentists() {

        String sql = "SELECT d.dentist_id, "
                + "d.user_id, "
                + "d.specialization, "
                + "u.full_name "
                + "FROM dentists d "
                + "INNER JOIN users u ON d.user_id = u.user_id "
                + "WHERE u.role = 'Dentist' "
                + "AND u.is_active = TRUE "
                + "ORDER BY u.full_name ASC";

        List<Dentist> dentists = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(sql);
             ResultSet rs = preparedStatement.executeQuery()) {

            while (rs.next()) {
                dentists.add(mapDentist(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to retrieve dentists from the database.",
                    e);
        }

        return dentists;
    }

    // Find an active dentist using their ID.
    public Dentist findDentist(int dentistId) {

        String sql = "SELECT d.dentist_id, "
                + "d.user_id, "
                + "d.specialization, "
                + "u.full_name "
                + "FROM dentists d "
                + "INNER JOIN users u ON d.user_id = u.user_id "
                + "WHERE d.dentist_id = ? "
                + "AND u.role = 'Dentist' "
                + "AND u.is_active = TRUE";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, dentistId);

            try (ResultSet rs = preparedStatement.executeQuery()) {

                if (rs.next()) {
                    return mapDentist(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to find dentist in the database.",
                    e);
        }

        return null;
    }

    // Convert a database row into a Dentist object.
    private Dentist mapDentist(ResultSet rs) throws SQLException {

        Dentist dentist = new Dentist();

        dentist.setDentistId(
                rs.getInt("dentist_id"));

        dentist.setUserId(
                rs.getInt("user_id"));

        dentist.setSpecialization(
                rs.getString("specialization"));

        dentist.setFullName(
                rs.getString("full_name"));

        return dentist;
    }
}
