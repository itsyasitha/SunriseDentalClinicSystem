/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package sunrisedentalclinicsystem.ui;

/**
 *
 * @author yasit
 */
public class MainFrame extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(MainFrame.class.getName());
    private javax.swing.Timer clockTimer;

    /**
     * Creates new form MainFrame
     */
    public MainFrame() {
        initComponents();
        setTitle(sunrisedentalclinicsystem.util.AppConstants.APP_NAME);
        setSize(sunrisedentalclinicsystem.util.AppConstants.WINDOW_WIDTH, sunrisedentalclinicsystem.util.AppConstants.WINDOW_HEIGHT);
        setMinimumSize(new java.awt.Dimension(sunrisedentalclinicsystem.util.AppConstants.MIN_WINDOW_WIDTH, sunrisedentalclinicsystem.util.AppConstants.MIN_WINDOW_HEIGHT));
        setResizable(true);
        setLocationRelativeTo(null);
        loadSessionData();
        configureNavigationByRole();
        showDashboard();
        btnDashboard.addActionListener(evt -> showDashboard());
        btnPatients.addActionListener(evt -> showPatients());
        btnAppointments.addActionListener(evt -> showAppointments());
        btnTreatments.addActionListener(evt -> showTreatments());
        btnBills.addActionListener(evt -> showBills());
        btnUsers.addActionListener(evt -> showUsers());
        btnLogout.addActionListener(evt -> handleLogout());
    }

    private void handleLogout() {
        sunrisedentalclinicsystem.util.Session.clear();

        if (clockTimer != null && clockTimer.isRunning()) {
            clockTimer.stop();
        }

        LoginForm loginForm = new LoginForm();
        loginForm.setVisible(true);
        dispose();
    }

    private void showDashboard() {
        contentPanel.removeAll();

        DashboardPanel dashboardPanel = new DashboardPanel();
        dashboardPanel.setRegisterPatientAction(this::showPatients);
        dashboardPanel.setNewAppointmentAction(this::showAppointments);
        dashboardPanel.setAddTreatmentAction(this::showTreatments);
        dashboardPanel.setCreateBillAction(this::showBills);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(contentPanel);
        contentPanel.setLayout(layout);

        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(dashboardPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(dashboardPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        contentPanel.revalidate();
        contentPanel.repaint();
        
        lblStatus.setText("Dashboard");
    }
    
    private void showPatients() {
        contentPanel.removeAll();
        
        PatientsPanel patientsPanel = new PatientsPanel();
        
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(contentPanel);
        contentPanel.setLayout(layout);
        
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(
                                patientsPanel,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE
                        )
        );
        
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(
                                patientsPanel,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE
                        )
        );
        
        contentPanel.revalidate();
        contentPanel.repaint();
        
        lblStatus.setText("Patients");
    }
    
    private void showAppointments() {
        contentPanel.removeAll();
        
        AppointmentsPanel appointmentsPanel = new AppointmentsPanel();
        
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(contentPanel);
        contentPanel.setLayout(layout);
        
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(
                                appointmentsPanel,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE
                        )
        );
        
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(
                                appointmentsPanel,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE
                        )
        );
        
        contentPanel.revalidate();
        contentPanel.repaint();
        
        lblStatus.setText("Appointments");
    }

        private void showTreatments() {
        contentPanel.removeAll();

        TreatmentsPanel treatmentsPanel = new TreatmentsPanel();

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(contentPanel);
        contentPanel.setLayout(layout);

        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(
                    treatmentsPanel,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    Short.MAX_VALUE
                )
        );

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(
                    treatmentsPanel,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    Short.MAX_VALUE
                )
        );

        contentPanel.revalidate();
        contentPanel.repaint();

        lblStatus.setText("Treatments");
        }
    
    private void showBills() {
        contentPanel.removeAll();
        
        BillsPanel billsPanel = new BillsPanel();
        
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(contentPanel);
        contentPanel.setLayout(layout);
        
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(
                                billsPanel,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE
                        )
        );
        
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(
                                billsPanel,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE
                        )
        );
        
        contentPanel.revalidate();
        contentPanel.repaint();
        
        lblStatus.setText("Bills");
    }

        private void showUsers() {
        contentPanel.removeAll();

        UsersPanel usersPanel = new UsersPanel();

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(contentPanel);
        contentPanel.setLayout(layout);

        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(
                    usersPanel,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    Short.MAX_VALUE
                )
        );

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(
                    usersPanel,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    Short.MAX_VALUE
                )
        );

        contentPanel.revalidate();
        contentPanel.repaint();

        lblStatus.setText("Users");
        }

    private void configureNavigationByRole() {
        sunrisedentalclinicsystem.model.User currentUser = sunrisedentalclinicsystem.util.Session.getCurrentUser();

        btnDashboard.setVisible(true);
        btnLogout.setVisible(true);

        if (currentUser == null) {
            btnPatients.setVisible(false);
            btnAppointments.setVisible(false);
            btnTreatments.setVisible(false);
            btnBills.setVisible(false);
            btnUsers.setVisible(false);
            return;
        }

        String role = currentUser.getRole();

        if ("Administrator".equalsIgnoreCase(role)) {
            btnPatients.setVisible(true);
            btnAppointments.setVisible(true);
            btnTreatments.setVisible(true);
            btnBills.setVisible(true);
            btnUsers.setVisible(true);
        } else if ("Receptionist".equalsIgnoreCase(role)) {
            btnPatients.setVisible(true);
            btnAppointments.setVisible(true);
            btnTreatments.setVisible(false);
            btnBills.setVisible(true);
            btnUsers.setVisible(false);
        } else if ("Dentist".equalsIgnoreCase(role)) {
            btnPatients.setVisible(true);
            btnAppointments.setVisible(true);
            btnTreatments.setVisible(false);
            btnBills.setVisible(false);
            btnUsers.setVisible(false);
        } else {
            btnPatients.setVisible(false);
            btnAppointments.setVisible(false);
            btnTreatments.setVisible(false);
            btnBills.setVisible(false);
            btnUsers.setVisible(false);
        }
    }

    private void loadSessionData() {
        sunrisedentalclinicsystem.model.User currentUser = sunrisedentalclinicsystem.util.Session.getCurrentUser();

        String fullName = "User Name";
        String role = "Role";
        String welcomeText = "Welcome";

        if (currentUser != null) {
            String sessionFullName = currentUser.getFullName();
            String sessionRole = currentUser.getRole();

            lblStatus.setText("Logged in as " + currentUser.getRole());

            if (sessionFullName != null && !sessionFullName.trim().isEmpty()) {
                fullName = sessionFullName;
                welcomeText = "Welcome, " + sessionFullName;
            }

            if (sessionRole != null && !sessionRole.trim().isEmpty()) {
                role = sessionRole;
            }
        } else {
            lblStatus.setText("Ready");
        }

        lblWelcome.setText(welcomeText);
        lblUserName.setText(fullName);
        lblUserRole.setText(role);

        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat(sunrisedentalclinicsystem.util.AppConstants.DATE_FORMAT);
        java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat(sunrisedentalclinicsystem.util.AppConstants.TIME_FORMAT);

        lblDate.setText(dateFormat.format(new java.util.Date()));
        lblTime.setText(timeFormat.format(new java.util.Date()));

        if (clockTimer == null) {
            clockTimer = new javax.swing.Timer(1000, evt -> lblTime.setText(timeFormat.format(new java.util.Date())));
        }

        if (!clockTimer.isRunning()) {
            clockTimer.start();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        headerPanel = new javax.swing.JPanel();
        lblApplicationTitle = new javax.swing.JLabel();
        lblWelcome = new javax.swing.JLabel();
        lblDate = new javax.swing.JLabel();
        lblTime = new javax.swing.JLabel();
        sidebarPanel = new javax.swing.JPanel();
        lblClinicName = new javax.swing.JLabel();
        lblUserName = new javax.swing.JLabel();
        lblUserRole = new javax.swing.JLabel();
        btnDashboard = new javax.swing.JButton();
        btnPatients = new javax.swing.JButton();
        btnAppointments = new javax.swing.JButton();
        btnTreatments = new javax.swing.JButton();
        btnBills = new javax.swing.JButton();
        btnUsers = new javax.swing.JButton();
        btnLogout = new javax.swing.JButton();
        contentPanel = new javax.swing.JPanel();
        statusBarPanel = new javax.swing.JPanel();
        lblStatus = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        headerPanel.setBackground(sunrisedentalclinicsystem.util.UIStyle.CARD_BACKGROUND);

        lblApplicationTitle.setFont(sunrisedentalclinicsystem.util.UIStyle.TITLE_FONT);
        lblApplicationTitle.setForeground(sunrisedentalclinicsystem.util.UIStyle.PRIMARY_TEXT);
        lblApplicationTitle.setText(sunrisedentalclinicsystem.util.AppConstants.APP_NAME);

        lblWelcome.setFont(sunrisedentalclinicsystem.util.UIStyle.LABEL_FONT);
        lblWelcome.setForeground(sunrisedentalclinicsystem.util.UIStyle.SECONDARY_TEXT);
        lblWelcome.setText("Welcome");

        lblDate.setFont(sunrisedentalclinicsystem.util.UIStyle.LABEL_FONT);
        lblDate.setForeground(sunrisedentalclinicsystem.util.UIStyle.SECONDARY_TEXT);
        lblDate.setText("Date");

        lblTime.setFont(sunrisedentalclinicsystem.util.UIStyle.LABEL_FONT);
        lblTime.setForeground(sunrisedentalclinicsystem.util.UIStyle.SECONDARY_TEXT);
        lblTime.setText("Time");

        javax.swing.GroupLayout headerPanelLayout = new javax.swing.GroupLayout(headerPanel);
        headerPanel.setLayout(headerPanelLayout);
        headerPanelLayout.setHorizontalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headerPanelLayout.createSequentialGroup()
                .addComponent(lblApplicationTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblWelcome)
                .addGap(18, 18, 18)
                .addComponent(lblDate)
                .addGap(18, 18, 18)
                .addComponent(lblTime)
                .addContainerGap())
        );
        headerPanelLayout.setVerticalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
            .addComponent(lblApplicationTitle)
            .addComponent(lblWelcome)
            .addComponent(lblDate)
            .addComponent(lblTime)
        );

        sidebarPanel.setBackground(sunrisedentalclinicsystem.util.UIStyle.SIDEBAR);

        lblClinicName.setFont(sunrisedentalclinicsystem.util.UIStyle.HEADER_FONT);
        lblClinicName.setForeground(sunrisedentalclinicsystem.util.UIStyle.CARD_BACKGROUND);
        lblClinicName.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblClinicName.setText("Sunrise Dental");

        lblUserName.setFont(sunrisedentalclinicsystem.util.UIStyle.LABEL_FONT);
        lblUserName.setForeground(sunrisedentalclinicsystem.util.UIStyle.CARD_BACKGROUND);
        lblUserName.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblUserName.setText("User Name");

        lblUserRole.setFont(sunrisedentalclinicsystem.util.UIStyle.LABEL_FONT);
        lblUserRole.setForeground(sunrisedentalclinicsystem.util.UIStyle.SECONDARY_TEXT);
        lblUserRole.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblUserRole.setText("Role");

        btnDashboard.setFont(sunrisedentalclinicsystem.util.UIStyle.BUTTON_FONT);
        btnDashboard.setText("Dashboard");

        btnPatients.setFont(sunrisedentalclinicsystem.util.UIStyle.BUTTON_FONT);
        btnPatients.setText("Patients");

        btnAppointments.setFont(sunrisedentalclinicsystem.util.UIStyle.BUTTON_FONT);
        btnAppointments.setText("Appointments");

        btnTreatments.setFont(sunrisedentalclinicsystem.util.UIStyle.BUTTON_FONT);
        btnTreatments.setText("Treatments");

        btnBills.setFont(sunrisedentalclinicsystem.util.UIStyle.BUTTON_FONT);
        btnBills.setText("Bills");

        btnUsers.setFont(sunrisedentalclinicsystem.util.UIStyle.BUTTON_FONT);
        btnUsers.setText("Users");

        btnLogout.setFont(sunrisedentalclinicsystem.util.UIStyle.BUTTON_FONT);
        btnLogout.setForeground(sunrisedentalclinicsystem.util.UIStyle.DANGER);
        btnLogout.setText("Logout");

        javax.swing.GroupLayout sidebarPanelLayout = new javax.swing.GroupLayout(sidebarPanel);
        sidebarPanel.setLayout(sidebarPanelLayout);
        sidebarPanelLayout.setHorizontalGroup(
            sidebarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sidebarPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(sidebarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnLogout, javax.swing.GroupLayout.DEFAULT_SIZE, 196, Short.MAX_VALUE)
                    .addComponent(btnUsers, javax.swing.GroupLayout.DEFAULT_SIZE, 196, Short.MAX_VALUE)
                    .addComponent(btnBills, javax.swing.GroupLayout.DEFAULT_SIZE, 196, Short.MAX_VALUE)
                    .addComponent(btnTreatments, javax.swing.GroupLayout.DEFAULT_SIZE, 196, Short.MAX_VALUE)
                    .addComponent(btnAppointments, javax.swing.GroupLayout.DEFAULT_SIZE, 196, Short.MAX_VALUE)
                    .addComponent(btnPatients, javax.swing.GroupLayout.DEFAULT_SIZE, 196, Short.MAX_VALUE)
                    .addComponent(btnDashboard, javax.swing.GroupLayout.DEFAULT_SIZE, 196, Short.MAX_VALUE)
                    .addComponent(lblUserRole, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(lblUserName, javax.swing.GroupLayout.DEFAULT_SIZE, 196, Short.MAX_VALUE)
                    .addComponent(lblClinicName, javax.swing.GroupLayout.DEFAULT_SIZE, 196, Short.MAX_VALUE))
                .addContainerGap())
        );
        sidebarPanelLayout.setVerticalGroup(
            sidebarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sidebarPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblClinicName)
                .addGap(18, 18, 18)
                .addComponent(lblUserName)
                .addGap(18, 18, 18)
                .addComponent(lblUserRole)
                .addGap(18, 18, 18)
                .addComponent(btnDashboard)
                .addGap(18, 18, 18)
                .addComponent(btnPatients)
                .addGap(18, 18, 18)
                .addComponent(btnAppointments)
                .addGap(18, 18, 18)
                .addComponent(btnTreatments)
                .addGap(18, 18, 18)
                .addComponent(btnBills)
                .addGap(18, 18, 18)
                .addComponent(btnUsers)
                .addGap(18, 18, 18)
                .addComponent(btnLogout)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        contentPanel.setBackground(sunrisedentalclinicsystem.util.UIStyle.BACKGROUND);

        javax.swing.GroupLayout contentPanelLayout = new javax.swing.GroupLayout(contentPanel);
        contentPanel.setLayout(contentPanelLayout);
        contentPanelLayout.setHorizontalGroup(
            contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 446, Short.MAX_VALUE)
        );
        contentPanelLayout.setVerticalGroup(
            contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        statusBarPanel.setBackground(sunrisedentalclinicsystem.util.UIStyle.SIDEBAR);

        lblStatus.setFont(sunrisedentalclinicsystem.util.UIStyle.LABEL_FONT);
        lblStatus.setForeground(new java.awt.Color(255, 255, 255));
        lblStatus.setText("Ready");

        javax.swing.GroupLayout statusBarPanelLayout = new javax.swing.GroupLayout(statusBarPanel);
        statusBarPanel.setLayout(statusBarPanelLayout);
        statusBarPanelLayout.setHorizontalGroup(
            statusBarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusBarPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblStatus)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        statusBarPanelLayout.setVerticalGroup(
            statusBarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblStatus, javax.swing.GroupLayout.Alignment.TRAILING)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(headerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(sidebarPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(contentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(statusBarPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(headerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sidebarPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(contentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addComponent(statusBarPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new MainFrame().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAppointments;
    private javax.swing.JButton btnBills;
    private javax.swing.JButton btnDashboard;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnPatients;
    private javax.swing.JButton btnTreatments;
    private javax.swing.JButton btnUsers;
    private javax.swing.JPanel contentPanel;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JLabel lblApplicationTitle;
    private javax.swing.JLabel lblClinicName;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JLabel lblTime;
    private javax.swing.JLabel lblUserName;
    private javax.swing.JLabel lblUserRole;
    private javax.swing.JLabel lblWelcome;
    private javax.swing.JPanel sidebarPanel;
    private javax.swing.JPanel statusBarPanel;
    // End of variables declaration//GEN-END:variables
}
