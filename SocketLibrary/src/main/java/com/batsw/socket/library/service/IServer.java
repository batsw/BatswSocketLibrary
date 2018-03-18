package com.batsw.socket.library.service;

public interface IServer extends ICallback {
	public boolean initialize();

	public void start();

	public void stop();

}
