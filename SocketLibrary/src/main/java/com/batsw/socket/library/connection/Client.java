package com.batsw.socket.library.connection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.batsw.socket.library.service.IClient;
import com.batsw.socket.library.service.payload.IEntity;
import com.batsw.socket.library.service.util.ClientStatus;

import socks.Socks5Proxy;
import socks.SocksSocket;

public class Client implements IClient {

	private static final Logger LOGGER = Logger.getLogger(Client.class);

	private String mUniqueId;

	private String mProxyHostName;
	private String mDestinationHostName;
	private int mInternalProxyPort;
	private int mExternalProxyPort;

	private Socket mSocketConnection;

	private OutputStream mOutputStream;
	private DataOutputStream mDataOutputStream;
	private DataInputStream mDataInputStream;

	boolean mIsConnected = false;

	private ClientReceiverThread mClientReceiverThread = null;
	private ExecutorService mExecutorService = null;

	private ClientStatus mClientStatus = ClientStatus.OFFLINE;

	/**
	 * This is used by {@link IConnectionManager} class to create a Client to create
	 * a CLient at the user's request
	 * 
	 * @param proxyHostname
	 * @param internalProxyPort
	 * @param destinationHostName
	 * @param externalProxyPort
	 * @param uniqueId
	 */
	protected Client(String proxyHostname, int internalProxyPort, String destinationHostName, int externalProxyPort,
			String uniqueId) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("constructor - ENTER");
		}

		mUniqueId = uniqueId;

		mProxyHostName = proxyHostname;
		mDestinationHostName = destinationHostName;
		mInternalProxyPort = internalProxyPort;
		mExternalProxyPort = externalProxyPort;

		mExecutorService = Executors.newSingleThreadExecutor();

		// use the static method of the bus to register to the event bus listener
		// Manager as an event generator

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("constructor - LEAVE");
		}
	}

	/**
	 * This is used by {@link IConnectionManager} class to create a Client object
	 * when an incoming connection comes through {@link Server}
	 * 
	 * @param socketConnection
	 * @param uniqueId
	 */
	protected Client(Socket socketConnection, String uniqueId) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("constructor - ENTER");
		}

		mUniqueId = uniqueId;

		try {

			mSocketConnection = socketConnection;

			mOutputStream = mSocketConnection.getOutputStream();
			mDataOutputStream = new DataOutputStream(mOutputStream);

			mDataInputStream = new DataInputStream(mSocketConnection.getInputStream());

		} catch (IOException ioEception) {
			LOGGER.error(
					"Error when when creating client socket bounded resources: " + ioEception.getStackTrace().toString()
							+ " Enrichment: " + "mSocketConnection: " + mSocketConnection.toString()
							+ " mOutputStream: " + mOutputStream.toString() + " mDataOutputStream: "
							+ mDataOutputStream.toString() + " mDataInputStream: " + mDataInputStream.toString(),
					ioEception);

		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("constructor - LEAVE");
		}
	}

	@Override
	public void start() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("start - ENTER");
		}

		if (!mIsConnected) {
			mIsConnected = establishConnectionToHostname();
		}

		if (mIsConnected) {
			mExecutorService.execute(mClientReceiverThread);
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("start - LEAVE");
		}
	}

	@Override
	public void stop() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("stop - ENTER");
		}

		if (mExecutorService != null) {
			closeConnection();

			mExecutorService.shutdownNow();
			mExecutorService = null;
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("stop - LEAVE");
		}
	}

	@Override
	public boolean callback(IEntity entity) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("callback - ENTER entity=" + entity);
		}
		boolean retVal = false;

		LOGGER.info("NOT IMPLEMENTED");

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("callback - LEAVE retVal=" + retVal);
		}
		return retVal;
	}

	@Override
	public boolean send(IEntity entity) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("send - ENTER entity=" + entity);
		}
		boolean retVal = false;

		if (mIsConnected) {

			try {

				// TODO: Encoder purpose to result byte[] from entity
				// byte[] byteArray = Encoder.encode(entity);
				// mDataOutputStream.write(byteArray);

				mDataOutputStream.flush();
				retVal = true;

			} catch (IOException ioException) {
				LOGGER.error("IOException: " + ioException.getMessage(), ioException);
			}

		} else {
			retVal = false;
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("send - LEAVE retVal=" + retVal);
		}
		return retVal;
	}

	private boolean establishConnectionToHostname() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("establishConnectionToHostname - ENTER");
		}
		boolean retVal = false;
		Socks5Proxy socks5Proxy = null;
		try {
			socks5Proxy = new Socks5Proxy(mProxyHostName, mInternalProxyPort);
			socks5Proxy.resolveAddrLocally(false);
			mSocketConnection = new SocksSocket(socks5Proxy, mDestinationHostName, mExternalProxyPort);
			LOGGER.info("successfully connected to hostname: " + mDestinationHostName);

			mOutputStream = mSocketConnection.getOutputStream();
			mDataOutputStream = new DataOutputStream(mOutputStream);

			mDataInputStream = new DataInputStream(mSocketConnection.getInputStream());

			mClientStatus = ClientStatus.ONLINE;

		} catch (IOException ioEception) {
			LOGGER.error("Error when when creating client socket bounded resources: "
					+ ioEception.getStackTrace().toString() + " Enrichment: " + "socks5Proxy: " + socks5Proxy.toString()
					+ "mSocketConnection: " + mSocketConnection.toString() + " mOutputStream: "
					+ mOutputStream.toString() + " mDataOutputStream: " + mDataOutputStream.toString()
					+ " mDataInputStream: " + mDataInputStream.toString(), ioEception);

			retVal = false;
		}

		if ((mDataInputStream != null) && (mDataOutputStream != null)) {
			retVal = true;
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("establishConnectionToHostname -> LEAVE retVal=" + retVal);
		}
		return retVal;
	}

	private void closeConnection() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("closeCommunication -> ENTER");
		}

		mIsConnected = false;
		mClientStatus = ClientStatus.OFFLINE;

		try {
			if (mDataInputStream != null)
				mDataInputStream.close();
			if (mDataOutputStream != null)
				mDataOutputStream.close();
			if (mOutputStream != null)
				mOutputStream.close();
			if (mSocketConnection != null)
				mSocketConnection.close();

		} catch (IOException e) {
			LOGGER.error("error when closing the communication" + e.getMessage(), e);
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("closeCommunication -> ENTER");
		}
	}

	@Override
	public String getUniqueId() {
		return mUniqueId;
	}

	@Override
	public boolean isIncommingConnection() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean enrichIncommingConnection(String destinationHostname, String uniqueIdentifierKey) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("enrichIncommingConnection -> destinationHostname" + destinationHostname
					+ ",uniqueIdentifierKey=" + uniqueIdentifierKey);
		}
		boolean retVal = false;

		if (uniqueIdentifierKey.equals(mUniqueId)) {
			mDestinationHostName = destinationHostname;

			LOGGER.info(
					"enrichIncommingConnection -> update the incomming connection with details: destinationHostname="
							+ destinationHostname);
		}

		// TODO: update client status ???

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("enrichIncommingConnection -> LEAVE retVal=" + retVal);
		}
		return retVal;
	}

	@Override
	public boolean isConnected() {
		return mIsConnected;
	}

	@Override
	public String getDestinationHostName() {
		return mDestinationHostName;
	}

	@Override
	public ClientStatus getClientStatus() {
		return mClientStatus;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mDestinationHostName == null) ? 0 : mDestinationHostName.hashCode());
		result = prime * result + ((mUniqueId == null) ? 0 : mUniqueId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Client other = (Client) obj;
		if (mDestinationHostName == null) {
			if (other.mDestinationHostName != null)
				return false;
		} else if (!mDestinationHostName.equals(other.mDestinationHostName))
			return false;
		if (mUniqueId == null) {
			if (other.mUniqueId != null)
				return false;
		} else if (!mUniqueId.equals(other.mUniqueId))
			return false;
		return true;
	}

	private class ClientReceiverThread implements Runnable {

		@Override
		public void run() {
			String incomingMessage = "";
			while (mIsConnected) {
				try {
					incomingMessage = mDataInputStream.readUTF();
					LOGGER.info("Message Receved___" + incomingMessage);

					// TODO: trigger message received event
					// mMessageReceivedListenerManager.messageReceived(incomingMessage, mSessionId);
				} catch (IOException e) {
					LOGGER.error("IOException: " + e.getMessage(), e);
					mIsConnected = false;
					try {
						mSocketConnection.close();
						break;
					} catch (IOException e1) {
						LOGGER.error("error when closing the connection" + e1.getMessage(), e1);
					}
				}
			}
		}
	}
}
