/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package sunrisedentalclinicsystem.ui;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.table.DefaultTableModel;
import sunrisedentalclinicsystem.dao.AppointmentDAO;
import sunrisedentalclinicsystem.dao.AppointmentDAO.AppointmentSummary;
import sunrisedentalclinicsystem.dao.BillDAO;
import sunrisedentalclinicsystem.dao.DentistDAO;
import sunrisedentalclinicsystem.dao.PatientDAO;
import sunrisedentalclinicsystem.model.Appointment;
import sunrisedentalclinicsystem.model.Bill;
import sunrisedentalclinicsystem.model.Dentist;
import sunrisedentalclinicsystem.util.AppConstants;

/**
 *
 * @author yasit
 */
public class DashboardPanel extends javax.swing.JPanel {

    private final PatientDAO patientDAO;
    private final AppointmentDAO appointmentDAO;
    private final BillDAO billDAO;
    private final DentistDAO dentistDAO;
    private final DefaultTableModel todayAppointmentsTableModel;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(AppConstants.TIME_FORMAT);
    private Runnable registerPatientAction;
    private Runnable newAppointmentAction;
    private Runnable addTreatmentAction;
    private Runnable createBillAction;

    /**
     * Creates new form DashboardPanel
     */
    public DashboardPanel() {
        initComponents();
        patientDAO = new PatientDAO();
        appointmentDAO = new AppointmentDAO();
        billDAO = new BillDAO();
        dentistDAO = new DentistDAO();
        todayAppointmentsTableModel = (DefaultTableModel) tblTodayAppointments.getModel();
        configureTodayAppointmentsTable();
        configureQuickActionsByRole();
        configureSummaryByRole();
        wireQuickActionButtons();
        loadWelcomeMessage();
        loadDashboardData();
    }

    public void setRegisterPatientAction(Runnable registerPatientAction) {
        this.registerPatientAction = registerPatientAction;
    }

    public void setNewAppointmentAction(Runnable newAppointmentAction) {
        this.newAppointmentAction = newAppointmentAction;
    }

    public void setAddTreatmentAction(Runnable addTreatmentAction) {
        this.addTreatmentAction = addTreatmentAction;
    }

    public void setCreateBillAction(Runnable createBillAction) {
        this.createBillAction = createBillAction;
    }

    private void wireQuickActionButtons() {
        btnRegisterPatient.addActionListener(evt -> runQuickAction(registerPatientAction));
        btnNewAppointment.addActionListener(evt -> runQuickAction(newAppointmentAction));
        btnAddTreatment.addActionListener(evt -> runQuickAction(addTreatmentAction));
        btnCreateBill.addActionListener(evt -> runQuickAction(createBillAction));
    }

    private void runQuickAction(Runnable action) {
        if (action != null) {
            action.run();
        }
    }

    private void configureQuickActionsByRole() {
        sunrisedentalclinicsystem.model.User currentUser = sunrisedentalclinicsystem.util.Session.getCurrentUser();
        String role = currentUser == null ? null : currentUser.getRole();

        btnRegisterPatient.setVisible(true);
        btnNewAppointment.setVisible(true);
        btnAddTreatment.setVisible("Administrator".equalsIgnoreCase(role));
        btnCreateBill.setVisible(!"Dentist".equalsIgnoreCase(role));
    }

    private void configureSummaryByRole() {
        String role = getCurrentRole();
        boolean isDentist = "Dentist".equalsIgnoreCase(role);
        cardBills.setVisible(!isDentist);
    }

    private void loadWelcomeMessage() {
        String fullName = "User";

        sunrisedentalclinicsystem.model.User currentUser = sunrisedentalclinicsystem.util.Session.getCurrentUser();
        if (currentUser != null) {
            String sessionFullName = currentUser.getFullName();
            if (sessionFullName != null && !sessionFullName.trim().isEmpty()) {
                fullName = sessionFullName;
            }
        }

        lblWelcomeMessage.setText("Welcome back, " + fullName);
    }

    private void loadDashboardData() {
        try {
            lblPatientsValue.setText(String.valueOf(patientDAO.countPatients()));

            String role = getCurrentRole();
            if ("Dentist".equalsIgnoreCase(role)) {
                loadDentistDashboardData();
            } else {
                loadClinicWideDashboardData();
            }
        } catch (RuntimeException e) {
            showDashboardLoadError();
        }
    }

