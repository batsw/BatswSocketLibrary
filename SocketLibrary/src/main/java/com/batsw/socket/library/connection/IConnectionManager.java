package com.batsw.socket.library.connection;

import java.net.Socket;

import com.batsw.socket.library.service.payload.IEntity;
import com.batsw.socket.library.service.util.ClientStatus;

public interface IConnectionManager {
	public void createServer(int internalPort);

	public void startServer();

	public void stopServer();

	public boolean serverState();

	public void triggerIncommingConnection(Socket socket);

	public void createClientConnection(String proxyHostname, int internalProxyPort, String destinationHostName,
			int externalProxyPort);

	public boolean sendToClient(String hostname, IEntity payload);

	public void triggerPayloadReceivedFromClient(String uniqueClientId, IEntity payload);

	public void triggerCLientStatusChanged(String uniqueClientId);

	public ClientStatus getClientStatus(String hostname);
}
