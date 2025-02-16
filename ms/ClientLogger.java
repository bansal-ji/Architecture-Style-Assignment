import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientLogger {
    private static final String LOG_FILE = "/usr/app/logs/client_application_logs.txt";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ExecutorService logExecutor = Executors.newSingleThreadExecutor();

    private static void log(String level, String message) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String logMessage = String.format("[%s] [%s] %s", timestamp, level, message);
        
        logExecutor.submit(() -> {
            File logFile = new File(LOG_FILE);
            File parentDir = logFile.getParentFile();
            if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
                System.err.println("Failed to create directory: " + parentDir.getAbsolutePath());
                return;
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
                writer.write(logMessage);
                writer.newLine();
                writer.flush(); 
            } catch (IOException e) {
                System.err.println("Failed to write log: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public static void info(String message) {
        log("INFO", message);
    }

    public static void warn(String message) {
        log("WARNING", message);
    }

    public static void error(String message) {
        log("ERROR", message);
    }
}
