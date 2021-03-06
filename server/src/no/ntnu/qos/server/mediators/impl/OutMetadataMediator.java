package no.ntnu.qos.server.mediators.impl;

import java.io.FileNotFoundException;
import java.io.IOException;

import no.ntnu.qos.server.mediators.AbstractQosMediator;
import no.ntnu.qos.server.mediators.MediatorConstants;
import no.ntnu.qos.server.mediators.QosLogType;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseLog;

/**
 * This mediator adds priority metadata to Message Context based on client role and service.
 * @author Ola Martin
 * @author Jørgen
 *
 */
public class OutMetadataMediator extends AbstractQosMediator {


	private final static PersistentPriorityData ppd = new PersistentPriorityData();
	private String ppdFilename;
	@Override
	public boolean mediateImpl(MessageContext synCtx, SynapseLog synLog) {

		//Check if data is available, if not, try reading it, if it fails return false
		if(!ppd.isDataAvailable()){
			if(ppdFilename!=null){
				ppd.setFilename(ppdFilename);
				this.logMessage(synLog, "Set Filename " +
						"in Persistent Data Store, filename=" + ppd.getFilename(), 
						QosLogType.INFO);
				try {
					ppd.readData();
				} catch (FileNotFoundException e) {
					//This means that the supplied file could not be found.
					this.logMessage(synLog, 
							e.getLocalizedMessage(), QosLogType.WARN);
					return false;
				} catch (IOException e) {
					//This means the file could not be closed.
					this.logMessage(synLog, 
							e.getLocalizedMessage(), QosLogType.WARN);
				}
				this.logMessage(synLog, "Successfully " +
						"read file into persistent storage", QosLogType.INFO);
			}        	
		}

		//This is the work this mediator does.
		final String clientRole = (String)synCtx.getProperty(MediatorConstants.QOS_CLIENT_ROLE);
		final String service = (String)synCtx.getProperty(MediatorConstants.QOS_SERVICE);
		int pri = ppd.getPriority(clientRole, service);
		int dif = ppd.getDiffserv(clientRole, service);
		if(pri<0 || dif <0){
			this.logMessage(synLog, "Client '"+clientRole+"' or service '"+service+
					"' not configured in '"+ppdFilename+"'", QosLogType.WARN);
			pri=0;
			dif=0;
		}
		synCtx.setProperty(MediatorConstants.QOS_PRIORITY, pri);
		synCtx.setProperty(MediatorConstants.QOS_DIFFSERV, dif);
		synCtx.setProperty(MediatorConstants.QOS_TIME_ADDED, System.currentTimeMillis());

		this.logMessage(synLog, "Successfully " +
				"added metadata to message context. " +
				"Added priority="+pri+", diffserv="+dif, QosLogType.INFO);

		return true;
	}

	public void setPpdFilename(String ppdFilename) {
		this.ppdFilename = ppdFilename;
	}

	public String getPpdFilename() {
		return ppdFilename;
	}

	@Override
	protected String getName() {
		return "OutMetadataMediator";
	}

}
