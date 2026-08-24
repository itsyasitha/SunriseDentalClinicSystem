/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package sunrisedentalclinicsystem.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import sunrisedentalclinicsystem.dao.AppointmentDAO;
import sunrisedentalclinicsystem.dao.AppointmentDAO.AppointmentSummary;
import sunrisedentalclinicsystem.dao.BillDAO;
import sunrisedentalclinicsystem.dao.DentistDAO;
import sunrisedentalclinicsystem.dao.PatientDAO;
import sunrisedentalclinicsystem.dao.TreatmentDAO;
import sunrisedentalclinicsystem.model.Appointment;
import sunrisedentalclinicsystem.model.Bill;
import sunrisedentalclinicsystem.model.Dentist;
import sunrisedentalclinicsystem.model.Patient;
import sunrisedentalclinicsystem.model.Treatment;
import sunrisedentalclinicsystem.util.AppConstants;
import sunrisedentalclinicsystem.util.Session;
import sunrisedentalclinicsystem.util.UIStyle;

/**
 *
 * @author yasit
 */
public class AppointmentsPanel extends javax.swing.JPanel {

    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER =
            DateTimeFormatter.ofPattern(AppConstants.DATE_FORMAT);
    private static final DateTimeFormatter DISPLAY_TIME_FORMATTER =
            DateTimeFormatter.ofPattern(AppConstants.TIME_FORMAT);
    private static final DateTimeFormatter MONTH_YEAR_FORMATTER =
        DateTimeFormatter.ofPattern("MMMM yyyy");
    private static final String NO_TREATMENT_OPTION = "(None - Optional)";
    private static final String NO_SLOT_OPTION = "No available slots";
    private static final String SELECT_SLOT_OPTION = "Select dentist and date first";

    private final AppointmentDAO appointmentDAO;
    private final BillDAO billDAO;
    private final PatientDAO patientDAO;
    private final DentistDAO dentistDAO;
    private final TreatmentDAO treatmentDAO;
    private final DefaultTableModel appointmentTableModel;
    private final List<AppointmentSummary> displayedAppointments = new ArrayList<>();
    private final List<Patient> patients = new ArrayList<>();
    private final List<Dentist> dentists = new ArrayList<>();
    private final List<Treatment> treatments = new ArrayList<>();

    /**
     * Creates new form AppointmentsPanel
     */
    public AppointmentsPanel() {
        initComponents();
        appointmentDAO = new AppointmentDAO();
        billDAO = new BillDAO();
        patientDAO = new PatientDAO();
        dentistDAO = new DentistDAO();
        treatmentDAO = new TreatmentDAO();
        appointmentTableModel = (DefaultTableModel) tblAppointments.getModel();
        configureTable();
        loadReferenceData();
        loadAppointments();
    }

    private void loadReferenceData() {
        try {
            patients.clear();
            patients.addAll(patientDAO.getAllPatients());

            dentists.clear();
            dentists.addAll(dentistDAO.getAllDentists());

            treatments.clear();
            treatments.addAll(treatmentDAO.getAllTreatments());
        } catch (RuntimeException e) {
            showError("Unable to load appointment reference data.");
        }
    }

    private void configureTable() {
        tblAppointments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblAppointments.getTableHeader().setReorderingAllowed(false);
        tblAppointments.setRowHeight(28);
        tblAppointments.setShowGrid(true);
        tblAppointments.setGridColor(new Color(0xD9, 0xE2, 0xEC));
        tblAppointments.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tblAppointments.getTableHeader().setFont(UIStyle.TABLE_FONT.deriveFont(Font.BOLD));
        tblAppointments.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
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

        tblAppointments.getColumnModel().getColumn(0).setPreferredWidth(140);
        tblAppointments.getColumnModel().getColumn(1).setPreferredWidth(170);
        tblAppointments.getColumnModel().getColumn(2).setPreferredWidth(170);
        tblAppointments.getColumnModel().getColumn(3).setPreferredWidth(105);
        tblAppointments.getColumnModel().getColumn(4).setPreferredWidth(100);
        tblAppointments.getColumnModel().getColumn(5).setPreferredWidth(170);
        tblAppointments.getColumnModel().getColumn(6).setPreferredWidth(100);
        tblAppointments.getColumnModel().getColumn(7).setPreferredWidth(260);
    }

    private void loadAppointments() {
        try {
            displayAppointments(filterVisibleAppointments(
                    appointmentDAO.getAllAppointmentSummaries()));
        } catch (RuntimeException e) {
            showError("Unable to load appointments.");
        }
    }

    private void displayAppointments(List<AppointmentSummary> appointments) {
        displayedAppointments.clear();
        displayedAppointments.addAll(appointments);
        appointmentTableModel.setRowCount(0);

        for (AppointmentSummary appointment : appointments) {
            appointmentTableModel.addRow(new Object[]{
                appointment.getAppointmentNumber(),
                appointment.getPatientName(),
                appointment.getDentistName(),
                appointment.getAppointmentDate() == null
                        ? ""
                        : appointment.getAppointmentDate().format(DISPLAY_DATE_FORMATTER),
                appointment.getAppointmentTime() == null
                        ? ""
                        : appointment.getAppointmentTime().format(DISPLAY_TIME_FORMATTER),
                appointment.getTreatmentName(),
                appointment.getStatus(),
                appointment.getNotes() == null ? "" : appointment.getNotes()
            });
        }
    }

