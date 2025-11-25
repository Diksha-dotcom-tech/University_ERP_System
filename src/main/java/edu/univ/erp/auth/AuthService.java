package edu.univ.erp.auth;

import edu.univ.erp.data.AuthDao;
import edu.univ.erp.data.AuthDao.AuthUser;
import edu.univ.erp.domain.Role;

public class AuthService {

    private final AuthDao authDao = new AuthDao();

    public SessionContext login(String username, String plainPassword) throws AuthException {
        try {
            // 1. Look up user in Auth DB
            AuthUser user = authDao.findByUsername(username);
            if (user == null) {
                throw new AuthException("Incorrect username or password.");
            }

            // 2. Check locked status
            if ("LOCKED".equalsIgnoreCase(user.status())) {
                throw new AuthException("Account locked after too many failed attempts.");
            }

            // 3. Verify bcrypt hash
            boolean ok = PasswordHasher.verify(plainPassword, user.passwordHash());
            if (!ok) {
                // increment failed attempts and maybe lock
                authDao.incrementFailedAttempts(user.userId());
                throw new AuthException("Incorrect username or password.");
            }

            // 4. Successful login → reset counter + update last_login
            authDao.resetFailedAttempts(user.userId());
            authDao.updateLastLogin(user.userId());

            // 5. Build session (Role enum from DB string)
            Role role = Role.valueOf(user.role());
            return new SessionContext(user.userId(), user.username(), role);

        } catch (Exception e) {
            throw new AuthException("Login failed due to database error.", e);
        }
    }

    public static class AuthException extends Exception {
        public AuthException(String message) { super(message); }
        public AuthException(String message, Throwable cause) { super(message, cause); }
    }
}
