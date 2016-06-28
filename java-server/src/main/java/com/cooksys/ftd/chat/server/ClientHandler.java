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
import com.cooksys.ftd.chat.server.Server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class ClientHandler implements Runnable, Closeable {

	Logger log = LoggerFactory.getLogger(ClientHandler.class);

	private Socket client;
	private PrintWriter writer;
	private BufferedReader reader;
	private String username;
	private Date date;
	private DateFormat dateFormat = new SimpleDateFormat("MM/dd HH:mm:ss");

	public ClientHandler(Socket client) throws IOException {
		super();
		this.client = client;
		this.reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
		this.writer = new PrintWriter(client.getOutputStream(), true);
	}

	@Override
	public void run() {
		try {

			log.info("handling client {}", this.client.getRemoteSocketAddress());

			//Reads in the username given from the client, or gives a default username if not specified
			String echoUsername = reader.readLine();
			if (echoUsername.startsWith("username")) {
				this.username = echoUsername.substring(9);
				log.info("received username [{}] from client {}, echoing...", this.username,
						this.client.getRemoteSocketAddress());
			} else {
				this.username = "temp";
				int i = 1;
				for (ClientHandler x : Server.handlerThreads.keySet()) {
					if (x.username.startsWith("Poopyface")) {
						i++;
					}
				}
				this.username = "Poopyface" + i;
				log.info("Client did not enter username, dafaulted to: {} {}.", this.username, this.client.getRemoteSocketAddress());
				writer.print("You did not enter a username; your username is now " + this.username + "\n");
				writer.flush();
			}

			for (ClientHandler x : Server.handlerThreads.keySet()) {
				this.date = new Date();
				x.writer.print("CON | " + this.dateFormat.format(this.date) + " | " + this.username);
				x.writer.flush();
			}

			while (!this.client.isClosed()) {
				String echo = reader.readLine();
				if (echo.startsWith("quit | ")) {
					for (ClientHandler x : Server.handlerThreads.keySet()) {
						this.date = new Date();
						x.writer.print("DIS | " + this.dateFormat.format(this.date) + " | " + this.username);
						x.writer.flush();
						if (x.username == this.username)
							Server.handlerThreads.remove(x);
					}
					this.close();
					this.client.close();
					this.writer.close();
					this.reader.close();
				}
				log.info("received message [{}] from client {} {}, echoing...", echo, this.username,
						this.client.getRemoteSocketAddress());
				for (ClientHandler x : Server.handlerThreads.keySet()) {
					log.debug("Sending to user: {} {} ", x.username, x.client.getRemoteSocketAddress());
					this.date = new Date();
					x.writer.print("MSG | " + this.dateFormat.format(this.date) + " | " + this.username + " | " + echo);
					x.writer.flush();
				}
				writer.flush();
			}
			//this.close();
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
