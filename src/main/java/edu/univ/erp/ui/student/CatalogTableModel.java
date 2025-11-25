package edu.univ.erp.ui.student;

import edu.univ.erp.domain.CatalogSectionRow;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class CatalogTableModel extends AbstractTableModel {

    private final String[] columns = {
            "Section ID", "Code", "Title", "Credits",
            "Instructor", "Day", "Time", "Room",
            "Capacity", "Enrolled", "Seats Left", "Sem", "Year"
    };

    private List<CatalogSectionRow> data;

    public CatalogTableModel(List<CatalogSectionRow> data) {
        this.data = data;
    }

    public void setData(List<CatalogSectionRow> data) {
        this.data = data;
        fireTableDataChanged();
    }

    public CatalogSectionRow getRow(int rowIndex) {
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
    public Object getValueAt(int rowIndex, int columnIndex) {
        CatalogSectionRow row = data.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> row.getSectionId();
            case 1 -> row.getCourseCode();
            case 2 -> row.getCourseTitle();
            case 3 -> row.getCredits();
            case 4 -> row.getInstructorName();
            case 5 -> row.getDayOfWeek();
            case 6 -> row.getTimeRange();
            case 7 -> row.getRoom();
            case 8 -> row.getCapacity();
            case 9 -> row.getEnrolled();
            case 10 -> row.getSeatsLeft();
            case 11 -> row.getSemester();
            case 12 -> row.getYear();
            default -> "";
        };
    }
}