    private void searchAppointments() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            loadAppointments();
            return;
        }

        try {
            displayAppointments(filterVisibleAppointments(
                    appointmentDAO.searchAppointmentSummaries(keyword)));
        } catch (RuntimeException e) {
            showError("Unable to search appointments.");
        }
    }

    private List<AppointmentSummary> filterVisibleAppointments(
            List<AppointmentSummary> appointments) {

        if (!isDentistSessionUser()) {
            return appointments;
        }

        int currentDentistId = resolveCurrentSessionDentistId();
        List<AppointmentSummary> filteredAppointments = new ArrayList<>();

        if (currentDentistId <= 0) {
            return filteredAppointments;
        }

        for (AppointmentSummary appointment : appointments) {
            if (appointment.getDentistId() == currentDentistId) {
                filteredAppointments.add(appointment);
            }
        }

        return filteredAppointments;
    }

    private boolean isDentistSessionUser() {
        return Session.getCurrentUser() != null
                && "Dentist".equalsIgnoreCase(
                        valueOrEmpty(Session.getCurrentUser().getRole()));
    }

    private int resolveCurrentSessionDentistId() {
        if (Session.getCurrentUser() == null) {
            return 0;
        }

        int currentUserId = Session.getCurrentUser().getUserId();
        for (Dentist dentist : dentists) {
            if (dentist.getUserId() == currentUserId) {
                return dentist.getDentistId();
            }
        }

        return -1;
    }

    private void clearSearch() {
        txtSearch.setText("");
        loadAppointments();
    }

    private AppointmentSummary getSelectedAppointmentSummary() {
        int selectedRow = tblAppointments.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= displayedAppointments.size()) {
            return null;
        }

        return displayedAppointments.get(selectedRow);
    }

    private Appointment findAppointmentById(int appointmentId) {
        return appointmentDAO.findAppointment(appointmentId);
    }

    private void scheduleAppointment() {
        try {
            loadReferenceData();
            Appointment appointment = showAppointmentDialog(null);
            if (appointment == null) {
                return;
            }

            if (appointment.getAppointmentNumber() == null
                    || appointment.getAppointmentNumber().isBlank()) {
                appointment.setAppointmentNumber(appointmentDAO.generateAppointmentNumber());
            }

            validateAppointmentBeforeSave(appointment, 0);

            if (appointmentDAO.addAppointment(appointment)) {
                JOptionPane.showMessageDialog(this, "Appointment scheduled successfully.");
                loadAppointments();
            } else {
                showError("Failed to schedule appointment.");
            }
        } catch (RuntimeException e) {
            showFriendlyAppointmentError(e, false);
        }
    }

    private void editAppointment() {
        AppointmentSummary summary = getSelectedAppointmentSummary();
        if (summary == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select an appointment to edit.",
                    "Edit Appointment",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Appointment appointment = findAppointmentById(summary.getAppointmentId());
        if (appointment == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Selected appointment was not found.",
                    "Edit Appointment",
                    JOptionPane.WARNING_MESSAGE);
            loadAppointments();
            return;
        }

        try {
            loadReferenceData();
            Appointment updated = showAppointmentDialog(appointment);
            if (updated == null) {
                return;
            }

            updated.setAppointmentId(appointment.getAppointmentId());
            updated.setAppointmentNumber(appointment.getAppointmentNumber());

            validateAppointmentBeforeSave(updated, updated.getAppointmentId());

            if (appointmentDAO.updateAppointment(updated)) {
                JOptionPane.showMessageDialog(this, "Appointment updated successfully.");
                loadAppointments();
            } else {
                showError("Failed to update appointment.");
            }
        } catch (RuntimeException e) {
            showFriendlyAppointmentError(e, true);
        }
    }

    private Appointment showAppointmentDialog(Appointment sourceAppointment) {
        boolean editing = sourceAppointment != null;
        Appointment workingAppointment = editing ? copyAppointment(sourceAppointment) : new Appointment();
        LocalDate initialDate = workingAppointment.getAppointmentDate() != null
                ? workingAppointment.getAppointmentDate()
                : LocalDate.now();
        final LocalDate[] selectedDate = {initialDate};
        final List<LocalTime> visibleTimeSlots = new ArrayList<>();
        final Set<LocalTime> bookedTimeSlots = new LinkedHashSet<>();
        final boolean[] adjustingTimeCombo = {false};
        final int[] lastValidTimeSelection = {-1};

        javax.swing.JTextField txtAppointmentNumberDialog = new javax.swing.JTextField();
        javax.swing.JComboBox<Object> cmbPatientDialog = new javax.swing.JComboBox<>();
        javax.swing.JComboBox<Object> cmbDentistDialog = new javax.swing.JComboBox<>();
        javax.swing.JTextField txtDateDialog = new javax.swing.JTextField();
        javax.swing.JButton btnChooseDateDialog = new javax.swing.JButton("Choose Date...");
        javax.swing.JComboBox<String> cmbTimeDialog = new javax.swing.JComboBox<>();
        javax.swing.JLabel lblTimeHelpDialog = new javax.swing.JLabel(" ");
        javax.swing.JComboBox<Object> cmbTreatmentDialog = new javax.swing.JComboBox<>();
        javax.swing.JComboBox<String> cmbStatusDialog = new javax.swing.JComboBox<>();
        javax.swing.JTextArea txtNotesDialog = new javax.swing.JTextArea(4, 20);

        txtAppointmentNumberDialog.setFont(UIStyle.LABEL_FONT);
        txtAppointmentNumberDialog.setEditable(false);
        txtAppointmentNumberDialog.setBackground(Color.WHITE);
        txtAppointmentNumberDialog.setFocusable(false);
        txtAppointmentNumberDialog.setRequestFocusEnabled(false);
        txtAppointmentNumberDialog.setText(editing
                ? valueOrEmpty(workingAppointment.getAppointmentNumber())
                : appointmentDAO.generateAppointmentNumber());

        txtDateDialog.setFont(UIStyle.LABEL_FONT);
        txtDateDialog.setEditable(false);
        txtDateDialog.setBackground(Color.WHITE);
        txtDateDialog.setFocusable(false);
        txtDateDialog.setRequestFocusEnabled(false);
        txtDateDialog.setText(selectedDate[0].format(DISPLAY_DATE_FORMATTER));

        btnChooseDateDialog.setFont(UIStyle.BUTTON_FONT);
        btnChooseDateDialog.setBackground(UIStyle.PRIMARY_BLUE);
        btnChooseDateDialog.setForeground(Color.WHITE);

        cmbPatientDialog.setFont(UIStyle.LABEL_FONT);
        cmbDentistDialog.setFont(UIStyle.LABEL_FONT);
        cmbTreatmentDialog.setFont(UIStyle.LABEL_FONT);
        cmbTimeDialog.setFont(UIStyle.LABEL_FONT);
        cmbStatusDialog.setFont(UIStyle.LABEL_FONT);
        lblTimeHelpDialog.setFont(UIStyle.LABEL_FONT);
        lblTimeHelpDialog.setForeground(UIStyle.SECONDARY_TEXT);
        txtNotesDialog.setFont(UIStyle.LABEL_FONT);
        txtNotesDialog.setLineWrap(true);
        txtNotesDialog.setWrapStyleWord(true);

        cmbPatientDialog.setEditable(false);
        cmbDentistDialog.setEditable(false);
        cmbTreatmentDialog.setEditable(false);
        cmbTimeDialog.setEditable(false);

        cmbPatientDialog.setRenderer(createEntityRenderer());
        cmbDentistDialog.setRenderer(createEntityRenderer());
        cmbTreatmentDialog.setRenderer(createEntityRenderer());
        cmbTimeDialog.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    javax.swing.JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {

                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);

                if (!isSelected) {
                    if (index >= 0 && index < visibleTimeSlots.size()) {
                        label.setForeground(bookedTimeSlots.contains(visibleTimeSlots.get(index))
                                ? UIStyle.DANGER
                                : UIStyle.PRIMARY_TEXT);
                    } else {
                        label.setForeground(UIStyle.SECONDARY_TEXT);
                    }
                }

                return label;
            }
        });

        populatePatientCombo(cmbPatientDialog);
        populateDentistCombo(cmbDentistDialog);
        populateTreatmentCombo(cmbTreatmentDialog);
        populateStatusCombo(cmbStatusDialog);

        installComboSearch(cmbPatientDialog);
        installComboSearch(cmbDentistDialog);
        installComboSearch(cmbTreatmentDialog);

        if (editing) {
            setSelectedPatient(cmbPatientDialog, findPatientById(workingAppointment.getPatientId()));
            setSelectedDentist(cmbDentistDialog, findDentistById(workingAppointment.getDentistId()));
            setSelectedTreatment(cmbTreatmentDialog, findTreatmentById(workingAppointment.getTreatmentId()));
            cmbStatusDialog.setSelectedItem(valueOrDefault(workingAppointment.getStatus(), "Scheduled"));
            txtNotesDialog.setText(valueOrEmpty(workingAppointment.getNotes()));
        } else {
            if (cmbPatientDialog.getItemCount() > 0) {
                cmbPatientDialog.setSelectedIndex(0);
            }
            if (cmbDentistDialog.getItemCount() > 0) {
                cmbDentistDialog.setSelectedIndex(0);
            }
            cmbTreatmentDialog.setSelectedItem(NO_TREATMENT_OPTION);
            cmbStatusDialog.setSelectedItem("Scheduled");
        }

        Runnable refreshTimeSlots = () -> {
            visibleTimeSlots.clear();
            bookedTimeSlots.clear();
            cmbTimeDialog.removeAllItems();

            Dentist dentist = resolveSelectedDentist(cmbDentistDialog);
            if (dentist == null || selectedDate[0] == null) {
                cmbTimeDialog.addItem(SELECT_SLOT_OPTION);
                cmbTimeDialog.setEnabled(false);
                lblTimeHelpDialog.setText("Choose a dentist and date to see available time slots.");
                lastValidTimeSelection[0] = -1;
                return;
            }

            try {
                bookedTimeSlots.addAll(appointmentDAO.getBookedDentistSlots(
                        dentist.getDentistId(),
                        selectedDate[0],
                        editing ? workingAppointment.getAppointmentId() : 0));

                LocalTime current = AppConstants.APPOINTMENT_DAY_START;
                adjustingTimeCombo[0] = true;
                while (!current.isAfter(AppConstants.APPOINTMENT_DAY_END)) {
                    visibleTimeSlots.add(current);
                    cmbTimeDialog.addItem(formatTimeSlotDisplay(current, bookedTimeSlots.contains(current)));
                    current = current.plusMinutes(AppConstants.APPOINTMENT_SLOT_MINUTES);
                }

                cmbTimeDialog.setEnabled(true);
                selectTimeSlot(
                        cmbTimeDialog,
                        visibleTimeSlots,
                        bookedTimeSlots,
                        editing ? workingAppointment.getAppointmentTime() : null,
                        lastValidTimeSelection);

                if (lastValidTimeSelection[0] >= 0) {
                    lblTimeHelpDialog.setText("All time slots are shown. Booked slots cannot be selected.");
                } else {
                    lblTimeHelpDialog.setText("No available time slots for the selected dentist and date.");
                }
            } catch (RuntimeException e) {
                cmbTimeDialog.removeAllItems();
                cmbTimeDialog.addItem("Unable to load time slots");
                cmbTimeDialog.setEnabled(false);
                lblTimeHelpDialog.setText("Unable to load time slots right now.");
                throw e;
            } finally {
                adjustingTimeCombo[0] = false;
            }
        };

        cmbTimeDialog.addActionListener(evt -> {
            if (adjustingTimeCombo[0]) {
                return;
            }

            int selectedIndex = cmbTimeDialog.getSelectedIndex();
            if (selectedIndex < 0 || selectedIndex >= visibleTimeSlots.size()) {
                return;
            }

            LocalTime selectedTime = visibleTimeSlots.get(selectedIndex);
            if (bookedTimeSlots.contains(selectedTime)) {
                adjustingTimeCombo[0] = true;
                if (lastValidTimeSelection[0] >= 0 && lastValidTimeSelection[0] < cmbTimeDialog.getItemCount()) {
                    cmbTimeDialog.setSelectedIndex(lastValidTimeSelection[0]);
                } else {
                    cmbTimeDialog.setSelectedIndex(-1);
                }
                adjustingTimeCombo[0] = false;

                JOptionPane.showMessageDialog(
                        this,
                        "The selected time slot is already booked. Please choose an available slot.",
                        editing ? "Edit Appointment" : "Schedule Appointment",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            lastValidTimeSelection[0] = selectedIndex;
        });

        cmbDentistDialog.addActionListener(evt -> {
            try {
                refreshTimeSlots.run();
            } catch (RuntimeException e) {
                JOptionPane.showMessageDialog(
                        this,
                        "Unable to load time slots.",
                        editing ? "Edit Appointment" : "Schedule Appointment",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        btnChooseDateDialog.addActionListener(evt -> {
            LocalDate allowedPastException = editing
                    && workingAppointment.getAppointmentDate() != null
                    && workingAppointment.getAppointmentDate().isBefore(LocalDate.now())
                            ? workingAppointment.getAppointmentDate()
                            : null;

            LocalDate pickedDate = showDatePickerDialog(selectedDate[0], allowedPastException);
            if (pickedDate != null) {
                selectedDate[0] = pickedDate;
                txtDateDialog.setText(selectedDate[0].format(DISPLAY_DATE_FORMATTER));
                try {
                    refreshTimeSlots.run();
                } catch (RuntimeException e) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Unable to load time slots.",
                            editing ? "Edit Appointment" : "Schedule Appointment",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        try {
            refreshTimeSlots.run();
        } catch (RuntimeException e) {
            return null;
        }

        if (!editing) {
            cmbStatusDialog.setVisible(false);
        }

        JPanel formPanel = new JPanel();
        formPanel.setBackground(UIStyle.CARD_BACKGROUND);
        javax.swing.GroupLayout dialogLayout = new javax.swing.GroupLayout(formPanel);
        formPanel.setLayout(dialogLayout);

        JLabel lblAppointmentNumberDialog = createDialogLabel("Appointment Number");
        JLabel lblPatientDialog = createDialogLabel("Patient");
        JLabel lblDentistDialog = createDialogLabel("Dentist");
        JLabel lblDateDialog = createDialogLabel("Appointment Date");
        JLabel lblTimeDialog = createDialogLabel("Appointment Time");
        JLabel lblTreatmentDialog = createDialogLabel("Treatment");
        JLabel lblStatusDialog = createDialogLabel("Status");
        JLabel lblNotesDialog = createDialogLabel("Notes");

        lblStatusDialog.setVisible(editing);

        javax.swing.JScrollPane notesScrollPane = new javax.swing.JScrollPane(txtNotesDialog);

        dialogLayout.setHorizontalGroup(
            dialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(dialogLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(dialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(lblAppointmentNumberDialog)
                        .addComponent(txtAppointmentNumberDialog)
                        .addComponent(lblPatientDialog)
                        .addComponent(cmbPatientDialog, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblDentistDialog)
                        .addComponent(cmbDentistDialog, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblDateDialog)
                        .addGroup(dialogLayout.createSequentialGroup()
                            .addComponent(txtDateDialog)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(btnChooseDateDialog))
                        .addComponent(lblTimeDialog)
                        .addComponent(cmbTimeDialog, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblTimeHelpDialog, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblTreatmentDialog)
                        .addComponent(cmbTreatmentDialog, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblStatusDialog)
                        .addComponent(cmbStatusDialog, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblNotesDialog)
                        .addComponent(notesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 520, Short.MAX_VALUE))
                    .addContainerGap())
        );

        dialogLayout.setVerticalGroup(
            dialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(dialogLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(lblAppointmentNumberDialog)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(txtAppointmentNumberDialog, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(lblPatientDialog)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(cmbPatientDialog, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(lblDentistDialog)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(cmbDentistDialog, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(lblDateDialog)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(dialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtDateDialog, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnChooseDateDialog))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(lblTimeDialog)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(cmbTimeDialog, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(lblTimeHelpDialog)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(lblTreatmentDialog)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(cmbTreatmentDialog, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(lblStatusDialog)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(cmbStatusDialog, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(lblNotesDialog)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(notesScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        while (true) {
            int option = JOptionPane.showConfirmDialog(
                    this,
                    formPanel,
                    editing ? "Edit Appointment" : "Schedule Appointment",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);

            if (option != JOptionPane.OK_OPTION) {
                return null;
            }

            try {
                Patient patient = resolveSelectedPatient(cmbPatientDialog);
                Dentist dentist = resolveSelectedDentist(cmbDentistDialog);
                Treatment treatment = resolveSelectedTreatment(cmbTreatmentDialog);
                LocalTime appointmentTime = resolveSelectedTime(cmbTimeDialog, visibleTimeSlots, bookedTimeSlots);

                if (patient == null) {
                    throw new IllegalArgumentException("Please select a patient.");
                }
                if (dentist == null) {
                    throw new IllegalArgumentException("Please select a dentist.");
                }
                if (selectedDate[0] == null) {
                    throw new IllegalArgumentException("Please choose an appointment date.");
                }
                if (selectedDate[0].isBefore(LocalDate.now())
                        && !(editing
                        && sourceAppointment.getAppointmentDate() != null
                        && selectedDate[0].equals(sourceAppointment.getAppointmentDate()))) {
                    throw new IllegalArgumentException("Appointment date cannot be in the past.");
                }
                if (appointmentTime == null) {
                    throw new IllegalArgumentException("Please select an available time slot.");
                }

                workingAppointment.setAppointmentNumber(txtAppointmentNumberDialog.getText().trim());
                workingAppointment.setPatientId(patient.getPatientId());
                workingAppointment.setDentistId(dentist.getDentistId());
                workingAppointment.setAppointmentDate(selectedDate[0]);
                workingAppointment.setAppointmentTime(appointmentTime);
                workingAppointment.setTreatmentId(treatment == null ? 0 : treatment.getTreatmentId());
                workingAppointment.setStatus(editing
                        ? String.valueOf(cmbStatusDialog.getSelectedItem())
                        : "Scheduled");

                String notes = txtNotesDialog.getText() == null ? "" : txtNotesDialog.getText().trim();
                workingAppointment.setNotes(notes.isEmpty() ? null : notes);

                return workingAppointment;
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        ex.getMessage(),
                        editing ? "Edit Appointment" : "Schedule Appointment",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private JLabel createDialogLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIStyle.LABEL_FONT);
        label.setForeground(UIStyle.PRIMARY_TEXT);
        return label;
    }

    private void populatePatientCombo(javax.swing.JComboBox<Object> comboBox) {
        DefaultComboBoxModel<Object> model = new DefaultComboBoxModel<>();
        for (Patient patient : patients) {
            model.addElement(patient);
        }
        comboBox.setModel(model);
    }

    private void populateDentistCombo(javax.swing.JComboBox<Object> comboBox) {
        DefaultComboBoxModel<Object> model = new DefaultComboBoxModel<>();
        for (Dentist dentist : dentists) {
            model.addElement(dentist);
        }
        comboBox.setModel(model);
    }

    private void populateTreatmentCombo(javax.swing.JComboBox<Object> comboBox) {
        DefaultComboBoxModel<Object> model = new DefaultComboBoxModel<>();
        model.addElement(NO_TREATMENT_OPTION);
        for (Treatment treatment : treatments) {
            model.addElement(treatment);
        }
        comboBox.setModel(model);
    }

    private void populateStatusCombo(javax.swing.JComboBox<String> comboBox) {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        model.addElement("Scheduled");
        model.addElement("Completed");
        model.addElement("Cancelled");
        model.addElement("No-Show");
        comboBox.setModel(model);
    }

    private DefaultListCellRenderer createEntityRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    javax.swing.JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {

                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                label.setText(getDisplayText(value));
                return label;
            }
        };
    }

    private void installComboSearch(javax.swing.JComboBox<Object> comboBox) {
        final StringBuilder searchBuffer = new StringBuilder();
        final long[] lastTypedAt = {0L};

        comboBox.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char keyChar = e.getKeyChar();
                if (Character.isISOControl(keyChar)) {
                    return;
                }

                long now = System.currentTimeMillis();
                if (now - lastTypedAt[0] > 1200L) {
                    searchBuffer.setLength(0);
                }
                lastTypedAt[0] = now;

                searchBuffer.append(Character.toLowerCase(keyChar));
                String query = searchBuffer.toString().trim();
                if (query.isEmpty()) {
                    return;
                }

                for (int i = 0; i < comboBox.getItemCount(); i++) {
                    String display = getDisplayText(comboBox.getItemAt(i)).toLowerCase();
                    if (display.startsWith(query) || display.contains(query)) {
                        comboBox.setSelectedIndex(i);
                        break;
                    }
                }
            }
        });
    }

    private void selectTimeSlot(
            javax.swing.JComboBox<String> comboBox,
            List<LocalTime> timeSlots,
            Set<LocalTime> bookedTimeSlots,
            LocalTime desiredTime,
            int[] lastValidTimeSelection) {

        if (desiredTime != null) {
            for (int i = 0; i < timeSlots.size(); i++) {
                if (timeSlots.get(i).equals(desiredTime) && !bookedTimeSlots.contains(desiredTime)) {
                    comboBox.setSelectedIndex(i);
                    lastValidTimeSelection[0] = i;
                    return;
                }
            }
        }

        for (int i = 0; i < timeSlots.size(); i++) {
            if (!bookedTimeSlots.contains(timeSlots.get(i))) {
                comboBox.setSelectedIndex(i);
                lastValidTimeSelection[0] = i;
                return;
            }
        }

        comboBox.setSelectedIndex(-1);
        lastValidTimeSelection[0] = -1;
    }

    private String formatTimeSlotDisplay(LocalTime timeSlot, boolean booked) {
        return timeSlot.format(DISPLAY_TIME_FORMATTER) + (booked ? " — Booked" : " — Available");
    }

    private LocalDate showDatePickerDialog(LocalDate initialDate, LocalDate allowedPastException) {
        LocalDate baseDate = initialDate == null ? LocalDate.now() : initialDate;
        if (baseDate.isBefore(LocalDate.now())
                && (allowedPastException == null || !baseDate.equals(allowedPastException))) {
            baseDate = LocalDate.now();
        }

        final LocalDate[] chosenDate = {baseDate};
        final YearMonth[] visibleMonth = {YearMonth.from(baseDate)};
        final boolean[] confirmed = {false};

        JDialog calendarDialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Select Appointment Date", Dialog.ModalityType.APPLICATION_MODAL);
        JPanel rootPanel = new JPanel(new BorderLayout(8, 8));
        rootPanel.setBackground(UIStyle.CARD_BACKGROUND);
        rootPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JButton btnPreviousMonth = new JButton("<");
        JButton btnNextMonth = new JButton(">");
        JLabel lblMonthTitle = new JLabel("", SwingConstants.CENTER);
        lblMonthTitle.setFont(UIStyle.HEADER_FONT);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIStyle.CARD_BACKGROUND);
        headerPanel.add(btnPreviousMonth, BorderLayout.WEST);
        headerPanel.add(lblMonthTitle, BorderLayout.CENTER);
        headerPanel.add(btnNextMonth, BorderLayout.EAST);

        JPanel weekDaysPanel = new JPanel(new GridLayout(1, 7, 4, 4));
        weekDaysPanel.setBackground(UIStyle.CARD_BACKGROUND);
        for (String day : new String[]{"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"}) {
            JLabel label = new JLabel(day, SwingConstants.CENTER);
            label.setFont(UIStyle.LABEL_FONT);
            weekDaysPanel.add(label);
        }

        JPanel calendarGrid = new JPanel(new GridLayout(0, 7, 4, 4));
        calendarGrid.setBackground(UIStyle.CARD_BACKGROUND);
        JButton btnClose = new JButton("Cancel");
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setBackground(UIStyle.CARD_BACKGROUND);
        footerPanel.add(btnClose);

        Runnable[] refreshCalendar = new Runnable[1];
        refreshCalendar[0] = () -> {
            calendarGrid.removeAll();
            lblMonthTitle.setText(visibleMonth[0].format(MONTH_YEAR_FORMATTER));

            LocalDate firstOfMonth = visibleMonth[0].atDay(1);
            int startOffset = firstOfMonth.getDayOfWeek().getValue() % 7;
            for (int i = 0; i < startOffset; i++) {
                calendarGrid.add(new JLabel(""));
            }

            int lengthOfMonth = visibleMonth[0].lengthOfMonth();
            for (int day = 1; day <= lengthOfMonth; day++) {
                LocalDate date = visibleMonth[0].atDay(day);
                JButton button = new JButton(String.valueOf(day));
                button.setEnabled(isSelectableDate(date, allowedPastException));
                if (date.equals(chosenDate[0])) {
                    button.setBackground(UIStyle.PRIMARY_BLUE);
                    button.setForeground(Color.WHITE);
                }
                button.addActionListener(e -> {
                    chosenDate[0] = date;
                    confirmed[0] = true;
                    calendarDialog.dispose();
                });
                calendarGrid.add(button);
            }

            calendarGrid.revalidate();
            calendarGrid.repaint();
        };

        btnPreviousMonth.addActionListener(e -> {
            visibleMonth[0] = visibleMonth[0].minusMonths(1);
            refreshCalendar[0].run();
        });
        btnNextMonth.addActionListener(e -> {
            visibleMonth[0] = visibleMonth[0].plusMonths(1);
            refreshCalendar[0].run();
        });
        btnClose.addActionListener(e -> calendarDialog.dispose());

        refreshCalendar[0].run();

        JPanel centerPanel = new JPanel(new BorderLayout(0, 8));
        centerPanel.setBackground(UIStyle.CARD_BACKGROUND);
        centerPanel.add(weekDaysPanel, BorderLayout.NORTH);
        centerPanel.add(calendarGrid, BorderLayout.CENTER);

        rootPanel.add(headerPanel, BorderLayout.NORTH);
        rootPanel.add(centerPanel, BorderLayout.CENTER);
        rootPanel.add(footerPanel, BorderLayout.SOUTH);

        calendarDialog.getContentPane().add(rootPanel);
        calendarDialog.pack();
        calendarDialog.setLocationRelativeTo(this);
        calendarDialog.setVisible(true);

        return confirmed[0] ? chosenDate[0] : null;
    }

    private boolean isSelectableDate(LocalDate date, LocalDate allowedPastException) {
        if (date.isBefore(LocalDate.now())) {
            return allowedPastException != null && date.equals(allowedPastException);
        }
        return true;
    }

    private Patient resolveSelectedPatient(javax.swing.JComboBox<Object> comboBox) {
        Object selected = comboBox.getSelectedItem();
        if (selected instanceof Patient patient) {
            return patient;
        }
        return null;
    }

    private Dentist resolveSelectedDentist(javax.swing.JComboBox<Object> comboBox) {
        Object selected = comboBox.getSelectedItem();
        if (selected instanceof Dentist dentist) {
            return dentist;
        }
        return null;
    }

    private Treatment resolveSelectedTreatment(javax.swing.JComboBox<Object> comboBox) {
        Object selected = comboBox.getSelectedItem();
        if (selected instanceof Treatment treatment) {
            return treatment;
        }

        if (selected == null || NO_TREATMENT_OPTION.equals(selected)) {
            return null;
        }

        return null;
    }

    private LocalTime resolveSelectedTime(
            javax.swing.JComboBox<String> comboBox,
            List<LocalTime> visibleTimeSlots,
            Set<LocalTime> bookedTimeSlots) {

        int selectedIndex = comboBox.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= visibleTimeSlots.size()) {
            return null;
        }

        LocalTime selectedTime = visibleTimeSlots.get(selectedIndex);
        if (bookedTimeSlots.contains(selectedTime)) {
            return null;
        }

        return selectedTime;
    }

    private void setSelectedPatient(javax.swing.JComboBox<Object> comboBox, Patient patient) {
        if (patient != null) {
            comboBox.setSelectedItem(patient);
        }
    }

    private void setSelectedDentist(javax.swing.JComboBox<Object> comboBox, Dentist dentist) {
        if (dentist != null) {
            comboBox.setSelectedItem(dentist);
        }
    }

    private void setSelectedTreatment(javax.swing.JComboBox<Object> comboBox, Treatment treatment) {
        if (treatment != null) {
            comboBox.setSelectedItem(treatment);
        } else {
            comboBox.setSelectedItem(NO_TREATMENT_OPTION);
        }
    }

    private Patient findPatientById(int patientId) {
        for (Patient patient : patients) {
            if (patient.getPatientId() == patientId) {
                return patient;
            }
        }
        return null;
    }

    private Dentist findDentistById(int dentistId) {
        for (Dentist dentist : dentists) {
            if (dentist.getDentistId() == dentistId) {
                return dentist;
            }
        }
        return null;
    }

    private Treatment findTreatmentById(int treatmentId) {
        for (Treatment treatment : treatments) {
            if (treatment.getTreatmentId() == treatmentId) {
                return treatment;
            }
        }
        return null;
    }

    private String getDisplayText(Object value) {
        if (value instanceof Patient patient) {
            return getPatientDisplay(patient);
        }
        if (value instanceof Dentist dentist) {
            return getDentistDisplay(dentist);
        }
        if (value instanceof Treatment treatment) {
            return getTreatmentDisplay(treatment);
        }
        return value == null ? "" : String.valueOf(value);
    }

    private String getPatientDisplay(Patient patient) {
        return (valueOrEmpty(patient.getFirstName()) + " " + valueOrEmpty(patient.getLastName())).trim();
    }

    private String getDentistDisplay(Dentist dentist) {
        return valueOrDefault(dentist.getFullName(), "Dentist");
    }

    private String getTreatmentDisplay(Treatment treatment) {
        return valueOrDefault(treatment.getTreatmentName(), "");
    }

    private Appointment copyAppointment(Appointment appointment) {
        Appointment copy = new Appointment();
        copy.setAppointmentId(appointment.getAppointmentId());
        copy.setAppointmentNumber(appointment.getAppointmentNumber());
        copy.setPatientId(appointment.getPatientId());
        copy.setDentistId(appointment.getDentistId());
        copy.setAppointmentDate(appointment.getAppointmentDate());
        copy.setAppointmentTime(appointment.getAppointmentTime());
        copy.setTreatmentId(appointment.getTreatmentId());
        copy.setStatus(appointment.getStatus());
        copy.setNotes(appointment.getNotes());
        copy.setCreatedDate(appointment.getCreatedDate());
        return copy;
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private void cancelAppointment() {
        AppointmentSummary summary = getSelectedAppointmentSummary();
        if (summary == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select an appointment to cancel.",
                    "Cancel Appointment",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Appointment appointment = findAppointmentById(summary.getAppointmentId());
        if (appointment == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Selected appointment was not found.",
                    "Cancel Appointment",
                    JOptionPane.WARNING_MESSAGE);
            loadAppointments();
            return;
        }

        if ("Cancelled".equalsIgnoreCase(appointment.getStatus())) {
            JOptionPane.showMessageDialog(
                    this,
                    "This appointment is already cancelled.",
                    "Appointment Already Cancelled",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to cancel this appointment?",
                "Cancel Appointment",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        appointment.setStatus("Cancelled");

        try {
            if (appointmentDAO.updateAppointment(appointment)) {
                JOptionPane.showMessageDialog(this, "Appointment cancelled successfully.");
                loadAppointments();
            } else {
                showError("Failed to cancel appointment.");
            }
        } catch (RuntimeException e) {
            showError("Unable to cancel appointment.");
        }
    }

    private void validateAppointmentBeforeSave(Appointment appointment, int appointmentId) {
        if (appointment.getPatientId() <= 0) {
            throw new IllegalArgumentException("Please select a patient.");
        }
        if (appointment.getDentistId() <= 0) {
            throw new IllegalArgumentException("Please select a dentist.");
        }
        if (appointment.getAppointmentDate() == null) {
            throw new IllegalArgumentException("Please choose an appointment date.");
        }
        if (appointment.getAppointmentDate().isBefore(LocalDate.now())) {
            if (appointmentId <= 0) {
                throw new IllegalArgumentException("Appointment date cannot be in the past.");
            }

            Appointment currentRecord = appointmentDAO.findAppointment(appointmentId);
            if (currentRecord == null
                    || currentRecord.getAppointmentDate() == null
                    || !currentRecord.getAppointmentDate().equals(appointment.getAppointmentDate())) {
                throw new IllegalArgumentException("Appointment date cannot be in the past.");
            }
        }
        if (appointment.getAppointmentTime() == null) {
            throw new IllegalArgumentException("Please choose an appointment time.");
        }

        if (appointmentDAO.hasPatientConflict(
                appointment.getPatientId(),
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime(),
                appointmentId)) {
            throw new IllegalArgumentException(
                    "This patient already has another appointment at this date and time.");
        }

        if (appointmentDAO.hasDentistConflict(
                appointment.getDentistId(),
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime(),
                appointmentId)) {
            throw new IllegalArgumentException(
                    "This appointment slot is already booked by the selected dentist.");
        }
    }

    private void showFriendlyAppointmentError(RuntimeException e, boolean editing) {
        String message = e.getMessage() == null ? "" : e.getMessage();

        if (message.contains("patient already has")) {
            JOptionPane.showMessageDialog(this, message, "Patient Conflict", JOptionPane.WARNING_MESSAGE);
        } else if (message.contains("appointment slot is already booked")) {
            JOptionPane.showMessageDialog(this, message, "Dentist Conflict", JOptionPane.WARNING_MESSAGE);
        } else if (message.contains("past")) {
            JOptionPane.showMessageDialog(this, message, "Invalid Date", JOptionPane.WARNING_MESSAGE);
        } else if (message.contains("choose") || message.contains("select")) {
            JOptionPane.showMessageDialog(
                    this,
                    message,
                    editing ? "Edit Appointment" : "Schedule Appointment",
                    JOptionPane.WARNING_MESSAGE);
        } else if (message.contains("duplicate") || message.contains("unique")) {
            JOptionPane.showMessageDialog(
                    this,
                    "This appointment slot is already booked.",
                    "Appointment Conflict",
                    JOptionPane.WARNING_MESSAGE);
        } else {
            showError(editing ? "Unable to update appointment." : "Unable to schedule appointment.");
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
        scrollPaneAppointments = new javax.swing.JScrollPane();
        tblAppointments = new javax.swing.JTable();
        actionsPanel = new javax.swing.JPanel();
        btnScheduleAppointment = new javax.swing.JButton();
        btnEditAppointment = new javax.swing.JButton();
        btnCancelAppointment = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();
        btnGenerateBill = new javax.swing.JButton();

        lblTitle.setFont(sunrisedentalclinicsystem.util.UIStyle.TITLE_FONT);
        lblTitle.setForeground(sunrisedentalclinicsystem.util.UIStyle.PRIMARY_TEXT);
        lblTitle.setText("Appointments");

        lblSubtitle.setFont(sunrisedentalclinicsystem.util.UIStyle.LABEL_FONT);
        lblSubtitle.setForeground(sunrisedentalclinicsystem.util.UIStyle.SECONDARY_TEXT);
        lblSubtitle.setText("Schedule and manage patient appointments");

        searchPanel.setBackground(sunrisedentalclinicsystem.util.UIStyle.CARD_BACKGROUND);

        lblSearch.setFont(sunrisedentalclinicsystem.util.UIStyle.LABEL_FONT);
        lblSearch.setForeground(sunrisedentalclinicsystem.util.UIStyle.PRIMARY_TEXT);
        lblSearch.setText("Search Appointments");

        txtSearch.setFont(sunrisedentalclinicsystem.util.UIStyle.LABEL_FONT);
        txtSearch.addActionListener(this::txtSearchActionPerformed);

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

        tblAppointments.setFont(sunrisedentalclinicsystem.util.UIStyle.TABLE_FONT);
        tblAppointments.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Appointment Number", "Patient", "Dentist", "Date", "Time", "Treatment", "Status", "Notes"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblAppointments.setRowHeight(28);
        tblAppointments.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblAppointments.getTableHeader().setReorderingAllowed(false);
        scrollPaneAppointments.setViewportView(tblAppointments);

        javax.swing.GroupLayout tablePanelLayout = new javax.swing.GroupLayout(tablePanel);
        tablePanel.setLayout(tablePanelLayout);
        tablePanelLayout.setHorizontalGroup(
            tablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tablePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollPaneAppointments, javax.swing.GroupLayout.DEFAULT_SIZE, 621, Short.MAX_VALUE)
                .addContainerGap())
        );
        tablePanelLayout.setVerticalGroup(
            tablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tablePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollPaneAppointments, javax.swing.GroupLayout.DEFAULT_SIZE, 384, Short.MAX_VALUE)
                .addContainerGap())
        );

        actionsPanel.setBackground(sunrisedentalclinicsystem.util.UIStyle.CARD_BACKGROUND);

        btnScheduleAppointment.setBackground(sunrisedentalclinicsystem.util.UIStyle.PRIMARY_BLUE);
        btnScheduleAppointment.setFont(sunrisedentalclinicsystem.util.UIStyle.BUTTON_FONT);
        btnScheduleAppointment.setForeground(sunrisedentalclinicsystem.util.UIStyle.CARD_BACKGROUND);
        btnScheduleAppointment.setText("Schedule Appointment");
        btnScheduleAppointment.addActionListener(this::btnScheduleAppointmentActionPerformed);

        btnEditAppointment.setFont(sunrisedentalclinicsystem.util.UIStyle.BUTTON_FONT);
        btnEditAppointment.setText("Edit Appointment");
        btnEditAppointment.addActionListener(this::btnEditAppointmentActionPerformed);

        btnCancelAppointment.setFont(sunrisedentalclinicsystem.util.UIStyle.BUTTON_FONT);
        btnCancelAppointment.setForeground(sunrisedentalclinicsystem.util.UIStyle.DANGER);
        btnCancelAppointment.setText("Cancel Appointment");
        btnCancelAppointment.addActionListener(this::btnCancelAppointmentActionPerformed);

        btnRefresh.setFont(sunrisedentalclinicsystem.util.UIStyle.BUTTON_FONT);
        btnRefresh.setText("Refresh");
        btnRefresh.addActionListener(this::btnRefreshActionPerformed);

        btnGenerateBill.setFont(sunrisedentalclinicsystem.util.UIStyle.BUTTON_FONT);
        btnGenerateBill.setText("Generate Bill");
        btnGenerateBill.addActionListener(this::btnGenerateBillActionPerformed);

        javax.swing.GroupLayout actionsPanelLayout = new javax.swing.GroupLayout(actionsPanel);
        actionsPanel.setLayout(actionsPanelLayout);
        actionsPanelLayout.setHorizontalGroup(
            actionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(actionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnScheduleAppointment)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnEditAppointment)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnCancelAppointment)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnGenerateBill)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnRefresh)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        actionsPanelLayout.setVerticalGroup(
            actionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(actionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(actionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnScheduleAppointment, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEditAppointment, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCancelAppointment, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnGenerateBill, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                    .addComponent(actionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 633, Short.MAX_VALUE)))
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

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
        searchAppointments();
    }//GEN-LAST:event_txtSearchActionPerformed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        searchAppointments();
    }//GEN-LAST:event_btnSearchActionPerformed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        clearSearch();
    }//GEN-LAST:event_btnClearActionPerformed

    private void btnScheduleAppointmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnScheduleAppointmentActionPerformed
        scheduleAppointment();
    }//GEN-LAST:event_btnScheduleAppointmentActionPerformed

    private void btnEditAppointmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditAppointmentActionPerformed
        editAppointment();
    }//GEN-LAST:event_btnEditAppointmentActionPerformed

    private void btnCancelAppointmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelAppointmentActionPerformed
        cancelAppointment();
    }//GEN-LAST:event_btnCancelAppointmentActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        loadAppointments();
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void btnGenerateBillActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenerateBillActionPerformed
        int selectedRow = tblAppointments.getSelectedRow();
        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select an appointment to generate a bill.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            AppointmentSummary selectedSummary = getSelectedAppointmentSummary();
            if (selectedSummary == null) {
                JOptionPane.showMessageDialog(this,
                    "Selected appointment could not be resolved.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Get appointment details
            Appointment appointment = findAppointmentById(selectedSummary.getAppointmentId());
            
            if (appointment == null) {
                JOptionPane.showMessageDialog(this, 
                    "Appointment not found.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            List<Bill> existingBills = billDAO.getBillsByAppointment(appointment.getAppointmentId());
            if (!existingBills.isEmpty()) {
                Bill existingBill = existingBills.get(0);
                String existingStatus = existingBill.getPaymentStatus() == null
                        || existingBill.getPaymentStatus().isBlank()
                        ? "Pending"
                        : existingBill.getPaymentStatus();
                String paymentDateText = existingBill.getPaymentDate() == null
                        ? "-"
                        : existingBill.getPaymentDate().format(DISPLAY_DATE_FORMATTER);

                if ("Paid".equalsIgnoreCase(existingStatus)) {
                    JOptionPane.showMessageDialog(this,
                        "This appointment has already been paid.\n\n"
                                + "Bill Number: " + existingBill.getBillNumber() + "\n"
                                + "Status: " + existingStatus + "\n"
                                + "Payment Date: " + paymentDateText,
                        "Generate Bill",
                        JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                if ("Pending".equalsIgnoreCase(existingStatus)) {
                    JOptionPane.showMessageDialog(this,
                        "A bill already exists for this appointment.\n\n"
                                + "Bill Number: " + existingBill.getBillNumber() + "\n"
                                + "Status: " + existingStatus + "\n"
                                + "Payment Date: " + paymentDateText,
                        "Generate Bill",
                        JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                if ("Cancelled".equalsIgnoreCase(existingStatus)) {
                    JOptionPane.showMessageDialog(this,
                        "A cancelled bill already exists for this appointment.\n\n"
                                + "Bill Number: " + existingBill.getBillNumber() + "\n"
                                + "Status: " + existingStatus + "\n"
                                + "Payment Date: " + paymentDateText + "\n\n"
                                + "Duplicate bill generation is not allowed.",
                        "Generate Bill",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }

                JOptionPane.showMessageDialog(this,
                    "A bill already exists for this appointment.\n\n"
                            + "Bill Number: " + existingBill.getBillNumber() + "\n"
                            + "Status: " + existingStatus + "\n"
                            + "Payment Date: " + paymentDateText + "\n\n"
                            + "Duplicate bill generation is not allowed.",
                    "Generate Bill",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            if ("Cancelled".equalsIgnoreCase(appointment.getStatus())) {
                JOptionPane.showMessageDialog(this,
                    "Cancelled appointments cannot be billed.",
                    "Generate Bill",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            Patient patient = patientDAO.findPatient(appointment.getPatientId());
            Treatment treatment = treatmentDAO.findTreatment(appointment.getTreatmentId());

            if (treatment == null) {
                JOptionPane.showMessageDialog(this,
                    "Selected appointment has no valid treatment assigned.",
                    "Generate Bill",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            double billTotal = billDAO.calculateBillTotal(treatment);
            String billPreviewText = buildBillPreviewText(
                    appointment,
                    patient,
                    treatment,
                    billTotal);

            JTextArea previewArea = new JTextArea(billPreviewText);
            previewArea.setEditable(false);
            previewArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
            previewArea.setCaretPosition(0);

            JScrollPane previewScroll = new JScrollPane(previewArea);
            previewScroll.setPreferredSize(new java.awt.Dimension(620, 380));

            Object[] options = {"Generate Bill", "Print Receipt", "Cancel"};
            int option = JOptionPane.showOptionDialog(
                    this,
                    previewScroll,
                    "Bill Preview",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    options,
                    options[0]);

            if (option == 2 || option == JOptionPane.CLOSED_OPTION) {
                return;
            }

            Bill billRecord = ensureBillExists(appointment, treatment);
            if (billRecord == null) {
                return;
            }

            if (option == 0) {
                JOptionPane.showMessageDialog(this,
                    "Bill ready.\n\nBill Number: " + billRecord.getBillNumber()
                            + "\nAmount: Rs. " + String.format("%.2f", billRecord.getTotal()),
                    "Generate Bill",
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            printReceipt(billRecord, billPreviewText);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error generating bill: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnGenerateBillActionPerformed

    private String buildBillPreviewText(
            Appointment appointment,
            Patient patient,
            Treatment treatment,
            double totalAmount) {

        String patientName = patient == null
                ? "N/A"
                : valueOrNA(patient.getFirstName()) + " " + valueOrNA(patient.getLastName());
        String contact = patient == null ? "N/A" : valueOrNA(patient.getContactNumber());
        String address = patient == null ? "N/A" : valueOrNA(patient.getAddress());
        String appointmentDate = appointment.getAppointmentDate() == null
                ? "N/A"
                : appointment.getAppointmentDate().format(DISPLAY_DATE_FORMATTER);
        String appointmentTime = appointment.getAppointmentTime() == null
                ? "N/A"
                : appointment.getAppointmentTime().format(DISPLAY_TIME_FORMATTER);
        double consultationFee = AppConstants.CONSULTATION_FEE.doubleValue();

        StringBuilder billText = new StringBuilder();
        billText.append("═══════════════════════════════════════════════════════════════\n");
        billText.append("                   SUNRISE DENTAL CLINIC                      \n");
        billText.append("                        INVOICE/BILL                          \n");
        billText.append("═══════════════════════════════════════════════════════════════\n\n");
        billText.append("Appointment Number: ")
                .append(valueOrNA(appointment.getAppointmentNumber()))
                .append("\n");
        billText.append("Appointment Date: ").append(appointmentDate).append("\n");
        billText.append("Appointment Time: ").append(appointmentTime).append("\n\n");
        billText.append("PATIENT INFORMATION\n");
        billText.append("- Name: ").append(patientName).append("\n");
        billText.append("- Contact: ").append(contact).append("\n");
        billText.append("- Address: ").append(address).append("\n\n");
        billText.append("TREATMENT DETAILS\n");
        billText.append("- Treatment: ").append(valueOrNA(treatment.getTreatmentName())).append("\n");
        billText.append("- Treatment Cost: Rs. ").append(String.format("%.2f", treatment.getCost())).append("\n");
        billText.append("- Consultation Fee: Rs. ").append(String.format("%.2f", consultationFee)).append("\n");
        billText.append("- Total Amount: Rs. ").append(String.format("%.2f", totalAmount)).append("\n\n");
        billText.append("Bill Date: ").append(LocalDate.now().format(DISPLAY_DATE_FORMATTER)).append("\n");
        billText.append("Status: Pending\n");
        billText.append("═══════════════════════════════════════════════════════════════\n");

        return billText.toString();
    }

    private Bill ensureBillExists(Appointment appointment, Treatment treatment) {
        List<Bill> existingBills = billDAO.getBillsByAppointment(appointment.getAppointmentId());
        if (!existingBills.isEmpty()) {
            return existingBills.get(0);
        }

        Bill bill = new Bill();
        bill.setBillNumber(billDAO.generateBillNumber());
        bill.setAppointmentId(appointment.getAppointmentId());
        bill.setPatientId(appointment.getPatientId());
        bill.setTreatmentId(appointment.getTreatmentId());
        bill.setTotal(billDAO.calculateBillTotal(treatment));

        if (!billDAO.addBill(bill)) {
            JOptionPane.showMessageDialog(this,
                "Failed to generate bill.",
                "Generate Bill",
                JOptionPane.ERROR_MESSAGE);
            return null;
        }

        return bill;
    }

    private void printReceipt(Bill bill, String previewText) {
        JTextArea receiptArea = new JTextArea(
                previewText + "\nBill Number: " + bill.getBillNumber());
        receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 11));

        try {
            boolean printed = receiptArea.print();
            JOptionPane.showMessageDialog(this,
                printed
                        ? "Receipt printed successfully."
                        : "Print cancelled.",
                "Print Receipt",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Unable to print receipt: " + ex.getMessage(),
                "Print Receipt",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private String valueOrNA(String value) {
        return value == null || value.isBlank() ? "N/A" : value;
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel actionsPanel;
    private javax.swing.JButton btnCancelAppointment;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnEditAppointment;
    private javax.swing.JButton btnGenerateBill;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnScheduleAppointment;
    private javax.swing.JButton btnSearch;
    private javax.swing.JLabel lblSearch;
    private javax.swing.JLabel lblSubtitle;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JScrollPane scrollPaneAppointments;
    private javax.swing.JPanel searchPanel;
    private javax.swing.JPanel tablePanel;
    private javax.swing.JTable tblAppointments;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
