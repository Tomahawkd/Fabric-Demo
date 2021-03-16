package io.tomahawkd.blockchain.application.user;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public enum OrgConfigurationConstants {

    INSTANCE;

    public static final Map<String, String> ORG_PEER_URL_MAP;
    private static final String DEFAULT_ROOT_DIR =
            Paths.get(System.getProperty("user.home"), "Desktop", "fabricdata").toAbsolutePath().toString();

    static {
        Map<String, String> urlMap = new HashMap<>();
        urlMap.put("Platform", "https://localhost:7054");
        urlMap.put("Seller", "https://localhost:8054");
        urlMap.put("Supplier", "https://localhost:9054");
        ORG_PEER_URL_MAP = Collections.unmodifiableMap(urlMap);
    }

    private String rootPath;
    private String organizationName;
    private String organizationMSP;
    private Path connectionConfig;
    private String organizationUrl;
    private String caCertPath;

    private final Path walletPath = Paths.get("wallet");

    public void loadConfig(String orgName) {
        if (rootPath == null) rootPath = DEFAULT_ROOT_DIR;
        this.organizationName = orgName;
        String lowOrgName = organizationName.toLowerCase(Locale.ROOT);
        this.organizationMSP = organizationName + "MSP";
        this.organizationUrl = ORG_PEER_URL_MAP.get(organizationName);
        this.connectionConfig = Paths.get(rootPath, lowOrgName, String.format("connection-%s.yaml", lowOrgName));
        this.caCertPath = String.format("%s/%s/ca/ca.%s.com-cert.pem", rootPath, lowOrgName, lowOrgName);
    }

    public boolean setRootPath(String path) {
        try {
            Path rp = Paths.get(path);
            this.rootPath = rp.toAbsolutePath().toString();
            if (!rp.toFile().exists()) {
                System.err.printf("The path [%s] is not exist, check again please.%n", path);
                return false;
            }

            // reload if needed
            if (this.organizationName != null) loadConfig(this.organizationName);
            return true;
        } catch (InvalidPathException e) {
            System.err.printf("The path [%s] cant be set to root path.%n", path);
            return false;
        }
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
