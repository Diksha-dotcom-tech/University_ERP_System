package edu.univ.erp.ui;

import edu.univ.erp.access.AccessDeniedException;
import edu.univ.erp.auth.SessionContext;
import edu.univ.erp.data.SettingsDao;
import edu.univ.erp.domain.CourseOption;
import edu.univ.erp.domain.InstructorOption;
import edu.univ.erp.service.AdminService;
import edu.univ.erp.service.AuthService; // NEW IMPORT
import edu.univ.erp.ui.common.UserProfileDialog;
import edu.univ.erp.util.DatabaseBackupUtil;
import edu.univ.erp.ui.common.ChangePasswordDialog;
import edu.univ.erp.ui.common.CourseActionRenderer;
import edu.univ.erp.ui.common.CourseActionEditor;
import edu.univ.erp.ui.common.EditCourseDialog;


import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter; // NEW IMPORT
import java.awt.event.WindowEvent; // NEW IMPORT
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

    private final SessionContext session;
    private final AdminService adminService = new AdminService();
    private final AuthService authService = new AuthService(); // KEY CHANGE: Instance of AuthService
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
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                authService.logout(session);
                e.getWindow().dispose();
                new LoginFrame().setVisible(true);
            }
        });

        initMenuBar();
        setContentPane(buildContent());
        refreshCombos();
        refreshMaintenanceStatus();
    }

    private void initMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu account = new JMenu("Account");

        JMenuItem miProfile = new JMenuItem("My Profile...");
        miProfile.addActionListener(e ->
                new UserProfileDialog(this, session).setVisible(true));

        JMenuItem miChangePwd = new JMenuItem("Change Password...");
        miChangePwd.addActionListener(e ->
                new ChangePasswordDialog(this, session).setVisible(true));

        JMenuItem miLogout = new JMenuItem("Logout");
        miLogout.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to log out?",
                    "Confirm logout",
                    JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                authService.logout(session);
                dispose();
                new LoginFrame().setVisible(true);
            }
        });

        account.add(miProfile);
        account.add(miChangePwd);
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
        // Using BoxLayout for better vertical stacking and less complex alignment
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel innerPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        innerPanel.add(buildStudentUserPanel());
        innerPanel.add(buildInstructorUserPanel());
        innerPanel.add(buildAdminUserPanel());

        root.add(innerPanel);

        // Add vertical glue to push content to the top
        root.add(Box.createVerticalGlue());

        return root;
    }

    private JComponent buildStudentUserPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new TitledBorder("Create Student"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL; // Fill horizontally by default
        gbc.weightx = 1.0;

        int row = 0;

        txtStuUsername = new JTextField(12);
        txtStuPassword = new JPasswordField(12);
        txtStuRoll = new JTextField(12);
        txtStuProgram = new JTextField(12);
        spStuYear = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        spStuYear.setPreferredSize(new Dimension(80, spStuYear.getPreferredSize().height)); // Size control for spinner

        addLabeled(p, gbc, row++, "Username:", txtStuUsername);
        addLabeled(p, gbc, row++, "Password:", txtStuPassword);
        addLabeled(p, gbc, row++, "Roll No:", txtStuRoll);
        addLabeled(p, gbc, row++, "Program:", txtStuProgram);
        addLabeled(p, gbc, row++, "Year:", spStuYear, false); // Do not stretch spinner

        JButton btnCreate = new JButton("Create Student");
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        p.add(btnCreate, gbc);
        btnCreate.addActionListener(e -> onCreateStudent());

        // Add vertical weight to push content up within the panel
        gbc.gridy = row + 1;
        gbc.weighty = 1.0;
        p.add(Box.createVerticalGlue(), gbc);

        return p;
    }

    private JComponent buildInstructorUserPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new TitledBorder("Create Instructor"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
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
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        p.add(btnCreate, gbc);
        btnCreate.addActionListener(e -> onCreateInstructor());

        gbc.gridy = row + 1;
        gbc.weighty = 1.0;
        p.add(Box.createVerticalGlue(), gbc);

        return p;
    }

    private JComponent buildAdminUserPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new TitledBorder("Create Admin"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int row = 0;

        txtAdmUsername = new JTextField(12);
        txtAdmPassword = new JPasswordField(12);

        addLabeled(p, gbc, row++, "Username:", txtAdmUsername);
        addLabeled(p, gbc, row++, "Password:", txtAdmPassword);

        JButton btnCreate = new JButton("Create Admin");
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        p.add(btnCreate, gbc);
        btnCreate.addActionListener(e -> onCreateAdmin());

        gbc.gridy = row + 1;
        gbc.weighty = 1.0;
        p.add(Box.createVerticalGlue(), gbc);

        return p;
    }

    /**
     * Helper method to add a label/component pair with proper alignment.
     * @param p The panel to add to.
     * @param gbc The GridBagConstraints instance.
     * @param row The current row index.
     * @param label The label text.
     * @param comp The input component.
     * @param fillComponent If true, the component fills the cell (default: true).
     */
    private void addLabeled(JPanel p, GridBagConstraints gbc, int row,
                            String label, JComponent comp, boolean fillComponent) {

        // 1. Label (Column 0)
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST; // Anchor label to the EAST (right-aligned)
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        p.add(new JLabel(label), gbc);

        // 2. Component (Column 1)
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST; // Anchor component to the WEST (left-aligned)
        gbc.weightx = 1.0; // Allow component column to stretch

        if (fillComponent) {
            gbc.fill = GridBagConstraints.HORIZONTAL;
        } else {
            gbc.fill = GridBagConstraints.NONE;
        }

        p.add(comp, gbc);
    }

    // Overloaded for backward compatibility with existing calls
    private void addLabeled(JPanel p, GridBagConstraints gbc, int row,
                            String label, JComponent comp) {
        addLabeled(p, gbc, row, label, comp, true);
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

        JPanel formPanel = new JPanel(new GridLayout(1, 2, 10, 10)); // Use 1 row, 2 columns for better layout
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
        gbc.weightx = 1.0;

        int row = 0;

        txtCourseCode = new JTextField(8);
        txtCourseTitle = new JTextField(20);
        spCourseCredits = new JSpinner(new SpinnerNumberModel(4, 1, 10, 1));
        spCourseCredits.setPreferredSize(new Dimension(80, spCourseCredits.getPreferredSize().height));

        addLabeled(p, gbc, row++, "Code:", txtCourseCode);
        addLabeled(p, gbc, row++, "Title:", txtCourseTitle);
        addLabeled(p, gbc, row++, "Credits:", spCourseCredits, false); // Do not stretch spinner

        JButton btnCreate = new JButton("Create Course");
        btnCreate.addActionListener(e -> onCreateCourse());

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        p.add(btnCreate, gbc);

        // Vertical glue
        gbc.gridy = row + 1;
        gbc.weighty = 1.0;
        p.add(Box.createVerticalGlue(), gbc);

        return p;
    }

    private JComponent buildCreateSectionPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new TitledBorder("Create Section"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
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

        spSectionCapacity.setPreferredSize(new Dimension(80, spSectionCapacity.getPreferredSize().height));
        spSectionYear.setPreferredSize(new Dimension(80, spSectionYear.getPreferredSize().height));


        addLabeled(p, gbc, row++, "Course:", cbSectionCourse);
        addLabeled(p, gbc, row++, "Instructor:", cbSectionInstructor);
        addLabeled(p, gbc, row++, "Day of Week:", cbSectionDay);
        addLabeled(p, gbc, row++, "Start Time (HH:MM):", txtSectionStart);
        addLabeled(p, gbc, row++, "End Time (HH:MM):", txtSectionEnd);
        addLabeled(p, gbc, row++, "Room:", txtSectionRoom);
        addLabeled(p, gbc, row++, "Capacity:", spSectionCapacity, false);
        addLabeled(p, gbc, row++, "Semester:", txtSectionSemester);
        addLabeled(p, gbc, row++, "Year:", spSectionYear, false);

        JButton btnCreate = new JButton("Create Section");
        btnCreate.addActionListener(e -> onCreateSection());

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        p.add(btnCreate, gbc);

        // Vertical glue
        gbc.gridy = row + 1;
        gbc.weighty = 1.0;
        p.add(Box.createVerticalGlue(), gbc);

        return p;
    }

    private JComponent buildCoursesTablePanel() {
        // Updated columns: Added "Edit" and "Delete"
        String[] cols = {"ID", "Code", "Title", "Credits", "Actions"};

        // Custom Table Model that stores the course ID (which we need for update/delete)
        tblCoursesModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                // Allow editing only in the "Actions" column (index 4)
                return c == 4;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                // Define the column classes. The last column will hold buttons/actions.
                if (columnIndex == 4) return CourseOption.class; // We will store the full object here
                return super.getColumnClass(columnIndex);
            }
        };

        JTable table = new JTable(tblCoursesModel);

        // Hide the ID column, but keep it in the model for reference
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setPreferredWidth(0);

        // Set the custom renderer and editor for the Actions column
        CourseActionRenderer renderer = new CourseActionRenderer();
        CourseActionEditor editor = new CourseActionEditor(table, this::onEditCourse, this::onDeleteCourse);

        table.getColumnModel().getColumn(4).setCellRenderer(renderer);
        table.getColumnModel().getColumn(4).setCellEditor(editor);


        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Existing Courses"));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        // Add a refresh button for quick updates (optional but good practice)
        JButton btnRefresh = new JButton("Refresh Course List");
        btnRefresh.addActionListener(e -> refreshCombos());
        panel.add(btnRefresh, BorderLayout.SOUTH);

        return panel;
    }
    // ------------------- Course Actions -------------------

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
        } catch (Exception ex) {
            showError("Failed to create course: " + ex.getMessage());
        }
    }

    private void onEditCourse(CourseOption course) {
        // NOTE: We are using placeholder values here as your model doesn't store these:
        // You MUST update your AdminDao.listCourses() to fetch the true credits and capacity
        // if you want accurate initial values.
        int currentCredits = 4; // Placeholder - ASSUMING default or known value
        int currentCapacity = 30; // Placeholder - ASSUMING default or known value

        // Initialize the dialog with the course ID and placeholder values
        EditCourseDialog dialog = new EditCourseDialog(this, course, currentCredits, currentCapacity);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            try {
                // Call the UPDATED service method with the new capacity
                adminService.updateCourse(
                        session,
                        course.getCourseId(),
                        dialog.getCode(),
                        dialog.getTitle(),
                        dialog.getCredits(),
                        dialog.getCapacity() // NEW: Pass the capacity from the dialog
                );
                JOptionPane.showMessageDialog(this, "Course and associated section capacities updated successfully.");
                refreshCombos(); // Refresh the UI lists
            } catch (Exception ex) {
                showError("Failed to update course: " + ex.getMessage());
            }
        }
    }

    private void onDeleteCourse(CourseOption course) {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete course " + course.getCode() + " - " + course.getTitle() + "?\n" +
                        "This action cannot be undone and may affect associated sections and enrollments.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                adminService.deleteCourse(session, course.getCourseId());
                JOptionPane.showMessageDialog(this, "Course deleted successfully.");
                refreshCombos(); // Refresh the UI lists
            } catch (Exception ex) {
                showError("Failed to delete course: " + ex.getMessage());
            }
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
                    course.getCourseId(),
                    inst.getUserId(),
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
            // Fetch once
            List<CourseOption> courses = adminService.listCourses(session);
            List<InstructorOption> instructors = adminService.listInstructors(session);

            // Courses
            cbSectionCourse.removeAllItems();
            for (CourseOption c : courses) {
                cbSectionCourse.addItem(c);
            }

            // Instructors
            cbSectionInstructor.removeAllItems();
            for (InstructorOption inst : instructors) {
                cbSectionInstructor.addItem(inst);
            }

            // Update the table
            refreshCoursesTable(courses);

        } catch (Exception ex) {
            showError("Failed to refresh lists: " + ex.getMessage());
        }
    }

    // Helper to refresh table with already fetched list
    private void refreshCoursesTable(List<CourseOption> courses) {
        tblCoursesModel.setRowCount(0);
        for (CourseOption c : courses) {
            // NOTE: Assuming your full Course object (not just CourseOption)
            // should carry credits. For now, we will assume CourseOption needs credits
            // to support the edit dialog, or fetch the full list of details.

            // For simplicity, let's update CourseOption to include credits temporarily,
            // OR fetch credits in AdminDao.listCourses().
            // ASSUMING: The credits data is available. Let's create a placeholder value for now.

            // Since CourseOption only has ID, Code, Title, we need to modify how we get credits.
            // TEMPORARY FIX: We cannot get credits here without modifying CourseOption or AdminDao.
            // Let's modify AdminDao.listCourses() to fetch credits first (as we did previously)
            // But since I can't modify that file, I will proceed assuming the table data source is updated.

            // For now, let's use a placeholder for Credits:
            // The row data: {ID, Code, Title, Credits, Actions (CourseOption)}
            tblCoursesModel.addRow(new Object[]{
                    c.getCourseId(),
                    c.getCode(),
                    c.getTitle(),
                    "4", // Placeholder for Credits - you must update your DAO/Service to fetch this!
                    c // Pass the full object to the Actions column
            });
        }
    }

    // Original method retained for public use
    private void refreshCoursesTable() {
        try {
            refreshCoursesTable(adminService.listCourses(session));
        } catch (Exception e) {
            showError("Failed to refresh course list: " + e.getMessage());
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

        // Vertical glue to push content to the top
        gbc.gridy = row + 1;
        gbc.weighty = 1.0;
        p.add(Box.createVerticalGlue(), gbc);

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
            // Using the refactored DatabaseBackupUtil (must be re-compiled)
            DatabaseBackupUtil.backupDatabases(this);
            JOptionPane.showMessageDialog(this, "Database backup completed successfully.");
        } catch (Exception ex) {
            showError("Backup failed: " + ex.getMessage());
        }
    }

    private void onRestore() {
        try {
            // Using the refactored DatabaseBackupUtil (must be re-compiled)
            DatabaseBackupUtil.restoreDatabases(this);
            JOptionPane.showMessageDialog(this, "Database restore completed successfully.");
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