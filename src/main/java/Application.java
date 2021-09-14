import configuration.Configuration;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import logger.Logger;
import networkUtils.HttpPinger;
import networkUtils.IcmpPinger;
import networkUtils.Pinger;
import networkUtils.RouteTracer;
import persistance.PingResultsStorage;
import persistance.RedisStorage;
import reporting.Reporter;

public class Application {
    public static void main(String[] args) throws Exception {

        Properties properties = new Properties();
        ClassLoader cl = Application.class.getClassLoader();
        // todo make configurable
        FileInputStream in = new FileInputStream(new File(cl.getResource("./configuration.properties").getFile()));
        properties.load(in);
        in.close();

        Configuration configuration = new Configuration(properties);

        PingResultsStorage storage = new RedisStorage();

        ScheduledExecutorService pool = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());

        Pinger httpPinger = new HttpPinger(configuration, storage, new Reporter(configuration), new Logger(configuration.pathToLogFile));
        for (Runnable pingingTask : httpPinger.createRunnables().values()) {
            pool.scheduleWithFixedDelay(pingingTask, 0, configuration.httpInterval.toSeconds(), TimeUnit.SECONDS);
        }

        Pinger icmpPinger = new IcmpPinger(configuration, storage, new Reporter(configuration), new Logger(configuration.pathToLogFile));
        for (Runnable pingingTask : icmpPinger.createRunnables().values()) {
            pool.scheduleWithFixedDelay(pingingTask, 0, configuration.icmpInterval.toSeconds(), TimeUnit.SECONDS);
        }

        Pinger routeTracer = new RouteTracer(configuration, storage, new Reporter(configuration), new Logger(configuration.pathToLogFile));
        for (Runnable pingingTask : routeTracer.createRunnables().values()) {
            pool.scheduleWithFixedDelay(pingingTask, 0, configuration.tracerouteInterval.toSeconds(), TimeUnit.SECONDS);
        }

    }
}
