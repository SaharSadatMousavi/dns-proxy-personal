import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DomainManager {
    private final List<String> adList = new ArrayList<>();
    private final List<String> sanctionList = new ArrayList<>();


    public void loadLists(String adListFile, String sanctionListFile) throws Exception {

        loadSingleList(adListFile, this.adList);
        loadSingleList(sanctionListFile, this.sanctionList);
    }


    private void loadSingleList(String filePath, List<String> targetList) throws Exception {
        File file = new File(filePath);


        if (!file.exists()) {
            throw new Exception("List file not found: " + filePath);
        }

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {

                String line = scanner.nextLine().trim();


                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }


                if (!isValidDomain(line)) {

                    System.out.println("Warning: bad line in " + filePath + ": " + line);
                    continue;
                }


                String normalizedEntry = normalize(line);
                targetList.add(normalizedEntry);
            }
        }
    }


    public boolean isValidDomain(String rawInput) {

        if (rawInput == null || rawInput.isEmpty()) {
            return false;
        }

        String regex = "^[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.?$";

        return rawInput.matches(regex);
    }

    public String normalize(String domain) {


        String normalized = domain.toLowerCase();
        if (normalized.endsWith(".")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        return normalized;
    }

    public DnsResponse checkLists(String normalizedDomain, Config config) {
        DnsResponse dnsResponse=null;

        for (String entry : adList) {
            if (normalizedDomain.equals(entry) || normalizedDomain.endsWith("." + entry)) {
                dnsResponse=new DnsResponse(normalizedDomain, "AD_BLOCK", "OK", config.getSinkholeIp(), config.getLocalTtl());
                return dnsResponse;
            }
        }


        for (String entry : sanctionList) {
            if (normalizedDomain.equals(entry) || normalizedDomain.endsWith("." + entry)) {
                dnsResponse= new DnsResponse(normalizedDomain, "SANCTION", "OK", config.getProxyIp(), config.getLocalTtl());
                return dnsResponse;
            }
        }

        return dnsResponse;
    }
}