package edu.univ.erp.ui.student;

import edu.univ.erp.domain.StudentTimetableRow;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class TimetableTableModel extends AbstractTableModel {

    private final String[] columns = {
            "Day", "Time", "Course Code", "Title", "Room", "Semester", "Year"
    };

    private List<StudentTimetableRow> data;

    public TimetableTableModel(List<StudentTimetableRow> data) {
        this.data = data;
    }

    public void setData(List<StudentTimetableRow> data) {
        this.data = data;
        fireTableDataChanged();
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
        StudentTimetableRow row = data.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> row.getDayOfWeek();
            case 1 -> row.getTimeRange();
            case 2 -> row.getCourseCode();
            case 3 -> row.getCourseTitle();
            case 4 -> row.getRoom();
            case 5 -> row.getSemester();
            case 6 -> row.getYear();
            default -> "";
        };
    }
}
