package no.ntnu.qos.server.mediators.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import no.ntnu.qos.server.mediators.AbstractQosMediator;
import no.ntnu.qos.server.mediators.MediatorConstants;
import no.ntnu.qos.server.mediators.QosContext;
import no.ntnu.qos.server.mediators.QosLogType;
import no.ntnu.qos.server.mediators.TRContext;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.core.axis2.Axis2MessageContext;
/**
 * This mediator finds out if there is enough bandwidth to send a message, 
 * if not, it will check if other sending messages has lower priority and should be stopped,
 * if not, it will wait until a message finishes sending. 
 * If the message is timed out this mediator will return false, otherwise true.
 * @author Ola Martin
 * @author Jørgen
 *
 */
public class ThrottleMediator extends AbstractQosMediator{

	private long minBandwidthPerMessage;
	private long timeout;
	private static final Map<String, TRContext> trCtxs = new HashMap<String, TRContext>();
	private static AtomicInteger threads = new AtomicInteger(0);

	@Override
	protected boolean mediateImpl(MessageContext synCtx, SynapseLog synLog) {
		this.logMessage(synLog, "NoThreads:"+threads.incrementAndGet(), QosLogType.INFO);


		String lastTR = (String) synCtx.getProperty(MediatorConstants.QOS_LAST_TR);
		long initialCapacity = (Long) synCtx.getProperty(MediatorConstants.QOS_BANDWIDTH)
				/minBandwidthPerMessage;
		QosContext qCtx;
		long timeStarted = System.currentTimeMillis();
		this.logMessage(synLog, this.getName() + " started at " + timeStarted, 
				QosLogType.INFO);
		try {
			qCtx = new DefaultQosContext(synCtx);
			TRContext trCtx = null;
			this.logMessage(synLog, "Fetching TRContext from map", QosLogType.INFO);
			synchronized (trCtxs) {
				trCtx = trCtxs.get(lastTR);
				if(trCtx==null){
					trCtx = new TRContextImpl(initialCapacity>1 ? initialCapacity:1);
					trCtxs.put(lastTR, trCtx);
				}else{
					trCtx.setAvailableBandwidth(initialCapacity>1 ? initialCapacity:1);
				}
			}
			this.logMessage(synLog, "Successfully got TRContext from map, " +
					"trying to send message", QosLogType.INFO);
			while((System.currentTimeMillis()-timeStarted) < timeout && 
					(!qCtx.useTTL() || qCtx.getTimeToLive()>0)){
				if(send(qCtx, trCtx, synLog)){
					threads.decrementAndGet();
					return true;
				}else{
					this.logMessage(synLog, "Could not send message right away, " +
							"trying to preempt messages, queue size: "+((TRContextImpl)trCtx).size()+
							", available bandwidth: "+trCtx.availableBandwidth()+
							", Message Size: "+((Axis2MessageContext)synCtx).getAxis2MessageContext().getInboundContentLength()+
							", Priority: "+qCtx.getPriority()
							, QosLogType.INFO);
					for(QosContext qCon:((TRContextImpl)trCtx).getQueue()){
						this.logMessage(synLog, "Message in queue, priority: "+qCon.getPriority()+",from: "+
								qCon.getMessageContext().getProperty(MediatorConstants.QOS_FROM_ADDR), QosLogType.INFO);
					}
					List<QosContext> preemptees = trCtx.preemptContexts(qCtx);
					for(QosContext preempted:preemptees){
						this.logMessage(synLog, "Preempted Message:" +
								preempted.getMessageContext().getMessageID()+", from: "+
								preempted.getMessageContext().getProperty(MediatorConstants.QOS_FROM_ADDR)
								, QosLogType.INFO);
					}
					this.logMessage(synLog, "Trying to send message again", QosLogType.INFO);
					if(send(qCtx, trCtx, synLog)){
						threads.decrementAndGet();
						return true;
					}
				}
				this.logMessage(synLog, "Message has to wait for "+trCtx.nextEvent()+
						"ms before trying again.", QosLogType.INFO);
				try {
					Thread.sleep(trCtx.nextEvent());
				} catch (InterruptedException e) {
					this.logMessage(synLog, "Could not sleep", QosLogType.WARN);
				}
			}
			if(System.currentTimeMillis()-timeStarted >= timeout){
				this.logMessage(synLog, "Message timed out: mediator timeout exceeded", QosLogType.INFO);
			}else if(qCtx.useTTL() && qCtx.getTimeToLive()<=0){
				this.logMessage(synLog, "Message timed out: message time to live exceeded", QosLogType.INFO);
			}
		} catch (IOException e) {
			this.logMessage(synLog, "Error reading message data: could not get it's size", QosLogType.WARN);
		}
		threads.decrementAndGet();
		return false;
	}

	private boolean send(QosContext qCtx,TRContext trCtx, SynapseLog synLog){
		trCtx.clearFinished();
		if(trCtx.availableBandwidth()>0){
			trCtx.add(qCtx);
			this.logMessage(synLog, "Successfully " +
					"added message context to TRContext.", QosLogType.INFO);
			return true;
		}
		return false;
	}

	@Override
	protected String getName() {
		return "Throttle Mediator";
	}


	public void setMinBandwidthPerMessage(long minBandwidthPerMessage) {
		this.minBandwidthPerMessage = minBandwidthPerMessage;
	}

	public long getMinBandwidthPerMessage() {
		return minBandwidthPerMessage;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

}
