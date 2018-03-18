package com.batsw.socket.library.service;

import com.batsw.socket.library.service.payload.IEntity;
import com.batsw.socket.library.service.util.ClientStatus;

public interface IClient extends ICallback {
	public void start();

	public void stop();

	public boolean send(IEntity entity);

	public String getDestinationHostName();

	public String getUniqueId();

	public boolean isIncommingConnection();

	public boolean enrichIncommingConnection(String destinationHostname, String uniqueIdentifierKey);

	public boolean isConnected();

	public ClientStatus getClientStatus();
}
