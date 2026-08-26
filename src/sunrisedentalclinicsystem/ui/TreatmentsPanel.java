/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package sunrisedentalclinicsystem.ui;

import java.awt.Dialog;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import sunrisedentalclinicsystem.dao.TreatmentDAO;
import sunrisedentalclinicsystem.model.Treatment;
import sunrisedentalclinicsystem.util.UIStyle;

/**
 *
 * @author yasit
 */
public class TreatmentsPanel extends javax.swing.JPanel {

    private final TreatmentDAO treatmentDAO;
    private final DefaultTableModel treatmentTableModel;

    /**
     * Creates new form TreatmentsPanel
     */
    public TreatmentsPanel() {
        initComponents();
        treatmentDAO = new TreatmentDAO();
        treatmentTableModel = (DefaultTableModel) tblTreatments.getModel();
        configureTable();
        loadTreatments();
    }

    private void configureTable() {
        tblTreatments.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblTreatments.getTableHeader().setReorderingAllowed(false);
        tblTreatments.setRowHeight(28);
        tblTreatments.setShowGrid(true);
        tblTreatments.setGridColor(new Color(0xD9, 0xE2, 0xEC));
        tblTreatments.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tblTreatments.getTableHeader().setFont(UIStyle.TABLE_FONT.deriveFont(Font.BOLD));

        tblTreatments.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table,
                    Object value,
                    boolean isSelected,
                    boolean hasFocus,
                    int row,
                    int column) {

                Component component = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    component.setBackground(row % 2 == 0
                            ? Color.WHITE
                            : new Color(0xF2, 0xF6, 0xFA));
                }

