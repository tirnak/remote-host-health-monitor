import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

import configuration.Configuration;
import java.util.List;
import java.util.Properties;
import org.junit.Test;

public class ConfigurationTest {

    @Test
    public void checkConfigurationCreation() throws Exception {
        Properties properties = new Properties();

        properties.setProperty("hosts", "asdf.com,google.lu");
        properties.setProperty("report-receiving-host", "localhost");
        properties.setProperty("icmp.interval", "5");
        properties.setProperty("icmp.timeout", "1");
        properties.setProperty("http.interval", "5");
        properties.setProperty("http.timeout", "1");
        properties.setProperty("traceroute.command", "traceroute %");

        properties.setProperty("log.file", "/tmp/host-health-monitor.log");
        properties.setProperty("log.level", "WARNING");

        Configuration configuration = new Configuration(properties);

        assertEquals(List.of("asdf.com", "google.lu"), configuration.getHosts());
    }
}
