import static org.junit.Assert.assertEquals;

import configuration.Configuration;
import java.nio.file.Files;
import java.util.Properties;
import logger.Logger;
import org.junit.Test;

public class LoggerTest {
    @Test
    public void happyPath() throws Exception {

        Properties properties = ConfigurationTest.getMinimumProperties();
        Configuration configuration = new Configuration(properties);
        Files.deleteIfExists(configuration.pathToLogFile);
        Files.createFile(configuration.pathToLogFile);

        Logger logger = new Logger(configuration.pathToLogFile);
        logger.warn("this is my message.");
        String result = Files.readString(configuration.pathToLogFile);

        assertEquals("[WARN] this is my message.\n", result);
        // TODO move file creation/deletion to setup/teardown
        Files.deleteIfExists(configuration.pathToLogFile);
    }
}
