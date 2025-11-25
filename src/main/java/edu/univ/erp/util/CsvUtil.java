package edu.univ.erp.util;

import edu.univ.erp.domain.StudentGradeRow;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CsvUtil {

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

    private static String safe(Double d) {
        return d == null ? "" : String.format("%.2f", d);
    }
}

