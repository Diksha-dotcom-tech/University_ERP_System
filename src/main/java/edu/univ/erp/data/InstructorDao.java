package edu.univ.erp.data;

import edu.univ.erp.domain.InstructorSectionRow;
import edu.univ.erp.util.DbUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InstructorDao {

    public List<InstructorSectionRow> findSectionsForInstructor(int instructorUserId) throws SQLException {
        String sql = """
                SELECT s.section_id,
                       c.code,
                       c.title,
                       s.day_of_week,
                       DATE_FORMAT(s.start_time, '%H:%i') AS start_time,
                       DATE_FORMAT(s.end_time, '%H:%i')   AS end_time,
                       s.room,
                       s.semester,
                       s.year,
                       COALESCE(ec.enrolled, 0) AS enrolled
                FROM sections s
                JOIN courses c ON s.course_id = c.course_id
                LEFT JOIN (
                    SELECT section_id, COUNT(*) AS enrolled
                    FROM enrollments
                    WHERE status = 'ENROLLED'
                    GROUP BY section_id
                ) ec ON s.section_id = ec.section_id
                WHERE s.instructor_id = ?
                ORDER BY s.year, s.semester, c.code
                """;

        List<InstructorSectionRow> list = new ArrayList<>();

        try (Connection conn = DbUtil.getErpConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, instructorUserId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InstructorSectionRow row = new InstructorSectionRow();
                    row.setSectionId(rs.getInt("section_id"));
                    row.setCourseCode(rs.getString("code"));
                    row.setCourseTitle(rs.getString("title"));
                    row.setDayOfWeek(rs.getString("day_of_week"));
                    row.setTimeRange(rs.getString("start_time") + " - " + rs.getString("end_time"));
                    row.setRoom(rs.getString("room"));
                    row.setSemester(rs.getString("semester"));
                    row.setYear(rs.getInt("year"));
                    row.setEnrolledCount(rs.getInt("enrolled"));
                    list.add(row);
                }
            }
        }
        return list;
    }

    /**
     * Checks if the given user ID is the assigned instructor for the section ID.
     */
    public boolean isInstructorAssignedToSection(int instructorUserId, int sectionId) throws SQLException {
        String sql = "SELECT 1 FROM sections WHERE section_id = ? AND instructor_id = ?";

        try (Connection conn = DbUtil.getErpConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sectionId);
            ps.setInt(2, instructorUserId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // True if a matching row is found
            }
        }
    }
}