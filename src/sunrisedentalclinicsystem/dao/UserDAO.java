package sunrisedentalclinicsystem.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import sunrisedentalclinicsystem.database.DBConnection;
import sunrisedentalclinicsystem.model.User;
import sunrisedentalclinicsystem.util.PasswordHasher;

/**
 *
 * @author yasit
 */
public class UserDAO {

    private static final String ROLE_DENTIST = "Dentist";

    // Check the username and password during login.
    public User authenticate(String username, String password) {
        User user = getUserByUsername(username);

        if (user != null
                && user.isActive()
                && PasswordHasher.verifyPassword(password, user.getPassword())) {
            return user;
        }

        return null;
    }

    // Get all users, with the newest users first.
    public List<User> getAllUsers() {
        String sql = "SELECT user_id, username, full_name, role, is_active, created_date "
                + "FROM users "
                + "ORDER BY user_id DESC";

        List<User> users = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet rs = preparedStatement.executeQuery()) {

            while (rs.next()) {
                users.add(mapUser(rs, false));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve users from the database.", e);
        }

        return users;
    }

    // Search users by ID, username, name or role.
    public List<User> searchUsers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllUsers();
        }

        String trimmedKeyword = keyword.trim();
        String lowercaseKeyword = trimmedKeyword.toLowerCase();

        StringBuilder sql = new StringBuilder(
                "SELECT user_id, username, full_name, role, is_active, created_date "
                + "FROM users "
                + "WHERE LOWER(username) LIKE ? "
                + "OR LOWER(full_name) LIKE ? "
                + "OR LOWER(role) LIKE ?");

        List<Object> parameters = new ArrayList<>();
        parameters.add("%" + lowercaseKeyword + "%");
        parameters.add("%" + lowercaseKeyword + "%");
        parameters.add("%" + lowercaseKeyword + "%");

        Integer numericUserId = parseInteger(trimmedKeyword);
        if (numericUserId != null) {
            sql.append(" OR user_id = ?");
            parameters.add(numericUserId);
        }

        sql.append(" ORDER BY user_id DESC");

        List<User> users = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql.toString())) {

            bindSearchParameters(preparedStatement, parameters);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    users.add(mapUser(rs, false));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to search users in the database.", e);
        }

        return users;
    }

    // Find a user using their ID.
    public User findUser(int userId) {
        String sql = "SELECT user_id, username, full_name, role, is_active, created_date "
                + "FROM users "
                + "WHERE user_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, userId);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs, false);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user in the database.", e);
        }

        return null;
    }

    // Add a new user and create a dentist profile if needed.
    public int addUser(User user) {
        if (user == null) {
            throw new RuntimeException("User cannot be null.");
        }

        String username = valueOrEmpty(user.getUsername()).trim();
        String password = valueOrEmpty(user.getPassword());
        String fullName = valueOrEmpty(user.getFullName()).trim();
        String role = valueOrEmpty(user.getRole()).trim();

        if (username.isEmpty()) {
            throw new RuntimeException("Username is required.");
        }
        if (password.isBlank()) {
            throw new RuntimeException("Password is required.");
        }
        if (fullName.isEmpty()) {
            throw new RuntimeException("Full name is required.");
        }
        if (role.isEmpty()) {
            throw new RuntimeException("Role is required.");
        }

        String insertUserSql = "INSERT INTO users (username, password, full_name, role, is_active) "
                + "VALUES (?, ?, ?, ?, ?)";
        String insertDentistSql = "INSERT INTO dentists (user_id, specialization) VALUES (?, ?)";

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement insertUserStatement =
                         connection.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS)) {

                insertUserStatement.setString(1, username);
                insertUserStatement.setString(2, PasswordHasher.hashPassword(password));
                insertUserStatement.setString(3, fullName);
                insertUserStatement.setString(4, role);
                insertUserStatement.setBoolean(5, user.isActive());

                int rows = insertUserStatement.executeUpdate();
                if (rows <= 0) {
                    throw new RuntimeException("Failed to add user.");
                }

                int generatedUserId;
                try (ResultSet keys = insertUserStatement.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new RuntimeException("Failed to obtain generated user ID.");
                    }
                    generatedUserId = keys.getInt(1);
                }

                if (ROLE_DENTIST.equalsIgnoreCase(role)) {
                    try (PreparedStatement insertDentistStatement = connection.prepareStatement(insertDentistSql)) {
                        insertDentistStatement.setInt(1, generatedUserId);
                        insertDentistStatement.setNull(2, Types.VARCHAR);
                        insertDentistStatement.executeUpdate();
                    }
                }

                connection.commit();
                user.setUserId(generatedUserId);
                return generatedUserId;

            } catch (SQLException e) {
                rollbackQuietly(connection);
                if (isDuplicateUsernameError(e)) {
                    throw new RuntimeException("Username already exists. Please choose a different username.", e);
                }
                throw new RuntimeException("Failed to add user to the database.", e);
            } catch (RuntimeException e) {
                rollbackQuietly(connection);
                throw e;
            } finally {
                resetAutoCommitQuietly(connection);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to add user to the database.", e);
        }
    }

    // Update a user's details and handle their dentist profile.
    public boolean updateUser(User user) {
        if (user == null) {
            throw new RuntimeException("User cannot be null.");
        }

        String username = valueOrEmpty(user.getUsername()).trim();
        String fullName = valueOrEmpty(user.getFullName()).trim();
        String newRole = valueOrEmpty(user.getRole()).trim();

        if (username.isEmpty()) {
            throw new RuntimeException("Username is required.");
        }
        if (fullName.isEmpty()) {
            throw new RuntimeException("Full name is required.");
        }
        if (newRole.isEmpty()) {
            throw new RuntimeException("Role is required.");
        }

        String selectRoleSql = "SELECT role FROM users WHERE user_id = ?";
        String updateUserSql = "UPDATE users SET username = ?, full_name = ?, role = ?, is_active = ? WHERE user_id = ?";
        String insertDentistSql = "INSERT INTO dentists (user_id, specialization) VALUES (?, ?)";

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                String existingRole = findCurrentRole(connection, selectRoleSql, user.getUserId());
                if (existingRole == null) {
                    connection.rollback();
                    return false;
                }

                boolean updated;
                try (PreparedStatement updateUserStatement = connection.prepareStatement(updateUserSql)) {
                    updateUserStatement.setString(1, username);
                    updateUserStatement.setString(2, fullName);
                    updateUserStatement.setString(3, newRole);
                    updateUserStatement.setBoolean(4, user.isActive());
                    updateUserStatement.setInt(5, user.getUserId());
                    updated = updateUserStatement.executeUpdate() > 0;
                }

                if (!updated) {
                    connection.rollback();
                    return false;
                }

                if (ROLE_DENTIST.equalsIgnoreCase(newRole)) {
                    if (!hasLinkedDentistRecord(connection, user.getUserId())) {
                        try (PreparedStatement insertDentistStatement = connection.prepareStatement(insertDentistSql)) {
                            insertDentistStatement.setInt(1, user.getUserId());
                            insertDentistStatement.setNull(2, Types.VARCHAR);
                            insertDentistStatement.executeUpdate();
                        }
                    }
                } else if (ROLE_DENTIST.equalsIgnoreCase(existingRole)) {
                    handleUnsafeDentistRoleChange(connection, user.getUserId());
                }

                connection.commit();
                return true;

            } catch (SQLException e) {
                rollbackQuietly(connection);
                if (isDuplicateUsernameError(e)) {
                    throw new RuntimeException("Username already exists. Please choose a different username.", e);
                }
                throw new RuntimeException("Failed to update user in the database.", e);
            } catch (RuntimeException e) {
                rollbackQuietly(connection);
                throw e;
            } finally {
                resetAutoCommitQuietly(connection);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update user in the database.", e);
        }
    }

    // Update a user's password.
    public boolean updatePassword(int userId, String password) {
        String normalizedPassword = valueOrEmpty(password);
        if (normalizedPassword.isBlank()) {
            throw new RuntimeException("Password is required.");
        }

        String sql = "UPDATE users SET password = ? WHERE user_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, PasswordHasher.hashPassword(normalizedPassword));
            preparedStatement.setInt(2, userId);
            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update user password in the database.", e);
        }
    }

    // Change whether a user account is active.
    public boolean updateUserActiveStatus(int userId, boolean active) {
        String sql = "UPDATE users SET is_active = ? WHERE user_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setBoolean(1, active);
            preparedStatement.setInt(2, userId);
            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update user active status in the database.", e);
        }
    }

    // Find a user using their username.
    private User getUserByUsername(String username) {
        String sql = "SELECT user_id, username, password, full_name, role, is_active, created_date "
                + "FROM users WHERE username = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, username);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs, true);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve user from the database.", e);
        }

        return null;
    }

    private User mapUser(ResultSet rs, boolean includePassword) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setFullName(rs.getString("full_name"));
        user.setRole(rs.getString("role"));
        user.setIsActive(rs.getBoolean("is_active"));

        Timestamp createdDate = rs.getTimestamp("created_date");
        if (createdDate != null) {
            user.setCreatedDate(createdDate.toLocalDateTime());
        }

        if (includePassword) {
            user.setPassword(rs.getString("password"));
        }

        return user;
    }

    private Integer parseInteger(String value) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void bindSearchParameters(PreparedStatement statement, List<Object> parameters) throws SQLException {
        for (int i = 0; i < parameters.size(); i++) {
            Object parameter = parameters.get(i);
            int index = i + 1;

            if (parameter instanceof Integer integerValue) {
                statement.setInt(index, integerValue);
            } else {
                statement.setString(index, String.valueOf(parameter));
            }
        }
    }

    private String findCurrentRole(Connection connection, String sql, int userId) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, userId);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("role");
                }
            }
        }

        return null;
    }

    private boolean hasLinkedDentistRecord(Connection connection, int userId) throws SQLException {
        String sql = "SELECT dentist_id FROM dentists WHERE user_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, userId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void handleUnsafeDentistRoleChange(Connection connection, int userId) throws SQLException {
        Integer dentistId = findDentistIdByUserId(connection, userId);
        if (dentistId == null) {
            return;
        }

        if (hasAppointmentsForDentist(connection, dentistId)) {
            throw new RuntimeException(
                    "Cannot change role from Dentist because this dentist record is referenced by appointments.");
        }

        throw new RuntimeException(
                "Cannot change role from Dentist while a linked dentist profile exists. "
                + "Please handle the dentist profile first.");
    }

    private Integer findDentistIdByUserId(Connection connection, int userId) throws SQLException {
        String sql = "SELECT dentist_id FROM dentists WHERE user_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, userId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("dentist_id");
                }
            }
        }

        return null;
    }

    private boolean hasAppointmentsForDentist(Connection connection, int dentistId) throws SQLException {
        String sql = "SELECT 1 FROM appointments WHERE dentist_id = ? LIMIT 1";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, dentistId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean isDuplicateUsernameError(SQLException e) {
        if (e == null) {
            return false;
        }

        String message = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
        return e.getErrorCode() == 1062
                || "23000".equals(e.getSQLState())
                || message.contains("duplicate")
                || message.contains("unique");
    }

    private void rollbackQuietly(Connection connection) {
        if (connection == null) {
            return;
        }

        try {
            connection.rollback();
        } catch (SQLException ignored) {
        }
    }

    private void resetAutoCommitQuietly(Connection connection) {
        if (connection == null) {
            return;
        }

        try {
            connection.setAutoCommit(true);
        } catch (SQLException ignored) {
        }
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}
