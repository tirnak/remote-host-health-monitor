package logger;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class Logger {

    private final Path fileTowrite;

    public Logger(Path fileTowrite) {
        this.fileTowrite = fileTowrite;
    }

    public void warn(String s) {
        try {
            FileWriter writer = new FileWriter(fileTowrite.toFile());
            writer.append("[WARN] ");
            writer.append(s);
            writer.append("\n");
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException("Unable to write log to file " + fileTowrite);
        }
    }

    public static enum LogLevel {
        ERROR,
        WARNING,
        DEBUG,
    }
}
