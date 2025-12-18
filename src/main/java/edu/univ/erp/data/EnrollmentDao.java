package edu.univ.erp.data;

import edu.univ.erp.util.DbUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Enrollment DAO.
 * Responsible for:
 *  - listing sections a student is enrolled in
 *  - checking if a (student, section) enrollment already exists (ENROLLED)
 *  - counting enrolled students in a section
 *  - reading section capacity
 *  - enrolling / dropping students (robust: re-enrolls dropped rows)
 */
public class EnrollmentDao {

    /**
     * Returns IDs of sections where the student is currently ENROLLED.
     */
    public List<Integer> findMySectionIds(int studentId) throws SQLException {
        String sql = """
                SELECT section_id
                FROM enrollments
                WHERE student_id = ?
                  AND status = 'ENROLLED'
                """;

        List<Integer> ids = new ArrayList<>();

        try (Connection c = DbUtil.getErpConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("section_id"));
                }
            }
        }
        return ids;
    }

    /**
     * Returns true if the student is currently ENROLLED in the section.
     * (Note: previously this checked any row regardless of status which blocked re-registering after dropping.)
     */
    public boolean isCurrentlyEnrolled(int studentId, int sectionId) throws SQLException {
        String sql = """
                SELECT 1
                FROM enrollments
                WHERE student_id = ?
                  AND section_id = ?
                  AND status = 'ENROLLED'
                """;

        try (Connection c = DbUtil.getErpConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setInt(2, sectionId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Counts how many students are ENROLLED in a section.
     */
    public int countEnrolledInSection(int sectionId) throws SQLException {
        String sql = """
                SELECT COUNT(*) AS cnt
                FROM enrollments
                WHERE section_id = ?
                  AND status = 'ENROLLED'
                """;

        try (Connection c = DbUtil.getErpConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, sectionId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cnt");
                }
            }
        }
        return 0;
    }

    /**
     * Reads the capacity of a section from the sections table.
     */
    public int getSectionCapacity(int sectionId) throws SQLException {
        String sql = """
                SELECT capacity
                FROM sections
                WHERE section_id = ?
                """;

        try (Connection c = DbUtil.getErpConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, sectionId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("capacity");
                }
            }
        }
        return 0;
    }

    /**
     * Enrolls a student into a section.
     *
     * Behavior:
     *  - If no enrollment row exists -> insert new ENROLLED row.
     *  - If a row exists with status = DROPPED -> update that row to ENROLLED.
     *  - If a row exists with status = ENROLLED -> throw SQLException to indicate duplicate enrollment.
     *
     * This method uses a short transaction (SELECT ... FOR UPDATE) to avoid races.
     *
     * NOTE: caller must ensure capacity / deadline / maintenance checks are done beforehand.
     */
    public void enroll(int studentId, int sectionId) throws SQLException {
        String selectSql = """
                SELECT status
                FROM enrollments
                WHERE student_id = ?
                  AND section_id = ?
                FOR UPDATE
                """;

        String insertSql = """
                INSERT INTO enrollments (student_id, section_id, status)
                VALUES (?, ?, 'ENROLLED')
                """;

        String updateSql = """
                UPDATE enrollments
                SET status = 'ENROLLED'
                WHERE student_id = ?
                  AND section_id = ?
                """;

        Connection conn = null;
        boolean previousAutoCommit = true;
        try {
            conn = DbUtil.getErpConnection();
            previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try (PreparedStatement psSelect = conn.prepareStatement(selectSql)) {
                psSelect.setInt(1, studentId);
                psSelect.setInt(2, sectionId);

                try (ResultSet rs = psSelect.executeQuery()) {
                    if (rs.next()) {
                        String status = rs.getString("status");
                        if ("ENROLLED".equalsIgnoreCase(status)) {
                            conn.rollback();
                            throw new SQLException("Student is already enrolled in this section.");
                        } else {
                            // Previously DROPPED (or other status) -> re-activate
                            try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                                psUpdate.setInt(1, studentId);
                                psUpdate.setInt(2, sectionId);
                                psUpdate.executeUpdate();
                            }
                            conn.commit();
                            return;
                        }
                    } else {
                        // No existing row -> insert
                        try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
                            psInsert.setInt(1, studentId);
                            psInsert.setInt(2, sectionId);
                            psInsert.executeUpdate();
                        }
                        conn.commit();
                        return;
                    }
                }
            }
        } catch (SQLException ex) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw ex;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(previousAutoCommit);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Marks an enrollment as DROPPED (rather than deleting the row).
     * Only affects rows currently ENROLLED.
     */
    public void drop(int studentId, int sectionId) throws SQLException {
        String sql = """
                UPDATE enrollments
                SET status = 'DROPPED'
                WHERE student_id = ?
                  AND section_id = ?
                  AND status = 'ENROLLED'
                """;

        try (Connection c = DbUtil.getErpConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setInt(2, sectionId);
            int updated = ps.executeUpdate();
        }
    }
}