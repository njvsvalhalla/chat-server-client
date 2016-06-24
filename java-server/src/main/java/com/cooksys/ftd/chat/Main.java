package com.cooksys.ftd.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.ftd.chat.server.Server;

public class Main {

	static Logger log = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		Server server = new Server(669);
		Thread serverThread = new Thread(server);
		serverThread.start();

		try {
			serverThread.join();
			System.exit(0);
		} catch (InterruptedException e) {
			log.error("Server thread interrupted :(", e);
			System.exit(-1);
		}
	}

}
