package configuration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import logger.Logger;

public class Configuration {

    public final String reportReceivingHost;
    public final Duration icmpInterval;
    public final Duration icmpTimeout;
    public final Duration httpInterval;
    public final Duration httpTimeout;
    public final Path pathToLogFile;
    public final Logger.LogLevel logLevel;
    private final List<String> hosts;
    private final String tracerouteTemplate;
    private final String icmpPingTemplate;

    public Configuration(Properties input) throws Exception {
        hosts = Arrays.asList(input.getProperty("hosts").split(","));
        reportReceivingHost = input.getProperty("report-receiving-host");
        icmpInterval = Duration.ofSeconds(
                Integer.parseInt(input.getProperty("icmp.interval")));
        icmpTimeout = Duration.ofSeconds(
                Integer.parseInt(input.getProperty("icmp.timeout")));
        httpInterval = Duration.ofSeconds(
                Integer.parseInt(input.getProperty("http.interval")));
        httpTimeout = Duration.ofSeconds(
                Integer.parseInt(input.getProperty("http.timeout")));

        tracerouteTemplate = input.getProperty("traceroute.command");
        icmpPingTemplate = input.getProperty("icmp.command");

        pathToLogFile = Paths.get(input.getProperty("log.file"));

        logLevel = Logger.LogLevel.valueOf(input.getProperty("log.level"));
    }

    public List<String> getHosts() {
        return new ArrayList<>(hosts);
    }

    /**
     * TODO make "%" a constant, add to docs.
     */
    public String templateIcmpPing(String host) {
        return icmpPingTemplate.replace("%", host);
    }

    public String templateTraceRoute(String host) {
        return tracerouteTemplate.replace("%", host);
    }
}
