package sunrisedentalclinicsystem.model;

import java.time.LocalDateTime;

/**
 *
 * @author yasit
 */
public class User {

    private int userId;
    private String username;
    private String password;
    private String fullName;
    private String role;
    private boolean isActive;
    private LocalDateTime createdDate;

    public User() {
    }

    public User(int userId, String username, String password, String fullName, 
                String role, boolean isActive, LocalDateTime createdDate) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.isActive = isActive;
        this.createdDate = createdDate;
    }

    // Getters

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRole() {
        return role;
    }

    public boolean isActive() {
        return isActive;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    // Setters

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", role='" + role + '\'' +
                ", isActive=" + isActive +
                ", createdDate=" + createdDate +
                '}';
    }
}
