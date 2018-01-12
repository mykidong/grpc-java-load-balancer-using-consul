package io.shunters.grpc.api.component;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by mykidong on 2017-09-13.
 */
public interface ServiceDiscovery {

    void createService(String serviceName, String id, List<String> tags, String address, int port, String script, String tcp, String interval, String timeout);

    List<ServiceNode> getHealthServices(String path);

    Set<String> getKVKeysOnly(String keyPath);

    String getKVValue(String key);

    Map<String, String> getKVValues(String keyPath);

    Map<String, String> getLeader(String keyPath);

    void setKVValue(String key, String value);

    void deleteKVValue(String key);

    void deleteKVValuesRecursively(String key);

    String createSession(String name, String node, String ttl, long lockDelay);

    void renewSession(String session);

    boolean acquireLock(String key, String value, String session);

    void destroySession(String session);

    public static class ServiceNode
    {
        private String id;
        private String host;
        private int port;

        public ServiceNode(String id, String host, int port)
        {
            this.id = id;
            this.host = host;
            this.port = port;
        }

        public String getId() {
            return id;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }
    }
}
