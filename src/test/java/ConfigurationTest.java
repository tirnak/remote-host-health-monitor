import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

import configuration.Configuration;
import java.util.List;
import java.util.Properties;
import org.junit.Test;

public class ConfigurationTest {

    @Test
    public void checkConfigurationCreation() throws Exception {
        Properties properties = getMinimumProperties();

        Configuration configuration = new Configuration(properties);

        assertEquals(List.of("asdf.com", "google.lu"), configuration.getHosts());
    }

    public static Properties getMinimumProperties() {
        Properties properties = new Properties();
        properties.setProperty("hosts", "asdf.com,google.lu");
        properties.setProperty("report-receiving-host", "localhost");

        properties.setProperty("icmp.interval", "5");
        properties.setProperty("icmp.timeout", "3");
        properties.setProperty("http.interval", "5");
        properties.setProperty("http.timeout", "3");
        properties.setProperty("traceroute.interval", "5");
        properties.setProperty("traceroute.timeout", "3");
        properties.setProperty("traceroute.command", "traceroute %");
        properties.setProperty("icmp.command", "ping -c1 %");

        properties.setProperty("log.file", "/tmp/host-health-monitor.log");
        properties.setProperty("log.level", "WARNING");

        return properties;
    }
}
