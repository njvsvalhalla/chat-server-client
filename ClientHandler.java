package com.cooksys.ftd.chat.server;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientHandler implements Runnable, Closeable {

	Logger log = LoggerFactory.getLogger(ClientHandler.class);

	private Socket client;
	private PrintWriter writer;
	private BufferedReader reader;
	private String username;

	public ClientHandler(Socket client) throws IOException {
		super();
		this.client = client;
		this.reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
		this.writer = new PrintWriter(client.getOutputStream(), true);
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Override
	public void run() {
		try {
			log.info("handling client {}", this.client.getRemoteSocketAddress());
			
			String echoUsername = reader.readLine();
			if (echoUsername.startsWith("username")) {
				this.username = echoUsername.substring(9);
				log.info("received username [{}] from client {}, echoing...", this.username,
					this.client.getRemoteSocketAddress());
			} else {
				this.username = "Poopyface";
				log.info("Client did not enter username, defaulted to {}", "Poopyface");
				writer.print("You did not enter a username; your username is now 'Poopyface'.");
			}
			writer.flush();
			
			while (!this.client.isClosed()) {
				String echo = reader.readLine();
				log.info("received message [{}] from client {} {}, echoing...", echo, this.username,
						this.client.getRemoteSocketAddress());
				
				DateFormat dateFormat = new SimpleDateFormat("MM/dd HH:mm:ss");
				Date date = new Date();
				
				writer.print("MSG | " + dateFormat.format(date) + " | " + this.username + " | " + echo);
				
				
				
				
				//Thread.sleep(500);
//				writer.print(echo);
				writer.flush();
			}
			this.close();
		} catch (IOException e) {
			log.error("Handler fail! oh noes :(", e);
		}
	}

	@Override
	public void close() throws IOException {
		log.info("closing connection to client {}", this.client.getRemoteSocketAddress());
		this.client.close();
	}

}
