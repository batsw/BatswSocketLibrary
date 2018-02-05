package com.batsw.socket.library.service;

import com.batsw.socket.library.service.payload.IEntity;

public interface IClient extends ICallback {
	public boolean send(IEntity entity);
}
