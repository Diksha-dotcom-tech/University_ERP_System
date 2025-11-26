package edu.univ.erp.util;

import javax.swing.*;
import java.io.*;

public class DatabaseBackupUtil {

    private static final String MYSQLDUMP_CMD = "mysqldump";
    private static final String MYSQL_CMD = "mysql";

    private static String getDbUser() { return DbUtil.get("erp.jdbc.user"); }
    private static String getDbPass() { return DbUtil.get("erp.jdbc.password"); }

    public static void backupDatabases(JFrame parent) throws Exception {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose backup file");
        chooser.setSelectedFile(new File("erp_backup.sql"));

        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();

        ProcessBuilder pb = new ProcessBuilder(
                MYSQLDUMP_CMD,
                "-u", getDbUser(),
                "-p" + getDbPass(),  // still masked
                "--databases", "univ_auth", "univ_erp"
        );
        pb.redirectOutput(file); // Safe output redirection

        executeProcess(pb, "Backup");
    }

    public static void restoreDatabases(JFrame parent) throws Exception {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose backup file to restore");

        if (chooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();

        // We must pipe the file into mysql input stream
        ProcessBuilder pb = new ProcessBuilder(
                MYSQL_CMD,
                "-u", getDbUser(),
                "-p" + getDbPass()
        );

        Process proc = pb.start();

        try (BufferedReader br = new BufferedReader(new FileReader(file));
             BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()))) {

            String line;
            while ((line = br.readLine()) != null) {
                bw.write(line);
                bw.newLine();
            }
        }

        waitForProcess(proc, "Restore");
    }

    private static void executeProcess(ProcessBuilder pb, String label) throws Exception {
        Process proc = pb.start();
        waitForProcess(proc, label);
    }

    private static void waitForProcess(Process proc, String label) throws Exception {
        int exit = proc.waitFor();
        if (exit != 0) {
            String error = readStream(proc.getErrorStream());
            throw new IOException(label + " failed. Exit code: " + exit + "\n" + error);
        }
    }

    private static String readStream(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append("\n");
        }
        return sb.toString();
    }
}