    private void loadClinicWideDashboardData() {
        lblAppointmentsValue.setText(String.valueOf(appointmentDAO.countTodayAppointments()));

        List<Appointment> todayAppointments = appointmentDAO.getAppointmentsByDate(LocalDate.now());
        List<AppointmentSummary> todaySummaries = getTodayAppointmentSummaries(todayAppointments);

        int scheduledTreatments = 0;
        for (Appointment appointment : todayAppointments) {
            if (appointment != null && "Scheduled".equalsIgnoreCase(valueOrEmpty(appointment.getStatus()))) {
                scheduledTreatments++;
            }
        }
        lblTreatmentsValue.setText(String.valueOf(scheduledTreatments));

        List<Bill> bills = billDAO.getAllBills();
        int pendingBills = 0;
        for (Bill bill : bills) {
            String paymentStatus = valueOrEmpty(bill.getPaymentStatus());
            if (paymentStatus.isBlank() || "Pending".equalsIgnoreCase(paymentStatus)) {
                pendingBills++;
            }
        }
        lblBillsValue.setText(String.valueOf(pendingBills));

        updateTodayAppointmentsPanel(todaySummaries);
    }

    private void loadDentistDashboardData() {
        int dentistId = resolveCurrentDentistId();
        if (dentistId <= 0) {
            lblAppointmentsValue.setText("0");
            lblTreatmentsValue.setText("0");
            lblBillsValue.setText("-");
            updateTodayAppointmentsPanel(new ArrayList<>());
            return;
        }

        List<Appointment> dentistAppointments = appointmentDAO.getAppointmentsByDentist(dentistId);
        List<Appointment> todayDentistAppointments = filterAppointmentsByDate(dentistAppointments, LocalDate.now());
        List<AppointmentSummary> todaySummaries = getTodayAppointmentSummaries(todayDentistAppointments);

        int todayNonCancelledCount = 0;
        int todayScheduledCount = 0;
        for (Appointment appointment : todayDentistAppointments) {
            if (appointment == null) {
                continue;
            }

            String status = valueOrEmpty(appointment.getStatus());
            if (!"Cancelled".equalsIgnoreCase(status)) {
                todayNonCancelledCount++;
            }
            if ("Scheduled".equalsIgnoreCase(status)) {
                todayScheduledCount++;
            }
        }

        lblAppointmentsValue.setText(String.valueOf(todayNonCancelledCount));
        lblTreatmentsValue.setText(String.valueOf(todayScheduledCount));
        lblBillsValue.setText("-");

        updateTodayAppointmentsPanel(todaySummaries);
    }

    private List<Appointment> filterAppointmentsByDate(List<Appointment> appointments, LocalDate date) {
        List<Appointment> filtered = new ArrayList<>();
        if (appointments == null) {
            return filtered;
        }

        for (Appointment appointment : appointments) {
            if (appointment == null) {
                continue;
            }

            if (date.equals(appointment.getAppointmentDate())) {
                filtered.add(appointment);
            }
        }

        return filtered;
    }

    private int resolveCurrentDentistId() {
        sunrisedentalclinicsystem.model.User currentUser = sunrisedentalclinicsystem.util.Session.getCurrentUser();
        if (currentUser == null) {
            return -1;
        }

        int currentUserId = currentUser.getUserId();
        if (currentUserId <= 0) {
            return -1;
        }

        List<Dentist> dentists = dentistDAO.getAllDentists();
        for (Dentist dentist : dentists) {
            if (dentist != null && dentist.getUserId() == currentUserId) {
                return dentist.getDentistId();
            }
        }

        return -1;
    }

    private String getCurrentRole() {
        sunrisedentalclinicsystem.model.User currentUser = sunrisedentalclinicsystem.util.Session.getCurrentUser();
        return currentUser == null ? null : currentUser.getRole();
    }

