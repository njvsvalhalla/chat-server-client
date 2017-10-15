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
	//Grab our logger for the class
	Logger log = LoggerFactory.getLogger(ClientHandler.class);

	//The following 3 are required for the server. We need to print and read our socket.
	private Socket client;
	private PrintWriter writer;
	private BufferedReader reader;
	
	//Initiate username, date and set a standard for our date format
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
			// username if not specified. You can essentially just rename it to anything.
			String echoUsername = reader.readLine();
			if (echoUsername.startsWith("username")) {
				this.username = echoUsername.substring(9);
				log.info("received username [{}] from client {}, echoing...", this.username,
						this.client.getRemoteSocketAddress());
			} else {
				this.username = "temp";
				int i = 1;
				for (ClientHandler x : Server.handlerThreads.keySet()) {
					if (x.username.startsWith("Anon")) {
						i++;
					}
				}
				this.username = "Anon" + i;
				log.info("Client did not enter username, defaulted to: {} {}.", this.username,
						this.client.getRemoteSocketAddress());
			}
			
			//Whenever someone connects, we need to send it out to all of our threads
			for (ClientHandler x : Server.handlerThreads.keySet()) {
				this.date = new Date();
				x.writer.print("CON | " + this.dateFormat.format(this.date) + " | " + this.username);
				x.writer.flush();
			}
			
			//Similarly, whenever a client quits, we need to recognize that and close the thread. Plus announce it
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
				
				//Whenever we get a new message, we have to unmarshall it and set a date to send out
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
				
				//Just as we had to unmarshall.. not to put it right back into a json object
				StringWriter sw = new StringWriter();
				Marshaller marshaller = jc.createMarshaller();
				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
				marshaller.marshal(msg, sw);

				log.info("received message [{}] from client {} {}, echoing...", msg.getMes(), msg.getUn(),
						this.client.getRemoteSocketAddress());
				//Now we just gotta send it out to all threads (clients) :)
				for (ClientHandler x : Server.handlerThreads.keySet()) {
					log.debug("Sending to user: {} {} ", x.username, x.client.getRemoteSocketAddress());
					x.writer.print(sw);
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
