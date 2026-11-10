import java.io.*;
import java.net.*;
import java.util.*;

public class DnsProxyApp {

 
    static class CacheEntry {
        String ip;
        long expireTimeMillis;

        CacheEntry(String ip, int ttlSeconds) {
            this.ip = ip;
            this.expireTimeMillis = System.currentTimeMillis() + (ttlSeconds * 1000L);
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expireTimeMillis;
        }

        int getRemainingTtl() {
            long remaining = (expireTimeMillis - System.currentTimeMillis()) / 1000L;
            return (int) Math.max(0, remaining);
        }
    }

    private static final Set<String> adBlockList = new HashSet<>();
    private static final Set<String> sanctionList = new HashSet<>();
    private static final Map<String, CacheEntry> localCache = new HashMap<>();

    public static void main(String[] args) {
        Config config;

       
        try {
            config = Config.loadFromFile("config.txt");
            loadListFile(config.adListFile, adBlockList);
            loadListFile(config.sanctionListFile, sanctionList);
        } catch (Exception e) {
            System.out.println("Config error: " + e.getMessage());
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

    private static DnsResponse resolveDomain(String domain, Config config) {
      
        if (adBlockList.contains(domain)) {
            return new DnsResponse(domain, "AD_BLOCK", "OK", config.sinkholeIp, String.valueOf(config.localTtl));
        }

       
        if (sanctionList.contains(domain)) {
            return new DnsResponse(domain, "SANCTION", "OK", config.proxyIp, String.valueOf(config.localTtl));
        }

        
        if (config.cacheEnabled) {
            CacheEntry entry = localCache.get(domain);
            if (entry != null) {
                if (!entry.isExpired()) {
                    return new DnsResponse(domain, "CACHE", "OK", entry.ip, String.valueOf(entry.getRemainingTtl()));
                } else {
                    localCache.remove(domain);
                }
            }
        }

       
        DnsResponse upstreamRes = queryUpstreamUdp(domain, config);

        // ذخیره در کش در صورت فعال بودن
        if (config.cacheEnabled && "OK".equals(upstreamRes.status)) {
            try {
                int ttl = Integer.parseInt(upstreamRes.ttl);
                if (ttl > 0) {
                    localCache.put(domain, new CacheEntry(upstreamRes.ip, ttl));
                }
            } catch (NumberFormatException ignored) {}
        }

        return upstreamRes;
    }

   
    private static DnsResponse queryUpstreamUdp(String domain, Config config) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(config.upstreamTimeoutSec * 1000);
            InetAddress address = InetAddress.getByName(config.upstreamIp);

            byte[] queryPacket = buildDnsQuery(domain);
            DatagramPacket sendPacket = new DatagramPacket(queryPacket, queryPacket.length, address, config.upstreamPort);
            socket.send(sendPacket);

            byte[] buffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
            socket.receive(receivePacket);

            return parseDnsResponse(receivePacket.getData(), receivePacket.getLength(), domain);

        } catch (SocketTimeoutException e) {
            return new DnsResponse(domain, "UPSTREAM", "SERVER_ERROR", "-", "-");
        } catch (Exception e) {
            return new DnsResponse(domain, "UPSTREAM", "SERVER_ERROR", "-", "-");
        }
    }

   
    private static byte[] buildDnsQuery(String domain) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeShort(0x1234); // Transaction ID
        dos.writeShort(0x0100); // Standard Query (Recursion Desired)
        dos.writeShort(1);      // Questions count
        dos.writeShort(0);      // Answer RRs
        dos.writeShort(0);      // Authority RRs
        dos.writeShort(0);      // Additional RRs

       
        String[] labels = domain.split("\\.");
        for (String label : labels) {
            byte[] bytes = label.getBytes();
            dos.writeByte(bytes.length);
            dos.write(bytes);
        }
        dos.writeByte(0);       
        dos.writeShort(1);     
        dos.writeShort(1);      

        return baos.toByteArray();
    }

    
    private static DnsResponse parseDnsResponse(byte[] data, int length, String domain) {
        if (length < 12) {
            return new DnsResponse(domain, "UPSTREAM", "SERVER_ERROR", "-", "-");
        }

        int flags = ((data[2] & 0xFF) << 8) | (data[3] & 0xFF);
        int rcode = flags & 0x0F;

        if (rcode == 3) { // NXDOMAIN
            return new DnsResponse(domain, "UPSTREAM", "NOT_FOUND", "-", "-");
        }
        if (rcode != 0) {
            return new DnsResponse(domain, "UPSTREAM", "SERVER_ERROR", "-", "-");
        }

        int ancount = ((data[6] & 0xFF) << 8) | (data[7] & 0xFF);
        if (ancount == 0) {
            return new DnsResponse(domain, "UPSTREAM", "NOT_FOUND", "-", "-");
        }

      
        int idx = 12;
        while (idx < length && data[idx] != 0) {
            int len = data[idx] & 0xFF;
            if ((len & 0xC0) == 0xC0) { 
                idx += 2;
                break;
            }
            idx += len + 1;
            if (idx < length && data[idx] == 0) {
                idx++;
                break;
            }
        }
        idx += 4;

       
        for (int i = 0; i < ancount; i++) {
            if (idx >= length) break;

            if ((data[idx] & 0xC0) == 0xC0) {
                idx += 2;
            } else {
                while (idx < length && data[idx] != 0) {
                    idx += (data[idx] & 0xFF) + 1;
                }
                idx++;
            }

            if (idx + 10 > length) break;
            int type = ((data[idx] & 0xFF) << 8) | (data[idx + 1] & 0xFF);
            idx += 4; // Type + Class
            int ttl = ((data[idx] & 0xFF) << 24) | ((data[idx + 1] & 0xFF) << 16) | ((data[idx + 2] & 0xFF) << 8) | (data[idx + 3] & 0xFF);
            idx += 4;
            int rdlength = ((data[idx] & 0xFF) << 8) | (data[idx + 1] & 0xFF);
            idx += 2;

            if (type == 1 && rdlength == 4 && idx + 4 <= length) { // IPv4 Record
                String ip = (data[idx] & 0xFF) + "." + (data[idx + 1] & 0xFF) + "." + (data[idx + 2] & 0xFF) + "." + (data[idx + 3] & 0xFF);
                return new DnsResponse(domain, "UPSTREAM", "OK", ip, String.valueOf(ttl));
            }
            idx += rdlength;
        }

        return new DnsResponse(domain, "UPSTREAM", "NOT_FOUND", "-", "-");
    }

    private static void loadListFile(String filePath, Set<String> targetSet) {
        File file = new File(filePath);
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim().toLowerCase();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    targetSet.add(line);
                }
            }
        } catch (IOException ignored) {}
    }

    private static boolean isValidDomain(String d) {
        if (d.contains(" ")) return false;
        if (d.startsWith(".") || d.endsWith("..")) return false;
        String[] labels = d.split("\\.");
        if (labels.length < 2) return false;
        for (String label : labels) {
            if (label.isEmpty()) return false;
            for (char c : label.toCharArray()) {
                if (!Character.isLetterOrDigit(c) && c != '-') return false;
            }
            if (label.startsWith("-") || label.endsWith("-")) return false;
        }
        return true;
    }

    
    private static void printResult(String domain, String source, String status, String ip, String ttl) {
        System.out.println("Domain: " + domain);
        System.out.println("Source: " + source);
        System.out.println("Status: " + status);
        System.out.println("IP:     " + ip);
        System.out.println("TTL:    " + ttl);
        System.out.println();
    }
}
