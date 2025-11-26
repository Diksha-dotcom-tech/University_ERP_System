package edu.univ.erp.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import edu.univ.erp.auth.SessionContext;
import edu.univ.erp.domain.CatalogSectionRow;
import edu.univ.erp.domain.StudentGradeRow;
import edu.univ.erp.domain.StudentTimetableRow;
import edu.univ.erp.service.StudentService;
import edu.univ.erp.ui.common.ChangePasswordDialog;
import edu.univ.erp.ui.common.UserProfileDialog;
import edu.univ.erp.ui.student.CatalogTableModel;
import edu.univ.erp.ui.student.GradesTableModel;
import edu.univ.erp.ui.student.TimetableTableModel;
import edu.univ.erp.ui.common.ChangePasswordDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

public class StudentDashboardFrame extends JFrame {

    private final SessionContext session;
    private final StudentService studentService = new StudentService();


    private JTable catalogTable;
    private JTable registrationsTable;
    private JTable timetableTable;
    private JTable gradesTable;

    private TimetableTableModel timetableModel;
    private GradesTableModel gradesModel;

    public StudentDashboardFrame(SessionContext session) {
        this.session = session;
        setTitle("Student Dashboard - " + session.getUsername());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1100, 600);
        setLocationRelativeTo(null);

        setJMenuBar(buildMenuBar());
        initUi();

