package edu.univ.erp.data;

import edu.univ.erp.util.DbUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentDao {

    public boolean hasEnrollment(int studentId, int sectionId) throws SQLException {
        String sql = "SELECT 1 FROM enrollments WHERE student_id = ? AND section_id = ? AND status = 'ENROLLED'";
        try (Connection conn = DbUtil.getErpConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int countEnrolledInSection(int sectionId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE section_id = ? AND status = 'ENROLLED'";
        try (Connection conn = DbUtil.getErpConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public int getSectionCapacity(int sectionId) throws SQLException {
        String sql = "SELECT capacity FROM sections WHERE section_id = ?";
        try (Connection conn = DbUtil.getErpConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Section not found: " + sectionId);
                }
                return rs.getInt(1);
            }
        }
    }

    public void enroll(int studentId, int sectionId) throws SQLException {
        String sql = "INSERT INTO enrollments(student_id, section_id, status) VALUES (?, ?, 'ENROLLED')";
        try (Connection conn = DbUtil.getErpConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, sectionId);
            ps.executeUpdate();
        }
    }

    public void drop(int studentId, int sectionId) throws SQLException {
        String sql = "UPDATE enrollments " +
                "SET status = 'DROPPED' " +
                "WHERE student_id = ? AND section_id = ? AND status = 'ENROLLED'";
        try (Connection conn = DbUtil.getErpConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, sectionId);
            ps.executeUpdate();
        }
    }

    /**
     * Return IDs of sections where this student is currently ENROLLED.
     */
    public List<Integer> findMySectionIds(int studentId) throws SQLException {
        String sql = """
                SELECT section_id
                FROM enrollments
                WHERE student_id = ? AND status = 'ENROLLED'
                """;

        try (Connection conn = DbUtil.getErpConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Integer> ids = new ArrayList<>();
                while (rs.next()) {
                    ids.add(rs.getInt(1));
                }
                return ids;
            }
        }
    }
}
