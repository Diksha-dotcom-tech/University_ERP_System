package edu.univ.erp.service;

import edu.univ.erp.access.AccessDeniedException;
import edu.univ.erp.auth.SessionContext;
import edu.univ.erp.data.AdminDao;
import edu.univ.erp.data.SettingsDao;
import edu.univ.erp.domain.CourseOption;
import edu.univ.erp.domain.InstructorOption;
import edu.univ.erp.domain.Role;
import edu.univ.erp.domain.UserAuth;

import java.sql.Time;
import java.sql.SQLException;
import java.util.List;

public class AdminService {

    private final SettingsDao settingsDao = new SettingsDao();
    private final AdminDao adminDao = new AdminDao();

    private void ensureAdmin(SessionContext session) throws AccessDeniedException {
        if (session == null || session.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only admin can perform this action.");
        }
    }

    // ===== Maintenance =====

    public boolean isMaintenanceOn(SessionContext session) throws AccessDeniedException {
        ensureAdmin(session);
        try {
            return settingsDao.isMaintenanceOn();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read maintenance flag: Database error.", e);
        }
    }

    public void setMaintenance(SessionContext session, boolean on) throws AccessDeniedException {
        ensureAdmin(session);
        try {
            settingsDao.setMaintenanceOn(on);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update maintenance flag: Database error.", e);
        }
    }

    // ===== Users =====

    /**
     * Lists all user account details from the Auth DB (for Admin UI integration).
     */
    public List<UserAuth> listAllUserAuths(SessionContext session) throws Exception {
        ensureAdmin(session);
        return adminDao.listAllUserAuths();
    }


    public void createStudentUser(SessionContext session,
                                  String username,
                                  String plainPassword,
                                  String rollNo,
                                  String program,
                                  int year) throws Exception {

        ensureAdmin(session);
        int userId = -1;

        try {
            // Step 1: Create user in Auth DB (secure hash)
            userId = adminDao.createAuthUser(username, Role.STUDENT, plainPassword);

            // Step 2: Create user profile in ERP DB
            adminDao.createStudentProfile(userId, rollNo, program, year);

        } catch (Exception e) {
            // CRITICAL ROLLBACK LOGIC: If profile creation failed, the Auth user must be deleted.
            if (userId != -1) {
                try {
                    // Placeholder for cleanup. Requires AdminDao.deleteAuthUser(userId)
                    System.err.println("CRITICAL: Failed to create student profile. Auth user with ID " + userId + " was partially created and needs manual cleanup.");
                } catch (Exception cleanupException) {
                    System.err.println("FATAL: Failed to clean up partial Auth user: " + cleanupException.getMessage());
                }
            }
            throw e;
        }
    }

    public void createInstructorUser(SessionContext session,
                                     String username,
                                     String plainPassword,
                                     String department) throws Exception {

        ensureAdmin(session);
        int userId = -1;

        try {
            // Step 1: Create user in Auth DB (secure hash)
            userId = adminDao.createAuthUser(username, Role.INSTRUCTOR, plainPassword);

            // Step 2: Create user profile in ERP DB
            adminDao.createInstructorProfile(userId, department);

        } catch (Exception e) {
            if (userId != -1) {
                try {
                    System.err.println("CRITICAL: Failed to create instructor profile. Auth user with ID " + userId + " was partially created and needs manual cleanup.");
                } catch (Exception cleanupException) {
                    System.err.println("FATAL: Failed to clean up partial Auth user: " + cleanupException.getMessage());
                }
            }
            throw e;
        }
    }

    public void createAdminUser(SessionContext session,
                                String username,
                                String plainPassword) throws Exception {

        ensureAdmin(session);
        adminDao.createAuthUser(username, Role.ADMIN, plainPassword);
    }

    public List<InstructorOption> listInstructors(SessionContext session) throws Exception {
        ensureAdmin(session);
        return adminDao.listAllInstructors();
    }

    // ===== Courses & Sections =====

    public int createCourse(SessionContext session,
                            String code,
                            String title,
                            int credits) throws Exception {

        ensureAdmin(session);
        // NOTE: Input validation (e.g., credits > 0) should be done here if not done in UI
        if (credits <= 0) {
            throw new IllegalArgumentException("Course credits must be positive.");
        }
        return adminDao.createCourse(code, title, credits);
    }

    public List<CourseOption> listCourses(SessionContext session) throws Exception {
        ensureAdmin(session);
        return adminDao.listCourses();
    }

    public void createSection(SessionContext session,
                              int courseId,
                              int instructorUserId,
                              String dayOfWeek,
                              Time start,
                              Time end,
                              String room,
                              int capacity,
                              String semester,
                              int year) throws Exception {

        ensureAdmin(session);

        // Basic input validation
        if (capacity <= 0) {
            throw new IllegalArgumentException("Section capacity must be positive.");
        }

        adminDao.createSection(courseId, instructorUserId, dayOfWeek,
                start, end, room, capacity, semester, year);
    }
}