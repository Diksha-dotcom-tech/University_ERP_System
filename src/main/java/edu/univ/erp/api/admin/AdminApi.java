
package edu.univ.erp.api.admin;

import edu.univ.erp.access.AccessDeniedException;
import edu.univ.erp.auth.SessionContext;
import edu.univ.erp.domain.CourseOption;
import edu.univ.erp.domain.InstructorOption;
import edu.univ.erp.service.AdminService;

import java.sql.SQLException;
import java.sql.Time;
import java.util.List;

/**
 * API layer placeholder for future extension.
 * UI currently interacts directly with Service layer.
 * This class is intentionally kept for demonstrating a multi-tier architecture.
 */
@SuppressWarnings("unused")

public class AdminApi {

    private final AdminService adminService = new AdminService();

    // ===== Maintenance =====

    public boolean isMaintenanceOn(SessionContext session) throws SQLException, AccessDeniedException {
        return adminService.isMaintenanceOn(session);
    }

    public void setMaintenance(SessionContext session, boolean on) throws SQLException, AccessDeniedException {
        adminService.setMaintenance(session, on);
    }

    // ===== Users =====

    public void createStudentUser(SessionContext session,
                                  String username,
                                  String password,
                                  String rollNo,
                                  String program,
                                  int year) throws Exception {
        adminService.createStudentUser(session, username, password, rollNo, program, year);
    }

    public void createInstructorUser(SessionContext session,
                                     String username,
                                     String password,
                                     String department) throws Exception {
        adminService.createInstructorUser(session, username, password, department);
    }

    public void createAdminUser(SessionContext session,
                                String username,
                                String password) throws Exception {
        adminService.createAdminUser(session, username, password);
    }

    public List<InstructorOption> listInstructors(SessionContext session) throws Exception {
        return adminService.listInstructors(session);
    }

    // ===== Courses & Sections =====

    public int createCourse(SessionContext session,
                            String code,
                            String title,
                            int credits) throws Exception {
        return adminService.createCourse(session, code, title, credits);
    }
    /**
     * Updates an existing course's details and the capacity of its associated sections.
     * @param session The current user session (must be Admin).
     * @param courseId The ID of the course to update.
     * @param code The new course code (e.g., "CS101").
     * @param title The new course title.
     * @param credits The new credit value.
     * @param capacity The new capacity for all sections of this course. // NEW PARAM
     * @throws Exception If access is denied or update fails.
     */
    public void updateCourse(SessionContext session,
                             int courseId,
                             String code,
                             String title,
                             int credits,
                             int capacity) throws Exception { // <--- ADD CAPACITY HERE
        // Pass all parameters, including the new 'capacity'
        adminService.updateCourse(session, courseId, code, title, credits, capacity);
    }

    /**
     * Deletes a course from the system.
     * @param session The current user session (must be Admin).
     * @param courseId The ID of the course to delete.
     * @throws Exception If access is denied or deletion fails.
     */
    public void deleteCourse(SessionContext session, int courseId) throws Exception {
        adminService.deleteCourse(session, courseId);
    }

    public List<CourseOption> listCourses(SessionContext session) throws Exception {
        return adminService.listCourses(session);
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
        adminService.createSection(session, courseId, instructorUserId,
                dayOfWeek, start, end, room, capacity, semester, year);
    }
}
