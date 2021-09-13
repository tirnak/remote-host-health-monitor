import static org.junit.Assert.assertTrue;

import configuration.Configuration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import logger.Logger;
import networkUtils.HttpPinger;
import networkUtils.IcmpPinger;
import networkUtils.Pinger;
import networkUtils.RouteTracer;
import org.junit.Ignore;
import org.junit.Test;
import persistance.PingResultsStorage;
import reporting.Reporter;

/**
 * Warning: not a "true" unit test, since assumes WWW access.
 * TODO either move to integration or have test profiles
 */
public class PingsTest {

    @Test
    public void happyPathIcmpPing() throws Exception {
        Configuration configuration = new Configuration(ConfigurationTest.getMinimumProperties());
        PingResultsStorage storage = getMockedStorage();
        Pinger pinger = new IcmpPinger(configuration, storage, getMockedReporter(), getMockedLogger(configuration));
        Map<String, Runnable> icmpPingerRunnables = pinger.createRunnables();
        String host = "google.lu";
        Runnable job = icmpPingerRunnables.get(host);
        job.run();
        assertTrue(storage.getLastEntriesForAHost("google.lu").get(IcmpPinger.class).isSuccessful());
    }

    @Test
    public void happyPathTraceroute() throws Exception {
        Configuration configuration = new Configuration(ConfigurationTest.getMinimumProperties());
        PingResultsStorage storage = getMockedStorage();
        Pinger pinger = new RouteTracer(configuration, storage, getMockedReporter(), getMockedLogger(configuration));
        Map<String, Runnable> RouteTracerRunnables = pinger.createRunnables();
        String host = "google.lu";
        Runnable job = RouteTracerRunnables.get(host);
        job.run();
        assertTrue(storage.getLastEntriesForAHost("google.lu").get(RouteTracer.class).isSuccessful());
    }

    /**
     * TODO replace with more reliable endpoint.
     * New endpoints can be created at https://webhook.site/
     */
    @Test
    public void happyPathHttpPing() throws Exception {
        Properties properties = ConfigurationTest.getMinimumProperties();
        String host = "webhook.site/741b40fe-9d21-48b5-a554-fed107198567";
        properties.setProperty("hosts", host);
        Configuration configuration = new Configuration(properties);
        PingResultsStorage storage = getMockedStorage();
        Pinger pinger = new HttpPinger(configuration, storage, getMockedReporter(), getMockedLogger(configuration));
        Map<String, Runnable> httpPingerRunnables = pinger.createRunnables();
        Runnable job = httpPingerRunnables.get(host);
        job.run();
        assertTrue(storage.getLastEntriesForAHost(host).get(HttpPinger.class).isSuccessful());
    }

    /**
     * Not a real test, but a testing ground to test Report functionality.
     */
    @Ignore
    @Test
    public void ReportGeneration() throws Exception {
        Properties properties = ConfigurationTest.getMinimumProperties();
        String host = "https://nonexistingdomain.io/";

        properties.setProperty("hosts", host);
        properties.setProperty("report-receiving-host", "webhook.site/741b40fe-9d21-48b5-a554-fed107198567");
        Configuration configuration = new Configuration(properties);
        PingResultsStorage storage = getMockedStorage();

        Pinger pinger = new HttpPinger(configuration, storage, new Reporter(configuration), getMockedLogger(configuration));
        Runnable httpPingerRunnable = pinger.createRunnables().get(host);
        httpPingerRunnable.run();

        pinger = new IcmpPinger(configuration, storage, new Reporter(configuration), getMockedLogger(configuration));
        Runnable icmpPingerRunnable = pinger.createRunnables().get(host);
        icmpPingerRunnable.run();

        pinger = new RouteTracer(configuration, storage, new Reporter(configuration), getMockedLogger(configuration));
        Runnable traceRouteRunnable = pinger.createRunnables().get(host);
        traceRouteRunnable.run();
    }

    private static Reporter getMockedReporter() {
        return new Reporter(null) {
            @Override
            public void notify(String host, Map<Class<? extends Pinger>, Pinger.Result> lastEntries) {
                // do nothing
            }
        } ;
    }

    public static PingResultsStorage getMockedStorage() {

        return new PingResultsStorage() {
            private volatile Map<Map.Entry<String, Class<? extends Pinger>>, Pinger.Result> data =
                new ConcurrentHashMap<>();

            @Override
            public void save(String host, Class<? extends Pinger> pingType, Pinger.Result result) {
                Map.Entry<String, Class<? extends Pinger>> key =Map.entry(host, pingType);
                data.put(key, result);
            }

            @Override
            public Map<Class<? extends Pinger>, Pinger.Result> getLastEntriesForAHost(String host) {
                List<Map.Entry<Map.Entry<String, Class<? extends Pinger>>, Pinger.Result>> relatedEntries =
                data.entrySet().stream()
                    .filter(entry -> entry.getKey().getKey().equals(host))
                    .collect(Collectors.toList());

                Map<Class<? extends Pinger>, Pinger.Result> result = new HashMap<>();
                for (Map.Entry<Map.Entry<String, Class<? extends Pinger>>, Pinger.Result> entry : relatedEntries) {
                    result.put(
                        entry.getKey().getValue(),
                        entry.getValue()
                    );
                }
                return result;
            }
        };
    }

    public Logger getMockedLogger(Configuration configuration) {
        return new Logger(null) {
            @Override
            public void warn(String s) {
                // do nothing
            }
        };
    }
}