    private void configureTodayAppointmentsTable() {
        tblTodayAppointments.setRowHeight(26);
        tblTodayAppointments.setShowGrid(true);
        tblTodayAppointments.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        tblTodayAppointments.getTableHeader().setReorderingAllowed(false);

        tblTodayAppointments.getColumnModel().getColumn(0).setMinWidth(55);
        tblTodayAppointments.getColumnModel().getColumn(0).setPreferredWidth(70);

        tblTodayAppointments.getColumnModel().getColumn(1).setMinWidth(95);
        tblTodayAppointments.getColumnModel().getColumn(1).setPreferredWidth(120);

        tblTodayAppointments.getColumnModel().getColumn(2).setMinWidth(130);
        tblTodayAppointments.getColumnModel().getColumn(2).setPreferredWidth(170);

        tblTodayAppointments.getColumnModel().getColumn(3).setMinWidth(130);
        tblTodayAppointments.getColumnModel().getColumn(3).setPreferredWidth(170);

        tblTodayAppointments.getColumnModel().getColumn(4).setMinWidth(130);
        tblTodayAppointments.getColumnModel().getColumn(4).setPreferredWidth(180);

        tblTodayAppointments.getColumnModel().getColumn(5).setMinWidth(65);
        tblTodayAppointments.getColumnModel().getColumn(5).setPreferredWidth(90);

        scrollTodayAppointments.setHorizontalScrollBarPolicy(
                javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    }

    private List<AppointmentSummary> getTodayAppointmentSummaries(List<Appointment> todayAppointments) {
        List<AppointmentSummary> allSummaries = appointmentDAO.getAllAppointmentSummaries();
        Map<Integer, AppointmentSummary> summaryById = new HashMap<>();
        for (AppointmentSummary summary : allSummaries) {
            summaryById.put(summary.getAppointmentId(), summary);
        }

        List<AppointmentSummary> todaySummaries = new ArrayList<>();
        for (Appointment appointment : todayAppointments) {
            if (appointment == null) {
                continue;
            }

            AppointmentSummary summary = summaryById.get(appointment.getAppointmentId());
            if (summary != null) {
                todaySummaries.add(summary);
            }
        }

        Collections.sort(todaySummaries, new Comparator<AppointmentSummary>() {
            @Override
            public int compare(AppointmentSummary first, AppointmentSummary second) {
                LocalTime firstTime = first == null ? null : first.getAppointmentTime();
                LocalTime secondTime = second == null ? null : second.getAppointmentTime();

                if (firstTime == null && secondTime == null) {
                    return 0;
                }
                if (firstTime == null) {
                    return 1;
                }
                if (secondTime == null) {
                    return -1;
                }
                return firstTime.compareTo(secondTime);
            }
        });

        return todaySummaries;
    }

    private void updateTodayAppointmentsPanel(List<AppointmentSummary> todaySummaries) {
        if (todaySummaries == null || todaySummaries.isEmpty()) {
            todayAppointmentsTableModel.setRowCount(0);
            scrollTodayAppointments.setVisible(false);
            lblNoAppointments.setVisible(true);
            lblNoAppointments.setText("No appointments to display.");
            return;
        }

        todayAppointmentsTableModel.setRowCount(0);
        for (AppointmentSummary summary : todaySummaries) {
            todayAppointmentsTableModel.addRow(new Object[]{
                formatTime(summary.getAppointmentTime()),
                valueOrEmpty(summary.getAppointmentNumber()),
                valueOrEmpty(summary.getPatientName()),
                valueOrEmpty(summary.getDentistName()),
                safeTreatmentName(summary.getTreatmentName()),
                valueOrEmpty(summary.getStatus())
            });
        }

        lblNoAppointments.setVisible(false);
        scrollTodayAppointments.setVisible(true);
    }

    private void showDashboardLoadError() {
        lblPatientsValue.setText("-");
        lblAppointmentsValue.setText("-");
        lblTreatmentsValue.setText("-");
        lblBillsValue.setText("-");
        todayAppointmentsTableModel.setRowCount(0);
        scrollTodayAppointments.setVisible(false);
        lblNoAppointments.setVisible(true);
        lblNoAppointments.setText("Unable to load dashboard data.");
    }

    private String formatTime(LocalTime time) {
        return time == null ? "--:--" : time.format(timeFormatter);
    }

    private String safeTreatmentName(String treatmentName) {
        return (treatmentName == null || treatmentName.isBlank()) ? "N/A" : treatmentName;
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblDashboardTitle = new javax.swing.JLabel();
        lblDashboardSubtitle = new javax.swing.JLabel();
        welcomePanel = new javax.swing.JPanel();
        lblWelcomeMessage = new javax.swing.JLabel();
        summaryPanel = new javax.swing.JPanel();
        cardPatients = new javax.swing.JPanel();
        lblPatientsTitle = new javax.swing.JLabel();
        lblPatientsValue = new javax.swing.JLabel();
        lblPatientsSubtitle = new javax.swing.JLabel();
        cardAppointments = new javax.swing.JPanel();
        lblAppointmentsTitle = new javax.swing.JLabel();
        lblAppointmentsValue = new javax.swing.JLabel();
        lblAppointmentsSubtitle = new javax.swing.JLabel();
        cardTreatments = new javax.swing.JPanel();
        lblTreatmentsTitle = new javax.swing.JLabel();
        lblTreatmentsValue = new javax.swing.JLabel();
        lblTreatmentsSubtitle = new javax.swing.JLabel();
        cardBills = new javax.swing.JPanel();
        lblBillsTitle = new javax.swing.JLabel();
        lblBillsValue = new javax.swing.JLabel();
        lblBillsSubtitle = new javax.swing.JLabel();
        appointmentsPanel = new javax.swing.JPanel();
        lblTodayAppointments = new javax.swing.JLabel();
        lblNoAppointments = new javax.swing.JLabel();
        scrollTodayAppointments = new javax.swing.JScrollPane();
        tblTodayAppointments = new javax.swing.JTable();
        quickActionsPanel = new javax.swing.JPanel();
        lblQuickActions = new javax.swing.JLabel();
        btnRegisterPatient = new javax.swing.JButton();
        btnNewAppointment = new javax.swing.JButton();
        btnAddTreatment = new javax.swing.JButton();
        btnCreateBill = new javax.swing.JButton();

        lblDashboardTitle.setFont(sunrisedentalclinicsystem.util.UIStyle.TITLE_FONT);
        lblDashboardTitle.setForeground(sunrisedentalclinicsystem.util.UIStyle.PRIMARY_TEXT);
        lblDashboardTitle.setText("Dashboard");

        lblDashboardSubtitle.setFont(sunrisedentalclinicsystem.util.UIStyle.LABEL_FONT);
        lblDashboardSubtitle.setForeground(sunrisedentalclinicsystem.util.UIStyle.SECONDARY_TEXT);
        lblDashboardSubtitle.setText("Overview of Sunrise Dental Clinic");

        welcomePanel.setBackground(sunrisedentalclinicsystem.util.UIStyle.CARD_BACKGROUND);

        lblWelcomeMessage.setFont(sunrisedentalclinicsystem.util.UIStyle.HEADER_FONT);
        lblWelcomeMessage.setForeground(sunrisedentalclinicsystem.util.UIStyle.PRIMARY_TEXT);
        lblWelcomeMessage.setText("Welcome back, User");

        javax.swing.GroupLayout welcomePanelLayout = new javax.swing.GroupLayout(welcomePanel);
        welcomePanel.setLayout(welcomePanelLayout);
        welcomePanelLayout.setHorizontalGroup(
            welcomePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(welcomePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblWelcomeMessage)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        welcomePanelLayout.setVerticalGroup(
            welcomePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(welcomePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblWelcomeMessage)
                .addContainerGap())
        );

        summaryPanel.setBackground(sunrisedentalclinicsystem.util.UIStyle.BACKGROUND);

        cardPatients.setBackground(sunrisedentalclinicsystem.util.UIStyle.CARD_BACKGROUND);

        lblPatientsTitle.setFont(sunrisedentalclinicsystem.util.UIStyle.HEADER_FONT);
        lblPatientsTitle.setForeground(sunrisedentalclinicsystem.util.UIStyle.PRIMARY_TEXT);
        lblPatientsTitle.setText("Total Patients");

        lblPatientsValue.setFont(new java.awt.Font("Segoe UI", 1, 30)); // NOI18N
        lblPatientsValue.setForeground(sunrisedentalclinicsystem.util.UIStyle.PRIMARY_BLUE);
        lblPatientsValue.setText("0");

        lblPatientsSubtitle.setFont(sunrisedentalclinicsystem.util.UIStyle.LABEL_FONT);
        lblPatientsSubtitle.setForeground(sunrisedentalclinicsystem.util.UIStyle.SECONDARY_TEXT);
        lblPatientsSubtitle.setText("Registered patients");

        javax.swing.GroupLayout cardPatientsLayout = new javax.swing.GroupLayout(cardPatients);
        cardPatients.setLayout(cardPatientsLayout);
        cardPatientsLayout.setHorizontalGroup(
            cardPatientsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardPatientsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(cardPatientsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblPatientsTitle)
                    .addComponent(lblPatientsValue)
                    .addComponent(lblPatientsSubtitle))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        cardPatientsLayout.setVerticalGroup(
            cardPatientsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardPatientsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblPatientsTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblPatientsValue)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblPatientsSubtitle)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        cardAppointments.setBackground(sunrisedentalclinicsystem.util.UIStyle.CARD_BACKGROUND);

        lblAppointmentsTitle.setFont(sunrisedentalclinicsystem.util.UIStyle.HEADER_FONT);
        lblAppointmentsTitle.setForeground(sunrisedentalclinicsystem.util.UIStyle.PRIMARY_TEXT);
        lblAppointmentsTitle.setText("Today's Appointments");

        lblAppointmentsValue.setFont(new java.awt.Font("Segoe UI", 1, 30)); // NOI18N
        lblAppointmentsValue.setForeground(sunrisedentalclinicsystem.util.UIStyle.SUCCESS);
        lblAppointmentsValue.setText("0");

        lblAppointmentsSubtitle.setFont(sunrisedentalclinicsystem.util.UIStyle.LABEL_FONT);
        lblAppointmentsSubtitle.setForeground(sunrisedentalclinicsystem.util.UIStyle.SECONDARY_TEXT);
        lblAppointmentsSubtitle.setText("Appointments today");

        javax.swing.GroupLayout cardAppointmentsLayout = new javax.swing.GroupLayout(cardAppointments);
        cardAppointments.setLayout(cardAppointmentsLayout);
        cardAppointmentsLayout.setHorizontalGroup(
            cardAppointmentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardAppointmentsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(cardAppointmentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblAppointmentsTitle)
                    .addComponent(lblAppointmentsValue)
                    .addComponent(lblAppointmentsSubtitle))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        cardAppointmentsLayout.setVerticalGroup(
            cardAppointmentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardAppointmentsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblAppointmentsTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblAppointmentsValue)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblAppointmentsSubtitle)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        cardTreatments.setBackground(sunrisedentalclinicsystem.util.UIStyle.CARD_BACKGROUND);

