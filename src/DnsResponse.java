public class DnsResponse {
    private final String domain;
    private final String source; // AD_BLOCK | SANCTION | CACHE | UPSTREAM | NONE
    private final String status; // OK | NOT_FOUND | SERVER_ERROR | INVALID_INPUT
    private final String ip;     // آدرس IP یا "-"
    private final String ttl;    // ثانیه یا "-"

    public DnsResponse(String domain, String source, String status, String ip, String ttl) {
        this.domain = domain;
        this.source = source;
        this.status = status;
        this.ip = ip;
        this.ttl = ttl;
    }

    public DnsResponse(String domain, String source, String status, String ip, int ttlSeconds) {
        this(domain, source, status, ip, String.valueOf(ttlSeconds));
    }


    public String getDomain() {
        return domain;
    }

    public String getSource() {
        return source;
    }

    public String getStatus() {
        return status;
    }

    public String getIp() {
        return ip;
    }

    public String getTtl() {
        return ttl;
    }


    public String toString() {
        return "Domain: " + domain + "\n" +
                "Source: " + source + "\n" +
                "Status: " + status + "\n" +
                "IP: " + ip + "\n" +
                "TTL: " + ttl + "\n";
    }
}