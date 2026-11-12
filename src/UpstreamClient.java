public class UpstreamClient {

    /**
     * TODO (عضو دوم): ارسال پیام متنی روی سوکت UDP به Upstream و دریافت پاسخ[cite: 9, 10, 21]
     * ۱. ساخت DatagramSocket و تنظیم setSoTimeout(config.upstreamTimeoutSec * 1000)[cite: 9, 10, 21]
     * ۲. ارسال پیام متنی "QUERY " + normalizedDomain به upstreamIp و upstreamPort[cite: 10, 21]
     * ۳. انتظار برای دریافت پاسخ (فقط یک تلاش، بدون ارسال مجدد)[cite: 8, 10]
     * ۴. پارس کردن پاسخ:
     *    - اگر "OK <ip> <ttl>" بود -> برگرداندن DnsResponse با Source="UPSTREAM" و Status="OK"[cite: 9, 10, 21]
     *    - اگر "NOT_FOUND" بود -> برگرداندن DnsResponse با Source="UPSTREAM", Status="NOT_FOUND", ip="-", ttl="-"[cite: 9, 10, 21]
     *    - اگر Timeout رخ داد یا خطای شبکه یا فرمت نامفهوم بود -> برگرداندن SERVER_ERROR بدون Crash[cite: 6, 9, 10, 21]
     */
    public DnsResponse query(String normalizedDomain, Config config) {
        // TODO: پیاده‌سازی توسط عضو دوم
        return new DnsResponse(normalizedDomain, "UPSTREAM", "SERVER_ERROR", "-", "-");
    }
}