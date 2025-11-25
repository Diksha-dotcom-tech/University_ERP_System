package edu.univ.erp.ui;

import edu.univ.erp.access.AccessDeniedException;
import edu.univ.erp.auth.SessionContext;
import edu.univ.erp.data.SettingsDao;
import edu.univ.erp.domain.CourseOption;
import edu.univ.erp.domain.InstructorOption;
import edu.univ.erp.service.AdminService;
import edu.univ.erp.ui.common.UserProfileDialog;
import edu.univ.erp.util.DatabaseBackupUtil; // Placeholder util

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Time;
import java.util.List;

/**
 * Admin dashboard:
 * - Manage users (students / instructors / admins)
 * - Manage courses & sections
 * - Toggle maintenance
 * - Optional backup / restore
 */
public class AdminDashboardFrame extends JFrame {

    // NOTE: Assumes LoginFrame exists in the same package (edu.univ.erp.ui)

    private final SessionContext session;
    private final AdminService adminService = new AdminService();
    private final SettingsDao settingsDao = new SettingsDao();

    // ------ Users tab components ------
    private JTextField txtStuUsername;
    private JPasswordField txtStuPassword;
    private JTextField txtStuRoll;
    private JTextField txtStuProgram;
    private JSpinner spStuYear;

    private JTextField txtInstUsername;
    private JPasswordField txtInstPassword;
    private JTextField txtInstDept;

    private JTextField txtAdmUsername;
    private JPasswordField txtAdmPassword;

    // ------ Courses & sections tab ------
    private JTextField txtCourseCode;
    private JTextField txtCourseTitle;
    private JSpinner spCourseCredits;

    private JComboBox<CourseOption> cbSectionCourse;
    private JComboBox<InstructorOption> cbSectionInstructor;
    private JComboBox<String> cbSectionDay;
    private JTextField txtSectionStart;
    private JTextField txtSectionEnd;
    private JTextField txtSectionRoom;
    private JSpinner spSectionCapacity;
    private JTextField txtSectionSemester;
    private JSpinner spSectionYear;

    private DefaultTableModel tblCoursesModel;

    // ------ Maintenance tab ------
    private JLabel lblMaintStatus;
    private JCheckBox chkMaintenance;

    public AdminDashboardFrame(SessionContext session) {
        this.session = session;

        setTitle("Admin Dashboard - " + session.getUsername());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        initMenuBar();
        setContentPane(buildContent());
        refreshCombos();
        refreshMaintenanceStatus();
    }

    // ======================= MENU BAR ===========================

    private void initMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu account = new JMenu("Account");

        JMenuItem miProfile = new JMenuItem("My Profile...");
        miProfile.addActionListener(e ->
                new UserProfileDialog(this, session).setVisible(true));

