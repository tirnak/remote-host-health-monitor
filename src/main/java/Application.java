import configuration.Configuration;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Properties;
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

        String host = "google.lu";

        Pinger pinger = new HttpPinger(configuration, storage, new Reporter(configuration), new Logger(configuration.pathToLogFile));
        Runnable httpPingerRunnable = pinger.createRunnables().get(host);
        httpPingerRunnable.run();

        pinger = new IcmpPinger(configuration, storage, new Reporter(configuration), new Logger(configuration.pathToLogFile));
        Runnable icmpPingerRunnable = pinger.createRunnables().get(host);
        icmpPingerRunnable.run();

        pinger = new RouteTracer(configuration, storage, new Reporter(configuration), new Logger(configuration.pathToLogFile));
        Runnable traceRouteRunnable = pinger.createRunnables().get(host);
        traceRouteRunnable.run();
    }
}
