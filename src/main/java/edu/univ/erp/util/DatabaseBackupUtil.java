package edu.univ.erp.util;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class DatabaseBackupUtil {

    // FIX: Removed hardcoded paths. Assumes mysqldump and mysql are on the system PATH.
    // Use full paths if they are not in PATH (e.g., C:\Program Files\MySQL\...\mysqldump.exe)
    private static final String MYSQLDUMP_CMD = "mysqldump";
    private static final String MYSQL_CMD = "mysql";

    // FIX: Credentials fetched dynamically via DbUtil (must be called outside of init)
    private static String getDbUser() { return DbUtil.get("erp.jdbc.user"); }
    private static String getDbPass() { return DbUtil.get("erp.jdbc.password"); }

    public static void backupDatabases(JFrame parent) throws IOException, InterruptedException {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose backup file");
        chooser.setSelectedFile(new File("erp_backup.sql"));

        int res = chooser.showSaveDialog(parent);
        if (res != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();

        // FIX: Replaced hardcoded credentials with dynamic lookups
        String cmd = MYSQLDUMP_CMD +
                " -u" + getDbUser() +
                " -p" + getDbPass() +
                " --databases univ_auth univ_erp -r \"" + file.getAbsolutePath() + "\"";

        Process p = Runtime.getRuntime().exec(cmd);
        int exit = p.waitFor();
        if (exit != 0) {
            throw new IOException("mysqldump exited with code " + exit);
        }
    }

    public static void restoreDatabases(JFrame parent) throws IOException, InterruptedException {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose backup file to restore");
        int res = chooser.showOpenDialog(parent);
        if (res != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();

        // FIX: Replaced hardcoded credentials with dynamic lookups
        String[] cmd = {
                MYSQL_CMD,
                "-u" + getDbUser(),
                "-p" + getDbPass(),
                "-e",
                "source " + file.getAbsolutePath()
        };

        Process p = Runtime.getRuntime().exec(cmd);
        int exit = p.waitFor();
        if (exit != 0) {
            throw new IOException("mysql restore exited with code " + exit);
        }
    }
}