package edu.univ.erp.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * Utility for backing up and restoring MySQL databases using mysqldump/mysql.
 *
 * Notes:
 *  - Requires mysqldump and mysql to be available on PATH or use full paths in MYSQLDUMP_CMD / MYSQL_CMD.
 *  - Uses MYSQL_PWD environment variable to avoid exposing the password on the process command-line.
 *  - Redirects output/input files so it works reliably on both Windows and Unix.
 */
public class DatabaseBackupUtil {

    // If mysqldump/mysql are not on PATH, set full absolute paths here:
    private static final String MYSQLDUMP_CMD = "mysqldump";
    private static final String MYSQL_CMD = "mysql";

    private static String getDbUser() { return DbUtil.get("erp.jdbc.user"); }
    private static String getDbPass() { return DbUtil.get("erp.jdbc.password"); }
    private static String getJdbcHostAndPort() {
        // Optional: if you store host and port in config; otherwise default to localhost:3306
        String url = DbUtil.get("erp.jdbc.url"); // e.g. jdbc:mysql://localhost:3306/univ_erp
        return url == null ? null : url;
    }

    public static void backupDatabases(JFrame parent) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose backup file (will be overwritten if exists)");
        chooser.setSelectedFile(new File("erp_backup.sql"));

        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;
        File outFile = chooser.getSelectedFile();

        List<String> cmd = new ArrayList<>();
        cmd.add(MYSQLDUMP_CMD);
        // user only (password supplied via env MYSQL_PWD)
        cmd.add("-u");
        cmd.add(getDbUser());

        // include both DBs:
        cmd.add("--databases");
        cmd.add("univ_auth");
        cmd.add("univ_erp");

        try {
            ProcessResult res = runProcess(cmd, null, outFile);
            if (res.exitCode == 0) {
                JOptionPane.showMessageDialog(parent, "Backup completed successfully:\n" + outFile.getAbsolutePath(),
                        "Backup", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(parent, "Backup failed. Exit code: " + res.exitCode +
                        "\nError output:\n" + res.stderr, "Backup failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(parent, "Backup failed: " + ex.getMessage(), "Backup failed", JOptionPane.ERROR_MESSAGE);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            JOptionPane.showMessageDialog(parent, "Backup interrupted.", "Backup interrupted", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void restoreDatabases(JFrame parent) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose SQL backup file to restore");

        if (chooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) return;
        File inFile = chooser.getSelectedFile();

        List<String> cmd = new ArrayList<>();
        cmd.add(MYSQL_CMD);
        cmd.add("-u");
        cmd.add(getDbUser());
        // do not add -p on command line; we set MYSQL_PWD env var to avoid exposing it

        try {
            ProcessResult res = runProcess(cmd, inFile, null);
            if (res.exitCode == 0) {
                JOptionPane.showMessageDialog(parent, "Restore completed successfully from:\n" + inFile.getAbsolutePath(),
                        "Restore", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(parent, "Restore failed. Exit code: " + res.exitCode +
                        "\nError output:\n" + res.stderr, "Restore failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(parent, "Restore failed: " + ex.getMessage(), "Restore failed", JOptionPane.ERROR_MESSAGE);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            JOptionPane.showMessageDialog(parent, "Restore interrupted.", "Restore interrupted", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Runs external process with optional input/output redirection and environment MYSQL_PWD set.
     *
     * @param command the command and arguments to run
     * @param inputFile if non-null, will be used as process stdin (redirectInput)
     * @param outputFile if non-null, will be used as process stdout (redirectOutput)
     * @return ProcessResult containing exit code and captured stderr (and a short timeout guarded runtime)
     */
    private static ProcessResult runProcess(List<String> command, File inputFile, File outputFile)
            throws IOException, InterruptedException {

        ProcessBuilder pb = new ProcessBuilder(command);

        // Set password via environment variable so it is not visible in process list arguments.
        Map<String, String> env = pb.environment();
        String pwd = getDbPass();
        if (pwd != null) env.put("MYSQL_PWD", pwd);

        // Redirect input/output if files are provided
        if (inputFile != null) {
            pb.redirectInput(inputFile);
        }
        if (outputFile != null) {
            pb.redirectOutput(outputFile);
            // Ensure error stream not redirected - we will capture it below
        }

        // Start process
        Process proc;
        try {
            proc = pb.start();
        } catch (IOException e) {
            // Common cause: command not found (mysqldump/mysql). Surface a friendly message.
            throw new IOException("Failed to start process: " + String.join(" ", command) +
                    ". Ensure executable is on PATH or update the command path. (" + e.getMessage() + ")", e);
        }

        // Consume stdout (only if not redirected to file). We still consume to avoid blocking.
        StreamGobbler stdoutGobbler = null;
        if (outputFile == null) {
            stdoutGobbler = new StreamGobbler(proc.getInputStream());
            stdoutGobbler.start();
        }

        // Always capture stderr
        StreamGobbler stderrGobbler = new StreamGobbler(proc.getErrorStream());
        stderrGobbler.start();

        // Wait with a sensible timeout (optional). Here we wait, but you may increase or remove timeout.
        // Using a timeout avoids hanging forever if a process stalls.
        Instant start = Instant.now();
        long timeoutSeconds = 300; // 5 minutes (tune as needed)

        while (true) {
            try {
                if (proc.waitFor(1, java.util.concurrent.TimeUnit.SECONDS)) {
                    break;
                }
            } catch (InterruptedException ie) {
                proc.destroyForcibly();
                throw ie;
            }
            // optional timeout check
            if (Duration.between(start, Instant.now()).getSeconds() > timeoutSeconds) {
                proc.destroyForcibly();
                throw new IOException("Process timed out after " + timeoutSeconds + " seconds.");
            }
        }

        int exit = proc.exitValue();
        // ensure gobblers finished reading
        if (stdoutGobbler != null) stdoutGobbler.joinQuietly();
        stderrGobbler.joinQuietly();

        String stderr = stderrGobbler.getContent();

        return new ProcessResult(exit, stderr);
    }

    // Helper to capture a stream asynchronously
    private static class StreamGobbler extends Thread {
        private final InputStream is;
        private final StringBuilder sb = new StringBuilder();

        StreamGobbler(InputStream is) {
            this.is = is;
            setDaemon(true);
        }

        @Override
        public void run() {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append('\n');
                }
            } catch (IOException ignored) {
            }
        }

        String getContent() {
            return sb.toString();
        }

        void joinQuietly() {
            try { join(1000); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
        }
    }

    // Simple holder for process result
    private static class ProcessResult {
        final int exitCode;
        final String stderr;
        ProcessResult(int exitCode, String stderr) {
            this.exitCode = exitCode;
            this.stderr = stderr == null ? "" : stderr;
        }
    }
}