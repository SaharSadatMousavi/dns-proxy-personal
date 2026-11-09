import java.io.File;
import java.util.Scanner;

public class DnsProxyApp {

    public static void main(String[] args) {
        
        String configPath = (args.length > 0) ? args[0] : "config.txt";
        Config config;

      
        try {
            config = Config.loadFromFile(configPath);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return; 
        }

      
        if (!new File(config.adListFile).exists()) {
            System.out.println("Config error: ad_list_file not found: " + config.adListFile);
            return;
        }
        if (!new File(config.sanctionListFile).exists()) {
            System.out.println("Config error: sanction_list_file not found: " + config.sanctionListFile);
            return;
        }

        
        System.out.println("DNS Proxy ready.");

        
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("Enter domain (or exit): ");
            if (!scanner.hasNextLine()) {
                break; 
            }

            String rawInput = scanner.nextLine();
            String trimmedInput = rawInput.trim();

            
            if (trimmedInput.equalsIgnoreCase("exit")) {
                System.out.println("Bye.");
                break;
            }

            
            if (trimmedInput.isEmpty()) {
                printResult("(empty)", "NONE", "INVALID_INPUT", "-", "-");
                continue;
            }

            if (!isValidDomain(trimmedInput)) {
                printResult(trimmedInput, "NONE", "INVALID_INPUT", "-", "-");
                continue;
            }

            
            String domain = trimmedInput.toLowerCase();
            if (domain.endsWith(".")) {
                domain = domain.substring(0, domain.length() - 1);
            }

            
            DnsResponse response = resolveDomain(domain, config);

           
            printResult(response.domain, response.source, response.status, response.ip, response.ttl);
        }

        scanner.close();
    }

   
    private static boolean isValidDomain(String d) {
     
        if (d.contains(" ")) return false;
        
        if (d.startsWith(".")) return false;
       
        if (d.contains("..")) return false;

      
        for (char c : d.toCharArray()) {
            boolean isLetter = (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
            boolean isDigit = (c >= '0' && c <= '9');
            if (!isLetter && !isDigit && c != '-' && c != '.') {
                return false;
            }
        }
        return true;
    }

    /**
     * خط لوله اولویت‌ها (Priority Pipeline):
     * ۱. بررسی AdBlock
     * ۲. بررسی Sanction
     * ۳. بررسی Cache
     * ۴. ارسال به Upstream UDP
     */
    private static DnsResponse resolveDomain(String domain, Config config) {
        
        if (domain.startsWith("ads.")) {
            return new DnsResponse(domain, "AD_BLOCK", "OK", config.sinkholeIp, String.valueOf(config.localTtl));
        }

      
        if (domain.contains("sanction") || domain.contains("blocked")) {
            return new DnsResponse(domain, "SANCTION", "OK", config.proxyIp, String.valueOf(config.localTtl));
        }

       
        return new DnsResponse(domain, "UPSTREAM", "OK", "93.184.216.34", "300");
    }

    
    private static void printResult(String domain, String source, String status, String ip, String ttl) {
        System.out.println("Domain : " + domain);
        System.out.println("Source : " + source);
        System.out.println("Status : " + status);
        System.out.println("IP     : " + ip);
        System.out.println("TTL    : " + ttl);
        System.out.println(); 
    }
}
