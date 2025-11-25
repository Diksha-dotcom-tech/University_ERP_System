package edu.univ.erp.data;

import edu.univ.erp.auth.PasswordHasher;
import edu.univ.erp.domain.CourseOption;
import edu.univ.erp.domain.InstructorOption;
import edu.univ.erp.domain.Role;
import edu.univ.erp.domain.UserAuth;
import edu.univ.erp.util.DbUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AdminDao {

    // ===== USERS =====

    /**
     * Create user in Auth DB and return generated user_id.
     */
    public int createAuthUser(String username, Role role, String plainPassword) throws SQLException {
        String hashed = PasswordHasher.hash(plainPassword);

        String sql = "INSERT INTO users_auth(username, role, password_hash, status) " +
                "VALUES (?, ?, ?, 'ACTIVE')";

        try (Connection conn = DbUtil.getAuthConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, username);
            ps.setString(2, role.name());
            ps.setString(3, hashed);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new SQLException("No user_id returned from users_auth insert");
                }
            }
        }
    }

    public void createStudentProfile(int userId, String rollNo, String program, int year) throws SQLException {
        String sql = "INSERT INTO students(user_id, roll_no, program, year) VALUES (?, ?, ?, ?)";
        try (Connection conn = DbUtil.getErpConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, rollNo);
            ps.setString(3, program);
            ps.setInt(4, year);
            ps.executeUpdate();
        }
    }

    public void createInstructorProfile(int userId, String department) throws SQLException {
        String sql = "INSERT INTO instructors(user_id, department) VALUES (?, ?)";
        try (Connection conn = DbUtil.getErpConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, department);
            ps.executeUpdate();
        }
    }

    /**
     * Lists all users from the Auth DB using the UserAuth domain object.
     */
    public List<UserAuth> listAllUserAuths() throws SQLException {
        String sql = """
                SELECT user_id, username, role, password_hash, status, failed_attempts, last_login
                FROM users_auth
                ORDER BY role, username
                """;

        List<UserAuth> users = new ArrayList<>();
        try (Connection conn = DbUtil.getAuthConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                UserAuth user = new UserAuth();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setRole(Role.valueOf(rs.getString("role")));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setStatus(rs.getString("status"));
                user.setFailedAttempts(rs.getInt("failed_attempts"));

                Timestamp ts = rs.getTimestamp("last_login");
                if (ts != null) {
                    user.setLastLogin(ts.toLocalDateTime());
                }

                users.add(user);
            }
        }
        return users;
    }

    public List<InstructorOption> listAllInstructors() throws SQLException {
        String sql = """
                SELECT i.user_id, ua.username, i.department
                FROM instructors i
                JOIN univ_auth.users_auth ua ON i.user_id = ua.user_id
                ORDER BY ua.username
                """;

        List<InstructorOption> list = new ArrayList<>();
        try (Connection conn = DbUtil.getErpConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new InstructorOption(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("department")
                ));
            }
        }
        return list;
    }

    // ===== COURSES & SECTIONS =====

    public int createCourse(String code, String title, int credits) throws SQLException {
        String sql = "INSERT INTO courses(code, title, credits) VALUES (?, ?, ?)";
        try (Connection conn = DbUtil.getErpConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, code);
            ps.setString(2, title);
            ps.setInt(3, credits);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new SQLException("No course_id generated");
                }
            }
        }
    }

    public List<CourseOption> listCourses() throws SQLException {
        String sql = "SELECT course_id, code, title FROM courses ORDER BY code";
        List<CourseOption> list = new ArrayList<>();
        try (Connection conn = DbUtil.getErpConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new CourseOption(
                        rs.getInt("course_id"),
                        rs.getString("code"),
                        rs.getString("title")
                ));
            }
        }
        return list;
    }

    public void createSection(int courseId,
                              int instructorUserId,
                              String dayOfWeek,
                              Time startTime,
                              Time endTime,
                              String room,
                              int capacity,
                              String semester,
                              int year) throws SQLException {

        String sql = """
                INSERT INTO sections
                (course_id, instructor_id, day_of_week, start_time, end_time,
                 room, capacity, semester, year)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DbUtil.getErpConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            ps.setInt(2, instructorUserId);
            ps.setString(3, dayOfWeek);
            ps.setTime(4, startTime);
            ps.setTime(5, endTime);
            ps.setString(6, room);
            ps.setInt(7, capacity);
            ps.setString(8, semester);
            ps.setInt(9, year);
            ps.executeUpdate();
        }
    }
}