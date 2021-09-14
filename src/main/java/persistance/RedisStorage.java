package persistance;

import java.util.HashMap;
import java.util.Map;
import networkUtils.HttpPinger;
import networkUtils.IcmpPinger;
import networkUtils.Pinger;
import networkUtils.RouteTracer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisStorage implements PingResultsStorage {

    private static final Map<Class<? extends Pinger>, String> PINGER_NAMES = Map.of(
            IcmpPinger.class, "icmp_ping",
            HttpPinger.class, "tcp_ping",
            RouteTracer.class, "trace"
    );

    private static final Map<String, Class<? extends Pinger>> PINGER_NAMES_REVERSE = Map.of(
            "icmp_ping", IcmpPinger.class,
            "tcp_ping", HttpPinger.class,
            "trace", RouteTracer.class
    );

    /**
     * TODO replace hardcoded values here and in the next method.
     */
    @Override
    public void save(String host, Class<? extends Pinger> pingType, Pinger.Result result) {
        JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "localhost", 6379);
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hmset(host, Map.of(PINGER_NAMES.get(pingType), result.serialize()));
        }
    }

    @Override
    public Map<Class<? extends Pinger>, Pinger.Result> getLastEntriesForAHost(String host) {
        JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "localhost", 6379);
        try (Jedis jedis = jedisPool.getResource()) {
            Map<String, String> entries = jedis.hgetAll(host);
            Map<Class<? extends Pinger>, Pinger.Result> result = new HashMap<>();

            for (Map.Entry<String, String> redisEntry : entries.entrySet()) {
                result.put(
                    PINGER_NAMES_REVERSE.get(redisEntry.getKey()),
                        Pinger.Result.fromString(redisEntry.getValue()));
            }
            return result;
        }
    }

    private String toKey(String host, Class<? extends Pinger> pingType) {
        return host + ":" + PINGER_NAMES.get(pingType);
    }
}
