package com.cooksys.ftd.chat.model;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "msg")
@XmlType(propOrder = { "un", "mes", "dateM" })
@XmlAccessorType(XmlAccessType.FIELD)
public class Message {
	@XmlElement(name = "un")
	private String un;
	@XmlElement(name = "mes")
	private String mes;
	@XmlElement(name = "date")
	private String dateM;
	
	public Message(String un, String mes) {
		this.un = un;
		this.mes = mes;
	}

	public Message() {

	}

	public String getUn() {
		return un;
	}

	public void setUn(String un) {
		this.un = un;
	}

	public String getMes() {
		return mes;
	}

	public void setMes(String mes) {
		this.mes = mes;
	}

	public String getDateM() {
		return dateM;
	}

	public void setDateM(String dateM) {
		this.dateM = dateM;
	}
}
