package edu.univ.erp.service;

import edu.univ.erp.access.AccessDeniedException;
import edu.univ.erp.access.AccessManager;
import edu.univ.erp.auth.SessionContext;
import edu.univ.erp.data.GradebookDao;
import edu.univ.erp.data.InstructorDao;
import edu.univ.erp.domain.GradeRow;
import edu.univ.erp.domain.InstructorSectionRow;
import edu.univ.erp.util.GradeCsvUtil;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

public class InstructorService {

    private final InstructorDao instructorDao = new InstructorDao();
    private final GradebookDao gradebookDao = new GradebookDao();
    private final AccessManager accessManager = AccessManager.getInstance();

    private void ensureInstructorOwnsSection(SessionContext session, int sectionId)
            throws AccessDeniedException, SQLException {

        if (!instructorDao.isInstructorAssignedToSection(session.getUserId(), sectionId)) {
            throw new AccessDeniedException("You are not authorized to access section ID " + sectionId + ".");
        }
    }

    public List<InstructorSectionRow> getMySections(SessionContext session)
            throws AccessDeniedException, SQLException {

        accessManager.ensureInstructor(session);
        return instructorDao.findSectionsForInstructor(session.getUserId());
    }

    public List<GradeRow> getGradebook(SessionContext session, int sectionId)
            throws AccessDeniedException, SQLException {

        accessManager.ensureInstructor(session);
        ensureInstructorOwnsSection(session, sectionId);

        return gradebookDao.getGradebookForSection(sectionId);
    }

    /**
     * Save scores and compute final using fixed weights: 20% quiz, 30% midterm, 50% endsem.
     */
    public void saveScoresAndComputeFinal(SessionContext session, int sectionId, List<GradeRow> rows)
            throws AccessDeniedException, SQLException {

        accessManager.ensureInstructor(session);
        accessManager.ensureNotInMaintenance(session);
        ensureInstructorOwnsSection(session, sectionId);

        for (GradeRow row : rows) {
            int enrollmentId = row.getEnrollmentId();

            gradebookDao.upsertComponentScore(enrollmentId, "QUIZ", row.getQuizScore());
            gradebookDao.upsertComponentScore(enrollmentId, "MIDTERM", row.getMidtermScore());
            gradebookDao.upsertComponentScore(enrollmentId, "ENDSEM", row.getEndsemScore());

            if (row.getQuizScore() != null &&
                    row.getMidtermScore() != null &&
                    row.getEndsemScore() != null) {

                double finalScore =
                        0.20 * row.getQuizScore()
                                + 0.30 * row.getMidtermScore()
                                + 0.50 * row.getEndsemScore();

                row.setFinalScore(finalScore);

                gradebookDao.upsertFinalScore(
                        enrollmentId,
                        finalScore,
                        String.format("%.1f", finalScore)
                );
            }
        }
    }

    /**
     * Simple class average based on FINAL scores.
     */
    public double computeClassAverage(SessionContext session, int sectionId)
            throws AccessDeniedException, SQLException {

        accessManager.ensureInstructor(session);
        ensureInstructorOwnsSection(session, sectionId);

        List<GradeRow> rows = gradebookDao.getGradebookForSection(sectionId);

        double sum = 0;
        int count = 0;
        for (GradeRow row : rows) {
            if (row.getFinalScore() != null) {
                sum += row.getFinalScore();
                count++;
            }
        }
        return count == 0 ? 0.0 : (sum / count);
    }

    /**
     * Export full gradebook for a section to CSV.
     */
    public void exportGradesCsv(SessionContext session, int sectionId, File destinationFile)
            throws Exception {

        accessManager.ensureInstructor(session);
        ensureInstructorOwnsSection(session, sectionId);

        List<GradeRow> rows = gradebookDao.getGradebookForSection(sectionId);
        GradeCsvUtil.writeGrades(rows, destinationFile);
    }
}
