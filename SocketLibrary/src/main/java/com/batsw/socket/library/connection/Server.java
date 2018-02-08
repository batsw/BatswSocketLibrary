package com.batsw.socket.library.connection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.batsw.socket.library.service.IServer;
import com.batsw.socket.library.service.payload.IEntity;

public class Server implements IServer {

	private static final Logger LOGGER = Logger.getLogger(Server.class);

	private SocketServerThread mSocketServerThread = null;
	private ExecutorService mExecutorService = null;

	private ServerSocket mProviderSocket = null;
	private Socket mIncommmingConnection;

	private int mInternalProxyPort;
	private boolean isStarted = false;

	protected Server(int internalProxyPort) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("constructor - ENTER internalProxyPort=" + internalProxyPort);
		}

		mInternalProxyPort = internalProxyPort;

		mSocketServerThread = new SocketServerThread();

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("constructor - ENTER");
		}
	}

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
		// TODO Auto-generated method stub
		return false;
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

	private class SocketServerThread implements Runnable {
		@Override
		public void run() {

			while (mProviderSocket.isBound() && isStarted) {
				// always listening for new connections. When a new on comes start a new message
				// receiving thread
				try {
					mIncommmingConnection = mProviderSocket.accept();

					LOGGER.info("Connection received from " + mIncommmingConnection.getInetAddress().getHostName());

					// TODO: trigger incomming connection to the manager

				} catch (IOException ioException) {
					LOGGER.error("IOException: " + ioException.getMessage(), ioException);
				}
			}

		}
	}
}