        JMenuItem miLogout = new JMenuItem("Logout");
        miLogout.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to log out?",
                    "Confirm logout",
                    JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                dispose();
                // FIX: Assuming LoginFrame exists in the same package
                new LoginFrame().setVisible(true);
            }
        });

        account.add(miProfile);
        account.addSeparator();
        account.add(miLogout);
        bar.add(account);

        setJMenuBar(bar);
    }

    // ======================= CONTENT ===========================

    private JComponent buildContent() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Users", buildUsersTab());
        tabs.addTab("Courses & Sections", buildCoursesTab());
        tabs.addTab("Maintenance", buildMaintenanceTab());
        return tabs;
    }

    // ---------------------- Users tab ---------------------------

    private JComponent buildUsersTab() {
        JPanel root = new JPanel(new GridLayout(1, 3, 8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        root.add(buildStudentUserPanel());
        root.add(buildInstructorUserPanel());
        root.add(buildAdminUserPanel());

        return root;
    }

    private JComponent buildStudentUserPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new TitledBorder("Create Student"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int row = 0;

        txtStuUsername = new JTextField(12);
        txtStuPassword = new JPasswordField(12);
        txtStuRoll = new JTextField(12);
        txtStuProgram = new JTextField(12);
        spStuYear = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));

        addLabeled(p, gbc, row++, "Username:", txtStuUsername);
        addLabeled(p, gbc, row++, "Password:", txtStuPassword);
        addLabeled(p, gbc, row++, "Roll No:", txtStuRoll);
        addLabeled(p, gbc, row++, "Program:", txtStuProgram);
        addLabeled(p, gbc, row++, "Year:", spStuYear);

        JButton btnCreate = new JButton("Create Student");
        btnCreate.addActionListener(e -> onCreateStudent());
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        p.add(btnCreate, gbc);

        return p;
    }

    private JComponent buildInstructorUserPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new TitledBorder("Create Instructor"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int row = 0;

        txtInstUsername = new JTextField(12);
        txtInstPassword = new JPasswordField(12);
        txtInstDept = new JTextField(12);

        addLabeled(p, gbc, row++, "Username:", txtInstUsername);
        addLabeled(p, gbc, row++, "Password:", txtInstPassword);
        addLabeled(p, gbc, row++, "Department:", txtInstDept);

        JButton btnCreate = new JButton("Create Instructor");
        btnCreate.addActionListener(e -> onCreateInstructor());
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        p.add(btnCreate, gbc);

        return p;
    }

    private JComponent buildAdminUserPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new TitledBorder("Create Admin"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int row = 0;

        txtAdmUsername = new JTextField(12);
        txtAdmPassword = new JPasswordField(12);

        addLabeled(p, gbc, row++, "Username:", txtAdmUsername);
        addLabeled(p, gbc, row++, "Password:", txtAdmPassword);

        JButton btnCreate = new JButton("Create Admin");
        btnCreate.addActionListener(e -> onCreateAdmin());
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        p.add(btnCreate, gbc);

        return p;
    }

    private void addLabeled(JPanel p, GridBagConstraints gbc, int row,
                            String label, JComponent comp) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        p.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.EAST;
        p.add(comp, gbc);
    }

    private void onCreateStudent() {
        try {
            adminService.createStudentUser(
                    session,
                    txtStuUsername.getText().trim(),
                    new String(txtStuPassword.getPassword()),
                    txtStuRoll.getText().trim(),
                    txtStuProgram.getText().trim(),
                    (int) spStuYear.getValue()
            );
            JOptionPane.showMessageDialog(this, "Student user created.");
            refreshCombos();
        } catch (Exception ex) {
            showError("Failed to create student: " + ex.getMessage());
        }
    }

    private void onCreateInstructor() {
        try {
            adminService.createInstructorUser(
                    session,
                    txtInstUsername.getText().trim(),
                    new String(txtInstPassword.getPassword()),
                    txtInstDept.getText().trim()
            );
            JOptionPane.showMessageDialog(this, "Instructor user created.");
            refreshCombos();
        } catch (Exception ex) {
            showError("Failed to create instructor: " + ex.getMessage());
        }
    }

    private void onCreateAdmin() {
        try {
            adminService.createAdminUser(
                    session,
                    txtAdmUsername.getText().trim(),
                    new String(txtAdmPassword.getPassword())
            );
            JOptionPane.showMessageDialog(this, "Admin user created.");
        } catch (Exception ex) {
            showError("Failed to create admin: " + ex.getMessage());
        }
    }

    // ------------------- Courses & sections tab -------------------

    private JComponent buildCoursesTab() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel formPanel = new JPanel(new GridLayout(2, 1, 8, 8));
        formPanel.add(buildCreateCoursePanel());
        formPanel.add(buildCreateSectionPanel());

        root.add(formPanel, BorderLayout.NORTH);
        root.add(buildCoursesTablePanel(), BorderLayout.CENTER);
        return root;
    }

    private JComponent buildCreateCoursePanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new TitledBorder("Create Course"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int row = 0;

        txtCourseCode = new JTextField(8);
        txtCourseTitle = new JTextField(20);
        spCourseCredits = new JSpinner(new SpinnerNumberModel(4, 1, 10, 1));

        addLabeled(p, gbc, row++, "Code:", txtCourseCode);
        addLabeled(p, gbc, row++, "Title:", txtCourseTitle);
        addLabeled(p, gbc, row++, "Credits:", spCourseCredits);

        JButton btnCreate = new JButton("Create Course");
        btnCreate.addActionListener(e -> onCreateCourse());

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        p.add(btnCreate, gbc);

        return p;
    }

    private JComponent buildCreateSectionPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new TitledBorder("Create Section"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int row = 0;

        cbSectionCourse = new JComboBox<>();
        cbSectionInstructor = new JComboBox<>();
        cbSectionDay = new JComboBox<>(new String[]{"MON", "TUE", "WED", "THU", "FRI"});
        txtSectionStart = new JTextField(5); // HH:MM
        txtSectionEnd = new JTextField(5);
        txtSectionRoom = new JTextField(8);
        spSectionCapacity = new JSpinner(new SpinnerNumberModel(30, 1, 300, 1));
        txtSectionSemester = new JTextField(6);
        spSectionYear = new JSpinner(new SpinnerNumberModel(2025, 2000, 2100, 1));

        addLabeled(p, gbc, row++, "Course:", cbSectionCourse);
        addLabeled(p, gbc, row++, "Instructor:", cbSectionInstructor);
        addLabeled(p, gbc, row++, "Day of Week:", cbSectionDay);
        addLabeled(p, gbc, row++, "Start Time (HH:MM):", txtSectionStart);
        addLabeled(p, gbc, row++, "End Time (HH:MM):", txtSectionEnd);
        addLabeled(p, gbc, row++, "Room:", txtSectionRoom);
        addLabeled(p, gbc, row++, "Capacity:", spSectionCapacity);
        addLabeled(p, gbc, row++, "Semester:", txtSectionSemester);
        addLabeled(p, gbc, row++, "Year:", spSectionYear);

        JButton btnCreate = new JButton("Create Section");
        btnCreate.addActionListener(e -> onCreateSection());

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        p.add(btnCreate, gbc);

        return p;
    }

    private JComponent buildCoursesTablePanel() {
        // FIX: Removed Credits column from display as CourseOption DTO does not contain it.
        String[] cols = {"Code", "Title"};
        tblCoursesModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tblCoursesModel);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Existing Courses"));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void onCreateCourse() {
        try {
            adminService.createCourse(
                    session,
                    txtCourseCode.getText().trim(),
                    txtCourseTitle.getText().trim(),
                    (int) spCourseCredits.getValue()
            );
            JOptionPane.showMessageDialog(this, "Course created.");
            refreshCombos();
            refreshCoursesTable();
        } catch (Exception ex) {
            showError("Failed to create course: " + ex.getMessage());
        }
    }

    private void onCreateSection() {
        try {
            CourseOption course = (CourseOption) cbSectionCourse.getSelectedItem();
            InstructorOption inst = (InstructorOption) cbSectionInstructor.getSelectedItem();

            if (course == null || inst == null) {
                JOptionPane.showMessageDialog(this, "Please select course and instructor.");
                return;
            }

            String day = (String) cbSectionDay.getSelectedItem();

            // Basic format validation
            String startTimeStr = txtSectionStart.getText().trim();
            String endTimeStr = txtSectionEnd.getText().trim();

            if (!startTimeStr.matches("\\d{2}:\\d{2}") || !endTimeStr.matches("\\d{2}:\\d{2}")) {
                JOptionPane.showMessageDialog(this, "Time must be in HH:MM format (e.g., 09:00).");
                return;
            }

            Time start = Time.valueOf(startTimeStr + ":00");
            Time end = Time.valueOf(endTimeStr + ":00");

            String room = txtSectionRoom.getText().trim();
            int capacity = (int) spSectionCapacity.getValue();
            String sem = txtSectionSemester.getText().trim();
            int year = (int) spSectionYear.getValue();

            adminService.createSection(
                    session,
                    course.getCourseId(), // FIX: Use standard getter
                    inst.getUserId(),     // FIX: Use standard getter
                    day,
                    start,
                    end,
                    room,
                    capacity,
                    sem,
                    year
            );
            JOptionPane.showMessageDialog(this, "Section created.");
        } catch (Exception ex) {
            showError("Failed to create section: " + ex.getMessage());
        }
    }

    private void refreshCombos() {
        try {
            cbSectionCourse.removeAllItems();
            for (CourseOption c : adminService.listCourses(session)) {
                cbSectionCourse.addItem(c);
            }

            cbSectionInstructor.removeAllItems();
            for (InstructorOption inst : adminService.listInstructors(session)) {
                cbSectionInstructor.addItem(inst);
            }

            refreshCoursesTable();
        } catch (Exception ex) {
            showError("Failed to refresh lists: " + ex.getMessage());
        }
    }

    private void refreshCoursesTable() throws Exception {
        List<CourseOption> courses = adminService.listCourses(session);
        tblCoursesModel.setRowCount(0);

        // FIX: Only display Code and Title
        for (CourseOption c : courses) {
            tblCoursesModel.addRow(new Object[]{c.getCode(), c.getTitle()});
        }
    }

    // ------------------- Maintenance tab -------------------

    private JComponent buildMaintenanceTab() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        lblMaintStatus = new JLabel();
        lblMaintStatus.setFont(lblMaintStatus.getFont().deriveFont(Font.BOLD));

        chkMaintenance = new JCheckBox("Maintenance mode (students & instructors read-only)");
        chkMaintenance.addActionListener(e -> onToggleMaintenance());

        JButton btnBackup = new JButton("Backup DBs...");
        btnBackup.addActionListener(e -> onBackup());

        JButton btnRestore = new JButton("Restore DBs...");
        btnRestore.addActionListener(e -> onRestore());

        int row = 0;

        gbc.gridx = 0; gbc.gridy = row++;
        p.add(lblMaintStatus, gbc);

        gbc.gridy = row++;
        p.add(chkMaintenance, gbc);

        gbc.gridy = row++;
        p.add(btnBackup, gbc);

        gbc.gridy = row;
        p.add(btnRestore, gbc);

        return p;
    }

    private void refreshMaintenanceStatus() {
        try {
            boolean on = adminService.isMaintenanceOn(session);
            lblMaintStatus.setText(on
                    ? "Maintenance is currently ON"
                    : "Maintenance is currently OFF");
            chkMaintenance.setSelected(on);
        } catch (Exception ex) {
            showError("Failed to read maintenance flag: " + ex.getMessage());
        }
    }

    private void onToggleMaintenance() {
        try {
            boolean on = chkMaintenance.isSelected();
            adminService.setMaintenance(session, on);
            refreshMaintenanceStatus();
        } catch (AccessDeniedException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Failed to toggle maintenance: " + ex.getMessage());
        }
    }

    private void onBackup() {
        try {
            // Placeholder: DatabaseBackupUtil.backupDatabases(this);
            JOptionPane.showMessageDialog(this, "Backup initiated (I/O logic placeholder).");
        } catch (Exception ex) {
            showError("Backup failed: " + ex.getMessage());
        }
    }

    private void onRestore() {
        try {
            // Placeholder: DatabaseBackupUtil.restoreDatabases(this);
            JOptionPane.showMessageDialog(this, "Restore initiated (I/O logic placeholder).");
        } catch (Exception ex) {
            showError("Restore failed: " + ex.getMessage());
        }
    }

    // ------------------- Utils -------------------

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg,
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}