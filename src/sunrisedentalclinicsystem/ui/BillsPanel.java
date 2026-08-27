/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package sunrisedentalclinicsystem.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import sunrisedentalclinicsystem.dao.AppointmentDAO;
import sunrisedentalclinicsystem.dao.BillDAO;
import sunrisedentalclinicsystem.dao.PatientDAO;
import sunrisedentalclinicsystem.dao.TreatmentDAO;
import sunrisedentalclinicsystem.model.Appointment;
import sunrisedentalclinicsystem.model.Bill;
import sunrisedentalclinicsystem.model.Patient;
import sunrisedentalclinicsystem.model.Treatment;
import sunrisedentalclinicsystem.util.AppConstants;
import sunrisedentalclinicsystem.util.UIStyle;

/**
 *
 * @author yasit
 */
public class BillsPanel extends javax.swing.JPanel {

    private final BillDAO billDAO;
    private final AppointmentDAO appointmentDAO;
    private final PatientDAO patientDAO;
    private final TreatmentDAO treatmentDAO;
    private final DefaultTableModel billTableModel;

    /**
     * Creates new form BillsPanel
     */
    public BillsPanel() {
        initComponents();
        billDAO = new BillDAO();
        appointmentDAO = new AppointmentDAO();
        patientDAO = new PatientDAO();
        treatmentDAO = new TreatmentDAO();
        billTableModel = (DefaultTableModel) tblBills.getModel();
        configureTable();
        loadBills();
    }

    private void configureTable() {
        tblBills.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblBills.getTableHeader().setReorderingAllowed(false);
        tblBills.setRowHeight(28);
        tblBills.setShowGrid(true);
        tblBills.setGridColor(new Color(0xD9, 0xE2, 0xEC));
        tblBills.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tblBills.getTableHeader().setFont(UIStyle.TABLE_FONT.deriveFont(Font.BOLD));
        
        tblBills.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
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

        tblBills.getColumnModel().getColumn(0).setPreferredWidth(130);
        tblBills.getColumnModel().getColumn(1).setPreferredWidth(120);
        tblBills.getColumnModel().getColumn(2).setPreferredWidth(150);
        tblBills.getColumnModel().getColumn(3).setPreferredWidth(150);
        tblBills.getColumnModel().getColumn(4).setPreferredWidth(130);
        tblBills.getColumnModel().getColumn(5).setPreferredWidth(130);
        tblBills.getColumnModel().getColumn(6).setPreferredWidth(110);
        tblBills.getColumnModel().getColumn(7).setPreferredWidth(110);
        tblBills.getColumnModel().getColumn(8).setPreferredWidth(120);
        tblBills.getColumnModel().getColumn(9).setPreferredWidth(120);
    }

    private void loadBills() {
        try {
            List<Bill> bills = billDAO.getAllBills();
            displayBills(bills);
        } catch (RuntimeException e) {
            showError("Unable to load bills.");
        }
    }

    private void displayBills(List<Bill> bills) {
        billTableModel.setRowCount(0);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(AppConstants.DATE_FORMAT);

        for (Bill bill : bills) {
            String appointmentNumber = "";
            String patientName = "";
            String treatmentName = "";
            double treatmentCost = 0.0;
            String billDate = "";
                String paymentDate = "-";
                String paymentStatus = bill.getPaymentStatus() == null || bill.getPaymentStatus().isBlank()
                    ? "Pending"
                    : bill.getPaymentStatus();

            try {
                if (bill.getAppointmentId() > 0) {
                    var appointment = appointmentDAO.findAppointment(bill.getAppointmentId());
                    if (appointment != null) {
                        appointmentNumber = appointment.getAppointmentNumber();
                        // Get patient name from patient ID
                        if (appointment.getPatientId() > 0) {
                            var patient = patientDAO.findPatient(appointment.getPatientId());
                            if (patient != null) {
                                patientName = patient.getFirstName() + " " + patient.getLastName();
                            }
                        }
                    }
                }

                if (bill.getTreatmentId() > 0) {
                    Treatment treatment = treatmentDAO.findTreatment(bill.getTreatmentId());
                    if (treatment != null) {
                        treatmentName = treatment.getTreatmentName();
                        treatmentCost = treatment.getCost();
                    }
                }

                if (bill.getCreatedDate() != null) {
                    billDate = bill.getCreatedDate().format(dateFormatter);
                }

                if (bill.getPaymentDate() != null) {
                    paymentDate = bill.getPaymentDate().format(dateFormatter);
                }
            } catch (RuntimeException e) {
                // Use empty defaults if lookup fails
            }

            double consultationFee = AppConstants.CONSULTATION_FEE.doubleValue();
            double total = bill.getTotal();

            billTableModel.addRow(new Object[]{
                bill.getBillNumber(),
                appointmentNumber,
                patientName,
                treatmentName,
                String.format("Rs. %.2f", treatmentCost),
                String.format("Rs. %.2f", consultationFee),
                String.format("Rs. %.2f", total),
                paymentStatus,
                billDate,
                paymentDate
            });
        }
    }

