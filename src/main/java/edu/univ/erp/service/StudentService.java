package edu.univ.erp.service;

import edu.univ.erp.access.AccessDeniedException;
import edu.univ.erp.access.AccessManager;
import edu.univ.erp.auth.SessionContext;
import edu.univ.erp.data.CatalogDao;
import edu.univ.erp.data.EnrollmentDao;
import edu.univ.erp.data.StudentViewDao;
import edu.univ.erp.domain.CatalogSectionRow;
import edu.univ.erp.domain.StudentGradeRow;
import edu.univ.erp.domain.StudentTimetableRow;
import edu.univ.erp.util.CsvUtil;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

public class StudentService {

    private final CatalogDao catalogDao = new CatalogDao();
    private final EnrollmentDao enrollmentDao = new EnrollmentDao();
    private final StudentViewDao studentViewDao = new StudentViewDao();
    private final AccessManager accessManager = AccessManager.getInstance();

    // ===== Catalog & registrations =====

    public List<CatalogSectionRow> viewCatalog(SessionContext session)
            throws AccessDeniedException, SQLException {

        accessManager.ensureStudent(session);
        return catalogDao.listCatalog();
    }

    public List<CatalogSectionRow> viewMyRegistrations(SessionContext session)
            throws AccessDeniedException, SQLException {

        accessManager.ensureStudent(session);

        int studentId = session.getUserId();
        List<Integer> mySectionIds = enrollmentDao.findMySectionIds(studentId);
        List<CatalogSectionRow> full = catalogDao.listCatalog();

        return full.stream()
                .filter(row -> mySectionIds.contains(row.getSectionId()))
                .toList();
    }

    public void register(SessionContext session, int sectionId)
            throws AccessDeniedException, SQLException {

        accessManager.ensureStudent(session);
        accessManager.ensureNotInMaintenance(session);
        accessManager.ensureBeforeRegistrationDeadline();

        int studentId = session.getUserId();

        if (enrollmentDao.hasEnrollment(studentId, sectionId)) {
            throw new AccessDeniedException("You are already registered in this section.");
        }

        int enrolled = enrollmentDao.countEnrolledInSection(sectionId);
        int capacity = enrollmentDao.getSectionCapacity(sectionId);
        if (enrolled >= capacity) {
            throw new AccessDeniedException("Section is full.");
        }

        enrollmentDao.enroll(studentId, sectionId);
    }

    public void drop(SessionContext session, int sectionId)
            throws AccessDeniedException, SQLException {

        accessManager.ensureStudent(session);
        accessManager.ensureNotInMaintenance(session);
        accessManager.ensureBeforeDropDeadline();

        int studentId = session.getUserId();
        if (!enrollmentDao.hasEnrollment(studentId, sectionId)) {
            throw new AccessDeniedException("You are not enrolled in this section.");
        }

        enrollmentDao.drop(studentId, sectionId);
    }

    // ===== Timetable & grades =====

    public List<StudentTimetableRow> viewTimetable(SessionContext session)
            throws AccessDeniedException, SQLException {

        accessManager.ensureStudent(session);
        int studentId = session.getUserId();
        return studentViewDao.getTimetableForStudent(studentId);
    }

    public List<StudentGradeRow> viewGrades(SessionContext session)
            throws AccessDeniedException, SQLException {

        accessManager.ensureStudent(session);
        int studentId = session.getUserId();
        return studentViewDao.getGradesForStudent(studentId);
    }

    /**
     * Export transcript as CSV using CsvUtil.
     */
    public void exportTranscriptCsv(SessionContext session, File destinationFile) throws Exception {
        accessManager.ensureStudent(session);

        List<StudentGradeRow> grades = studentViewDao.getGradesForStudent(session.getUserId());
        CsvUtil.writeTranscriptCsv(grades, destinationFile);
    }
}
