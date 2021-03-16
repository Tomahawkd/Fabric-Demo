package io.tomahawkd.blockchain.application.user;

import java.nio.file.Path;
import java.util.Locale;

public class UserInfo {

    private final String userId;
    private Path wallet;
    private final String affiliation;
    private final String organization;
    private final String password;

    public UserInfo(String userId, Path wallet, String affiliation, String organization, String password) {
        OrgConfigurationConstants.INSTANCE.loadConfig(organization);
        this.userId = userId;
        this.wallet = wallet;
        this.affiliation = affiliation;
        this.organization = organization;
        this.password = password;
    }

    public UserInfo(String userId, String affiliation, String organization, String password) {
        this(userId, null, affiliation, organization, password);
        this.wallet = OrgConfigurationConstants.INSTANCE.getWalletPath();
    }


    public UserInfo(String userId, String organization, String password) {
        this(userId, organization.toLowerCase(Locale.ROOT) + ".department1", organization, password);
    }

    public String getUserId() {
        return userId;
    }

    public Path getWallet() {
        return wallet;
    }

    public String getAffiliation() {
        return affiliation;
    }

    public String getOrganization() {
        return organization;
    }

    public String getPassword() {
        return password;
    }
}
