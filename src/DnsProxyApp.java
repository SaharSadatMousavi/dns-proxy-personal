import java.util.Scanner;

public class DnsProxyApp {

    public static void main(String[] args) {
        Config config;
        DomainManager domainManager = new DomainManager();
        CacheManager cacheManager = new CacheManager();
        UpstreamClient upstreamClient = new UpstreamClient();

        // ۱. بارگذاری تنظیمات و فهرست‌ها در شروع برنامه[cite: 7, 8, 9]
        try {
            config = Config.loadFromFile("config.txt");
            domainManager.loadLists(config.getAdListFile(), config.getSanctionListFile());
        } catch (Exception e) {
            System.out.println("Config error: " + e.getMessage());
            return; // تنها حالت مجاز توقف برنامه به جز دستور exit[cite: 8]
        }

        // ۲. چاپ پیام آماده بودن و شروع حلقه اصلی کنسول[cite: 9, 22]
        System.out.println("DNS Proxy ready.");
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("Enter domain (or exit): ");
            if (!scanner.hasNextLine()) {
                break;
            }
            String input = scanner.nextLine().trim(); // حذف فاصله‌های ابتدا و انتها[cite: 9]

            // گام ۱ فلوچارت: بررسی دستور exit (بدون حساسیت به حروف بزرگ و کوچک)[cite: 6, 7, 9, 22]
            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Bye.");
                break;
            }

            // گام ۲ فلوچارت: بررسی خط خالی و اعتبارسنجی ورودی[cite: 6, 7, 9, 20, 22]
            if (input.isEmpty()) {
                System.out.println(new DnsResponse("(empty)", "NONE", "INVALID_INPUT", "-", "-"));
                continue;
            }
            if (!domainManager.isValidDomain(input)) {
                System.out.println(new DnsResponse(input, "NONE", "INVALID_INPUT", "-", "-"));
                continue;
            }

            // گام ۳ فلوچارت: نرمال‌سازی نام دامنه[cite: 6, 7, 20]
            String normalizedDomain = domainManager.normalize(input);

            // گام ۴ و ۵ فلوچارت: بررسی فهرست تبلیغات و تحریم (توسط ماژول عضو اول)[cite: 6, 7, 20]
            DnsResponse listResponse = domainManager.checkLists(normalizedDomain, config);
            if (listResponse != null) {
                System.out.println(listResponse);
                continue;
            }

            // گام ۶ فلوچارت: بررسی کش محلی در صورت فعال بودن (توسط ماژول عضو دوم)[cite: 6, 7, 10, 21, 22]
            if (config.isCacheEnabled()) {
                DnsResponse cachedResponse = cacheManager.get(normalizedDomain);
                if (cachedResponse != null) {
                    System.out.println(cachedResponse);
                    continue;
                }
            }

            // گام ۷ فلوچارت: پرس‌وجو از Upstream روی UDP (توسط ماژول عضو دوم)[cite: 6, 7, 9, 10, 21]
            DnsResponse upstreamResponse = upstreamClient.query(normalizedDomain, config);

            // ذخیره در کش فقط اگر کش فعال باشد و پاسخ OK با TTL > 0 باشد[cite: 6, 10, 21, 22]
            if (config.isCacheEnabled() && "OK".equals(upstreamResponse.getStatus())) {
                try {
                    int ttl = Integer.parseInt(upstreamResponse.getTtl());
                    if (ttl > 0) {
                        cacheManager.put(normalizedDomain, upstreamResponse.getIp(), ttl);
                    }
                } catch (NumberFormatException ignored) {
                }
            }

            System.out.println(upstreamResponse);
        }

        scanner.close();
    }


}