package com.batsw.socket.library.connection;

import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.batsw.socket.library.service.IClient;
import com.batsw.socket.library.service.IServer;
import com.batsw.socket.library.service.payload.IEntity;

/**
 * @author tudor
 *
 */
public class Server implements IServer {

	private static final Logger LOGGER = Logger.getLogger(Server.class);

	private SocketServerThread mSocketServerThread = null;
	private ExecutorService mExecutorService = null;

	private ServerSocket mProviderSocket = null;
	private Socket mIncommmingConnection;

	private int mInternalProxyPort;
	private boolean isStarted = false;

	protected Server(int internalPort) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("constructor - ENTER internalProxyPort=" + internalPort);
		}

		mInternalProxyPort = internalPort;
		mSocketServerThread = new SocketServerThread();

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("constructor - ENTER");
		}
	}

	@Override
	public void start() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("start - ENTER");
		}
		isStarted = true;
		mExecutorService.execute(mSocketServerThread);

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("start - LEAVE");
		}
	}

	@Override
	public void stop() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("start - ENTER");
		}

		isStarted = false;

		try {
			mProviderSocket.close();
		} catch (IOException e) {
			LOGGER.error("IOException: " + e.getMessage(), e);
		}

		if (mExecutorService != null) {

			mExecutorService.shutdownNow();
			mExecutorService = null;
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("start - ENTER");
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
	public boolean initialize() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("initialize - ENTER");
		}

		boolean retVal = false;

		try {
			mProviderSocket = new ServerSocket(mInternalProxyPort, 10);
		} catch (IOException e) {
			LOGGER.error("IOException: " + e.getMessage(), e);

			retVal = false;
		}

		mExecutorService = Executors.newSingleThreadExecutor();
		retVal = true;

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("initialize - LEAVE retVal=" + retVal);
		}
		return retVal;
	}

	/**
	 * Internal class that is always listening for new connections. When a new on
	 * comes it is triggering {@link IConnectionManager} to create a new
	 * {@link IClient}
	 */
	private class SocketServerThread implements Runnable {
		@Override
		public void run() {

			while (mProviderSocket.isBound() && isStarted) {
				try {
					mIncommmingConnection = mProviderSocket.accept();

					LOGGER.info("Connection received from " + mIncommmingConnection.getInetAddress().getHostName());

					ConnectionManagerImpl.getInstance().triggerIncommingConnection(mIncommmingConnection);

				} catch (IOException ioException) {
					LOGGER.error("IOException: " + ioException.getMessage(), ioException);
				}
			}

		}
	}
}
