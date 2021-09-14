package networkUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public interface Pinger {

    public Map<String, Runnable> createRunnables();

    public static String getReportName() {
        throw new UnsupportedOperationException("name should be redefined in children");
    };

    interface Result {
        boolean isSuccessful();
        String getOutput();

        default String serialize() {
            return "" + isSuccessful() + "$" + getOutput();
        }

        // add validation, maybe move out
        static Result fromString(String serialized) {
            String[] entries = serialized.split("\\$");
            return new SimpleResult(
                Boolean.parseBoolean(entries[0]),
                entries[1]
            );
        }
    }

    static class SimpleResult implements Result {
        private final boolean successful;
        private final String output;

        public SimpleResult(boolean successful, String output) {
            this.successful = successful;
            this.output = output;
        }

        @Override
        public boolean isSuccessful() {
            return successful;
        }

        @Override
        public String getOutput() {
            return output != null ? output : "Unknown ping error.";
        }

        public static Result getDefault() {
            return new SimpleResult(false, "Unknown ping error.");
        }
    }

    static class ProcessResult implements Result {
        private final int exitCode;
        private final String result;

        public ProcessResult(int exitCode, String result) {
            this.exitCode = exitCode;
            this.result = result;
        }

        @Override
        public boolean isSuccessful() {
            return exitCode == 0;
        }

        @Override
        public String getOutput() {
            return result == null ? "" : result;
        }
    }

    static class HttpResult implements Result {
        private final int httpStatus;
        private final String result;

        public HttpResult(HttpResponse<String> response) {
            this.httpStatus = response.statusCode();
            this.result = response.body();
        }

        @Override
        public boolean isSuccessful() {
            int statusGroup = httpStatus / 100;
            return statusGroup == 2 || statusGroup == 3;
        }

        @Override
        public String getOutput() {
            return result == null ? "" : result;
        }
    }

    class ProcessRunner {
        private final String command;

        public ProcessRunner(String command) {
            this.command = command;
        }

        /**
         * Careful, blocking operation!
         * @param timeout
         */
        public Pinger.Result execute(Duration timeout) throws IOException, InterruptedException {
            ProcessBuilder processBuilder = new ProcessBuilder();

            processBuilder.command(command.split("\\s+"));
            Process process = processBuilder.start();

            StringBuilder output = new StringBuilder();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }

            while ((line = errorReader.readLine()) != null) {
                output.append(line + "\n");
            }

            boolean success = process.waitFor(timeout.toSeconds(), TimeUnit.SECONDS);
            int exitVal = success ? process.exitValue() : -1;
            return new Pinger.ProcessResult(exitVal, output.toString());
        }
    }
}
