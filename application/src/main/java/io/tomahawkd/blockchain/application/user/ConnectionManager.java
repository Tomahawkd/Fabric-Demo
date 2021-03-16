package io.tomahawkd.blockchain.application.user;

import org.hyperledger.fabric.gateway.*;

import java.nio.file.Path;

public enum ConnectionManager {

    INSTANCE;

    private Gateway gateway;
    private Contract contract;

    void init(UserInfo info) throws Exception {
        if (gateway != null) return;
        // Load a file system based wallet for managing identities.
        Wallet wallet = Wallets.newFileSystemWallet(info.getWallet());
        // load a CCP
        Path networkConfigPath = OrgConfigurationConstants.INSTANCE.getConnectionConfig();

        Gateway.Builder builder = Gateway.createBuilder();
        builder.identity(wallet, info.getUserId()).networkConfig(networkConfigPath).discovery(true);
        this.gateway = builder.connect();
        Network network = gateway.getNetwork("track");
        this.contract = network.getContract("basic");
    }

    public String submit(String function, String... args) throws Exception {
        return new String(contract.submitTransaction(function, args));
    }

    public String evaluate(String function, String... args) throws Exception {
        return new String(contract.evaluateTransaction(function, args));
    }

    public void close() {
        if (gateway != null) gateway.close();
    }
}
