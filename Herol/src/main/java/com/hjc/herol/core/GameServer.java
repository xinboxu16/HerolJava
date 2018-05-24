package com.hjc.herol.core;

import com.hjc.herol.util.Helper;

public class GameServer extends Helper<GameServer> {
	public static volatile boolean shutdown = false;
	private volatile static GameServer server;
	
	private GameServer() {
	}
	
	public static GameServer getInstance() {
		if (null == server) {
			synchronized (GameServer.class) {
				if (null == server) {
					server = new GameServer();
				}
			}
		}
		return server;
	}
}
