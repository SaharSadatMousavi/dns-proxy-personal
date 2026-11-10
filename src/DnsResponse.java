public class DnsResponse {
    public String domain;
    public String source;
    public String status;
    public String ip;
    public String ttl;

    public DnsResponse(String domain, String source, String status, String ip, String ttl) {
        this.domain = domain;
        this.source = source;
        this.status = status;
        this.ip = ip;
        this.ttl = ttl;
    }

    public DnsResponse(String domain, String source, String status, String ip, int ttl) {
        this.domain = domain;
        this.source = source;
        this.status = status;
        this.ip = ip;
        this.ttl = String.valueOf(ttl);
    }
}
