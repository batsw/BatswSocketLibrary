package com.batsw.socket.library.service.util;

import com.batsw.socket.library.service.payload.IEntity;

public interface IEncoder {
	public IEntity toEntity(Object object);
}
