/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package sunrisedentalclinicsystem.ui;

import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import sunrisedentalclinicsystem.dao.PatientDAO;
import sunrisedentalclinicsystem.model.Patient;

/**
 *
 * @author yasit
 */
public class PatientsPanel extends javax.swing.JPanel {

    private final PatientDAO patientDAO;
    private final DefaultTableModel patientTableModel;

    /**
     * Creates new form PatientsPanel
     */
    public PatientsPanel() {
        initComponents();
        patientDAO = new PatientDAO();
        patientTableModel = (DefaultTableModel) tblPatients.getModel();
        configureTable();
        loadPatients();
    }

    private void configureTable() {
        tblPatients.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblPatients.getTableHeader().setReorderingAllowed(false);
    }

    private void loadPatients() {
        try {
            List<Patient> patients = patientDAO.getAllPatients();
            displayPatients(patients);
        } catch (RuntimeException e) {
            showError("Unable to load patients.");
        }
    }

    private void displayPatients(List<Patient> patients) {
        patientTableModel.setRowCount(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(sunrisedentalclinicsystem.util.AppConstants.DATE_FORMAT + " " + sunrisedentalclinicsystem.util.AppConstants.TIME_FORMAT);

        for (Patient patient : patients) {
            String registeredDate = "";
            if (patient.getCreatedDate() != null) {
                registeredDate = patient.getCreatedDate().format(formatter);
            }

            patientTableModel.addRow(new Object[]{
                patient.getPatientId(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getContactNumber(),
                patient.getAddress(),
                registeredDate
            });
        }
    }

    private void searchPatients() {
        try {
            List<Patient> patients = patientDAO.searchPatients(txtSearch.getText());
            displayPatients(patients);
        } catch (RuntimeException e) {
            showError("Unable to search patients.");
        }
    }

    private void clearSearch() {
        txtSearch.setText("");
        loadPatients();
    }

    private void registerPatient() {
        javax.swing.JTextField txtFirstName = new javax.swing.JTextField();
        javax.swing.JTextField txtLastName = new javax.swing.JTextField();
        javax.swing.JTextField txtContactNumber = new javax.swing.JTextField();
        javax.swing.JTextField txtAddress = new javax.swing.JTextField();

        Object[] message = {
            "First Name:", txtFirstName,
            "Last Name:", txtLastName,
            "Contact Number:", txtContactNumber,
            "Address:", txtAddress
        };

        int option = JOptionPane.showConfirmDialog(
                this,
                message,
                "Register Patient",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (option != JOptionPane.OK_OPTION) {
            return;
        }

        String firstName = txtFirstName.getText().trim();
        String lastName = txtLastName.getText().trim();
        String contactNumber = txtContactNumber.getText().trim();
        String address = txtAddress.getText().trim();

        if (firstName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "First name cannot be blank.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (lastName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Last name cannot be blank.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (contactNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Contact number cannot be blank.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Patient patient = new Patient();
        patient.setFirstName(firstName);
        patient.setLastName(lastName);
        patient.setContactNumber(contactNumber);
        patient.setAddress(address);

        try {
            boolean added = patientDAO.addPatient(patient);
            if (added) {
                JOptionPane.showMessageDialog(this, "Patient registered successfully.");
                loadPatients();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to register patient.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (RuntimeException e) {
            showError("Unable to register patient.");
        }
    }

    private void editPatient() {
        int patientId = getSelectedPatientId();
        if (patientId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a patient to edit.", "Edit Patient", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Patient patient = patientDAO.findPatient(patientId);
            if (patient == null) {
                JOptionPane.showMessageDialog(this, "Selected patient was not found.", "Edit Patient", JOptionPane.WARNING_MESSAGE);
                loadPatients();
                return;
            }

            javax.swing.JTextField txtFirstName = new javax.swing.JTextField(patient.getFirstName());
            javax.swing.JTextField txtLastName = new javax.swing.JTextField(patient.getLastName());
            javax.swing.JTextField txtContactNumber = new javax.swing.JTextField(patient.getContactNumber());
            javax.swing.JTextField txtAddress = new javax.swing.JTextField(patient.getAddress());

            Object[] message = {
                "Patient ID: " + patient.getPatientId(),
                "Registered Date: " + (patient.getCreatedDate() == null ? "" : patient.getCreatedDate().toString()),
                "First Name:", txtFirstName,
                "Last Name:", txtLastName,
                "Contact Number:", txtContactNumber,
                "Address:", txtAddress
            };

            int option = JOptionPane.showConfirmDialog(
                    this,
                    message,
                    "Edit Patient",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (option != JOptionPane.OK_OPTION) {
                return;
            }

            String firstName = txtFirstName.getText().trim();
            String lastName = txtLastName.getText().trim();
            String contactNumber = txtContactNumber.getText().trim();
            String address = txtAddress.getText().trim();

            if (firstName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "First name cannot be blank.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (lastName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Last name cannot be blank.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (contactNumber.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Contact number cannot be blank.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            patient.setFirstName(firstName);
            patient.setLastName(lastName);
            patient.setContactNumber(contactNumber);
            patient.setAddress(address);

            boolean updated = patientDAO.updatePatient(patient);
            if (updated) {
                JOptionPane.showMessageDialog(this, "Patient updated successfully.");
                loadPatients();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update patient.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (RuntimeException e) {
            showError("Unable to update patient.");
        }
    }

    private void deletePatient() {
        int patientId = getSelectedPatientId();
        if (patientId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a patient to delete.", "Delete Patient", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete the selected patient?",
                "Delete Patient",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            boolean deleted = patientDAO.deletePatient(patientId);
            if (deleted) {
                JOptionPane.showMessageDialog(this, "Patient deleted successfully.");
                loadPatients();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete patient.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (RuntimeException e) {
            String message = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
            if (message.contains("foreign key") || message.contains("constraint") || message.contains("restrict")) {
                JOptionPane.showMessageDialog(
                        this,
                        "Cannot delete this patient because related appointments or bills exist.",
                        "Delete Patient",
                        JOptionPane.WARNING_MESSAGE
                );
            } else {
                showError("Unable to delete patient.");
            }
        }
    }

    private int getSelectedPatientId() {
        int selectedRow = tblPatients.getSelectedRow();
        if (selectedRow == -1) {
            return -1;
        }

        Object value = tblPatients.getValueAt(selectedRow, 0);
        if (value instanceof Number number) {
            return number.intValue();
        }

        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return -1;
        }
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
        tablePanel = new javax.swing.JPanel();
        scrollPanePatients = new javax.swing.JScrollPane();
        tblPatients = new javax.swing.JTable();
        actionsPanel = new javax.swing.JPanel();
        btnRegisterPatient = new javax.swing.JButton();
        btnEditPatient = new javax.swing.JButton();
        btnDeletePatient = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();

        lblTitle.setFont(sunrisedentalclinicsystem.util.UIStyle.TITLE_FONT);
        lblTitle.setForeground(sunrisedentalclinicsystem.util.UIStyle.PRIMARY_TEXT);
        lblTitle.setText("Patients");

        lblSubtitle.setFont(sunrisedentalclinicsystem.util.UIStyle.LABEL_FONT);
        lblSubtitle.setForeground(sunrisedentalclinicsystem.util.UIStyle.SECONDARY_TEXT);
        lblSubtitle.setText("Manage registered patients");

        searchPanel.setBackground(sunrisedentalclinicsystem.util.UIStyle.CARD_BACKGROUND);

        lblSearch.setFont(sunrisedentalclinicsystem.util.UIStyle.LABEL_FONT);
        lblSearch.setForeground(sunrisedentalclinicsystem.util.UIStyle.PRIMARY_TEXT);
        lblSearch.setText("Search Patients");

        txtSearch.setFont(sunrisedentalclinicsystem.util.UIStyle.LABEL_FONT);

        btnSearch.setBackground(sunrisedentalclinicsystem.util.UIStyle.PRIMARY_BLUE);
        btnSearch.setFont(sunrisedentalclinicsystem.util.UIStyle.BUTTON_FONT);
        btnSearch.setForeground(sunrisedentalclinicsystem.util.UIStyle.CARD_BACKGROUND);
        btnSearch.setText("Search");
        btnSearch.addActionListener(this::btnSearchActionPerformed);

        btnClear.setFont(sunrisedentalclinicsystem.util.UIStyle.BUTTON_FONT);
        btnClear.setText("Clear");
        btnClear.addActionListener(this::btnClearActionPerformed);

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
                    .addComponent(btnClear))
                .addContainerGap())
        );

        tablePanel.setBackground(sunrisedentalclinicsystem.util.UIStyle.CARD_BACKGROUND);

        tblPatients.setFont(sunrisedentalclinicsystem.util.UIStyle.TABLE_FONT);
        tblPatients.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Patient ID", "First Name", "Last Name", "Contact Number", "Address", "Registered Date"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblPatients.setRowHeight(24);
        tblPatients.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        scrollPanePatients.setViewportView(tblPatients);

        javax.swing.GroupLayout tablePanelLayout = new javax.swing.GroupLayout(tablePanel);
        tablePanel.setLayout(tablePanelLayout);
        tablePanelLayout.setHorizontalGroup(
            tablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tablePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollPanePatients, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );
        tablePanelLayout.setVerticalGroup(
            tablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tablePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollPanePatients, javax.swing.GroupLayout.DEFAULT_SIZE, 384, Short.MAX_VALUE)
                .addContainerGap())
        );

        actionsPanel.setBackground(sunrisedentalclinicsystem.util.UIStyle.CARD_BACKGROUND);

        btnRegisterPatient.setBackground(sunrisedentalclinicsystem.util.UIStyle.PRIMARY_BLUE);
        btnRegisterPatient.setFont(sunrisedentalclinicsystem.util.UIStyle.BUTTON_FONT);
        btnRegisterPatient.setForeground(sunrisedentalclinicsystem.util.UIStyle.CARD_BACKGROUND);
        btnRegisterPatient.setText("Register Patient");
        btnRegisterPatient.addActionListener(this::btnRegisterPatientActionPerformed);

        btnEditPatient.setFont(sunrisedentalclinicsystem.util.UIStyle.BUTTON_FONT);
        btnEditPatient.setText("Edit Patient");
        btnEditPatient.addActionListener(this::btnEditPatientActionPerformed);

        btnDeletePatient.setFont(sunrisedentalclinicsystem.util.UIStyle.BUTTON_FONT);
        btnDeletePatient.setForeground(sunrisedentalclinicsystem.util.UIStyle.DANGER);
        btnDeletePatient.setText("Delete Patient");
        btnDeletePatient.addActionListener(this::btnDeletePatientActionPerformed);

        btnRefresh.setFont(sunrisedentalclinicsystem.util.UIStyle.BUTTON_FONT);
        btnRefresh.setText("Refresh");
        btnRefresh.addActionListener(this::btnRefreshActionPerformed);

        javax.swing.GroupLayout actionsPanelLayout = new javax.swing.GroupLayout(actionsPanel);
        actionsPanel.setLayout(actionsPanelLayout);
        actionsPanelLayout.setHorizontalGroup(
            actionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(actionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnRegisterPatient)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnEditPatient)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnDeletePatient)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnRefresh)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        actionsPanelLayout.setVerticalGroup(
            actionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(actionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(actionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnRegisterPatient, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEditPatient, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDeletePatient, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                    .addComponent(actionsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
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
        searchPatients();
    }//GEN-LAST:event_btnSearchActionPerformed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        clearSearch();
    }//GEN-LAST:event_btnClearActionPerformed

    private void btnRegisterPatientActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegisterPatientActionPerformed
        registerPatient();
    }//GEN-LAST:event_btnRegisterPatientActionPerformed

    private void btnEditPatientActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditPatientActionPerformed
        editPatient();
    }//GEN-LAST:event_btnEditPatientActionPerformed

    private void btnDeletePatientActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeletePatientActionPerformed
        deletePatient();
    }//GEN-LAST:event_btnDeletePatientActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        loadPatients();
    }//GEN-LAST:event_btnRefreshActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel actionsPanel;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnDeletePatient;
    private javax.swing.JButton btnEditPatient;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnRegisterPatient;
    private javax.swing.JButton btnSearch;
    private javax.swing.JLabel lblSearch;
    private javax.swing.JLabel lblSubtitle;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JScrollPane scrollPanePatients;
    private javax.swing.JPanel searchPanel;
    private javax.swing.JPanel tablePanel;
    private javax.swing.JTable tblPatients;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
