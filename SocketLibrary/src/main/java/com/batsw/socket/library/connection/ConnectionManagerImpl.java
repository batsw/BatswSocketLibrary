package com.batsw.socket.library.connection;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.batsw.socket.library.service.IClient;
import com.batsw.socket.library.service.IServer;
import com.batsw.socket.library.service.payload.IEntity;
import com.batsw.socket.library.service.util.ClientStatus;

/**
 * @author tudor
 *
 */
public class ConnectionManagerImpl implements IConnectionManager {

	private static final Logger LOGGER = Logger.getLogger(ConnectionManagerImpl.class);

	private static IConnectionManager mInstace = null;

	private static Map<String, IClient> mClientsMap = new HashMap<>();

	private static IServer mServer;
	private boolean mIsServerStarted;

	@Override
	public void createClientConnection(String proxyHostname, int internalProxyPort, String destinationHostName,
			int externalProxyPort) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("createClientConnection - ENTER");
		}

		String uniqueId = generateUniqueIdForClient();

		IClient client = new Client(proxyHostname, internalProxyPort, destinationHostName, externalProxyPort, uniqueId);
		mClientsMap.put(uniqueId, client);

		LOGGER.info("Created client: " + destinationHostName);

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("createClientConnection - LEAVE");
		}
	}

	@Override
	public boolean sendToClient(String hostname, IEntity payload) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("sendToClient - ENTER hostname=" + hostname + " payload=" + payload);
		}
		boolean retVal = false;

		for (String uniqueId : mClientsMap.keySet()) {

			IClient client = mClientsMap.get(uniqueId);
			if (hostname.equals(client.getDestinationHostName())) {

				client.send(payload);

				retVal = true;

				LOGGER.info("Payload sent to: " + hostname);
				break;
			}
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("sendToClient - LEAVE retVal=" + retVal);
		}
		return retVal;
	}

	@Override
	public void triggerIncommingConnection(Socket socketConnection) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("triggerIncommingConnection - ENTER socketConnection=" + socketConnection);
		}

		if (socketConnection != null && socketConnection.isConnected()) {
			String generatedClientId = generateUniqueIdForClient();
			Client client = new Client(socketConnection, generatedClientId);

			mClientsMap.put(generatedClientId, client);
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("triggerIncommingConnection - LEAVE");
		}
	}

	@Override
	public void triggerPayloadReceivedFromClient(String uniqueClientId, IEntity payload) {
		// TODO Auto-generated method stub

	}

	@Override
	public void triggerCLientStatusChanged(String uniqueClientId) {
		// TODO Auto-generated method stub

	}

	@Override
	public ClientStatus getClientStatus(String hostname) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void createServer(int internalPort) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("createServer - ENTER internalPort=" + internalPort);
		}

		if (mServer == null) {
			mServer = new Server(internalPort);

			LOGGER.trace("createServer - server created: " + mServer);
		} else {
			LOGGER.trace("createServer - server already exists: " + mServer);
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("createServer - LEAVE");
		}
	}

	@Override
	public boolean serverState() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("serverState - ENTER");
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("serverState - LEAVE mIsServerStarted" + mIsServerStarted);
		}
		return mIsServerStarted;
	}

	@Override
	public void startServer() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("startServer - ENTER");
		}

		if (mServer != null) {
			mServer.start();

			mIsServerStarted = true;
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("startServer - LEAVE");
		}
	}

	@Override
	public void stopServer() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("stopServer - ENTER");
		}

		if (mServer != null) {
			mServer.stop();

			mIsServerStarted = false;
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("stopServer - LEAVE");
		}
	}

	public static IConnectionManager getInstance() {
		if (mInstace == null) {
			mInstace = new ConnectionManagerImpl();

			LOGGER.info("Created IConnectionManager: " + mInstace);
		}
		return mInstace;
	}

	private String generateUniqueIdForClient() {
		UUID uuid = UUID.randomUUID();
		return uuid.toString();
	}

	private ConnectionManagerImpl() {
	}

	@Override
	public String toString() {
		return ConnectionManagerImpl.class.getName();
	}
}
