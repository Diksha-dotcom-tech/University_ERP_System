package edu.univ.erp.ui;

import edu.univ.erp.access.AccessDeniedException;
import edu.univ.erp.access.AccessManager;
import edu.univ.erp.auth.SessionContext;
import edu.univ.erp.domain.InstructorSectionRow;
import edu.univ.erp.domain.GradeRow;
import edu.univ.erp.service.InstructorService;
import edu.univ.erp.ui.common.UserProfileDialog;
import edu.univ.erp.ui.instructor.GradebookTableModel;
import edu.univ.erp.ui.LoginFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

    // ======================= CONTENT =======================

    private JComponent buildContent() {
        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setBorder(new EmptyBorder(5, 5, 5, 5));

        lblMaintenanceBanner = new JLabel("MAINTENANCE MODE IS ON – You can view, but cannot change grades.");
        lblMaintenanceBanner.setOpaque(true);
        lblMaintenanceBanner.setBackground(new Color(255, 230, 230));
        lblMaintenanceBanner.setForeground(new Color(160, 0, 0));
        lblMaintenanceBanner.setHorizontalAlignment(SwingConstants.CENTER);
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

        // Setup renderer/editor to handle Double values
        tblGrades.setDefaultEditor(Double.class, new DefaultCellEditor(new JTextField()));
        tblGrades.setDefaultRenderer(Double.class, (table, value, isSelected, hasFocus, row, column) -> {
            JLabel label = new JLabel(value != null ? String.format("%.1f", value) : "");
            label.setHorizontalAlignment(SwingConstants.RIGHT);
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
        try {
            boolean readOnly = accessManager.isReadOnly(session);
            lblMaintenanceBanner.setVisible(readOnly);
        } catch (Exception ex) {
            lblMaintenanceBanner.setText("Error checking maintenance status: " + ex.getMessage());
            lblMaintenanceBanner.setVisible(true);
        }
    }

    private void loadSections() {
        try {
            List<InstructorSectionRow> rows = instructorService.getMySections(session);
            sectionsModel.setRowCount(0);
            for (InstructorSectionRow r : rows) {
                sectionsModel.addRow(new Object[]{
                        r.getSectionId(),
                        r.getCourseCode(),
                        r.getCourseTitle(),
                        r.getDayOfWeek(),
                        r.getTimeRange(),
                        r.getRoom(),
                        r.getEnrolledCount(),
                        r.getEnrolledCount(),
                        r.getSemester(),
                        r.getYear()
                });
            }
        } catch (Exception ex) {
            showError("Failed to load sections: " + ex.getMessage());
        }
    }

    private Integer getSelectedSectionId() {
        int row = tblSections.getSelectedRow();
        if (row < 0) return null;
        // Correctly handle row sorter
        int modelRow = tblSections.convertRowIndexToModel(row);
        return (Integer) sectionsModel.getValueAt(modelRow, 0);
    }

    private void loadGradesForSelectedSection() {
        Integer sectionId = getSelectedSectionId();
        if (sectionId == null) {
            gradesTableModel.setData(new ArrayList<>());
            return;
        }

        try {
            // FIX: Corrected service method name to getGradebook
            List<GradeRow> rows = instructorService.getGradebook(session, sectionId);
            // FIX: Set data on the custom table model
            gradesTableModel.setData(rows);
        } catch (Exception ex) {
            showError("Failed to load grades: " + ex.getMessage());
        }
    }

    /**
     * Handles saving all edited scores and then computing the final grade based on those scores.
     */
    private void onSaveAndCompute() {
        Integer sectionId = getSelectedSectionId();
        if (sectionId == null) {
            JOptionPane.showMessageDialog(this, "Select a section first.");
            return;
        }

        // Ensure the JTable stops editing before we read the model's data
        if (tblGrades.isEditing()) {
            tblGrades.getCellEditor().stopCellEditing();
        }

        try {
            // Get the list of DTOs currently in the table model
            List<GradeRow> rowsToSave = gradesTableModel.getData();

            // FIX: Use the single, correct service method
            instructorService.saveScoresAndComputeFinal(session, sectionId, rowsToSave);

            // Refresh the table to show the newly computed final score/grade text
            loadGradesForSelectedSection();

            JOptionPane.showMessageDialog(this, "Scores saved and final grades computed successfully.");
        } catch (AccessDeniedException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Operation failed: " + ex.getMessage());
        }
    }

    private void onViewStats() {
        Integer sectionId = getSelectedSectionId();
        if (sectionId == null) {
            JOptionPane.showMessageDialog(this, "Select a section first.");
            return;
        }

        try {
            double average = instructorService.computeClassAverage(session, sectionId);
            JOptionPane.showMessageDialog(this,
                    String.format("Class Average (based on calculated final scores): %.2f", average),
                    "Class Statistics",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (AccessDeniedException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Failed to compute class average: " + ex.getMessage());
        }
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
        try {
            // Placeholder: This method needs to be implemented in the InstructorService
            // to fetch grade data and write it to CSV.
            // instructorService.exportGradesCsv(session, sectionId, file);
            JOptionPane.showMessageDialog(this, "Grades export feature triggered. (I/O logic not implemented in service yet).");
        } catch (Exception ex) {
            showError("Failed to export grades: " + ex.getMessage());
        }
    }

    // ------------------- Utils -------------------

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg,
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}