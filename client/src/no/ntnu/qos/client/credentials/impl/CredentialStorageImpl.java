package no.ntnu.qos.client.credentials.impl;

import java.net.URI;

import no.ntnu.qos.client.credentials.CredentialStorage;
import no.ntnu.qos.client.credentials.Token;

public class CredentialStorageImpl implements CredentialStorage{

	@Override
	public boolean hasToken(URI destination) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Token getToken(URI destination) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getCredentials() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void storeToken(Token token) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCredentials(String username, String role, String password) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void flushTokens() {
		// TODO Auto-generated method stub
		
	}

}
