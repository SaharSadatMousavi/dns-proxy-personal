import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Config {
    private String upstreamIp;
    private int upstreamPort;
    private int upstreamTimeoutSec ; 
    private String sinkholeIp;
    private String proxyIp;
    private int localTtl ;         
    private String adListFile;
    private String sanctionListFile;
    private boolean cacheEnabled ; 

    public String getUpstreamIp() {
        return upstreamIp;
    }

    public void setUpstreamIp(String upstreamIp) {
        this.upstreamIp = upstreamIp;
    }

    public int getUpstreamPort() {
        return upstreamPort;
    }

    public void setUpstreamPort(int upstreamPort) {
        this.upstreamPort = upstreamPort;
    }

    public int getUpstreamTimeoutSec() {
        return upstreamTimeoutSec;
    }

    public void setUpstreamTimeoutSec(int upstreamTimeoutSec) {
        this.upstreamTimeoutSec = upstreamTimeoutSec;
    }

    public String getSinkholeIp() {
        return sinkholeIp;
    }

    public void setSinkholeIp(String sinkholeIp) {
        this.sinkholeIp = sinkholeIp;
    }

    public String getProxyIp() {
        return proxyIp;
    }

    public void setProxyIp(String proxyIp) {
        this.proxyIp = proxyIp;
    }

    public int getLocalTtl() {
        return localTtl;
    }

    public void setLocalTtl(int localTtl) {
        this.localTtl = localTtl;
    }

    public String getAdListFile() {
        return adListFile;
    }

    public void setAdListFile(String adListFile) {
        this.adListFile = adListFile;
    }

    public String getSanctionListFile() {
        return sanctionListFile;
    }

    public void setSanctionListFile(String sanctionListFile) {
        this.sanctionListFile = sanctionListFile;
    }

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public void setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }

    
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

        config.sinkholeIp = require(map, "sinkhole_ip");
        validateIpv4(config.sinkholeIp, "sinkhole_ip");

        config.proxyIp = require(map, "proxy_ip");
        validateIpv4(config.proxyIp, "proxy_ip");

        config.adListFile = require(map, "ad_list_file");
        config.sanctionListFile = require(map, "sanction_list_file");

       
        if (map.containsKey("upstream_timeout_sec")) {
            config.upstreamTimeoutSec = parseInt(map.get("upstream_timeout_sec"), "upstream_timeout_sec", 1, 300);
        }

        if (map.containsKey("local_ttl")) {
            config.localTtl = parseInt(map.get("local_ttl"), "local_ttl", 0, 86400);
        }

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
