package com.cooksys.ftd.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.ftd.chat.server.Server;

public class Main {
	//Let's start our logger
	static Logger log = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		/*
		 * Our server will be started on port 669. You may need to change this depending on system + permissions
		 * Our server will start on a thread.
		 */
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
