package com.batsw.socket.library.connection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.batsw.socket.library.service.IClient;
import com.batsw.socket.library.service.payload.IEntity;

import socks.Socks5Proxy;
import socks.SocksSocket;
import sun.rmi.runtime.Log;

//ONLY the connectionManager can create a client connection...
public class Client implements IClient {

	private static final Logger LOGGER = Logger.getLogger(Client.class);

	private String mProxyHostName;
	private String mDestinationHostName;
	private int mInternalProxyPort;
	private int mExternalProxyPort;

	// public MessageReceivedListenerManager mMessageReceivedListenerManager;

	private Socket mSocketConnection;

	private OutputStream mOutputStream;
	private DataOutputStream mDataOutputStream;
	private DataInputStream mDataInputStream;

	boolean mIsConnected = false;

	private ClientReceiverThread mClientReceiverThread = null;
	private ExecutorService mExecutorService = null;

	protected Client(String proxyHostname, int internalProxyPort, String destinationHostName, int externalProxyPort) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("constructor - ENTER");
		}

		mProxyHostName = proxyHostname;
		mDestinationHostName = destinationHostName;
		mInternalProxyPort = internalProxyPort;
		mExternalProxyPort = externalProxyPort;

		mExecutorService = Executors.newSingleThreadExecutor();

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("constructor - LEAVE");
		}
	}

	public void start() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("start - ENTER");
		}

		if (!mIsConnected) {
			mIsConnected = establishConnectionToHostname();

			if (mIsConnected) {
				mExecutorService.execute(mClientReceiverThread);
			}
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("start - LEAVE");
		}
	}

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

		// TODO: implement

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
		try {
			Socks5Proxy socks5Proxy = new Socks5Proxy(mProxyHostName, mInternalProxyPort);
			socks5Proxy.resolveAddrLocally(false);
			mSocketConnection = new SocksSocket(socks5Proxy, mDestinationHostName, mExternalProxyPort);
			LOGGER.info("successfully connected to hostname: " + mDestinationHostName);

			mOutputStream = mSocketConnection.getOutputStream();
			mDataOutputStream = new DataOutputStream(mOutputStream);

			mDataInputStream = new DataInputStream(mSocketConnection.getInputStream());

		} catch (UnknownHostException unknownHost) {
			LOGGER.error("UnknownHostException: " + unknownHost.getStackTrace().toString(), unknownHost);

			retVal = false;

		} catch (IOException ioException) {
			LOGGER.error("IOException: " + ioException.getMessage(), ioException);

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

	public boolean isConnected() {
		return mIsConnected;
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