                return component;
            }
        });

        tblTreatments.getColumnModel().getColumn(0).setMinWidth(90);
        tblTreatments.getColumnModel().getColumn(0).setPreferredWidth(110);
        tblTreatments.getColumnModel().getColumn(1).setMinWidth(220);
        tblTreatments.getColumnModel().getColumn(1).setPreferredWidth(300);
        tblTreatments.getColumnModel().getColumn(2).setMinWidth(120);
        tblTreatments.getColumnModel().getColumn(2).setPreferredWidth(150);
    }

    private void loadTreatments() {
        try {
            displayTreatments(treatmentDAO.getAllTreatments());
        } catch (RuntimeException e) {
            showError("Unable to load treatments.");
        }
    }

    private void displayTreatments(List<Treatment> treatments) {
        treatmentTableModel.setRowCount(0);

        for (Treatment treatment : treatments) {
            treatmentTableModel.addRow(new Object[]{
                treatment.getTreatmentId(),
                valueOrEmpty(treatment.getTreatmentName()),
                formatCost(treatment.getCost())
            });
        }
    }

    private void searchTreatments() {
        try {
            displayTreatments(treatmentDAO.searchTreatments(txtSearch.getText()));
        } catch (RuntimeException e) {
            showError("Unable to search treatments.");
        }
    }

    private void clearSearch() {
        txtSearch.setText("");
        loadTreatments();
    }

    private void refreshTreatments() {
        loadTreatments();
    }

    private void addTreatment() {
        showTreatmentEditorDialog(null);
    }

    private void editTreatment() {
        int treatmentId = getSelectedTreatmentId();
        if (treatmentId <= 0) {
            JOptionPane.showMessageDialog(this, "Please select a treatment to edit.", "Edit Treatment", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Treatment treatment = treatmentDAO.findTreatment(treatmentId);
            if (treatment == null) {
                JOptionPane.showMessageDialog(this, "Selected treatment was not found.", "Edit Treatment", JOptionPane.WARNING_MESSAGE);
                loadTreatments();
                return;
            }

            showTreatmentEditorDialog(treatment);
        } catch (RuntimeException e) {
            showError("Unable to update treatment.");
        }
    }

    private void showTreatmentEditorDialog(Treatment existingTreatment) {
        boolean editing = existingTreatment != null;

        JTextField txtTreatmentName = new JTextField(editing
                ? valueOrEmpty(existingTreatment.getTreatmentName())
                : "");
        JTextField txtCost = new JTextField(editing
                ? String.valueOf(existingTreatment.getCost())
                : "");

        JLabel lblTreatmentName = new JLabel("Treatment Name:");
        JLabel lblCost = new JLabel("Cost:");

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
            formPanel.add(new JLabel("Treatment ID: " + existingTreatment.getTreatmentId()), gbc);
            row++;
            gbc.gridwidth = 1;
        }

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        formPanel.add(lblTreatmentName, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        formPanel.add(txtTreatmentName, gbc);
        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        formPanel.add(lblCost, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        formPanel.add(txtCost, gbc);

        JButton btnSave = new JButton(editing ? "Update" : "Add");
        JButton btnCancel = new JButton("Cancel");

        String title = editing ? "Edit Treatment" : "Add Treatment";

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
                handleEditValidationAndSave(dialog, existingTreatment, txtTreatmentName, txtCost);
            } else {
                handleAddValidationAndSave(dialog, txtTreatmentName, txtCost);
            }
        });

        btnCancel.addActionListener(evt -> dialog.dispose());

        dialog.setVisible(true);
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
            JTextField txtTreatmentName,
            JTextField txtCost) {

        String treatmentName = txtTreatmentName.getText().trim();
        String costText = txtCost.getText().trim();

        String validationMessage = validateTreatmentInput(
                treatmentName,
                costText,
                -1,
                true
        );

        if (validationMessage != null) {
            JOptionPane.showMessageDialog(dialog, validationMessage, "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Double cost = parsePositiveCost(costText, dialog);
        if (cost == null) {
            return;
        }

        Treatment treatment = new Treatment();
        treatment.setTreatmentName(treatmentName);
        treatment.setCost(cost);

        try {
            boolean added = treatmentDAO.addTreatment(treatment);
            if (added) {
                JOptionPane.showMessageDialog(dialog, "Treatment added successfully.", "Add Treatment", JOptionPane.INFORMATION_MESSAGE);
                loadTreatments();
                dialog.dispose();
            } else {
                showError("Failed to add treatment.");
            }
        } catch (RuntimeException e) {
            showError("Unable to add treatment.");
        }
    }

    private void handleEditValidationAndSave(
            JDialog dialog,
            Treatment treatment,
            JTextField txtTreatmentName,
            JTextField txtCost) {

        String treatmentName = txtTreatmentName.getText().trim();
        String costText = txtCost.getText().trim();

        String validationMessage = validateTreatmentInput(
                treatmentName,
                costText,
                treatment.getTreatmentId(),
                false
        );

        if (validationMessage != null) {
            JOptionPane.showMessageDialog(dialog, validationMessage, "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Double cost = parsePositiveCost(costText, dialog);
        if (cost == null) {
            return;
        }

        treatment.setTreatmentName(treatmentName);
        treatment.setCost(cost);

        try {
            boolean updated = treatmentDAO.updateTreatment(treatment);
            if (updated) {
                JOptionPane.showMessageDialog(dialog, "Treatment updated successfully.", "Edit Treatment", JOptionPane.INFORMATION_MESSAGE);
                loadTreatments();
                dialog.dispose();
            } else {
                showError("Failed to update treatment.");
            }
        } catch (RuntimeException e) {
            showError("Unable to update treatment.");
        }
    }

    private String validateTreatmentInput(
            String treatmentName,
            String costText,
            int excludeTreatmentId,
            boolean includeCombinedRequiredMessage) {

        boolean nameEmpty = treatmentName == null || treatmentName.isBlank();
        boolean costEmpty = costText == null || costText.isBlank();

        if (includeCombinedRequiredMessage && nameEmpty && costEmpty) {
            return "Treatment name and cost are required.";
        }

        if (nameEmpty) {
            return "Treatment name is required.";
        }

        if (costEmpty) {
            return "Treatment cost is required.";
        }

        if (isDuplicateTreatmentName(treatmentName, excludeTreatmentId)) {
            return "A treatment with this name already exists.";
        }

        return null;
    }

    private void deleteTreatment() {
        int treatmentId = getSelectedTreatmentId();
        if (treatmentId <= 0) {
            JOptionPane.showMessageDialog(this, "Please select a treatment to delete.", "Delete Treatment", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete the selected treatment?",
                "Delete Treatment",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            boolean deleted = treatmentDAO.deleteTreatment(treatmentId);
            if (deleted) {
                JOptionPane.showMessageDialog(this, "Treatment deleted successfully.", "Delete Treatment", JOptionPane.INFORMATION_MESSAGE);
                loadTreatments();
            } else {
                showError("Failed to delete treatment.");
            }
        } catch (RuntimeException e) {
            String message = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
            if (message.contains("foreign key") || message.contains("constraint") || message.contains("restrict")) {
                JOptionPane.showMessageDialog(
                        this,
                        "Cannot delete this treatment because it is used in appointments or bills.",
                        "Delete Treatment",
                        JOptionPane.WARNING_MESSAGE
                );
            } else {
                showError("Unable to delete treatment.");
            }
        }
    }

    private int getSelectedTreatmentId() {
        int selectedRow = tblTreatments.getSelectedRow();
        if (selectedRow == -1) {
            return -1;
        }

        Object value = tblTreatments.getValueAt(selectedRow, 0);
        if (value instanceof Number number) {
            return number.intValue();
        }

        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private boolean isDuplicateTreatmentName(String treatmentName, int excludeTreatmentId) {
        String normalizedName = valueOrEmpty(treatmentName).trim();
        if (normalizedName.isEmpty()) {
            return false;
        }

        try {
            List<Treatment> treatments = treatmentDAO.getAllTreatments();
            for (Treatment treatment : treatments) {
                if (treatment == null) {
                    continue;
                }

                if (treatment.getTreatmentId() == excludeTreatmentId) {
                    continue;
                }

                String existingName = valueOrEmpty(treatment.getTreatmentName()).trim();
                if (!existingName.isEmpty() && existingName.equalsIgnoreCase(normalizedName)) {
                    return true;
                }
            }
        } catch (RuntimeException e) {
            // Ignore lookup failures here; persistence errors are handled by the DAO call.
        }

        return false;
    }

    private Double parsePositiveCost(String costText, Component parentComponent) {
        if (costText == null || costText.trim().isEmpty()) {
            JOptionPane.showMessageDialog(parentComponent, "Cost is required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        try {
            double cost = Double.parseDouble(costText.trim());
            if (cost <= 0) {
                JOptionPane.showMessageDialog(parentComponent, "Cost must be greater than 0.", "Validation", JOptionPane.WARNING_MESSAGE);
                return null;
            }
            return cost;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(parentComponent, "Cost must be a valid number.", "Validation", JOptionPane.WARNING_MESSAGE);
            return null;
        }
    }

    private String formatCost(double cost) {
        return String.format("Rs. %.2f", cost);
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
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
        scrollPaneTreatments = new javax.swing.JScrollPane();
        tblTreatments = new javax.swing.JTable();
        actionsPanel = new javax.swing.JPanel();
        btnAddTreatment = new javax.swing.JButton();
        btnEditTreatment = new javax.swing.JButton();
        btnDeleteTreatment = new javax.swing.JButton();

        lblTitle.setFont(sunrisedentalclinicsystem.util.UIStyle.TITLE_FONT);
        lblTitle.setForeground(sunrisedentalclinicsystem.util.UIStyle.PRIMARY_TEXT);
        lblTitle.setText("Treatments");

        lblSubtitle.setFont(sunrisedentalclinicsystem.util.UIStyle.LABEL_FONT);
        lblSubtitle.setForeground(sunrisedentalclinicsystem.util.UIStyle.SECONDARY_TEXT);
        lblSubtitle.setText("Manage treatments and pricing");

        searchPanel.setBackground(sunrisedentalclinicsystem.util.UIStyle.CARD_BACKGROUND);

        lblSearch.setFont(sunrisedentalclinicsystem.util.UIStyle.LABEL_FONT);
        lblSearch.setForeground(sunrisedentalclinicsystem.util.UIStyle.PRIMARY_TEXT);
        lblSearch.setText("Search Treatments");

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

        tblTreatments.setFont(sunrisedentalclinicsystem.util.UIStyle.TABLE_FONT);
        tblTreatments.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Treatment ID", "Treatment Name", "Cost"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblTreatments.setRowHeight(24);
        tblTreatments.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        scrollPaneTreatments.setViewportView(tblTreatments);

        javax.swing.GroupLayout tablePanelLayout = new javax.swing.GroupLayout(tablePanel);
        tablePanel.setLayout(tablePanelLayout);
        tablePanelLayout.setHorizontalGroup(
            tablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tablePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollPaneTreatments, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );
        tablePanelLayout.setVerticalGroup(
            tablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tablePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollPaneTreatments, javax.swing.GroupLayout.DEFAULT_SIZE, 384, Short.MAX_VALUE)
                .addContainerGap())
        );

        actionsPanel.setBackground(sunrisedentalclinicsystem.util.UIStyle.CARD_BACKGROUND);

        btnAddTreatment.setBackground(sunrisedentalclinicsystem.util.UIStyle.PRIMARY_BLUE);
        btnAddTreatment.setFont(sunrisedentalclinicsystem.util.UIStyle.BUTTON_FONT);
        btnAddTreatment.setForeground(sunrisedentalclinicsystem.util.UIStyle.CARD_BACKGROUND);
        btnAddTreatment.setText("Add Treatment");
        btnAddTreatment.addActionListener(this::btnAddTreatmentActionPerformed);

        btnEditTreatment.setFont(sunrisedentalclinicsystem.util.UIStyle.BUTTON_FONT);
        btnEditTreatment.setText("Edit Treatment");
        btnEditTreatment.addActionListener(this::btnEditTreatmentActionPerformed);

        btnDeleteTreatment.setFont(sunrisedentalclinicsystem.util.UIStyle.BUTTON_FONT);
        btnDeleteTreatment.setForeground(sunrisedentalclinicsystem.util.UIStyle.DANGER);
        btnDeleteTreatment.setText("Delete Treatment");
        btnDeleteTreatment.addActionListener(this::btnDeleteTreatmentActionPerformed);

        javax.swing.GroupLayout actionsPanelLayout = new javax.swing.GroupLayout(actionsPanel);
        actionsPanel.setLayout(actionsPanelLayout);
        actionsPanelLayout.setHorizontalGroup(
            actionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(actionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnAddTreatment)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnEditTreatment)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnDeleteTreatment)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        actionsPanelLayout.setVerticalGroup(
            actionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(actionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(actionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddTreatment, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEditTreatment, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDeleteTreatment, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
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
        searchTreatments();
    }//GEN-LAST:event_btnSearchActionPerformed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        clearSearch();
    }//GEN-LAST:event_btnClearActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        refreshTreatments();
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void btnAddTreatmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddTreatmentActionPerformed
        addTreatment();
    }//GEN-LAST:event_btnAddTreatmentActionPerformed

    private void btnEditTreatmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditTreatmentActionPerformed
        editTreatment();
    }//GEN-LAST:event_btnEditTreatmentActionPerformed

    private void btnDeleteTreatmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteTreatmentActionPerformed
        deleteTreatment();
    }//GEN-LAST:event_btnDeleteTreatmentActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel actionsPanel;
    private javax.swing.JButton btnAddTreatment;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnDeleteTreatment;
    private javax.swing.JButton btnEditTreatment;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnSearch;
    private javax.swing.JLabel lblSearch;
    private javax.swing.JLabel lblSubtitle;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JScrollPane scrollPaneTreatments;
    private javax.swing.JPanel searchPanel;
    private javax.swing.JPanel tablePanel;
    private javax.swing.JTable tblTreatments;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
