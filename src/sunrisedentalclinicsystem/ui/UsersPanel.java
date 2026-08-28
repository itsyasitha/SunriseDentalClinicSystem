/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package sunrisedentalclinicsystem.ui;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import sunrisedentalclinicsystem.dao.UserDAO;
import sunrisedentalclinicsystem.model.User;
import sunrisedentalclinicsystem.util.AppConstants;
import sunrisedentalclinicsystem.util.Session;

/**
 *
 * @author yasit
 */
public class UsersPanel extends javax.swing.JPanel {

    private static final String ROLE_RECEPTIONIST = "Receptionist";
    private static final String ROLE_DENTIST = "Dentist";
    private static final String ROLE_ADMINISTRATOR = "Administrator";

    private final UserDAO userDAO;
    private final DefaultTableModel userTableModel;
    private final DateTimeFormatter createdDateFormatter;

    /**
     * Creates new form UsersPanel
     */
    public UsersPanel() {
        initComponents();
        userDAO = new UserDAO();
        userTableModel = (DefaultTableModel) tblUsers.getModel();
        createdDateFormatter = DateTimeFormatter.ofPattern(AppConstants.DATE_FORMAT + " " + AppConstants.TIME_FORMAT);
        configureTable();
        configureSelectionHandling();
        configureAdministratorAccess();
        loadUsers();
    }

    private void configureAdministratorAccess() {
        boolean administrator = isAdministrator();
        btnAddUser.setEnabled(administrator);
        btnEditUser.setEnabled(administrator);
        btnChangePassword.setEnabled(administrator);
        btnToggleActive.setEnabled(administrator);
    }

    private boolean isAdministrator() {
        User currentUser = Session.getCurrentUser();
        if (currentUser == null) {
            return false;
        }

        return ROLE_ADMINISTRATOR.equalsIgnoreCase(valueOrEmpty(currentUser.getRole()).trim());
    }

    private boolean ensureAdministratorAccess() {
        if (isAdministrator()) {
            return true;
        }

        JOptionPane.showMessageDialog(
                this,
                "Only administrators can manage users.",
                "Access Denied",
                JOptionPane.WARNING_MESSAGE
        );
        return false;
    }

    private void configureTable() {
        tblUsers.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblUsers.getTableHeader().setReorderingAllowed(false);
    }

