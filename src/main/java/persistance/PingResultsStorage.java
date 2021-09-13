package persistance;

import java.util.Map;
import networkUtils.Pinger;

public interface PingResultsStorage {
    void save(String host, Class<? extends Pinger> pingType, Pinger.Result result);
    Map<Class<? extends Pinger>, Pinger.Result> getLastEntriesForAHost(String host);
}
