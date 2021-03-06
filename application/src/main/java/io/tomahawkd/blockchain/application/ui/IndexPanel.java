package io.tomahawkd.blockchain.application.ui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import io.tomahawkd.blockchain.application.server.SearchServer;
import io.tomahawkd.blockchain.application.user.OrgConfigurationConstants;
import io.tomahawkd.blockchain.application.user.TransactionHelper;
import io.tomahawkd.blockchain.application.user.UserInfo;
import io.tomahawkd.blockchain.application.user.UserManager;
import io.tomahawkd.blockchain.application.utils.ThreadManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class IndexPanel extends JPanel {
    private JPanel mainPanel;
    private JLabel usernameField;
    private JLabel orgField;
    private JLabel affiliationField;
    private JLabel walletField;
    private JButton loadDemoButton;
    private JLabel configField;
    private JLabel serverConnField;
    private JLabel statusField;
    private JButton serverButton;
    private JButton exitButton;

    public IndexPanel() {
        add(mainPanel);
        UserInfo info = UserManager.INSTANCE.getInfo();
        usernameField.setText("Username: " + info.getUserId());
        orgField.setText("Organization: " + info.getOrganization());
        affiliationField.setText("Affiliation: " + info.getAffiliation());
        walletField.setText("Wallet Path: " + info.getWallet().toAbsolutePath().toString());
        configField.setText("Connection Config: " + OrgConfigurationConstants.INSTANCE.getConnectionConfig().toString());
        serverConnField.setText("Server Address: " + SearchServer.MAIN_URL);
        statusField.setText("Server Status: " + (SearchServer.getInstance().isAlive() ? "Active" : "Inactive"));
        loadDemoButton.addActionListener(e ->
                ThreadManager.INSTANCE.addNewTask(() -> {
                    try {
                        TransactionHelper.INSTANCE.LoadDemo();
                    } catch (Exception exception) {
                        exception.printStackTrace();
                        new AlertDialog(exception).setVisible(true);
                    }
                }));
        serverButton.addActionListener(e -> {
            if (SearchServer.getInstance().isAlive()) {
                SearchServer.getInstance().stop();
                statusField.setText("Server Status: " + (SearchServer.getInstance().isAlive() ? "Active" : "Inactive"));
            } else {
                ThreadManager.INSTANCE.addNewTask(() -> {
                    try {
                        SearchServer.getInstance().start();
                        statusField.setText("Server Status: " + (SearchServer.getInstance().isAlive() ? "Active" : "Inactive"));
                    } catch (Exception exception) {
                        exception.printStackTrace();
                        new AlertDialog(exception).setVisible(true);
                    }
                });
            }
        });
    }

    public void setExit(ActionListener l) {
        exitButton.addActionListener(l);
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(11, 2, new Insets(0, 0, 0, 0), -1, -1));
        final JLabel label1 = new JLabel();
        label1.setText("Welcome to trace system control panel!");
        mainPanel.add(label1, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("User Info:");
        mainPanel.add(label2, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        usernameField = new JLabel();
        usernameField.setText("Username: ");
        mainPanel.add(usernameField, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        orgField = new JLabel();
        orgField.setText("Organization: ");
        mainPanel.add(orgField, new GridConstraints(3, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        affiliationField = new JLabel();
        affiliationField.setText("Affiliation: ");
        mainPanel.add(affiliationField, new GridConstraints(4, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        walletField = new JLabel();
        walletField.setText("Wallet Path: ");
        mainPanel.add(walletField, new GridConstraints(5, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        loadDemoButton = new JButton();
        loadDemoButton.setText("Load Demo");
        mainPanel.add(loadDemoButton, new GridConstraints(9, 1, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        configField = new JLabel();
        configField.setText("Connection Config: ");
        mainPanel.add(configField, new GridConstraints(6, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        serverConnField = new JLabel();
        serverConnField.setText("Server Address: ");
        mainPanel.add(serverConnField, new GridConstraints(7, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        statusField = new JLabel();
        statusField.setText("Server Status: ");
        mainPanel.add(statusField, new GridConstraints(8, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        serverButton = new JButton();
        serverButton.setText("Toggle Server");
        mainPanel.add(serverButton, new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        exitButton = new JButton();
        exitButton.setText("Exit");
        mainPanel.add(exitButton, new GridConstraints(10, 1, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}