        lblTreatmentsTitle.setFont(sunrisedentalclinicsystem.util.UIStyle.HEADER_FONT);
        lblTreatmentsTitle.setForeground(sunrisedentalclinicsystem.util.UIStyle.PRIMARY_TEXT);
        lblTreatmentsTitle.setText("Pending Treatments");

        lblTreatmentsValue.setFont(new java.awt.Font("Segoe UI", 1, 30)); // NOI18N
        lblTreatmentsValue.setForeground(sunrisedentalclinicsystem.util.UIStyle.WARNING);
        lblTreatmentsValue.setText("0");

        lblTreatmentsSubtitle.setFont(sunrisedentalclinicsystem.util.UIStyle.LABEL_FONT);
        lblTreatmentsSubtitle.setForeground(sunrisedentalclinicsystem.util.UIStyle.SECONDARY_TEXT);
        lblTreatmentsSubtitle.setText("Awaiting treatment");

        javax.swing.GroupLayout cardTreatmentsLayout = new javax.swing.GroupLayout(cardTreatments);
        cardTreatments.setLayout(cardTreatmentsLayout);
        cardTreatmentsLayout.setHorizontalGroup(
            cardTreatmentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardTreatmentsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(cardTreatmentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTreatmentsTitle)
                    .addComponent(lblTreatmentsValue)
                    .addComponent(lblTreatmentsSubtitle))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        cardTreatmentsLayout.setVerticalGroup(
            cardTreatmentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardTreatmentsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTreatmentsTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblTreatmentsValue)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblTreatmentsSubtitle)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        cardBills.setBackground(sunrisedentalclinicsystem.util.UIStyle.CARD_BACKGROUND);

        lblBillsTitle.setFont(sunrisedentalclinicsystem.util.UIStyle.HEADER_FONT);
        lblBillsTitle.setForeground(sunrisedentalclinicsystem.util.UIStyle.PRIMARY_TEXT);
        lblBillsTitle.setText("Outstanding Bills");

        lblBillsValue.setFont(new java.awt.Font("Segoe UI", 1, 30)); // NOI18N
        lblBillsValue.setForeground(sunrisedentalclinicsystem.util.UIStyle.DANGER);
        lblBillsValue.setText("0");

        lblBillsSubtitle.setFont(sunrisedentalclinicsystem.util.UIStyle.LABEL_FONT);
        lblBillsSubtitle.setForeground(sunrisedentalclinicsystem.util.UIStyle.SECONDARY_TEXT);
        lblBillsSubtitle.setText("Pending payments");

        javax.swing.GroupLayout cardBillsLayout = new javax.swing.GroupLayout(cardBills);
        cardBills.setLayout(cardBillsLayout);
        cardBillsLayout.setHorizontalGroup(
            cardBillsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardBillsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(cardBillsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblBillsTitle)
                    .addComponent(lblBillsValue)
                    .addComponent(lblBillsSubtitle))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        cardBillsLayout.setVerticalGroup(
            cardBillsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardBillsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblBillsTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblBillsValue)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblBillsSubtitle)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout summaryPanelLayout = new javax.swing.GroupLayout(summaryPanel);
        summaryPanel.setLayout(summaryPanelLayout);
        summaryPanelLayout.setHorizontalGroup(
            summaryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(summaryPanelLayout.createSequentialGroup()
                .addComponent(cardPatients, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cardAppointments, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cardTreatments, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cardBills, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        summaryPanelLayout.setVerticalGroup(
            summaryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(cardPatients, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(cardAppointments, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(cardTreatments, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(cardBills, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        appointmentsPanel.setBackground(sunrisedentalclinicsystem.util.UIStyle.CARD_BACKGROUND);

        lblTodayAppointments.setFont(sunrisedentalclinicsystem.util.UIStyle.HEADER_FONT);
        lblTodayAppointments.setForeground(sunrisedentalclinicsystem.util.UIStyle.PRIMARY_TEXT);
        lblTodayAppointments.setText("Today's Appointments");

        lblNoAppointments.setFont(sunrisedentalclinicsystem.util.UIStyle.LABEL_FONT);
        lblNoAppointments.setForeground(sunrisedentalclinicsystem.util.UIStyle.SECONDARY_TEXT);
        lblNoAppointments.setText("No appointments to display.");

        scrollTodayAppointments.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        tblTodayAppointments.setFont(sunrisedentalclinicsystem.util.UIStyle.TABLE_FONT);
        tblTodayAppointments.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Time", "Appointment #", "Patient", "Dentist", "Treatment", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblTodayAppointments.setRowHeight(26);
        tblTodayAppointments.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblTodayAppointments.getTableHeader().setReorderingAllowed(false);
        scrollTodayAppointments.setViewportView(tblTodayAppointments);

        javax.swing.GroupLayout appointmentsPanelLayout = new javax.swing.GroupLayout(appointmentsPanel);
        appointmentsPanel.setLayout(appointmentsPanelLayout);
        appointmentsPanelLayout.setHorizontalGroup(
            appointmentsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(appointmentsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(appointmentsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTodayAppointments)
                    .addComponent(scrollTodayAppointments, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(lblNoAppointments))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        appointmentsPanelLayout.setVerticalGroup(
            appointmentsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(appointmentsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTodayAppointments)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(scrollTodayAppointments, javax.swing.GroupLayout.PREFERRED_SIZE, 160, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblNoAppointments)
                .addContainerGap(20, Short.MAX_VALUE))
        );

        quickActionsPanel.setBackground(sunrisedentalclinicsystem.util.UIStyle.CARD_BACKGROUND);

        lblQuickActions.setFont(sunrisedentalclinicsystem.util.UIStyle.HEADER_FONT);
        lblQuickActions.setForeground(sunrisedentalclinicsystem.util.UIStyle.PRIMARY_TEXT);
        lblQuickActions.setText("Quick Actions");

        btnRegisterPatient.setBackground(sunrisedentalclinicsystem.util.UIStyle.PRIMARY_BLUE);
        btnRegisterPatient.setFont(sunrisedentalclinicsystem.util.UIStyle.BUTTON_FONT);
        btnRegisterPatient.setForeground(sunrisedentalclinicsystem.util.UIStyle.CARD_BACKGROUND);
        btnRegisterPatient.setText("Register Patient");

        btnNewAppointment.setBackground(sunrisedentalclinicsystem.util.UIStyle.PRIMARY_BLUE);
        btnNewAppointment.setFont(sunrisedentalclinicsystem.util.UIStyle.BUTTON_FONT);
        btnNewAppointment.setForeground(sunrisedentalclinicsystem.util.UIStyle.CARD_BACKGROUND);
        btnNewAppointment.setText("New Appointment");

        btnAddTreatment.setBackground(sunrisedentalclinicsystem.util.UIStyle.PRIMARY_BLUE);
        btnAddTreatment.setFont(sunrisedentalclinicsystem.util.UIStyle.BUTTON_FONT);
        btnAddTreatment.setForeground(sunrisedentalclinicsystem.util.UIStyle.CARD_BACKGROUND);
        btnAddTreatment.setText("Add Treatment");

        btnCreateBill.setBackground(sunrisedentalclinicsystem.util.UIStyle.PRIMARY_BLUE);
        btnCreateBill.setFont(sunrisedentalclinicsystem.util.UIStyle.BUTTON_FONT);
        btnCreateBill.setForeground(sunrisedentalclinicsystem.util.UIStyle.CARD_BACKGROUND);
        btnCreateBill.setText("Create Bill");

        javax.swing.GroupLayout quickActionsPanelLayout = new javax.swing.GroupLayout(quickActionsPanel);
        quickActionsPanel.setLayout(quickActionsPanelLayout);
        quickActionsPanelLayout.setHorizontalGroup(
            quickActionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(quickActionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(quickActionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblQuickActions)
                    .addGroup(quickActionsPanelLayout.createSequentialGroup()
                        .addComponent(btnRegisterPatient)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnNewAppointment)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnAddTreatment)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCreateBill)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        quickActionsPanelLayout.setVerticalGroup(
            quickActionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(quickActionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblQuickActions)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(quickActionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnRegisterPatient, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnNewAppointment, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAddTreatment, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCreateBill, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblDashboardTitle)
                    .addComponent(lblDashboardSubtitle)
                    .addComponent(welcomePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(summaryPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(appointmentsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(quickActionsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblDashboardTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblDashboardSubtitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(welcomePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(summaryPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(appointmentsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(quickActionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel appointmentsPanel;
    private javax.swing.JButton btnAddTreatment;
    private javax.swing.JButton btnCreateBill;
    private javax.swing.JButton btnNewAppointment;
    private javax.swing.JButton btnRegisterPatient;
    private javax.swing.JPanel cardAppointments;
    private javax.swing.JPanel cardBills;
    private javax.swing.JPanel cardPatients;
    private javax.swing.JPanel cardTreatments;
    private javax.swing.JLabel lblAppointmentsSubtitle;
    private javax.swing.JLabel lblAppointmentsTitle;
    private javax.swing.JLabel lblAppointmentsValue;
    private javax.swing.JLabel lblBillsSubtitle;
    private javax.swing.JLabel lblBillsTitle;
    private javax.swing.JLabel lblBillsValue;
    private javax.swing.JLabel lblDashboardSubtitle;
    private javax.swing.JLabel lblDashboardTitle;
    private javax.swing.JLabel lblNoAppointments;
    private javax.swing.JLabel lblPatientsSubtitle;
    private javax.swing.JLabel lblPatientsTitle;
    private javax.swing.JLabel lblPatientsValue;
    private javax.swing.JLabel lblQuickActions;
    private javax.swing.JLabel lblTodayAppointments;
    private javax.swing.JLabel lblTreatmentsSubtitle;
    private javax.swing.JLabel lblTreatmentsTitle;
    private javax.swing.JLabel lblTreatmentsValue;
    private javax.swing.JLabel lblWelcomeMessage;
    private javax.swing.JPanel quickActionsPanel;
    private javax.swing.JScrollPane scrollTodayAppointments;
    private javax.swing.JPanel summaryPanel;
    private javax.swing.JTable tblTodayAppointments;
    private javax.swing.JPanel welcomePanel;
    // End of variables declaration//GEN-END:variables
}
