public class UpstreamServer {
    /**
     * TODO (عضو دوم): برنامه مستقل شبیه‌ساز Upstream که در کنسول ۱ اجرا می‌شود[cite: 10, 11, 21]
     * - گوش دادن روی پورت UDP 5354[cite: 10]
     * - دریافت پیام به شکل "QUERY <domain>"[cite: 10]
     * - اگر دامنه برابر "missing.example.net" بود، پاسخ "NOT_FOUND" بفرستد[cite: 10, 21]
     * - برای هر دامنه دیگر، پاسخ "OK 198.51.100.7 5" بفرستد[cite: 10, 21]
     * - در کنسول خودش به ازای هر درخواست چاپ کند (N از ۱ شروع می‌شود):
     *   Request #N: <domain> -> <reply>[cite: 10]
     */
    public static void main(String[] args) {
        // TODO: پیاده‌سازی توسط عضو دوم
        System.out.println("Upstream simulator started on UDP port 5354...");
    }
}