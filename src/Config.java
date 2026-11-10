import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Config {
    public String upstreamIp;
    public int upstreamPort;
    public int upstreamTimeoutSec = 2;
    public String sinkholeIp;
    public String proxyIp;
    public int localTtl = 60;
    public String adListFile;
    public String sanctionListFile;
    public boolean cacheEnabled = true;

    public static Config loadFromFile(String filePath) throws Exception {
        Config config = new Config();
        Map<String, String> map = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int eqIndex = line.indexOf('=');
                if (eqIndex == -1) {
                    throw new Exception("invalid line in config: " + line);
                }
                String key = line.substring(0, eqIndex).trim();
                String value = line.substring(eqIndex + 1).trim();
                map.put(key, value);
            }
        } catch (IOException e) {
            throw new Exception("could not read file " + filePath + ": " + e.getMessage());
        }

        config.upstreamIp = require(map, "upstream_ip");
        validateIpv4(config.upstreamIp, "upstream_ip");

        config.upstreamPort = parseInt(require(map, "upstream_port"), "upstream_port", 1, 65535);

        if (map.containsKey("upstream_timeout_sec")) {
            config.upstreamTimeoutSec = parseInt(map.get("upstream_timeout_sec"), "upstream_timeout_sec", 1, 300);
        }

        config.sinkholeIp = require(map, "sinkhole_ip");
        validateIpv4(config.sinkholeIp, "sinkhole_ip");

        config.proxyIp = require(map, "proxy_ip");
        validateIpv4(config.proxyIp, "proxy_ip");

        if (map.containsKey("local_ttl")) {
            config.localTtl = parseInt(map.get("local_ttl"), "local_ttl", 0, 86400);
        }

        config.adListFile = require(map, "ad_list_file");
        config.sanctionListFile = require(map, "sanction_list_file");

        if (map.containsKey("cache_enabled")) {
            config.cacheEnabled = parseBoolean(map.get("cache_enabled"), "cache_enabled");
        }

        return config;
    }

    private static String require(Map<String, String> map, String key) throws Exception {
        String val = map.get(key);
        if (val == null || val.isEmpty()) {
            throw new Exception("missing required key: " + key);
        }
        return val;
    }

    private static int parseInt(String val, String keyName, int min, int max) throws Exception {
        try {
            int parsed = Integer.parseInt(val);
            if (parsed < min || parsed > max) {
                throw new Exception("value out of range for " + keyName + ": " + val);
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new Exception("invalid integer for " + keyName + ": " + val);
        }
    }

    private static boolean parseBoolean(String val, String keyName) throws Exception {
        if ("true".equalsIgnoreCase(val)) return true;
        if ("false".equalsIgnoreCase(val)) return false;
        throw new Exception("invalid boolean for " + keyName + ": " + val);
    }

    private static void validateIpv4(String ip, String keyName) throws Exception {
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            throw new Exception("invalid IPv4 address for " + keyName + ": " + ip);
        }
        for (String part : parts) {
            try {
                int octet = Integer.parseInt(part);
                if (octet < 0 || octet > 255) {
                    throw new Exception("octet out of range (0-255) in " + keyName + ": " + ip);
                }
            } catch (NumberFormatException e) {
                throw new Exception("invalid number in IPv4 address for " + keyName + ": " + ip);
            }
        }
    }
}
