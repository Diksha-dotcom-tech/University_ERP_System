package edu.univ.erp.data;

import edu.univ.erp.util.DbUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

/**
 * Reads basic profile information for the "My Profile" dialog
 * from both univ_auth and univ_erp.
 */
public class UserProfileDao {

    // ---------- simple DTOs (Java 17 records) ----------

    public record StudentProfile(
            String roll,
            String program,
            int year
    ) {}

    public record InstructorProfile(
            String department,
            int sectionCount
    ) {}

    public record AdminInfo(
            String status,
            int failedAttempts
    ) {}

    // ---------- STUDENT ----------

    public StudentProfile getStudentProfile(int userId) throws Exception {
        String sql = """
            SELECT roll_no, program, year
            FROM students
            WHERE user_id = ?
        """;

        try (Connection c = DbUtil.getErpConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null; // no student row
                }
                return new StudentProfile(
                        rs.getString("roll_no"),
                        rs.getString("program"),
                        rs.getInt("year")
                );
            }
        }
    }

    // ---------- INSTRUCTOR ----------

    public InstructorProfile getInstructorProfile(int userId) throws Exception {
        String sqlProfile = """
            SELECT department
            FROM instructors
            WHERE user_id = ?
        """;

        String sqlCount = """
            SELECT COUNT(*) AS cnt
            FROM sections
            WHERE instructor_id = ?
        """;

        String dept = null;
        int count = 0;

        try (Connection c = DbUtil.getErpConnection()) {
            // department
            try (PreparedStatement ps = c.prepareStatement(sqlProfile)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        dept = rs.getString("department");
                    }
                }
            }

            // section count
            try (PreparedStatement ps = c.prepareStatement(sqlCount)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        count = rs.getInt("cnt");
                    }
                }
            }
        }

        if (dept == null) {
            return null; // no instructor row
        }
        return new InstructorProfile(dept, count);
    }

    // ---------- ADMIN ----------

    public AdminInfo getAdminInfo(int userId) throws Exception {
        String sql = """
            SELECT status, failed_attempts
            FROM univ_auth.users_auth
            WHERE user_id = ?
        """;

        // Must use the ERP connection for this cross-DB query or stick to Auth connection.
        // Using Auth connection here is cleaner for security data.
        try (Connection c = DbUtil.getAuthConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new AdminInfo(
                        rs.getString("status"),
                        rs.getInt("failed_attempts")
                );
            }
        }
    }

    // ---------- last login (all roles) ----------

    public String getLastLogin(int userId) throws Exception {
        String sql = """
            SELECT last_login
            FROM users_auth
            WHERE user_id = ?
        """;

        try (Connection c = DbUtil.getAuthConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Timestamp ts = rs.getTimestamp("last_login");
                return ts == null ? "Never" : ts.toString();
            }
        }
    }
}