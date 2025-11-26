package edu.univ.erp.service;

import edu.univ.erp.access.AccessDeniedException;
import edu.univ.erp.access.AccessManager;
import edu.univ.erp.auth.SessionContext;
import edu.univ.erp.data.AdminDao;
import edu.univ.erp.data.SettingsDao;
import edu.univ.erp.domain.CourseOption;
import edu.univ.erp.domain.InstructorOption;
import edu.univ.erp.domain.Role;
import edu.univ.erp.domain.UserAuth;

import java.sql.Time;
import java.util.List;

public class AdminService {

    private final SettingsDao settingsDao = new SettingsDao();
    private final AdminDao adminDao = new AdminDao();
    private final AccessManager access = AccessManager.getInstance();

    // ===== Maintenance =====

    public boolean isMaintenanceOn(SessionContext session) {
        access.ensureAdmin(session);
        try {
            return settingsDao.isMaintenanceOn();
        } catch (Exception e) {
            throw new RuntimeException("Database error checking maintenance flag.", e);
        }
    }

    public void setMaintenance(SessionContext session, boolean on) {
        access.ensureAdmin(session);
        try {
            settingsDao.setMaintenanceOn(on);
        } catch (Exception e) {
            throw new RuntimeException("Database error updating maintenance flag.", e);
        }
    }

    // ===== Users =====

    public List<UserAuth> listAllUserAuths(SessionContext session) throws Exception {
        access.ensureAdmin(session);
        return adminDao.listAllUserAuths();
    }

    public void createStudentUser(SessionContext session,
                                  String username,
                                  String password,
                                  String rollNo,
                                  String program,
                                  int year) throws Exception {

        access.ensureAdmin(session);

        int userId = -1;
        try {
            userId = adminDao.createAuthUser(username, Role.STUDENT, password);
            adminDao.createStudentProfile(userId, rollNo, program, year);

        } catch (Exception e) {
            if (userId != -1) {
                adminDao.deleteAuthUser(userId); // FULL ROLLBACK ✔
            }
            throw new RuntimeException("Could not create student user: " + e.getMessage(), e);
        }
    }

    public void createInstructorUser(SessionContext session,
                                     String username,
                                     String password,
                                     String department) throws Exception {

        access.ensureAdmin(session);

        int userId = -1;
        try {
            userId = adminDao.createAuthUser(username, Role.INSTRUCTOR, password);
            adminDao.createInstructorProfile(userId, department);

        } catch (Exception e) {
            if (userId != -1) {
                adminDao.deleteAuthUser(userId); // FULL ROLLBACK ✔
            }
            throw new RuntimeException("Could not create instructor user: " + e.getMessage(), e);
        }
    }

    public void createAdminUser(SessionContext session,
                                String username,
                                String password) throws Exception {

        access.ensureAdmin(session);
        adminDao.createAuthUser(username, Role.ADMIN, password);
    }

    public List<InstructorOption> listInstructors(SessionContext session) throws Exception {
        access.ensureAdmin(session);
        return adminDao.listAllInstructors();
    }

    // ===== Courses & Sections =====

    public int createCourse(SessionContext session,
                            String code,
                            String title,
                            int credits) throws Exception {

        access.ensureAdmin(session);

        if (code == null || code.isBlank()) throw new IllegalArgumentException("Course code required.");
        if (title == null || title.isBlank()) throw new IllegalArgumentException("Course title required.");
        if (credits <= 0) throw new IllegalArgumentException("Credits must be > 0.");

        return adminDao.createCourse(code, title, credits);
    }

    public List<CourseOption> listCourses(SessionContext session) throws Exception {
        access.ensureAdmin(session);
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

        access.ensureAdmin(session);

        if (capacity <= 0) throw new IllegalArgumentException("Capacity must be > 0.");
        if (room == null || room.isBlank()) throw new IllegalArgumentException("Room required.");

        adminDao.createSection(courseId, instructorUserId, dayOfWeek,
                start, end, room, capacity, semester, year);
    }
}
