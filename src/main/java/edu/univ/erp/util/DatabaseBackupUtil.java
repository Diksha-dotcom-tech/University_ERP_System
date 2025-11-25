package edu.univ.erp.util;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class DatabaseBackupUtil {

    // Paths specific to YOUR machine (from your screenshots)
    private static final String MYSQLDUMP_PATH =
            "\"C:\\Program Files\\MySQL\\MySQL Workbench 8.0\\mysqldump.exe\"";

    private static final String MYSQL_PATH =
            "\"C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysql.exe\"";

    // Must match application.properties
    // auth.jdbc.user / erp.jdbc.user and their passwords
    private static final String DB_USER = "erpuser";
    private static final String DB_PASSWORD = "erp_pass";

    public static void backupDatabases(JFrame parent) throws IOException, InterruptedException {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose backup file");
        chooser.setSelectedFile(new File("erp_backup.sql"));

        int res = chooser.showSaveDialog(parent);
        if (res != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();

        String cmd = MYSQLDUMP_PATH +
                " -u" + DB_USER +
                " -p" + DB_PASSWORD +
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

        String[] cmd = {
                MYSQL_PATH,
                "-u" + DB_USER,
                "-p" + DB_PASSWORD,
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
