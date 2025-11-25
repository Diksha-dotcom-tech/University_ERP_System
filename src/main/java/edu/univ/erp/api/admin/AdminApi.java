package edu.univ.erp.api.admin;

import edu.univ.erp.access.AccessDeniedException;
import edu.univ.erp.auth.SessionContext;
import edu.univ.erp.domain.CourseOption;
import edu.univ.erp.domain.InstructorOption;
import edu.univ.erp.service.AdminService;

import java.sql.SQLException;
import java.sql.Time;
import java.util.List;

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
