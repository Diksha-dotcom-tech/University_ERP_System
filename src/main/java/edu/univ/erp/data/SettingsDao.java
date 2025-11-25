package edu.univ.erp.data;

import edu.univ.erp.util.DbUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public class SettingsDao {

    // ---------------- Basic helpers ----------------

    public boolean isMaintenanceOn() throws Exception {
        String sql = "SELECT value FROM settings WHERE `key` = 'maintenance_on'";

        try (Connection c = DbUtil.getErpConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (!rs.next()) return false;
            return Boolean.parseBoolean(rs.getString("value"));
        }
    }

    public void setMaintenance(boolean on) throws Exception {
        String sql = """
            INSERT INTO settings(`key`, `value`)
            VALUES ('maintenance_on', ?)
            ON DUPLICATE KEY UPDATE `value` = VALUES(`value`)
        """;

        try (Connection c = DbUtil.getErpConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, Boolean.toString(on));
            ps.executeUpdate();
        }
    }

    // This method name is what your existing AdminService expects.
    // It simply forwards to setMaintenance(...)
    public void setMaintenanceOn(boolean on) throws Exception {
        setMaintenance(on);
    }

    public String getSetting(String key, String defaultValue) throws Exception {
        String sql = "SELECT value FROM settings WHERE `key` = ?";

        try (Connection c = DbUtil.getErpConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return defaultValue;
                return rs.getString("value");
            }
        }
    }

    // This method name is what your existing AccessManager expects.
    // It uses getSetting internally and parses a LocalDate.
    public LocalDate getDateSetting(String key, LocalDate defaultValue) throws Exception {
        String raw = getSetting(key, null);
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        try {
            return LocalDate.parse(raw);
        } catch (Exception e) {
            // If parsing fails, fall back to default to be safe
            return defaultValue;
        }
    }
}
