package no.ntnu.qos.client.credentials.impl;

import no.ntnu.qos.client.DataObject;
import no.ntnu.qos.client.credentials.CredentialStorage;
import no.ntnu.qos.client.credentials.TokenManager;

public class TokenManagerImpl implements TokenManager {
	CredentialStorage credentialStorage;
	
	public TokenManagerImpl(String user, String role, String password) {
		credentialStorage = new CredentialStorageImpl(user, role, password);
	}

    public CredentialStorage getCredentialStorage() {
        return credentialStorage;
    }

    @Override
	public void getToken(DataObject dataObject) {
        // if(dataObject.getToken()) return;

        // token = samlCommunicator.getToken(params);
        // dataObject.setToken(token);
        // credentialStorage.storeToken(token);
	}

    @Override
    public void setCredentials(String username, String role, String password) {
        credentialStorage.setCredentials(username,role,password);
    }

}