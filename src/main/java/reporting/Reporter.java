package reporting;

import configuration.Configuration;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import networkUtils.HttpPinger;
import networkUtils.IcmpPinger;
import networkUtils.Pinger;
import networkUtils.RouteTracer;

public class Reporter {

    private final URI reportingWebhook;

    private static final Map<Class<? extends Pinger>, String> PINGER_NAMES = Map.of(
            IcmpPinger.class, "icmp_ping",
            HttpPinger.class, "tcp_ping",
            RouteTracer.class, "trace"
    );



    public Reporter(Configuration configuration) {
        this.reportingWebhook = URI.create("https://%/".replace("%", configuration.reportReceivingHost));
    }

    public void notify(String host, Map<Class<? extends Pinger>, Pinger.Result> lastEntries) {

        String data = toJson(host, lastEntries);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(data))
                .setHeader("Content-Type", "application/json")
                .uri(reportingWebhook)
                .build();

        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Expected format:
     * {"host":"the given host",
     *  "icmp_ping":"result lines of the last icmp ping command",
     *  "tcp_ping":"result lines of the last tcp ping command",
     *  "trace":"result lines of the last trace command"
     *  }
     */
    private String toJson(String host, Map<Class<? extends Pinger>, Pinger.Result> lastEntries) {
        StringBuilder sb = new StringBuilder("{");
        for (Map.Entry<Class<? extends Pinger>, Pinger.Result> entries : lastEntries.entrySet()) {
            sb.append("\"" + PINGER_NAMES.get(entries.getKey()) + "\" : \""
                + escapeForJson(entries.getValue().getOutput()) + "\",");
        }
        sb.append("\"host\" : \"" + host + "\" }");
        return sb.toString();
    }

    /**
     * TODO replace with framework/lib implementation.
     */
    private String escapeForJson(String input) {
        return input
            .replace("\"", "\\\"");
    }
}
