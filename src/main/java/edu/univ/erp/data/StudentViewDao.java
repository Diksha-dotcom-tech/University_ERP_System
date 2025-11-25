package edu.univ.erp.data;

import edu.univ.erp.domain.StudentGradeRow;
import edu.univ.erp.domain.StudentTimetableRow;
import edu.univ.erp.util.DbUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentViewDao {

    public List<StudentTimetableRow> getTimetableForStudent(int studentId) throws SQLException {
        String sql = """
                SELECT s.day_of_week,
                       DATE_FORMAT(s.start_time, '%H:%i') AS start_time,
                       DATE_FORMAT(s.end_time, '%H:%i')   AS end_time,
                       c.code,
                       c.title,
                       s.room,
                       s.semester,
                       s.year
                FROM enrollments e
                JOIN sections s ON e.section_id = s.section_id
                JOIN courses c ON s.course_id = c.course_id
                WHERE e.student_id = ?
                  AND e.status = 'ENROLLED'
                ORDER BY 
                  FIELD(s.day_of_week, 'MON','TUE','WED','THU','FRI'),
                  s.start_time
                """;

        List<StudentTimetableRow> list = new ArrayList<>();

        try (Connection conn = DbUtil.getErpConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StudentTimetableRow row = new StudentTimetableRow();
                    row.setDayOfWeek(rs.getString("day_of_week"));
                    row.setTimeRange(rs.getString("start_time") + " - " + rs.getString("end_time"));
                    row.setCourseCode(rs.getString("code"));
                    row.setCourseTitle(rs.getString("title"));
                    row.setRoom(rs.getString("room"));
                    row.setSemester(rs.getString("semester"));
                    row.setYear(rs.getInt("year"));
                    list.add(row);
                }
            }
        }
        return list;
    }

    public List<StudentGradeRow> getGradesForStudent(int studentId) throws SQLException {
        String sql = """
                SELECT c.code,
                       c.title,
                       s.semester,
                       s.year,
                       MAX(CASE WHEN g.component = 'QUIZ'   THEN g.score END)  AS quiz_score,
                       MAX(CASE WHEN g.component = 'MIDTERM' THEN g.score END) AS midterm_score,
                       MAX(CASE WHEN g.component = 'ENDSEM' THEN g.score END)  AS endsem_score,
                       MAX(CASE WHEN g.component = 'FINAL'  THEN g.score END)  AS final_score,
                       MAX(CASE WHEN g.component = 'FINAL'  THEN g.final_grade END) AS final_grade_text
                FROM enrollments e
                JOIN sections s ON e.section_id = s.section_id
                JOIN courses c ON s.course_id = c.course_id
                LEFT JOIN grades g ON e.enrollment_id = g.enrollment_id
                WHERE e.student_id = ?
                  AND e.status = 'ENROLLED'
                GROUP BY c.code, c.title, s.semester, s.year
                ORDER BY s.year, s.semester, c.code
                """;

        List<StudentGradeRow> list = new ArrayList<>();

        try (Connection conn = DbUtil.getErpConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StudentGradeRow row = new StudentGradeRow();
                    row.setCourseCode(rs.getString("code"));
                    row.setCourseTitle(rs.getString("title"));
                    row.setSemester(rs.getString("semester"));
                    row.setYear(rs.getInt("year"));
                    row.setQuizScore((Double) rs.getObject("quiz_score"));
                    row.setMidtermScore((Double) rs.getObject("midterm_score"));
                    row.setEndsemScore((Double) rs.getObject("endsem_score"));
                    row.setFinalScore((Double) rs.getObject("final_score"));
                    row.setFinalGradeText(rs.getString("final_grade_text"));
                    list.add(row);
                }
            }
        }
        return list;
    }
}

