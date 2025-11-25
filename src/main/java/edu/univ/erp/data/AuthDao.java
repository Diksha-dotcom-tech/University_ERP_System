package edu.univ.erp.data;

import edu.univ.erp.domain.UserAuth;
import edu.univ.erp.domain.Role;
import edu.univ.erp.util.DbUtil;
import org.mindrot.jbcrypt.BCrypt; // Assuming BCrypt is used for password hashing

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthDao {

    // FIX 1: Corrected table name from 'auth_user' to 'users_auth' to match SQL script.
    private static final String GET_USER_AUTH_SQL =
            "SELECT user_id, username, role, password_hash, status FROM univ_auth.users_auth WHERE username = ?";

    // FIX 2: Uses the correct table name for updating the session status (login/logout).
    private static final String UPDATE_STATUS_SQL =
            "UPDATE univ_auth.users_auth SET status = ? WHERE username = ?";


    /**
     * Retrieves authentication details for a given username.
     * @param username The username to look up.
     * @return UserAuth object if found, null otherwise.
     * @throws SQLException if a database access error occurs.
     */
    public UserAuth getUserAuth(String username) throws SQLException {
        try (Connection conn = DbUtil.getAuthConnection();
             PreparedStatement pstmt = conn.prepareStatement(GET_USER_AUTH_SQL)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    UserAuth user = new UserAuth();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setRole(Role.valueOf(rs.getString("role"))); // Assumes Role is an Enum
                    user.setPasswordHash(rs.getString("password_hash"));
                    user.setStatus(rs.getString("status"));

                    // Note: failed_attempts and last_login fields are omitted here
                    // but can be added if your UserAuth model includes them.

                    return user;
                }
            }
        }
        return null;
    }

    /**
     * Updates the session status (ACTIVE/LOCKED) for a user.
     * @param username The user whose status is to be updated.
     * @param status The new status (e.g., "ACTIVE" or "INACTIVE").
     * @throws SQLException if a database access error occurs.
     */
    public void updateSessionStatus(String username, String status) throws SQLException {
        try (Connection conn = DbUtil.getAuthConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE_STATUS_SQL)) {

            pstmt.setString(1, status);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        }
    }
}