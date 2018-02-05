package com.batsw.socket.library.service;

import com.batsw.socket.library.service.payload.IEntity;

public interface ICallback {
	public boolean callback(IEntity entity);
}
