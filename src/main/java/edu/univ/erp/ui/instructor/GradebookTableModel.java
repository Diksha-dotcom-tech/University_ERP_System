package edu.univ.erp.ui.instructor;

import edu.univ.erp.domain.GradeRow;

import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.ArrayList;

public class GradebookTableModel extends AbstractTableModel {

    private final String[] columns = {
            "Enrollment ID", "Roll No", "Name",
            "Quiz", "Midterm", "End-Sem", "Final", "Grade"
    };

    private List<GradeRow> data;

    public GradebookTableModel(List<GradeRow> data) {
        this.data = data != null ? data : new ArrayList<>();
    }

    public void setData(List<GradeRow> data) {
        this.data = data;
        fireTableDataChanged();
    }

    // Crucial getter for the Service layer to retrieve edited data
    public List<GradeRow> getData() {
        return data;
    }

    public GradeRow getRow(int rowIndex) {
        return data.get(rowIndex);
    }

    @Override
    public int getRowCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 3, 4, 5, 6 -> Double.class; // Quiz, Midterm, Endsem, Final Score
            default -> Object.class;
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        // allow editing of Quiz, Midterm, End-Sem only (3, 4, 5)
        return columnIndex == 3 || columnIndex == 4 || columnIndex == 5;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        GradeRow row = data.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> row.getEnrollmentId();
            case 1 -> row.getRollNo();
            case 2 -> row.getStudentName();
            case 3 -> row.getQuizScore();
            case 4 -> row.getMidtermScore();
            case 5 -> row.getEndsemScore();
            case 6 -> row.getFinalScore();
            case 7 -> row.getFinalGradeText();
            default -> null;
        };
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        GradeRow row = data.get(rowIndex);
        try {
            Double val = null;
            if (aValue != null && !aValue.toString().trim().isEmpty()) {
                double parsedVal = Double.parseDouble(aValue.toString().trim());
                if (parsedVal < 0 || parsedVal > 100) {
                    return;
                }
                val = parsedVal;
            }
            switch (columnIndex) {
                case 3 -> row.setQuizScore(val);
                case 4 -> row.setMidtermScore(val);
                case 5 -> row.setEndsemScore(val);
            }
            fireTableCellUpdated(rowIndex, columnIndex);
        } catch (NumberFormatException ex) {
            // ignore bad input quietly
        }
    }
}