package io.tomahawkd.blockchain.application;

import io.tomahawkd.blockchain.application.server.SearchServer;
import io.tomahawkd.blockchain.application.ui.LoginPanel;
import io.tomahawkd.blockchain.application.user.ConnectionManager;
import io.tomahawkd.blockchain.application.utils.ThreadManager;

public class MainApplication {

    static {
        System.setProperty("org.hyperledger.fabric.sdk.service_discovery.as_localhost", "true");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ConnectionManager.INSTANCE.close();
            if (SearchServer.getInstance().isAlive()) {
                SearchServer.getInstance().stop();
            }
            ThreadManager.INSTANCE.close();
        }));
    }

    public static void main(String[] args) {
        LoginPanel dialog = new LoginPanel();
        dialog.pack();
        dialog.setVisible(true);
    }
}
