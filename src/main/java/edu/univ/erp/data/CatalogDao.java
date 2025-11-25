package edu.univ.erp.data;

import edu.univ.erp.domain.CatalogSectionRow;
import edu.univ.erp.util.DbUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CatalogDao {

    public List<CatalogSectionRow> listCatalog() throws SQLException {
        String sql = """
                SELECT s.section_id,
                       c.code,
                       c.title,
                       c.credits,
                       u.username AS instructor_name,
                       s.day_of_week,
                       DATE_FORMAT(s.start_time, '%H:%i') AS start_time,
                       DATE_FORMAT(s.end_time, '%H:%i')   AS end_time,
                       s.room,
                       s.capacity,
                       s.semester,
                       s.year,
                       COALESCE(enrolled_counts.enrolled, 0) AS enrolled
                FROM sections s
                JOIN courses c ON s.course_id = c.course_id
                JOIN univ_auth.users_auth u ON s.instructor_id = u.user_id
                LEFT JOIN (
                    SELECT section_id, COUNT(*) AS enrolled
                    FROM enrollments
                    WHERE status = 'ENROLLED'
                    GROUP BY section_id
                ) AS enrolled_counts
                  ON s.section_id = enrolled_counts.section_id
                ORDER BY c.code, s.section_id
                """;

        List<CatalogSectionRow> rows = new ArrayList<>();

        try (Connection conn = DbUtil.getErpConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                CatalogSectionRow row = new CatalogSectionRow();
                row.setSectionId(rs.getInt("section_id"));
                row.setCourseCode(rs.getString("code"));
                row.setCourseTitle(rs.getString("title"));
                row.setCredits(rs.getInt("credits"));
                row.setInstructorName(rs.getString("instructor_name"));
                row.setDayOfWeek(rs.getString("day_of_week"));
                String timeRange = rs.getString("start_time") + " - " + rs.getString("end_time");
                row.setTimeRange(timeRange);
                row.setRoom(rs.getString("room"));
                row.setCapacity(rs.getInt("capacity"));
                int enrolled = rs.getInt("enrolled");
                row.setEnrolled(enrolled);
                row.setSeatsLeft(row.getCapacity() - enrolled);
                row.setSemester(rs.getString("semester"));
                row.setYear(rs.getInt("year"));
                rows.add(row);
            }
        }
        return rows;
    }
}

