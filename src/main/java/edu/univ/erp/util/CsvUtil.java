package edu.univ.erp.util;

import edu.univ.erp.domain.StudentGradeRow;
import edu.univ.erp.domain.GradeRow;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CsvUtil {

    /**
     * Student transcript export: one row per course.
     */
    public static void writeTranscriptCsv(List<StudentGradeRow> grades, File file) throws IOException {
        try (FileWriter writer = new FileWriter(file);
             CSVPrinter csv = new CSVPrinter(writer, CSVFormat.DEFAULT
                     .withHeader("Course Code", "Title", "Semester", "Year",
                             "Quiz", "Midterm", "End-Sem", "Final Score", "Final Grade"))) {

            for (StudentGradeRow row : grades) {
                csv.printRecord(
                        row.getCourseCode(),
                        row.getCourseTitle(),
                        row.getSemester(),
                        row.getYear(),
                        safe(row.getQuizScore()),
                        safe(row.getMidtermScore()),
                        safe(row.getEndsemScore()),
                        safe(row.getFinalScore()),
                        row.getFinalGradeText()
                );
            }
        }
    }

    /**
     * Instructor gradebook export: one row per student (per section).
     */
    public static void writeGradesCsv(List<GradeRow> grades, File file) throws IOException {
        try (FileWriter writer = new FileWriter(file);
             CSVPrinter csv = new CSVPrinter(writer, CSVFormat.DEFAULT
                     .withHeader("Enrollment ID", "Roll No", "Name",
                             "Quiz", "Midterm", "End-Sem", "Final Score", "Final Grade"))) {

            for (GradeRow row : grades) {
                csv.printRecord(
                        row.getEnrollmentId(),
                        row.getRollNo(),
                        row.getStudentName(),
                        safe(row.getQuizScore()),
                        safe(row.getMidtermScore()),
                        safe(row.getEndsemScore()),
                        safe(row.getFinalScore()),
                        row.getFinalGradeText()
                );
            }
        }
    }

    private static String safe(Double d) {
        return d == null ? "" : String.format("%.2f", d);
    }
}
