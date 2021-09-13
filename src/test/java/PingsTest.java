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


//    @Test
//    @Ignore
//    public void happyPathTraceroute() throws Exception {
//        Configuration configuration = new Configuration(ConfigurationTest.getMinimumProperties());
//        PingResultsStorage storage = getMockedStorage();
//        Pinger pinger = new RouteTracer(configuration, storage, getMockedReporter(), getMockedLogger(configuration));
//        Map<String, Runnable> icmpPingerRunnables = pinger.createRunnables();
//        String host = "google.lu";
//        Runnable job = icmpPingerRunnables.get(host);
//        job.run();
//        assertTrue(storage.getLastEntriesForAHost("google.lu").get(IcmpPinger.class).isSuccessful());
//    }

    /**
     * TODO replace with more reliable endpoint.
     * New endpoints can be created at https://webhook.site/
     */
    @Test
    public void simpleHttpPing() throws Exception {
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

    private static Reporter getMockedReporter() {
        return new Reporter();
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