    private void configureSelectionHandling() {
        tblUsers.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (!e.getValueIsAdjusting()) {
                updateStatusButtonLabel();
            }
        });
        updateStatusButtonLabel();
    }

    private void updateStatusButtonLabel() {
        User selectedUser = getSelectedUser();
        if (selectedUser == null) {
            btnToggleActive.setText("Activate/Deactivate");
            return;
        }

        btnToggleActive.setText(selectedUser.isActive() ? "Deactivate" : "Activate");
    }

    private void loadUsers() {
        try {
            displayUsers(userDAO.getAllUsers());
        } catch (RuntimeException e) {
            showError("Unable to load users.");
        }
    }

    private void displayUsers(List<User> users) {
        userTableModel.setRowCount(0);

        for (User user : users) {
            userTableModel.addRow(new Object[]{
                user.getUserId(),
                valueOrEmpty(user.getUsername()),
                valueOrEmpty(user.getFullName()),
                valueOrEmpty(user.getRole()),
                user.isActive() ? "Active" : "Inactive",
                formatCreatedDate(user.getCreatedDate())
            });
        }

        updateStatusButtonLabel();
    }

    private void searchUsers() {
        try {
            displayUsers(userDAO.searchUsers(txtSearch.getText()));
        } catch (RuntimeException e) {
            showError("Unable to search users.");
        }
    }

    private void clearSearch() {
        txtSearch.setText("");
        loadUsers();
    }

    private void refreshUsers() {
        if (txtSearch.getText() == null || txtSearch.getText().trim().isEmpty()) {
            loadUsers();
        } else {
            searchUsers();
        }
    }

    private void addUser() {
        if (!ensureAdministratorAccess()) {
            return;
        }
        showUserEditorDialog(null);
    }

    private void editUser() {
        if (!ensureAdministratorAccess()) {
            return;
        }
        int userId = getSelectedUserId();
        if (userId <= 0) {
            JOptionPane.showMessageDialog(this, "Please select a user to edit.", "Edit User", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            User user = userDAO.findUser(userId);
            if (user == null) {
                JOptionPane.showMessageDialog(this, "Selected user was not found.", "Edit User", JOptionPane.WARNING_MESSAGE);
                refreshUsers();
                return;
            }

            showUserEditorDialog(user);
        } catch (RuntimeException e) {
            showFriendlyDaoError("Unable to load selected user for editing.", e);
        }
    }

    private void showUserEditorDialog(User existingUser) {
        boolean editing = existingUser != null;

        JTextField txtUsername = new JTextField(editing ? valueOrEmpty(existingUser.getUsername()) : "");
        JPasswordField txtPassword = new JPasswordField();
        JTextField txtFullName = new JTextField(editing ? valueOrEmpty(existingUser.getFullName()) : "");
        JComboBox<String> cmbRole = new JComboBox<>(new String[]{ROLE_RECEPTIONIST, ROLE_DENTIST, ROLE_ADMINISTRATOR});
        JCheckBox chkActive = new JCheckBox("Active", !editing || existingUser.isActive());

        if (editing) {
            cmbRole.setSelectedItem(valueOrEmpty(existingUser.getRole()));
        }

        javax.swing.JPanel formPanel = new javax.swing.JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        if (editing) {
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.gridwidth = 2;
            formPanel.add(new JLabel("User ID: " + existingUser.getUserId()), gbc);
            row++;
            gbc.gridwidth = 1;
        }

        row = addFormField(formPanel, gbc, row, "Username:", txtUsername);

        if (!editing) {
            row = addFormField(formPanel, gbc, row, "Password:", txtPassword);
        }

        row = addFormField(formPanel, gbc, row, "Full Name:", txtFullName);
        row = addFormField(formPanel, gbc, row, "Role:", cmbRole);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 1;
        formPanel.add(chkActive, gbc);

        JButton btnSave = new JButton(editing ? "Update" : "Add");
        JButton btnCancel = new JButton("Cancel");
        String title = editing ? "Edit User" : "Add User";

        JDialog dialog = createModalDialog(title);
        dialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        JOptionPane optionPane = new JOptionPane(
                formPanel,
                JOptionPane.PLAIN_MESSAGE,
                JOptionPane.DEFAULT_OPTION,
                null,
                new Object[]{btnSave, btnCancel},
                btnSave
        );

        dialog.setContentPane(optionPane);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        btnSave.addActionListener(evt -> {
            if (editing) {
                handleEditValidationAndSave(dialog, existingUser, txtUsername, txtFullName, cmbRole, chkActive);
            } else {
                handleAddValidationAndSave(dialog, txtUsername, txtPassword, txtFullName, cmbRole, chkActive);
            }
        });

        btnCancel.addActionListener(evt -> dialog.dispose());
        dialog.setVisible(true);
    }

    private int addFormField(
            javax.swing.JPanel formPanel,
            GridBagConstraints gbc,
            int row,
            String labelText,
            java.awt.Component field) {

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        formPanel.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        formPanel.add(field, gbc);

        return row + 1;
    }

    private JDialog createModalDialog(String title) {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        if (parentWindow instanceof Frame frame) {
            return new JDialog(frame, title, true);
        }
        if (parentWindow instanceof Dialog parentDialog) {
            return new JDialog(parentDialog, title, true);
        }

        JDialog dialog = new JDialog();
        dialog.setTitle(title);
        dialog.setModal(true);
        return dialog;
    }

    private void handleAddValidationAndSave(
            JDialog dialog,
            JTextField txtUsername,
            JPasswordField txtPassword,
            JTextField txtFullName,
            JComboBox<String> cmbRole,
            JCheckBox chkActive) {

        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        String fullName = txtFullName.getText().trim();
        String role = valueOrEmpty((String) cmbRole.getSelectedItem()).trim();

        String validationMessage = validateUserInput(username, password, fullName, role, true);
        if (validationMessage != null) {
            JOptionPane.showMessageDialog(dialog, validationMessage, "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setFullName(fullName);
        user.setRole(role);
        user.setIsActive(chkActive.isSelected());

        try {
            int userId = userDAO.addUser(user);
            if (userId > 0) {
                JOptionPane.showMessageDialog(dialog, "User added successfully.", "Add User", JOptionPane.INFORMATION_MESSAGE);
                refreshUsers();
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to add user.", "Add User", JOptionPane.ERROR_MESSAGE);
            }
        } catch (RuntimeException e) {
            showFriendlyDaoError(dialog, "Unable to add user.", e);
        }
    }

    private void handleEditValidationAndSave(
            JDialog dialog,
            User existingUser,
            JTextField txtUsername,
            JTextField txtFullName,
            JComboBox<String> cmbRole,
            JCheckBox chkActive) {

        String username = txtUsername.getText().trim();
        String fullName = txtFullName.getText().trim();
        String role = valueOrEmpty((String) cmbRole.getSelectedItem()).trim();

        String validationMessage = validateUserInput(username, null, fullName, role, false);
        if (validationMessage != null) {
            JOptionPane.showMessageDialog(dialog, validationMessage, "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User updatedUser = new User();
        updatedUser.setUserId(existingUser.getUserId());
        updatedUser.setUsername(username);
        updatedUser.setFullName(fullName);
        updatedUser.setRole(role);
        updatedUser.setIsActive(chkActive.isSelected());

        try {
            boolean updated = userDAO.updateUser(updatedUser);
            if (updated) {
                JOptionPane.showMessageDialog(dialog, "User updated successfully.", "Edit User", JOptionPane.INFORMATION_MESSAGE);
                refreshUsers();
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to update user.", "Edit User", JOptionPane.ERROR_MESSAGE);
            }
        } catch (RuntimeException e) {
            showFriendlyDaoError(dialog, "Unable to update user.", e);
        }
    }

    private String validateUserInput(
            String username,
            String password,
            String fullName,
            String role,
            boolean requirePassword) {

        if (username == null || username.isBlank()) {
            return "Username is required.";
        }

        if (requirePassword && (password == null || password.isBlank())) {
            return "Password is required when creating a user.";
        }

        if (fullName == null || fullName.isBlank()) {
            return "Full name is required.";
        }

        if (!isValidRole(role)) {
            return "Role must be Receptionist, Dentist, or Administrator.";
        }

        return null;
    }

    private boolean isValidRole(String role) {
        return ROLE_RECEPTIONIST.equals(role)
                || ROLE_DENTIST.equals(role)
                || ROLE_ADMINISTRATOR.equals(role);
    }

    private void changePassword() {
        if (!ensureAdministratorAccess()) {
            return;
        }
        User selectedUser = getSelectedUser();
        if (selectedUser == null) {
            JOptionPane.showMessageDialog(this, "Please select a user to change password.", "Change Password", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JPasswordField txtPassword = new JPasswordField();
        Object[] message = {
            "Set a new password for user: " + selectedUser.getUsername(),
            "New Password:", txtPassword
        };

        int option = JOptionPane.showConfirmDialog(
                this,
                message,
                "Change Password",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (option != JOptionPane.OK_OPTION) {
            return;
        }

        String newPassword = new String(txtPassword.getPassword());
        if (newPassword.isBlank()) {
            JOptionPane.showMessageDialog(this, "New password cannot be blank.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            boolean updated = userDAO.updatePassword(selectedUser.getUserId(), newPassword);
            if (updated) {
                JOptionPane.showMessageDialog(this, "Password updated successfully.", "Change Password", JOptionPane.INFORMATION_MESSAGE);
                refreshUsers();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update password.", "Change Password", JOptionPane.ERROR_MESSAGE);
            }
        } catch (RuntimeException e) {
            showFriendlyDaoError("Unable to update password.", e);
        }
    }

    private void toggleUserActiveStatus() {
        if (!ensureAdministratorAccess()) {
            return;
        }
        User selectedUser = getSelectedUser();
        if (selectedUser == null) {
            JOptionPane.showMessageDialog(this, "Please select a user first.", "Activate/Deactivate", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean targetStatus = !selectedUser.isActive();
        String actionText = targetStatus ? "activate" : "deactivate";

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to " + actionText + " user '" + selectedUser.getUsername() + "'?",
                targetStatus ? "Activate User" : "Deactivate User",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            boolean updated = userDAO.updateUserActiveStatus(selectedUser.getUserId(), targetStatus);
            if (updated) {
                JOptionPane.showMessageDialog(
                        this,
                        "User " + (targetStatus ? "activated" : "deactivated") + " successfully.",
                        targetStatus ? "Activate User" : "Deactivate User",
                        JOptionPane.INFORMATION_MESSAGE
                );
                refreshUsers();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update user status.", "Activate/Deactivate", JOptionPane.ERROR_MESSAGE);
            }
        } catch (RuntimeException e) {
            showFriendlyDaoError("Unable to update user status.", e);
        }
    }

    private User getSelectedUser() {
        int userId = getSelectedUserId();
        if (userId <= 0) {
            return null;
        }

        try {
            return userDAO.findUser(userId);
        } catch (RuntimeException e) {
            showFriendlyDaoError("Unable to load selected user.", e);
            return null;
        }
    }

    private int getSelectedUserId() {
        int selectedRow = tblUsers.getSelectedRow();
        if (selectedRow == -1) {
            return -1;
        }

        Object value = tblUsers.getValueAt(selectedRow, 0);
        if (value instanceof Number number) {
            return number.intValue();
        }

        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String formatCreatedDate(LocalDateTime createdDate) {
        if (createdDate == null) {
            return "";
        }

        try {
            return createdDate.format(createdDateFormatter);
        } catch (RuntimeException e) {
            return createdDate.toString();
        }
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private void showFriendlyDaoError(String fallbackMessage, RuntimeException e) {
        showFriendlyDaoError(this, fallbackMessage, e);
    }

    private void showFriendlyDaoError(java.awt.Component parent, String fallbackMessage, RuntimeException e) {
        String message = e == null || e.getMessage() == null ? "" : e.getMessage();
        String normalized = message.toLowerCase();

        if (normalized.contains("username already exists")
                || normalized.contains("duplicate")
                || normalized.contains("unique")) {
            JOptionPane.showMessageDialog(
                    parent,
                    "That username is already in use. Please choose a different username.",
                    "Validation",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (normalized.contains("cannot change role from dentist")) {
            JOptionPane.showMessageDialog(
                    parent,
                    message,
                    "Edit User",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        showError(fallbackMessage);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTitle = new javax.swing.JLabel();
        lblSubtitle = new javax.swing.JLabel();
        searchPanel = new javax.swing.JPanel();
        lblSearch = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();
        tablePanel = new javax.swing.JPanel();
        scrollPaneUsers = new javax.swing.JScrollPane();
        tblUsers = new javax.swing.JTable();
        actionsPanel = new javax.swing.JPanel();
        btnAddUser = new javax.swing.JButton();
        btnEditUser = new javax.swing.JButton();
        btnChangePassword = new javax.swing.JButton();
        btnToggleActive = new javax.swing.JButton();

        lblTitle.setFont(sunrisedentalclinicsystem.util.UIStyle.TITLE_FONT);
        lblTitle.setForeground(sunrisedentalclinicsystem.util.UIStyle.PRIMARY_TEXT);
        lblTitle.setText("Users");

        lblSubtitle.setFont(sunrisedentalclinicsystem.util.UIStyle.LABEL_FONT);
        lblSubtitle.setForeground(sunrisedentalclinicsystem.util.UIStyle.SECONDARY_TEXT);
        lblSubtitle.setText("Manage administrator, dentist, and receptionist accounts");

        searchPanel.setBackground(sunrisedentalclinicsystem.util.UIStyle.CARD_BACKGROUND);

        lblSearch.setFont(sunrisedentalclinicsystem.util.UIStyle.LABEL_FONT);
        lblSearch.setForeground(sunrisedentalclinicsystem.util.UIStyle.PRIMARY_TEXT);
        lblSearch.setText("Search Users");

        txtSearch.setFont(sunrisedentalclinicsystem.util.UIStyle.LABEL_FONT);

        btnSearch.setBackground(sunrisedentalclinicsystem.util.UIStyle.PRIMARY_BLUE);
        btnSearch.setFont(sunrisedentalclinicsystem.util.UIStyle.BUTTON_FONT);
        btnSearch.setForeground(sunrisedentalclinicsystem.util.UIStyle.CARD_BACKGROUND);
        btnSearch.setText("Search");
        btnSearch.addActionListener(this::btnSearchActionPerformed);

        btnClear.setFont(sunrisedentalclinicsystem.util.UIStyle.BUTTON_FONT);
        btnClear.setText("Clear");
        btnClear.addActionListener(this::btnClearActionPerformed);

        btnRefresh.setFont(sunrisedentalclinicsystem.util.UIStyle.BUTTON_FONT);
        btnRefresh.setText("Refresh");
        btnRefresh.addActionListener(this::btnRefreshActionPerformed);

        javax.swing.GroupLayout searchPanelLayout = new javax.swing.GroupLayout(searchPanel);
        searchPanel.setLayout(searchPanelLayout);
        searchPanelLayout.setHorizontalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblSearch)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtSearch)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnSearch)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnClear)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRefresh)
                .addContainerGap())
        );
        searchPanelLayout.setVerticalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSearch)
                    .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSearch)
                    .addComponent(btnClear)
                    .addComponent(btnRefresh))
                .addContainerGap())
        );

        tablePanel.setBackground(sunrisedentalclinicsystem.util.UIStyle.CARD_BACKGROUND);

        tblUsers.setFont(sunrisedentalclinicsystem.util.UIStyle.TABLE_FONT);
        tblUsers.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "User ID", "Username", "Full Name", "Role", "Status", "Created Date"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblUsers.setRowHeight(24);
        tblUsers.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        scrollPaneUsers.setViewportView(tblUsers);

        javax.swing.GroupLayout tablePanelLayout = new javax.swing.GroupLayout(tablePanel);
        tablePanel.setLayout(tablePanelLayout);
        tablePanelLayout.setHorizontalGroup(
            tablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tablePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollPaneUsers)
                .addContainerGap())
        );
        tablePanelLayout.setVerticalGroup(
            tablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tablePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollPaneUsers, javax.swing.GroupLayout.DEFAULT_SIZE, 384, Short.MAX_VALUE)
                .addContainerGap())
        );

        actionsPanel.setBackground(sunrisedentalclinicsystem.util.UIStyle.CARD_BACKGROUND);

        btnAddUser.setBackground(sunrisedentalclinicsystem.util.UIStyle.PRIMARY_BLUE);
        btnAddUser.setFont(sunrisedentalclinicsystem.util.UIStyle.BUTTON_FONT);
        btnAddUser.setForeground(sunrisedentalclinicsystem.util.UIStyle.CARD_BACKGROUND);
        btnAddUser.setText("Add User");
        btnAddUser.addActionListener(this::btnAddUserActionPerformed);

        btnEditUser.setFont(sunrisedentalclinicsystem.util.UIStyle.BUTTON_FONT);
        btnEditUser.setText("Edit User");
        btnEditUser.addActionListener(this::btnEditUserActionPerformed);

        btnChangePassword.setFont(sunrisedentalclinicsystem.util.UIStyle.BUTTON_FONT);
        btnChangePassword.setText("Change Password");
        btnChangePassword.addActionListener(this::btnChangePasswordActionPerformed);

        btnToggleActive.setFont(sunrisedentalclinicsystem.util.UIStyle.BUTTON_FONT);
        btnToggleActive.setText("Activate/Deactivate");
        btnToggleActive.addActionListener(this::btnToggleActiveActionPerformed);

        javax.swing.GroupLayout actionsPanelLayout = new javax.swing.GroupLayout(actionsPanel);
        actionsPanel.setLayout(actionsPanelLayout);
        actionsPanelLayout.setHorizontalGroup(
            actionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(actionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnAddUser)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnEditUser)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnChangePassword)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnToggleActive)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        actionsPanelLayout.setVerticalGroup(
            actionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(actionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(actionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddUser, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEditUser, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnChangePassword, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnToggleActive, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTitle)
                    .addComponent(lblSubtitle)
                    .addComponent(searchPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(actionsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblSubtitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(searchPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(tablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(actionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        searchUsers();
    }//GEN-LAST:event_btnSearchActionPerformed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        clearSearch();
    }//GEN-LAST:event_btnClearActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        refreshUsers();
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void btnAddUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddUserActionPerformed
        addUser();
    }//GEN-LAST:event_btnAddUserActionPerformed

    private void btnEditUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditUserActionPerformed
        editUser();
    }//GEN-LAST:event_btnEditUserActionPerformed

    private void btnChangePasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChangePasswordActionPerformed
        changePassword();
    }//GEN-LAST:event_btnChangePasswordActionPerformed

    private void btnToggleActiveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnToggleActiveActionPerformed
        toggleUserActiveStatus();
    }//GEN-LAST:event_btnToggleActiveActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel actionsPanel;
    private javax.swing.JButton btnAddUser;
    private javax.swing.JButton btnChangePassword;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnEditUser;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnToggleActive;
    private javax.swing.JLabel lblSearch;
    private javax.swing.JLabel lblSubtitle;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JScrollPane scrollPaneUsers;
    private javax.swing.JPanel searchPanel;
    private javax.swing.JPanel tablePanel;
    private javax.swing.JTable tblUsers;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
