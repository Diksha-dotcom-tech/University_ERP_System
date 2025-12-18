package edu.univ.erp.data;

import edu.univ.erp.domain.GradeRow;
import edu.univ.erp.util.DbUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GradebookDao {

    /**
     * Fetch gradebook for a section: one row per enrolled student, with scores aggregated.
     */
    public List<GradeRow> getGradebookForSection(int sectionId) throws SQLException {
        String sql = """
                SELECT e.enrollment_id,
                       s.user_id AS student_id,
                       s.roll_no,
                       ua.username AS student_name,
                       MAX(CASE WHEN g.component = 'QUIZ'   THEN g.score END)  AS quiz_score,
                       MAX(CASE WHEN g.component = 'MIDTERM' THEN g.score END) AS midterm_score,
                       MAX(CASE WHEN g.component = 'ENDSEM' THEN g.score END)  AS endsem_score,
                       MAX(CASE WHEN g.component = 'FINAL'  THEN g.score END)  AS final_score,
                       MAX(CASE WHEN g.component = 'FINAL'  THEN g.final_grade END) AS final_grade_text
                FROM enrollments e
                JOIN students s ON e.student_id = s.user_id
                JOIN univ_auth.users_auth ua ON s.user_id = ua.user_id
                LEFT JOIN grades g ON e.enrollment_id = g.enrollment_id
                WHERE e.section_id = ?
                  AND e.status = 'ENROLLED'
                GROUP BY e.enrollment_id, s.user_id, s.roll_no, ua.username
                ORDER BY s.roll_no
                """;

        List<GradeRow> list = new ArrayList<>();

        try (Connection conn = DbUtil.getErpConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    GradeRow row = new GradeRow();
                    row.setEnrollmentId(rs.getInt("enrollment_id"));
                    row.setStudentId(rs.getInt("student_id"));
                    row.setRollNo(rs.getString("roll_no"));
                    row.setStudentName(rs.getString("student_name"));
                    // Use rs.getObject for potential nulls
                    row.setQuizScore((Double) rs.getObject("quiz_score"));
                    row.setMidtermScore((Double) rs.getObject("midterm_score"));
                    row.setEndsemScore((Double) rs.getObject("endsem_score"));
                    row.setFinalScore((Double) rs.getObject("final_score"));
                    row.setFinalGradeText(rs.getString("final_grade_text")); // Add this field mapping
                    list.add(row);
                }
            }
        }
        return list;
    }

    /**
     * Insert or update a grade component score.
     */
    public void upsertComponentScore(int enrollmentId, String component, Double score) throws SQLException {
        if (score == null) return; // nothing to save

        String selectSql = "SELECT grade_id FROM grades WHERE enrollment_id = ? AND component = ?";
        try (Connection conn = DbUtil.getErpConnection();
             PreparedStatement sel = conn.prepareStatement(selectSql)) {
            sel.setInt(1, enrollmentId);
            sel.setString(2, component);
            try (ResultSet rs = sel.executeQuery()) {
                if (rs.next()) {
                    int gradeId = rs.getInt(1);
                    String updateSql = "UPDATE grades SET score = ? WHERE grade_id = ?";
                    try (PreparedStatement upd = conn.prepareStatement(updateSql)) {
                        upd.setDouble(1, score);
                        upd.setInt(2, gradeId);
                        upd.executeUpdate();
                    }
                } else {
                    String insertSql = "INSERT INTO grades(enrollment_id, component, score) VALUES (?, ?, ?)";
                    try (PreparedStatement ins = conn.prepareStatement(insertSql)) {
                        ins.setInt(1, enrollmentId);
                        ins.setString(2, component);
                        ins.setDouble(3, score);
                        ins.executeUpdate();
                    }
                }
            }
        }
    }

    /**
     * Save the final calculated score and the final letter grade text.
     */
    public void upsertFinalScore(int enrollmentId, Double finalScore, String finalGradeText) throws SQLException {
        if (finalScore == null) return;

        String selectSql = "SELECT grade_id FROM grades WHERE enrollment_id = ? AND component = 'FINAL'";
        try (Connection conn = DbUtil.getErpConnection();
             PreparedStatement sel = conn.prepareStatement(selectSql)) {
            sel.setInt(1, enrollmentId);
            try (ResultSet rs = sel.executeQuery()) {
                if (rs.next()) {
                    int gradeId = rs.getInt(1);
                    // CRITICAL FIX: Update final_grade column with the letter grade text
                    String updateSql = "UPDATE grades SET score = ?, final_grade = ? WHERE grade_id = ?";
                    try (PreparedStatement upd = conn.prepareStatement(updateSql)) {
                        upd.setDouble(1, finalScore);
                        upd.setString(2, finalGradeText);
                        upd.setInt(3, gradeId);
                        upd.executeUpdate();
                    }
                } else {
                    //final_grade text
                    String insertSql = "INSERT INTO grades(enrollment_id, component, score, final_grade) VALUES (?, 'FINAL', ?, ?)";
                    try (PreparedStatement ins = conn.prepareStatement(insertSql)) {
                        ins.setInt(1, enrollmentId);
                        ins.setDouble(2, finalScore);
                        ins.setString(3, finalGradeText);
                        ins.executeUpdate();
                    }
                }
            }
        }
    }
}