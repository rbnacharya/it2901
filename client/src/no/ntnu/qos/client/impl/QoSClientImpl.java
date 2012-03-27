package no.ntnu.qos.client.impl;

import no.ntnu.qos.client.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Magnus Kirø
 *
 * The Implementation of QoSClient interface.
 */
public class QoSClientImpl implements QoSClient {

    private Sequencer sequencer;
    List<DataListener> dataListenerList = new ArrayList<DataListener>();

    public QoSClientImpl(String userName, String role, String password, ExceptionHandler exceptionHandler){
        sequencer = new SequencerImpl(this, userName, role, password, exceptionHandler);
    }

    @Override
    public void setCredentials(String username, String role, String password) {
        this.sequencer.setCredentials(username, role, password);
    }

    @Override
    public ReceiveObject sendData(String data, URI destination) {
       return sequencer.sendData(data, destination);
    }

    @Override
    public void addListener(DataListener listener) {
        this.dataListenerList.add(listener);
    }

    @Override
    public void removeListener(DataListener listener) {
        this.dataListenerList.remove(listener);
    }

    public List<DataListener> getDataListenerList() {
        return dataListenerList;
    }

    @Override
    public Sequencer getSequencer() {
        return this.sequencer;
    }

	@Override
	public void Receive() {
		// TODO Auto-generated method stub
		
	}
}