package edu.univ.erp.api.instructor;

import edu.univ.erp.auth.SessionContext;
import edu.univ.erp.domain.GradeRow;
import edu.univ.erp.domain.InstructorSectionRow;
import edu.univ.erp.service.InstructorService;

import java.util.List;
/**
 * API layer placeholder for future extension.
 * UI currently interacts directly with Service layer.
 * This class is intentionally kept for demonstrating a multi-tier architecture.
 */
@SuppressWarnings("unused")

public class InstructorApi {

    private final InstructorService instructorService = new InstructorService();

    public List<InstructorSectionRow> getMySections(SessionContext session) throws Exception {
        return instructorService.getMySections(session);
    }

    public List<GradeRow> getGradebook(SessionContext session, int sectionId) throws Exception {
        return instructorService.getGradebook(session, sectionId);
    }

    public void saveScoresAndComputeFinal(SessionContext session, int sectionId, List<GradeRow> rows) throws Exception {
        instructorService.saveScoresAndComputeFinal(session, sectionId, rows);
    }

    public double getClassAverage(SessionContext session, int sectionId) throws Exception {
        return instructorService.computeClassAverage(session, sectionId);
    }
}

