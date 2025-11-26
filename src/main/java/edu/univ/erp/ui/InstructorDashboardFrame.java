package edu.univ.erp.ui;

import edu.univ.erp.access.AccessDeniedException;
import edu.univ.erp.access.AccessManager;
import edu.univ.erp.auth.SessionContext;
import edu.univ.erp.domain.InstructorSectionRow;
import edu.univ.erp.domain.GradeRow;
import edu.univ.erp.service.InstructorService;
import edu.univ.erp.ui.common.ChangePasswordDialog;
import edu.univ.erp.ui.common.UserProfileDialog;
import edu.univ.erp.ui.instructor.GradebookTableModel;
import edu.univ.erp.ui.common.ChangePasswordDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Instructor dashboard:
 * - View "my sections"
 * - Enter scores per student
 * - Compute final grades
 * - View simple stats
 * - Export grades CSV
 */
public class InstructorDashboardFrame extends JFrame {

    private final SessionContext session;
    private final InstructorService instructorService = new InstructorService();
    private final AccessManager accessManager = AccessManager.getInstance();

    private JLabel lblMaintenanceBanner;

    private JTable tblSections;
    private JTable tblGrades;

    private DefaultTableModel sectionsModel;
    private GradebookTableModel gradesTableModel;

    public InstructorDashboardFrame(SessionContext session) {
        this.session = session;

        setTitle("Instructor Dashboard - " + session.getUsername());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1050, 600);
        setLocationRelativeTo(null);

