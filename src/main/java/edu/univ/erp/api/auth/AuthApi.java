package edu.univ.erp.api.auth;

import edu.univ.erp.auth.AuthException;
import edu.univ.erp.auth.PasswordHasher;
import edu.univ.erp.auth.SessionContext;
import edu.univ.erp.domain.Role;
import edu.univ.erp.service.AuthService;
import edu.univ.erp.util.DbUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * API facade used by the UI.
 * - login with expected Role (wraps AuthService)
 * - change password for current session
 */
public class AuthApi {

    private final AuthService authService = new AuthService();

    /**
     * Legacy login used by older code: they pass an expected role.
     * We delegate to AuthService (bcrypt-based) and then check the role.
     */
    public SessionContext login(String username, Role expectedRole, String plainPassword)
            throws AuthException {

        SessionContext session = authService.login(username, plainPassword);
        if (session.getRole() != expectedRole) {
            throw new AuthException("Incorrect role selected for this user.");
        }
        return session;
    }

    /**
     * Change password for the current user.
     * Checks old password and then stores new BCrypt hash.
     */
    public void changePassword(SessionContext session,
                               String oldPassword,
                               String newPassword) throws AuthException {

        if (session == null) {
            throw new AuthException("No active session.");
        }

        String selectSql = "SELECT password_hash FROM users_auth WHERE user_id = ?";
        String updateSql = "UPDATE users_auth " +
                "SET password_hash = ?, failed_attempts = 0, lock_until = NULL " +
                "WHERE user_id = ?";

        try (Connection conn = DbUtil.getAuthConnection();
             PreparedStatement sel = conn.prepareStatement(selectSql)) {

            sel.setInt(1, session.getUserId());
            try (ResultSet rs = sel.executeQuery()) {
                if (!rs.next()) {
                    throw new AuthException("User not found.");
                }
                String currentHash = rs.getString(1);
                if (!PasswordHasher.verify(oldPassword, currentHash)) {
                    throw new AuthException("Current password is incorrect.");
                }
            }

            String newHash = PasswordHasher.hash(newPassword);
            try (PreparedStatement upd = conn.prepareStatement(updateSql)) {
                upd.setString(1, newHash);
                upd.setInt(2, session.getUserId());
                upd.executeUpdate();
            }

        } catch (SQLException e) {
            throw new AuthException("Could not change password: " + e.getMessage(), e);
        }
    }
}
