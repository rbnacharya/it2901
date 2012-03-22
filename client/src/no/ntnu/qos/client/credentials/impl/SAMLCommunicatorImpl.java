package no.ntnu.qos.client.credentials.impl;

import java.net.URI;

import no.ntnu.qos.client.credentials.SAMLCommunicator;
import no.ntnu.qos.client.credentials.Token;

public class SAMLCommunicatorImpl implements SAMLCommunicator {

	@Override
	public Token getToken(URI destination, String userName, String password,
			String role) {
        // TODO write the rest of the method. which probably includes the network communication with the identity server.

        // Todo -  this method has to set the diffserv and priority in the token before it is returned.
        // do you get the diffserv and priority values from the IdentityServer? (hopefully)

        // TODO: has to be changed to the correct variables
        // takes "token" - valid Long, destination URI
        return new TokenImpl("token", System.currentTimeMillis()+3600000, destination);
	}

}
