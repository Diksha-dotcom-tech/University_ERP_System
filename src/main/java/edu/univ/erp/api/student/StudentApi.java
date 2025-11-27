package edu.univ.erp.api.student;

import edu.univ.erp.auth.SessionContext;
import edu.univ.erp.domain.CatalogSectionRow;
import edu.univ.erp.domain.StudentGradeRow;
import edu.univ.erp.domain.StudentTimetableRow;
import edu.univ.erp.service.StudentService;

import java.util.List;
/**
 * API layer placeholder for future extension.
 * UI currently interacts directly with Service layer.
 * This class is intentionally kept for demonstrating a multi-tier architecture.
 */
@SuppressWarnings("unused")
public class StudentApi {

    private final StudentService studentService = new StudentService();

    public List<CatalogSectionRow> getCatalog(SessionContext session) throws Exception {
        return studentService.viewCatalog(session);
    }

    public List<CatalogSectionRow> getMyRegistrations(SessionContext session) throws Exception {
        return studentService.viewMyRegistrations(session);
    }

    public void register(SessionContext session, int sectionId) throws Exception {
        studentService.register(session, sectionId);
    }

    public void drop(SessionContext session, int sectionId) throws Exception {
        studentService.drop(session, sectionId);
    }

    public List<StudentTimetableRow> getTimetable(SessionContext session) throws Exception {
        return studentService.viewTimetable(session);
    }

    public List<StudentGradeRow> getGrades(SessionContext session) throws Exception {
        return studentService.viewGrades(session);
    }
}