        refreshCatalog();
        refreshRegistrations();
        refreshTimetable();
        refreshGrades();
    }

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu account = new JMenu("Account");
        JMenuItem profileItem = new JMenuItem("My Profile");
        profileItem.addActionListener(e ->
                new UserProfileDialog(this, session).setVisible(true));

        JMenuItem changePwdItem = new JMenuItem("Change Password");
        changePwdItem.addActionListener(e ->
                new ChangePasswordDialog(this, session).setVisible(true));

        JMenuItem themeItem = new JMenuItem("Toggle Dark Mode");
        themeItem.addActionListener(e -> toggleTheme());

        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(e -> doLogout());

        account.add(profileItem);
        account.add(changePwdItem);
        account.addSeparator();
        account.add(themeItem);
        account.addSeparator();
        account.add(logoutItem);

        bar.add(account);
        return bar;
    }

    private void toggleTheme() {
        boolean dark = UIManager.getLookAndFeel() instanceof FlatLightLaf;
        try {
            if (dark) {
                FlatDarkLaf.setup();
            } else {
                FlatLightLaf.setup();
            }
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to switch theme: " + ex.getMessage());
        }
    }

    private void doLogout() {
        int res = JOptionPane.showConfirmDialog(
                this,
                "Do you really want to log out?",
                "Confirm logout",
                JOptionPane.YES_NO_OPTION
        );
        if (res == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    private void initUi() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Catalog", buildCatalogPanel());
        tabs.addTab("My Registrations", buildRegistrationsPanel());
        tabs.addTab("Timetable", buildTimetablePanel());
        tabs.addTab("Grades", buildGradesPanel());

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(tabs, BorderLayout.CENTER);
    }

    // ---------- Catalog tab ----------

    private JPanel buildCatalogPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        catalogTable = new JTable();
        catalogTable.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{"Section ID", "Code", "Title", "Instructor", "Day",
                        "Time", "Room", "Capacity", "Enrolled", "Seats Left", "Sem", "Year"}
        ) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        });

        catalogTable.setAutoCreateRowSorter(true);

        panel.add(new JScrollPane(catalogTable), BorderLayout.CENTER);

        JButton registerBtn = new JButton("Register in selected section");
        registerBtn.addActionListener(e -> doRegister());
        panel.add(registerBtn, BorderLayout.SOUTH);

        return panel;
    }

    private void doRegister() {
        int row = catalogTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a section in the catalog.");
            return;
        }

        int modelRow = catalogTable.convertRowIndexToModel(row);
        int sectionId = (Integer) catalogTable.getModel().getValueAt(modelRow, 0);

        try {
            studentService.register(session, sectionId);
            JOptionPane.showMessageDialog(this, "Registration successful.");
            refreshCatalog();
            refreshRegistrations();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Registration failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------- Registrations tab ----------

    private JPanel buildRegistrationsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        registrationsTable = new JTable();
        registrationsTable.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{"Section ID", "Code", "Title", "Instructor",
                        "Day", "Time", "Room", "Capacity", "Enrolled",
                        "Seats Left", "Sem", "Year"}
        ) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        });

        registrationsTable.setAutoCreateRowSorter(true);

        panel.add(new JScrollPane(registrationsTable), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton dropBtn = new JButton("Drop selected section");
        dropBtn.addActionListener(e -> doDrop());

        JButton exportBtn = new JButton("Export transcript (CSV)");
        exportBtn.addActionListener(e -> doExportTranscript());

        bottom.add(dropBtn);
        bottom.add(exportBtn);

        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private void doDrop() {
        int row = registrationsTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a registration to drop.");
            return;
        }

        int modelRow = registrationsTable.convertRowIndexToModel(row);
        int sectionId = (Integer) registrationsTable.getModel().getValueAt(modelRow, 0);

        try {
            studentService.drop(session, sectionId);
            JOptionPane.showMessageDialog(this, "Drop successful.");
            refreshCatalog();
            refreshRegistrations();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Drop failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doExportTranscript() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save transcript CSV");
        chooser.setSelectedFile(new File("transcript_" + session.getUsername() + ".csv"));
        int res = chooser.showSaveDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;

        File f = chooser.getSelectedFile();
        try {
            studentService.exportTranscriptCsv(session, f);
            JOptionPane.showMessageDialog(this, "Transcript exported successfully to: " + f.getAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(),
                    "Export error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------- Timetable tab ----------

    private JPanel buildTimetablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        timetableModel = new TimetableTableModel(List.of());
        timetableTable = new JTable(timetableModel);
        timetableTable.setAutoCreateRowSorter(true);
        panel.add(new JScrollPane(timetableTable), BorderLayout.CENTER);
        return panel;
    }

    // ---------- Grades tab ----------

    private JPanel buildGradesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        gradesModel = new GradesTableModel(List.of());
        gradesTable = new JTable(gradesModel);
        gradesTable.setAutoCreateRowSorter(true);
        panel.add(new JScrollPane(gradesTable), BorderLayout.CENTER);
        return panel;
    }

    // ---------- Data loading ----------

    private void refreshCatalog() {
        try {
            List<CatalogSectionRow> rows = studentService.viewCatalog(session);
            DefaultTableModel model = (DefaultTableModel) catalogTable.getModel();
            model.setRowCount(0);
            for (CatalogSectionRow r : rows) {
                model.addRow(new Object[]{
                        r.getSectionId(),
                        r.getCourseCode(),
                        r.getCourseTitle(),
                        r.getInstructorName(),
                        r.getDayOfWeek(),
                        r.getTimeRange(),
                        r.getRoom(),
                        r.getCapacity(),
                        r.getEnrolled(),
                        r.getSeatsLeft(),
                        r.getSemester(),
                        r.getYear()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to load catalog: " + ex.getMessage());
        }
    }

    private void refreshRegistrations() {
        try {
            List<CatalogSectionRow> rows = studentService.viewMyRegistrations(session);
            DefaultTableModel model = (DefaultTableModel) registrationsTable.getModel();
            model.setRowCount(0);
            for (CatalogSectionRow r : rows) {
                model.addRow(new Object[]{
                        r.getSectionId(),
                        r.getCourseCode(),
                        r.getCourseTitle(),
                        r.getInstructorName(),
                        r.getDayOfWeek(),
                        r.getTimeRange(),
                        r.getRoom(),
                        r.getCapacity(),
                        r.getEnrolled(),
                        r.getSeatsLeft(),
                        r.getSemester(),
                        r.getYear()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to load registrations: " + ex.getMessage());
        }
    }

    private void refreshTimetable() {
        try {
            List<StudentTimetableRow> rows = studentService.viewTimetable(session);
            timetableModel.setData(rows);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to load timetable: " + ex.getMessage());
        }
    }

    private void refreshGrades() {
        try {
            List<StudentGradeRow> rows = studentService.viewGrades(session);
            gradesModel.setData(rows);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to load grades: " + ex.getMessage());
        }
    }
}
