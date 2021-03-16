package io.tomahawkd.blockchain.application.user;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public enum OrgConfigurationConstants {

    INSTANCE;

    private static OrgConfigurationConstants instance;
    public static final Map<String, String> ORG_PEER_URL_MAP;
    private static final String ROOT_DIR =
            Paths.get(System.getProperty("user.home"), "Desktop", "fabricdata").toAbsolutePath().toString();

    static {
        Map<String, String> urlMap = new HashMap<>();
        urlMap.put("Platform", "https://localhost:7054");
        urlMap.put("Seller", "https://localhost:8054");
        urlMap.put("Supplier", "https://localhost:9054");
        ORG_PEER_URL_MAP = Collections.unmodifiableMap(urlMap);
    }

    private String organizationName;
    private String organizationMSP;
    private Path connectionConfig;
    private String organizationUrl ;
    private String caCertPath;

    private final Path walletPath = Paths.get("wallet");

    public void loadConfig(String orgName) {
        this.organizationName = orgName;
        String lowOrgName = organizationName.toLowerCase(Locale.ROOT);
        this.organizationMSP = organizationName + "MSP";
        this.organizationUrl = ORG_PEER_URL_MAP.get(organizationName);
        this.connectionConfig = Paths.get(ROOT_DIR, lowOrgName, String.format("connection-%s.yaml", lowOrgName));
        this.caCertPath = String.format("%s/%s/ca/ca.%s.com-cert.pem", ROOT_DIR, lowOrgName, lowOrgName);
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public String getOrganizationMSP() {
        return organizationMSP;
    }

    public Path getConnectionConfig() {
        return connectionConfig;
    }

    public String getOrganizationUrl() {
        return organizationUrl;
    }

    public String getCaCertPath() {
        return caCertPath;
    }

    public Path getWalletPath() {
        return walletPath;
    }
}