        initMenuBar();
        setContentPane(buildContent());
        refreshMaintenanceBanner();
        loadSections();
    }

    // ======================= MENU =======================

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

    // ======================= CONTENT =======================

    private JComponent buildContent() {
        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setBorder(new EmptyBorder(5, 5, 5, 5));

        lblMaintenanceBanner = new JLabel("MAINTENANCE MODE IS ON – You can view, but cannot change grades.");
        lblMaintenanceBanner.setOpaque(true);
        lblMaintenanceBanner.setBackground(new Color(255, 230, 230));
        lblMaintenanceBanner.setForeground(new Color(160, 0, 0));
        lblMaintenanceBanner.setHorizontalAlignment(SwingConstants.CENTER);
        lblMaintenanceBanner.setVisible(false);
        root.add(lblMaintenanceBanner, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                buildSectionsPanel(),
                buildGradesPanel());
        split.setResizeWeight(0.4);
        root.add(split, BorderLayout.CENTER);

        return root;
    }

    private JComponent buildSectionsPanel() {
        String[] cols = {
                "Section ID", "Code", "Title",
                "Day", "Time", "Room",
                "Capacity", "Enrolled", "Sem", "Year"
        };
        sectionsModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblSections = new JTable(sectionsModel);
        tblSections.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblSections.setAutoCreateRowSorter(true);

        tblSections.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadGradesForSelectedSection();
        });

        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("My Sections"));
        p.add(new JScrollPane(tblSections), BorderLayout.CENTER);

        JButton btnExport = new JButton("Export grades CSV for selected");
        btnExport.addActionListener(e -> onExportGrades());

        p.add(btnExport, BorderLayout.SOUTH);
        return p;
    }

    private JComponent buildGradesPanel() {
        gradesTableModel = new GradebookTableModel(new ArrayList<>());
        tblGrades = new JTable(gradesTableModel);
        tblGrades.setAutoCreateRowSorter(true);

        tblGrades.setDefaultEditor(Double.class, new DefaultCellEditor(new JTextField()));
        tblGrades.setDefaultRenderer(Double.class, (table, value, isSelected, hasFocus, row, column) -> {
            String text = "";
            if (value instanceof Double d) {
                text = String.format("%.1f", d);
            }
            JLabel label = new JLabel(text);
            label.setHorizontalAlignment(SwingConstants.RIGHT);
            if (isSelected) {
                label.setOpaque(true);
                label.setBackground(table.getSelectionBackground());
                label.setForeground(table.getSelectionForeground());
            }
            return label;
        });

        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Gradebook"));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSaveAndCompute = new JButton("Save & Compute Final Grades");
        btnSaveAndCompute.addActionListener(e -> onSaveAndCompute());

        JButton btnViewStats = new JButton("View Class Average");
        btnViewStats.addActionListener(e -> onViewStats());

        buttons.add(btnSaveAndCompute);
        buttons.add(btnViewStats);

        p.add(new JScrollPane(tblGrades), BorderLayout.CENTER);
        p.add(buttons, BorderLayout.SOUTH);

        return p;
    }

    // ======================= ACTIONS =======================

    private void refreshMaintenanceBanner() {
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return accessManager.isReadOnly(session);
            }

            @Override
            protected void done() {
                try {
                    boolean readOnly = get();
                    lblMaintenanceBanner.setVisible(readOnly);
                } catch (Exception ex) {
                    lblMaintenanceBanner.setText("Error checking maintenance status: " + ex.getMessage());
                    lblMaintenanceBanner.setVisible(true);
                }
            }
        }.execute();
    }

    private void loadSections() {
        new SwingWorker<List<InstructorSectionRow>, Void>() {
            @Override
            protected List<InstructorSectionRow> doInBackground() throws Exception {
                return instructorService.getMySections(session);
            }

            @Override
            protected void done() {
                try {
                    List<InstructorSectionRow> rows = get();
                    sectionsModel.setRowCount(0);
                    for (InstructorSectionRow r : rows) {
                        sectionsModel.addRow(new Object[]{
                                r.getSectionId(),
                                r.getCourseCode(),
                                r.getCourseTitle(),
                                r.getDayOfWeek(),
                                r.getTimeRange(),
                                r.getRoom(),
                                r.getCapacity(),
                                r.getEnrolledCount(),
                                r.getSemester(),
                                r.getYear()
                        });
                    }
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    showError("Failed to load sections: " + cause.getMessage());
                }
            }
        }.execute();
    }

    private Integer getSelectedSectionId() {
        int row = tblSections.getSelectedRow();
        if (row < 0) return null;
        int modelRow = tblSections.convertRowIndexToModel(row);
        return (Integer) sectionsModel.getValueAt(modelRow, 0);
    }

    private void loadGradesForSelectedSection() {
        Integer sectionId = getSelectedSectionId();
        if (sectionId == null) {
            gradesTableModel.setData(new ArrayList<>());
            return;
        }

        new SwingWorker<List<GradeRow>, Void>() {
            @Override
            protected List<GradeRow> doInBackground() throws Exception {
                return instructorService.getGradebook(session, sectionId);
            }

            @Override
            protected void done() {
                try {
                    List<GradeRow> rows = get();
                    gradesTableModel.setData(rows);
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    showError("Failed to load grades: " + cause.getMessage());
                }
            }
        }.execute();
    }

    private void onSaveAndCompute() {
        Integer sectionId = getSelectedSectionId();
        if (sectionId == null) {
            JOptionPane.showMessageDialog(this, "Select a section first.");
            return;
        }

        if (tblGrades.isEditing()) {
            tblGrades.getCellEditor().stopCellEditing();
        }

        List<GradeRow> rowsToSave = gradesTableModel.getData();

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                instructorService.saveScoresAndComputeFinal(session, sectionId, rowsToSave);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    loadGradesForSelectedSection();
                    JOptionPane.showMessageDialog(InstructorDashboardFrame.this,
                            "Scores saved and final grades computed successfully.");
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    showError("Operation failed: " + cause.getMessage());
                }
            }
        }.execute();
    }

    private void onViewStats() {
        Integer sectionId = getSelectedSectionId();
        if (sectionId == null) {
            JOptionPane.showMessageDialog(this, "Select a section first.");
            return;
        }

        new SwingWorker<Double, Void>() {
            @Override
            protected Double doInBackground() throws Exception {
                return instructorService.computeClassAverage(session, sectionId);
            }

            @Override
            protected void done() {
                try {
                    Double average = get();
                    String msg = (average != null)
                            ? String.format("Class Average (based on calculated final scores): %.2f", average)
                            : "No final scores available to calculate the class average.";
                    JOptionPane.showMessageDialog(InstructorDashboardFrame.this,
                            msg, "Class Statistics",
                            JOptionPane.INFORMATION_MESSAGE);

                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    showError("Failed to compute class average: " + cause.getMessage());
                }
            }
        }.execute();
    }

    private void onExportGrades() {
        Integer sectionId = getSelectedSectionId();
        if (sectionId == null) {
            JOptionPane.showMessageDialog(this, "Select a section first.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save grades CSV");
        chooser.setSelectedFile(new File("grades_section_" + sectionId + ".csv"));
        int res = chooser.showSaveDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                instructorService.exportGradesCsv(session, sectionId, file);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(InstructorDashboardFrame.this,
                            "Grades exported to " + file.getAbsolutePath());
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    showError("Failed to export grades: " + cause.getMessage());
                }
            }
        }.execute();
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg,
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}
