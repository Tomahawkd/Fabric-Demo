/*
SPDX-License-Identifier: Apache-2.0
*/

package io.tomahawkd.blockchain.application.user;

import org.hyperledger.fabric.gateway.*;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric.sdk.security.CryptoSuiteFactory;
import org.hyperledger.fabric_ca.sdk.EnrollmentRequest;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;

import java.security.PrivateKey;
import java.util.Properties;
import java.util.Set;

public enum UserManager {

	INSTANCE;

	private UserInfo info;

	public void register(UserInfo info) throws Exception {

		this.info = info;

		// Create a CA client for interacting with the CA.
		Properties props = new Properties();
		props.put("pemFile", OrgConfigurationConstants.INSTANCE.getCaCertPath());
		props.put("allowAllHostNames", "true");
		HFCAClient caClient = HFCAClient.createNewInstance(OrgConfigurationConstants.INSTANCE.getOrganizationUrl(), props);
		CryptoSuite cryptoSuite = CryptoSuiteFactory.getDefault().getCryptoSuite();
		caClient.setCryptoSuite(cryptoSuite);

		// Create a wallet for managing identities
		Wallet wallet = Wallets.newFileSystemWallet(info.getWallet());

		// Check to see if we've already enrolled the user.
		if (wallet.get(info.getUserId()) != null) {
			System.out.println("An identity for the user \"" + info.getUserId() + "\" already exists in the wallet");
			ConnectionManager.INSTANCE.init(info);
			return;
		}

		if (wallet.get(AdminConstants.adminName) == null) {
			// Enroll the admin user, and import the new identity into the wallet.
			final EnrollmentRequest enrollmentRequestTLS = new EnrollmentRequest();
			enrollmentRequestTLS.addHost("localhost");
			enrollmentRequestTLS.setProfile("tls");
			Enrollment enrollment = caClient.enroll(AdminConstants.adminName, AdminConstants.adminPassword, enrollmentRequestTLS);
			Identity user = Identities.newX509Identity(OrgConfigurationConstants.INSTANCE.getOrganizationMSP(), enrollment);
			wallet.put(AdminConstants.adminName, user);
			System.out.println("Successfully enrolled admin and imported it into the wallet");
		}

		X509Identity adminIdentity = (X509Identity)wallet.get(AdminConstants.adminName);
		if (adminIdentity == null) {
			System.out.println("admin needs to be enrolled and added to the wallet first");
			throw new RuntimeException("admin needs to be enrolled first");
		}
		User admin = new User() {

			@Override
			public String getName() {
				return AdminConstants.adminName;
			}

			@Override
			public Set<String> getRoles() {
				return null;
			}

			@Override
			public String getAccount() {
				return null;
			}

			@Override
			public String getAffiliation() {
				return info.getAffiliation();
			}

			@Override
			public Enrollment getEnrollment() {
				return new Enrollment() {

					@Override
					public PrivateKey getKey() {
						return adminIdentity.getPrivateKey();
					}

					@Override
					public String getCert() {
						return Identities.toPemString(adminIdentity.getCertificate());
					}
				};
			}

			@Override
			public String getMspId() {
				return OrgConfigurationConstants.INSTANCE.getOrganizationMSP();
			}

		};

		// Register the user, enroll the user, and import the new identity into the wallet.
		RegistrationRequest registrationRequest = new RegistrationRequest(info.getUserId());
		registrationRequest.setAffiliation(info.getAffiliation());
		registrationRequest.setEnrollmentID(info.getUserId());
		String enrollmentSecret = caClient.register(registrationRequest, admin);
		Enrollment enrollment = caClient.enroll(info.getUserId(), enrollmentSecret);
		Identity user = Identities.newX509Identity(OrgConfigurationConstants.INSTANCE.getOrganizationMSP(), enrollment);
		wallet.put(info.getUserId(), user);
		System.out.println("Successfully enrolled user \"" + info.getUserId() + "\" and imported it into the wallet");

		ConnectionManager.INSTANCE.init(info);
	}

	public UserInfo getInfo() {
		return info;
	}
}
