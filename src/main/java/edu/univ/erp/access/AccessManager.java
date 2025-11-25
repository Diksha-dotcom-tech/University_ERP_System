package edu.univ.erp.access;
import edu.univ.erp.auth.SessionContext;
import edu.univ.erp.data.SettingsDao;
import edu.univ.erp.domain.Role;

import java.time.LocalDate;

public class AccessManager {

    private static final AccessManager INSTANCE = new AccessManager();

    private final SettingsDao settingsDao = new SettingsDao();

    public AccessManager() {
        // keep public so old code using "new AccessManager()" still compiles
    }

    public static AccessManager getInstance() {
        return INSTANCE;
    }

    // ---------- safe wrappers for SettingsDao (no checked exceptions escape) ----------

    private boolean safeMaintenanceOn() {
        try {
            return settingsDao.isMaintenanceOn();
        } catch (Exception e) {
            e.printStackTrace();
            // if settings fail, assume maintenance is OFF so app still works
            return false;
        }
    }

    private LocalDate safeDateSetting(String key, LocalDate defaultValue) {
        try {
            return settingsDao.getDateSetting(key, defaultValue);
        } catch (Exception e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    // ------------------------- query helpers -------------------------

    public boolean isReadOnly(SessionContext session) {
        if (session == null) return true;
        if (session.getRole() == Role.ADMIN) return false;
        return safeMaintenanceOn();
    }

    public boolean canStudentModify(SessionContext session) {
        if (session == null || session.getRole() != Role.STUDENT) return false;
        return !safeMaintenanceOn();
    }

    public boolean isRegistrationOpen() {
        LocalDate today = LocalDate.now();
        LocalDate deadline = safeDateSetting("registration_deadline", today);
        return !today.isAfter(deadline);
    }

    public boolean isDropOpen() {
        LocalDate today = LocalDate.now();
        LocalDate deadline = safeDateSetting("drop_deadline", today);
        return !today.isAfter(deadline);
    }

    public boolean shouldBlockStudentWrite(SessionContext session) {
        return !canStudentModify(session);
    }

    // --------------------- ENSURE methods (throw AccessDeniedException) ---------------------

    public void ensureAdmin(SessionContext session) {
        if (session == null || session.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Action requires ADMIN privileges.");
        }
    }

    public void ensureInstructor(SessionContext session) {
        if (session == null || session.getRole() != Role.INSTRUCTOR) {
            throw new AccessDeniedException("Action requires INSTRUCTOR privileges.");
        }
    }

    public void ensureStudent(SessionContext session) {
        if (session == null || session.getRole() != Role.STUDENT) {
            throw new AccessDeniedException("Action requires STUDENT privileges.");
        }
    }

    /** Admin can always write; others are blocked if maintenance is ON. */
    public void ensureNotInMaintenance(SessionContext session) {
        if (session != null && session.getRole() == Role.ADMIN) return;
        if (safeMaintenanceOn()) {
            throw new AccessDeniedException("Maintenance is ON. Write operations are disabled.");
        }
    }

    public void ensureStudentAndNotInMaintenance(SessionContext session) {
        ensureStudent(session);
        ensureNotInMaintenance(session);
    }

    /** Ensure today is on/before registration deadline. */
    public void ensureBeforeRegistrationDeadline() {
        LocalDate today = LocalDate.now();
        LocalDate deadline = safeDateSetting("registration_deadline", today);
        if (today.isAfter(deadline)) {
            throw new AccessDeniedException("Registration deadline has passed.");
        }
    }

    /** Ensure today is on/before drop deadline. */
    public void ensureBeforeDropDeadline() {
        LocalDate today = LocalDate.now();
        LocalDate deadline = safeDateSetting("drop_deadline", today);
        if (today.isAfter(deadline)) {
            throw new AccessDeniedException("Drop deadline has passed.");
        }
    }
}
