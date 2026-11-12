import java.util.HashMap;
import java.util.Map;

public class CacheManager {
    // ساختار پیشنهادی برای نگهداری هر رکورد در کش[cite: 10, 21]
    private static class CacheEntry {
        private String ip;
        private int initialTtl;
        private long savedTimeMillis;

        CacheEntry(String ip, int initialTtl, long savedTimeMillis) {
            this.ip = ip;
            this.initialTtl = initialTtl;
            this.savedTimeMillis = savedTimeMillis;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public int getInitialTtl() {
            return initialTtl;
        }

        public void setInitialTtl(int initialTtl) {
            this.initialTtl = initialTtl;
        }

        public long getSavedTimeMillis() {
            return savedTimeMillis;
        }

        public void setSavedTimeMillis(long savedTimeMillis) {
            this.savedTimeMillis = savedTimeMillis;
        }
    }

    private final Map<String, CacheEntry> cache = new HashMap<>();

    /**
     * TODO (عضو دوم): جستجو در کش محلی[cite: 6, 10, 21]
     * - محاسبه ثانیه‌های گذشته از زمان ذخیره: (System.currentTimeMillis() - savedTimeMillis) / 1000
     * - محاسبه TTL باقی‌مانده: initialTtl - elapsedSeconds[cite: 10, 21]
     * - اگر باقی‌مانده >= 1 بود، برگرداندن DnsResponse با Source="CACHE" و Status="OK"[cite: 9, 10]
     * - اگر منقضی شده بود (باقی‌مانده < 1)، حذف از کش و برگرداندن null[cite: 10, 21]
     */
    public DnsResponse get(String normalizedDomain) {
        // TODO: پیاده‌سازی توسط عضو دوم
        return null;
    }

    /**
     * TODO (عضو دوم): ذخیره پاسخ در کش[cite: 6, 10, 21]
     * - فقط پاسخ‌های OK از Upstream که ttl > 0 دارند ذخیره شوند[cite: 10]
     * - پاسخ‌های NOT_FOUND و SERVER_ERROR هرگز ذخیره نشوند[cite: 10, 21]
     */
    public void put(String normalizedDomain, String ip, int ttl) {
        // TODO: پیاده‌سازی توسط عضو دوم
    }
}