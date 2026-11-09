import java.io.BufferedReader;
import java.io.File;
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
        File file = new File(filePath);
        if (!file.exists() || file.isDirectory()) {
            throw new Exception("Configuration file not found: " + filePath);
        }

        Map<String, String> map = new HashMap<>();

       
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                line = line.trim();

               
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                int eqIndex = line.indexOf('=');
                if (eqIndex == -1) {
                    throw new Exception("Malformed line " + lineNumber + " in " + filePath + ": " + line);
                }

                String key = line.substring(0, eqIndex).trim();
                String value = line.substring(eqIndex + 1).trim();
                map.put(key, value);
            }
        }
           Config cfg = new Config();

       
        cfg.upstreamIp = require(map, "upstream_ip");
        validateIpv4(cfg.upstreamIp, "upstream_ip");

        cfg.sinkholeIp = require(map, "sinkhole_ip");
        validateIpv4(cfg.sinkholeIp, "sinkhole_ip");

        cfg.proxyIp = require(map, "proxy_ip");
        validateIpv4(cfg.proxyIp, "proxy_ip");

        cfg.adListFile = require(map, "ad_list_file");
        cfg.sanctionListFile = require(map, "sanction_list_file");
        cfg.upstreamPort = parseInt(require(map, "upstream_port"), "upstream_port", 1, 65535);

        
        if (map.containsKey("upstream_timeout_sec")) {
            cfg.upstreamTimeoutSec = parseInt(map.get("upstream_timeout_sec"), "upstream_timeout_sec", 1, 60);
        }
        if (map.containsKey("local_ttl")) {
            cfg.localTtl = parseInt(map.get("local_ttl"), "local_ttl", 0, Integer.MAX_VALUE);
        }
        if (map.containsKey("cache_enabled")) {
            cfg.cacheEnabled = parseBoolean(map.get("cache_enabled"), "cache_enabled");
        }

        return cfg;
    }

    
    private static String require(Map<String, String> map, String key) throws Exception {
        String val = map.get(key);
        if (val == null || val.isEmpty()) {
            throw new Exception("Missing required configuration key: " + key);
        }
        return val;
    }

    
    private static int parseInt(String val, String keyName, int min, int max) throws Exception {
        try {
            int num = Integer.parseInt(val);
            if (num < min || num > max) {
                throw new Exception("Key " + keyName + " out of valid range [" + min + ", " + max + "]: " + val);
            }
            return num;
        } catch (NumberFormatException e) {
            throw new Exception("Key " + keyName + " must be an integer: " + val);
        }
    }

   
    private static boolean parseBoolean(String val, String keyName) throws Exception {
        if ("true".equalsIgnoreCase(val)) return true;
        if ("false".equalsIgnoreCase(val)) return false;
        throw new Exception("Key " + keyName + " must be 'true' or 'false': " + val);
    }

   
    private static void validateIpv4(String ip, String keyName) throws Exception {
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            throw new Exception("Invalid IPv4 address for " + keyName + ": " + ip);
        }
        for (String p : parts) {
            try {
                int octet = Integer.parseInt(p);
                if (octet < 0 || octet > 255) {
                    throw new Exception("Invalid IPv4 octet for " + keyName + ": " + ip);
                }
            } catch (NumberFormatException e) {
                throw new Exception("Invalid IPv4 octet for " + keyName + ": " + ip);
            }
        }
    }
}

