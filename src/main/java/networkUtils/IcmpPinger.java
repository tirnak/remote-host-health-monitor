package networkUtils;

import configuration.Configuration;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import logger.Logger;
import persistance.PingResultsStorage;
import reporting.Reporter;

public class IcmpPinger implements Pinger {

    private final Configuration configuration;
    private volatile PingResultsStorage storage;
    private volatile Reporter reporter;
    private final Logger logger;

    public IcmpPinger(Configuration configuration, PingResultsStorage storage, Reporter reporter, Logger logger) {
        this.configuration = configuration;
        this.storage = storage;
        this.reporter = reporter;
        this.logger = logger;
    }

    @Override
    public Map<String, Runnable> createRunnables() {
        List<Map.Entry<String, Runnable>> jobsForHosts = configuration.getHosts().stream()
            .map(host -> {
                Runnable job = () -> {
                    Result result = SimpleResult.getDefault();
                    try {
                        String cmd = configuration.templateIcmpPing(host);
                        ProcessRunner shellRunner = new ProcessRunner(cmd);
                        result = shellRunner.execute(configuration.icmpTimeout);
                        storage.save(host, IcmpPinger.class, result);

                    } catch (IOException | InterruptedException e) {
                        result = new SimpleResult(
                                false,
                                e.getMessage()
                        );
                        storage.save(host, IcmpPinger.class, result);
                    } finally {
                        if (!result.isSuccessful()) {
                            Map<Class<? extends Pinger>, Result> lastEntries = storage.getLastEntriesForAHost(host);
                            reporter.notify(host, lastEntries);
                        }
                    }
                };
                return new AbstractMap.SimpleEntry<String, Runnable>(host, job);
            }).collect(Collectors.toList());

        Map<String, Runnable> runnables = new HashMap<>();
        for (Map.Entry<String, Runnable> jobsForHost : jobsForHosts) {
            runnables.put(jobsForHost.getKey(), jobsForHost.getValue());
        }

        return runnables;

//
//        return () -> {
//            List<Thread> threads = jobsForHosts.stream().map(job -> {
//                    Thread jobThread = new Thread(job);
//                    jobThread.start();
//                    return jobThread;
//                }).collect(Collectors.toList());
//
//            for (Thread thread : threads) {
//                try {
//                    thread.join();
//                    // TODO better exception handling
//                } catch (InterruptedException e) {
//                    logger.warn("ICMP ping thread execution failed: " + e.getMessage());
//                }
//            };
//        };
    }
}