    private void searchBills() {
        try {
            List<Bill> bills = billDAO.searchBills(txtSearch.getText());
            displayBills(bills);
        } catch (RuntimeException e) {
            showError("Unable to search bills.");
        }
    }

    private void clearSearch() {
        txtSearch.setText("");
        loadBills();
    }

    private void refreshBills() {
        loadBills();
    }

    private int getSelectedBillId() {
        int selectedRow = tblBills.getSelectedRow();
        if (selectedRow == -1) {
            return -1;
        }

        try {
            String billNumber = (String) billTableModel.getValueAt(selectedRow, 0);
            Bill bill = billDAO.findBillByNumber(billNumber);
            return bill != null ? bill.getBillId() : -1;
        } catch (RuntimeException e) {
            return -1;
        }
    }

    private void viewBill() {
        Bill bill = getSelectedBill();
        if (bill == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a bill to view.",
                    "View Bill",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JTextArea detailsArea = new JTextArea(createBillDetailsText(bill));
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        detailsArea.setCaretPosition(0);

        JOptionPane.showMessageDialog(
                this,
                new javax.swing.JScrollPane(detailsArea),
                "View Bill",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void printReceipt() {
        Bill bill = getSelectedBill();
        if (bill == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a bill to print receipt.",
                    "Print Receipt",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JTextArea receiptArea = new JTextArea(createBillDetailsText(bill));
        receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        try {
            boolean printed = receiptArea.print();
            JOptionPane.showMessageDialog(this,
                    printed ? "Receipt printed successfully." : "Print cancelled.",
                    "Print Receipt",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            showError("Unable to print receipt: " + ex.getMessage());
        }
    }

    private void deleteBill() {
        // TODO: Implement bill deletion logic
        JOptionPane.showMessageDialog(this, "Delete Bill feature coming soon.", "Delete Bill", JOptionPane.INFORMATION_MESSAGE);
    }

    private void markAsPaid() {
        Bill bill = getSelectedBill();
        if (bill == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a bill first.",
                    "Mark as Paid",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String paymentStatus = bill.getPaymentStatus() == null || bill.getPaymentStatus().isBlank()
                ? "Pending"
                : bill.getPaymentStatus();

        if ("Paid".equalsIgnoreCase(paymentStatus)) {
            JOptionPane.showMessageDialog(this,
                    "This bill is already marked as Paid.",
                    "Mark as Paid",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if ("Cancelled".equalsIgnoreCase(paymentStatus)) {
            JOptionPane.showMessageDialog(this,
                    "Cancelled bills cannot be marked as Paid.",
                    "Mark as Paid",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        bill.setPaymentStatus("Paid");
        bill.setPaymentDate(LocalDate.now());

        if (billDAO.updateBill(bill)) {
            JOptionPane.showMessageDialog(this,
                    "Bill marked as Paid successfully.",
                    "Mark as Paid",
                    JOptionPane.INFORMATION_MESSAGE);
            loadBills();
        } else {
            showError("Failed to update payment status.");
        }
    }

    private Bill getSelectedBill() {
        int selectedBillId = getSelectedBillId();
        if (selectedBillId <= 0) {
            return null;
        }
        return billDAO.findBill(selectedBillId);
    }

    private String createBillDetailsText(Bill bill) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(AppConstants.DATE_FORMAT);
        String appointmentNumber = "N/A";
        String patientName = "N/A";
        String treatmentName = "N/A";
        double treatmentCost = 0.0;
        double consultationFee = AppConstants.CONSULTATION_FEE.doubleValue();
        String billDate = "-";
        String paymentDate = "-";
        String paymentStatus = bill.getPaymentStatus() == null || bill.getPaymentStatus().isBlank()
                ? "Pending"
                : bill.getPaymentStatus();

        try {
            Appointment appointment = appointmentDAO.findAppointment(bill.getAppointmentId());
            if (appointment != null) {
                appointmentNumber = appointment.getAppointmentNumber();
                Patient patient = patientDAO.findPatient(appointment.getPatientId());
                if (patient != null) {
                    patientName = patient.getFirstName() + " " + patient.getLastName();
                }
            }

            Treatment treatment = treatmentDAO.findTreatment(bill.getTreatmentId());
            if (treatment != null) {
                treatmentName = treatment.getTreatmentName();
                treatmentCost = treatment.getCost();
            }

            if (bill.getCreatedDate() != null) {
                billDate = bill.getCreatedDate().format(dateFormatter);
            }

            if (bill.getPaymentDate() != null) {
                paymentDate = bill.getPaymentDate().format(dateFormatter);
            }
        } catch (RuntimeException e) {
            // Keep defaults if lookup fails
        }

        StringBuilder text = new StringBuilder();
        text.append("═══════════════════════════════════════════════════════════════\n");
        text.append("                   SUNRISE DENTAL CLINIC                      \n");
        text.append("                        INVOICE/BILL                          \n");
        text.append("═══════════════════════════════════════════════════════════════\n\n");
        text.append("Bill Number: ").append(bill.getBillNumber()).append("\n");
        text.append("Appointment #: ").append(appointmentNumber).append("\n");
        text.append("Patient: ").append(patientName).append("\n");
        text.append("Treatment: ").append(treatmentName).append("\n");
        text.append("Treatment Cost: Rs. ").append(String.format("%.2f", treatmentCost)).append("\n");
        text.append("Consultation Fee: Rs. ").append(String.format("%.2f", consultationFee)).append("\n");
        text.append("Total: Rs. ").append(String.format("%.2f", bill.getTotal())).append("\n");
        text.append("Status: ").append(paymentStatus).append("\n");
        text.append("Bill Date: ").append(billDate).append("\n");
        text.append("Payment Date: ").append(paymentDate).append("\n");
        text.append("═══════════════════════════════════════════════════════════════\n");

        return text.toString();
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
        scrollPaneBills = new javax.swing.JScrollPane();
        tblBills = new javax.swing.JTable();
        actionsPanel = new javax.swing.JPanel();
        btnMarkAsPaid = new javax.swing.JButton();
        btnViewBill = new javax.swing.JButton();
        btnPrintReceipt = new javax.swing.JButton();
        btnDeleteBill = new javax.swing.JButton();

        lblTitle.setFont(sunrisedentalclinicsystem.util.UIStyle.TITLE_FONT);
        lblTitle.setForeground(sunrisedentalclinicsystem.util.UIStyle.PRIMARY_TEXT);
        lblTitle.setText("Bills");

        lblSubtitle.setFont(sunrisedentalclinicsystem.util.UIStyle.LABEL_FONT);
        lblSubtitle.setForeground(sunrisedentalclinicsystem.util.UIStyle.SECONDARY_TEXT);
        lblSubtitle.setText("View and manage billing records and receipts");

        searchPanel.setBackground(sunrisedentalclinicsystem.util.UIStyle.CARD_BACKGROUND);

        lblSearch.setFont(sunrisedentalclinicsystem.util.UIStyle.LABEL_FONT);
        lblSearch.setForeground(sunrisedentalclinicsystem.util.UIStyle.PRIMARY_TEXT);
        lblSearch.setText("Search Bills");

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

        tblBills.setFont(sunrisedentalclinicsystem.util.UIStyle.TABLE_FONT);
        tblBills.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Bill Number", "Appointment #", "Patient", "Treatment", "Treatment Cost", "Consultation Fee", "Total", "Status", "Bill Date", "Payment Date"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblBills.setRowHeight(24);
        tblBills.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        scrollPaneBills.setViewportView(tblBills);

        javax.swing.GroupLayout tablePanelLayout = new javax.swing.GroupLayout(tablePanel);
        tablePanel.setLayout(tablePanelLayout);
        tablePanelLayout.setHorizontalGroup(
            tablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tablePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollPaneBills)
                .addContainerGap())
        );
        tablePanelLayout.setVerticalGroup(
            tablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tablePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollPaneBills, javax.swing.GroupLayout.DEFAULT_SIZE, 384, Short.MAX_VALUE)
                .addContainerGap())
        );

        actionsPanel.setBackground(sunrisedentalclinicsystem.util.UIStyle.CARD_BACKGROUND);

        btnMarkAsPaid.setBackground(sunrisedentalclinicsystem.util.UIStyle.PRIMARY_BLUE);
        btnMarkAsPaid.setFont(sunrisedentalclinicsystem.util.UIStyle.BUTTON_FONT);
        btnMarkAsPaid.setForeground(sunrisedentalclinicsystem.util.UIStyle.CARD_BACKGROUND);
        btnMarkAsPaid.setText("Mark as Paid");
        btnMarkAsPaid.addActionListener(this::btnMarkAsPaidActionPerformed);

        btnViewBill.setFont(sunrisedentalclinicsystem.util.UIStyle.BUTTON_FONT);
        btnViewBill.setText("View Bill");
        btnViewBill.addActionListener(this::btnViewBillActionPerformed);

        btnPrintReceipt.setFont(sunrisedentalclinicsystem.util.UIStyle.BUTTON_FONT);
        btnPrintReceipt.setText("Print Receipt");
        btnPrintReceipt.addActionListener(this::btnPrintReceiptActionPerformed);

        btnDeleteBill.setFont(sunrisedentalclinicsystem.util.UIStyle.BUTTON_FONT);
        btnDeleteBill.setForeground(sunrisedentalclinicsystem.util.UIStyle.DANGER);
        btnDeleteBill.setText("Delete Bill");
        btnDeleteBill.addActionListener(this::btnDeleteBillActionPerformed);

        javax.swing.GroupLayout actionsPanelLayout = new javax.swing.GroupLayout(actionsPanel);
        actionsPanel.setLayout(actionsPanelLayout);
        actionsPanelLayout.setHorizontalGroup(
            actionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(actionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnMarkAsPaid)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnViewBill)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnPrintReceipt)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnDeleteBill)
                .addContainerGap(55, Short.MAX_VALUE))
        );
        actionsPanelLayout.setVerticalGroup(
            actionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(actionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(actionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnMarkAsPaid, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnViewBill, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnPrintReceipt, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDeleteBill, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
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
        searchBills();
    }//GEN-LAST:event_btnSearchActionPerformed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        clearSearch();
    }//GEN-LAST:event_btnClearActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        refreshBills();
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void btnMarkAsPaidActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMarkAsPaidActionPerformed
        markAsPaid();
    }//GEN-LAST:event_btnMarkAsPaidActionPerformed

    private void btnViewBillActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnViewBillActionPerformed
        viewBill();
    }//GEN-LAST:event_btnViewBillActionPerformed

    private void btnPrintReceiptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrintReceiptActionPerformed
        printReceipt();
    }//GEN-LAST:event_btnPrintReceiptActionPerformed

    private void btnDeleteBillActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteBillActionPerformed
        deleteBill();
    }//GEN-LAST:event_btnDeleteBillActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel actionsPanel;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnDeleteBill;
    private javax.swing.JButton btnMarkAsPaid;
    private javax.swing.JButton btnPrintReceipt;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnViewBill;
    private javax.swing.JLabel lblSearch;
    private javax.swing.JLabel lblSubtitle;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JScrollPane scrollPaneBills;
    private javax.swing.JPanel searchPanel;
    private javax.swing.JPanel tablePanel;
    private javax.swing.JTable tblBills;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
