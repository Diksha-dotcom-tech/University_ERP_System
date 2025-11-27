package edu.univ.erp.data;

import edu.univ.erp.domain.CatalogSectionRow;
import edu.univ.erp.util.DbUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CatalogDao {

    private static final String LIST_CATALOG_SQL = """
            SELECT 
                s.section_id,
                s.course_id,
                c.code AS course_code,
                c.title AS course_title,
                s.instructor_id,
                CONCAT(u.username) AS instructor_name,   -- or full name if you store it
                s.day_of_week,
                s.start_time,
                s.end_time,
                s.room,
                s.capacity,
                s.semester,
                s.year,
                s.registration_deadline,
                s.drop_deadline
            FROM sections s
            JOIN courses c ON s.course_id = c.course_id
            JOIN instructors i ON s.instructor_id = i.user_id
            JOIN univ_auth.users_auth u ON i.user_id = u.user_id
            ORDER BY c.code ASC, s.section_id ASC
            """;

    /**
     * Returns the full catalog (all sections, with joined course + instructor info).
     */
    public List<CatalogSectionRow> listCatalog() throws SQLException {
        List<CatalogSectionRow> list = new ArrayList<>();

        try (Connection conn = DbUtil.getErpConnection();
             PreparedStatement ps = conn.prepareStatement(LIST_CATALOG_SQL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }

        return list;
    }

    /**
     * Maps a ResultSet row into a CatalogSectionRow object.
     */
    private CatalogSectionRow mapRow(ResultSet rs) throws SQLException {
        CatalogSectionRow row = new CatalogSectionRow();

        row.setSectionId(rs.getInt("section_id"));
        row.setCourseId(rs.getInt("course_id"));
        row.setCourseCode(rs.getString("course_code"));
        row.setCourseTitle(rs.getString("course_title"));
        row.setInstructorId(rs.getInt("instructor_id"));
        row.setInstructorName(rs.getString("instructor_name"));
        row.setDayOfWeek(rs.getString("day_of_week"));
        row.setStartTime(rs.getTime("start_time"));
        row.setEndTime(rs.getTime("end_time"));
        row.setRoom(rs.getString("room"));
        row.setCapacity(rs.getInt("capacity"));
        row.setSemester(rs.getString("semester"));
        row.setYear(rs.getInt("year"));

        // timestamps → LocalDateTime
        Timestamp regTs = rs.getTimestamp("registration_deadline");
        if (regTs != null) row.setRegistrationDeadline(regTs.toLocalDateTime());

        Timestamp dropTs = rs.getTimestamp("drop_deadline");
        if (dropTs != null) row.setDropDeadline(dropTs.toLocalDateTime());

        return row;
    }
}