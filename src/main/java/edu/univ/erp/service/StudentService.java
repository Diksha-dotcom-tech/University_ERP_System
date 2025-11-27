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
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    /**
     * Register the current student into the given sectionId.
     *
     * Checks performed (in this order):
     *  - student access & maintenance mode
     *  - section exists in catalog
     *  - registration deadline (per-section)
     *  - duplicate: student is currently ENROLLED (prevents double-register)
     *  - capacity
     *  - attempts to enroll via DAO (handles re-enroll of previously DROPPED rows)
     */
    public void register(SessionContext session, int sectionId)
            throws AccessDeniedException, SQLException {

        accessManager.ensureStudent(session);
        accessManager.ensureNotInMaintenance(session);

        int studentId = session.getUserId();

        // 0. fetch section info from catalog (to get deadlines and optionally other metadata)
        CatalogSectionRow section = findCatalogSectionRow(sectionId);
        if (section == null) {
            throw new AccessDeniedException("Section not found.");
        }

        // 1. registration deadline check (per-section)
        LocalDateTime regDeadline = section.getRegistrationDeadline();
        if (regDeadline != null && LocalDateTime.now().isAfter(regDeadline)) {
            throw new AccessDeniedException("Registration closed for this section. Deadline was: " + regDeadline);
        }

        // 2. Guard against duplicate enrollment (currently ENROLLED)
        if (enrollmentDao.isCurrentlyEnrolled(studentId, sectionId)) {
            throw new AccessDeniedException("You are already registered in this section.");
        }

        // 3. Capacity check
        int enrolled = enrollmentDao.countEnrolledInSection(sectionId);
        int capacity = enrollmentDao.getSectionCapacity(sectionId);
        if (enrolled >= capacity) {
            throw new AccessDeniedException("Section is full.");
        }

        // 4. Attempt to enroll. DAO will:
        //    - insert a new row if none exists,
        //    - or flip a DROPPED row back to ENROLLED.
        // Convert DB exceptions to user-friendly messages.
        try {
            enrollmentDao.enroll(studentId, sectionId);
        } catch (SQLIntegrityConstraintViolationException e) {
            // unlikely with current DAO but keep for safety
            throw new AccessDeniedException("You are already registered in this section.");
        } catch (SQLException e) {
            // EnrollmentDao may throw "Student is already enrolled in this section."
            String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
            if (msg.contains("already enrolled")) {
                throw new AccessDeniedException("You are already registered in this section.");
            }
            // otherwise rethrow to be handled at a higher level (or logged)
            throw e;
        }
    }

    /**
     * Drop the student's enrollment in the given section.
     *
     * Checks:
     *  - student access & maintenance mode
     *  - section exists
     *  - drop deadline (per-section)
     *  - student is currently ENROLLED
     * Then mark dropped via DAO.
     */
    public void drop(SessionContext session, int sectionId)
            throws AccessDeniedException, SQLException {

        accessManager.ensureStudent(session);
        accessManager.ensureNotInMaintenance(session);

        int studentId = session.getUserId();

        // fetch section metadata to check drop deadline
        CatalogSectionRow section = findCatalogSectionRow(sectionId);
        if (section == null) {
            throw new AccessDeniedException("Section not found.");
        }

        LocalDateTime dropDeadline = section.getDropDeadline();
        if (dropDeadline != null && LocalDateTime.now().isAfter(dropDeadline)) {
            throw new AccessDeniedException("Drop period expired. Last date was: " + dropDeadline);
        }

        // ensure currently enrolled (only ENROLLED can be dropped)
        if (!enrollmentDao.isCurrentlyEnrolled(studentId, sectionId)) {
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
     * Transcript export – CSV.
     */
    public void exportTranscriptCsv(SessionContext session, File destinationFile) throws Exception {
        accessManager.ensureStudent(session);

        List<StudentGradeRow> grades = studentViewDao.getGradesForStudent(session.getUserId());

        if (grades.isEmpty()) {
            throw new RuntimeException("No grades found to export.");
        }

        try {
            CsvUtil.writeTranscriptCsv(grades, destinationFile);
        } catch (Exception e) {
            throw new RuntimeException("Failed to export CSV: " + e.getMessage(), e);
        }
    }

    // -----------------------
    // Helper: find section in catalog
    // -----------------------
    private CatalogSectionRow findCatalogSectionRow(int sectionId) throws SQLException {
        List<CatalogSectionRow> full = catalogDao.listCatalog();
        Optional<CatalogSectionRow> opt = full.stream()
                .filter(r -> r.getSectionId() == sectionId)
                .findFirst();
        return opt.orElse(null);
    }
}