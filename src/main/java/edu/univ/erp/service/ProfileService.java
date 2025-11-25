package edu.univ.erp.service;

import edu.univ.erp.access.AccessDeniedException;
import edu.univ.erp.auth.SessionContext;
import edu.univ.erp.data.UserProfileDao;
import edu.univ.erp.data.UserProfileDao.StudentProfile;
import edu.univ.erp.data.UserProfileDao.InstructorProfile;
import edu.univ.erp.data.UserProfileDao.AdminInfo;
import edu.univ.erp.domain.Role;

/**
 * Service to retrieve complete user profile, combining Auth DB info (username, last login)
 * and ERP DB info (roll no, department).
 */
public class ProfileService {

    private final UserProfileDao profileDao = new UserProfileDao();

    /**
     * Retrieves all profile data needed for the dialog, packaged into a single DTO.
     */
    public ProfileDto getProfile(SessionContext session) throws Exception {
        if (session == null) {
            throw new AccessDeniedException("No active session.");
        }

        int userId = session.getUserId();
        Role role = session.getRole();

        ProfileDto dto = new ProfileDto();
        dto.username = session.getUsername();
        dto.role = role.toString();
        dto.lastLogin = profileDao.getLastLogin(userId);

        switch (role) {
            case STUDENT:
                StudentProfile student = profileDao.getStudentProfile(userId);
                if (student != null) {
                    dto.primaryInfo = "Roll No: " + student.roll() + "\nProgram: " + student.program() + " (Year " + student.year() + ")";
                } else {
                    dto.primaryInfo = "Student profile data incomplete.";
                }
                break;

            case INSTRUCTOR:
                InstructorProfile instructor = profileDao.getInstructorProfile(userId);
                if (instructor != null) {
                    dto.primaryInfo = "Department: " + instructor.department() + "\nAssigned Sections: " + instructor.sectionCount();
                } else {
                    dto.primaryInfo = "Instructor profile data incomplete.";
                }
                break;

            case ADMIN:
                AdminInfo admin = profileDao.getAdminInfo(userId);
                if (admin != null) {
                    dto.primaryInfo = "Account Status: " + admin.status() + "\nFailed Login Attempts: " + admin.failedAttempts();
                } else {
                    dto.primaryInfo = "Admin profile data incomplete.";
                }
                break;
        }

        return dto;
    }

    /**
     * Simple DTO to transfer aggregated profile information to the UI.
     */
    public static class ProfileDto {
        public String username;
        public String role;
        public String primaryInfo;
        public String lastLogin;
    }
}
