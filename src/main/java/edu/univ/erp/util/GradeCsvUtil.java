package edu.univ.erp.util;

import edu.univ.erp.domain.GradeRow;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class GradeCsvUtil {

    /**
     * Export grade rows to CSV.
     */
    public static void writeGrades(List<GradeRow> rows, File file) throws IOException {
        try (FileWriter writer = new FileWriter(file);
             CSVPrinter csv = new CSVPrinter(writer,
                     CSVFormat.DEFAULT.withHeader(
                             "EnrollmentId", "RollNo", "Name",
                             "Quiz", "Midterm", "EndSem", "Final"))) {

            for (GradeRow row : rows) {
                csv.printRecord(
                        row.getEnrollmentId(),
                        row.getRollNo(),
                        row.getStudentName(),
                        safe(row.getQuizScore()),
                        safe(row.getMidtermScore()),
                        safe(row.getEndsemScore()),
                        safe(row.getFinalScore())
                );
            }
        }
    }

    private static String safe(Double d) {
        return d == null ? "" : String.format("%.2f", d);
    }

    /**
     * Import grades from CSV into existing GradeRow list.
     * Match rows by EnrollmentId.
     */
    public static void readGrades(File file, List<GradeRow> rows) throws IOException {
        Map<Integer, GradeRow> byId = new HashMap<>();
        for (GradeRow r : rows) {
            byId.put(r.getEnrollmentId(), r);
        }

        try (FileReader reader = new FileReader(file)) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .parse(reader);

            for (CSVRecord rec : records) {
                int enrollmentId = Integer.parseInt(rec.get("EnrollmentId"));
                GradeRow row = byId.get(enrollmentId);
                if (row == null) continue;

                row.setQuizScore(parseDouble(rec.get("Quiz")));
                row.setMidtermScore(parseDouble(rec.get("Midterm")));
                row.setEndsemScore(parseDouble(rec.get("EndSem")));
                // don't set final here; InstructorService will recompute
            }
        }
    }

    private static Double parseDouble(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}

