package edu.univ.erp.ui.student;

import edu.univ.erp.domain.StudentGradeRow;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class GradesTableModel extends AbstractTableModel {

    private final String[] columns = {
            "Course Code", "Title", "Semester", "Year",
            "Quiz", "Midterm", "End-Sem", "Final Score", "Final Grade"
    };

    private List<StudentGradeRow> data;

    public GradesTableModel(List<StudentGradeRow> data) {
        this.data = data;
    }

    public void setData(List<StudentGradeRow> data) {
        this.data = data;
        fireTableDataChanged();
    }

    public List<StudentGradeRow> getData() {
        return data;
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
    public Object getValueAt(int rowIndex, int columnIndex) {
        StudentGradeRow row = data.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> row.getCourseCode();
            case 1 -> row.getCourseTitle();
            case 2 -> row.getSemester();
            case 3 -> row.getYear();
            case 4 -> row.getQuizScore();
            case 5 -> row.getMidtermScore();
            case 6 -> row.getEndsemScore();
            case 7 -> row.getFinalScore();
            case 8 -> row.getFinalGradeText();
            default -> "";
        };
    }
}
