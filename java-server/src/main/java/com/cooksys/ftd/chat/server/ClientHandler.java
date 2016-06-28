package com.cooksys.ftd.chat.server;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.ftd.chat.model.Message;

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

			// Reads in the username given from the client, or gives a default
			// username if not specified
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
				log.info("Client did not enter username, dafaulted to: {} {}.", this.username,
						this.client.getRemoteSocketAddress());
//				writer.print("You did not enter a username; your username is now " + this.username + "\n");
//				writer.flush();
				// writer.println("und | " + this.username + "\n");
				// writer.flush();
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
				// System.out.println(echo);
				// JAXBContext jc = JAXBContext.newInstance(Message.class);
				// Unmarshaller unmarshaller = jc.createUnmarshaller();
				// unmarshaller.setProperty("eclipselink.media-type",
				// "application/json");
				// StringReader json = new
				// StringReader(echo.replaceAll("[^\\x20-\\x7e\\x0A]", ""));
				// Message msg = (Message) unmarshaller.unmarshal(json);
				// msg.setDateM(new Date());

				Map<String, Object> properties = new HashMap<String, Object>(1);
				properties.put("eclipselink.media-type", "application/json");
				JAXBContext jc = JAXBContext.newInstance(new Class[] { Message.class }, properties);

				Unmarshaller unmarshaller = jc.createUnmarshaller();
				StringReader json = new StringReader(echo);
				Message msg = (Message) unmarshaller.unmarshal(json);
				msg.setDateM(this.dateFormat.format(new Date()));

				if (msg.getUn() == null) {
					msg.setUn(this.username);
				}
				StringWriter sw = new StringWriter();
				Marshaller marshaller = jc.createMarshaller();
				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
				marshaller.marshal(msg, sw);

				log.info("received message [{}] from client {} {}, echoing...", msg.getMes(), msg.getUn(),
						this.client.getRemoteSocketAddress());
				for (ClientHandler x : Server.handlerThreads.keySet()) {
					log.debug("Sending to user: {} {} ", x.username, x.client.getRemoteSocketAddress());
					x.writer.print(sw);
					// x.writer.print("MSG | " +
					// dateFormat.format(msg.getDateM()) + " | " + msg.getUn() +
					// " | " + msg.getMes());
					x.writer.flush();
				}
				writer.flush();
			}
			// this.close();
		} catch (IOException e) {
			log.error("Handler fail! oh noes :(", e);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void close() throws IOException {
		log.info("closing connection to client {}", this.client.getRemoteSocketAddress());
		this.client.close();
	}

}
