package edu.univ.erp.api.auth;

import edu.univ.erp.auth.AuthException;
import edu.univ.erp.auth.PasswordHasher;
import edu.univ.erp.auth.SessionContext;
import edu.univ.erp.util.DbUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Small API used only by ChangePasswordDialog.
 * Login / logout are handled directly by AuthService + LoginFrame.
 */
public class AuthApi {

    /**
     * Change the logged-in user's password.
     * Steps:
     *  1. Load current password_hash from univ_auth.users_auth for this user_id.
     *  2. Verify the "currentPw" using PasswordHasher.verify (bcrypt).
     *  3. If correct, store a new bcrypt hash for newPw.
     */
    public void changePassword(SessionContext session,
                               String currentPw,
                               String newPw) throws AuthException {
        if (session == null) {
            throw new AuthException("No active session.");
        }

        int userId = session.getUserId();

        try (Connection conn = DbUtil.getAuthConnection()) {

            // 1. Fetch existing hash for this user
            String selectSql = "SELECT password_hash FROM users_auth WHERE user_id = ?";
            String existingHash;
            try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new AuthException("User record not found.");
                    }
                    existingHash = rs.getString("password_hash");
                }
            }

            // 2. Verify current password
            if (!PasswordHasher.verify(currentPw, existingHash)) {
                throw new AuthException("Current password is incorrect.");
            }

            // 3. Hash new password with bcrypt
            String newHash = PasswordHasher.hash(newPw);

            // 4. Update DB
            String updateSql = "UPDATE users_auth SET password_hash = ? WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setString(1, newHash);
                ps.setInt(2, userId);
                ps.executeUpdate();
            }

        } catch (SQLException e) {
            throw new AuthException("Failed to change password: " + e.getMessage(), e);
        }
    }
}
