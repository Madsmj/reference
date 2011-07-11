/*
 * #%L
 * bitrepository-access-client
 * *
 * $Id: AccessComponentFactory.java 212 2011-07-05 10:04:10Z bam $
 * $HeadURL: https://sbforge.org/svn/bitrepository/trunk/bitrepository-access-client/src/main/java/org/bitrepository/access/AccessComponentFactory.java $
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package org.bitrepository.alarm.handler;

import java.net.InetAddress;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.bitrepository.alarm.AlarmException;
import org.bitrepository.alarm.AlarmHandler;
import org.bitrepository.alarm_client.alarmclientconfiguration.AlarmConfiguration;
import org.bitrepository.alarm_client.alarmclientconfiguration.AlarmConfiguration.MailingConfiguration;
import org.bitrepository.bitrepositoryelements.AlarmDescription;
import org.bitrepository.bitrepositorymessages.Alarm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A quite simple AlarmHandler, which sends an mail with the Alarm.
 * TODO have a setting for how often, a mail should be send.
 */
public class MailingAlarmHandler implements AlarmHandler {

	/** The logger to log the Alarms.*/
	private Logger log = LoggerFactory.getLogger(this.getClass());

	/** The configuration for mailing the messages.*/
	private final MailingConfiguration config;
	
	/** The message receiver.*/
	private final String messageReceiver;
	/** The message sender.*/
	private final String messageSender;
	/** The mail server.*/
	private final String mailServer;

	/** The key for the MAIL_FROM_PROPERTY.*/
	private static final String MAIL_FROM_PROPERTY_KEY = "mail.from";
	/** The key for the MAIL_HOST_PROPERTY.*/
	private static final String MAIL_HOST_PROPERTY_KEY = "mail.host";
	/** The default mimetype of a mail.*/
	private static final String MIMETYPE = "text/plain";

	/**
	 * Constructor.
	 */
	public MailingAlarmHandler(AlarmConfiguration conf) {
		this.config = conf.getMailingConfiguration();
		this.messageReceiver = config.getMailReceiver();
		this.messageSender = config.getMailSender();
		this.mailServer = config.getMailServer();
	}

	@Override
	public void notify(Alarm msg) {
		AlarmDescription description = msg.getAlarmDescription();
		String subject = "Received alarm with code '" + description.getAlarmCode() + "' and text '" 
		+ description.getAlarmText() + "'";
		log.error(subject + ":\n{}", msg.toString());

		sendMail(subject, msg.toString());
	}

	@Override
	public void notify(Object msg) {
		String subject = "Received unexpected object of type '" + msg.getClass() + "'";
		log.error(subject + ":\n{}", msg.toString());

		sendMail(subject, msg.toString());
	}

	/**
	 * Method fore sending a mail.
	 * 
	 * @param subject The subject of the mail.
	 * @param content The content of the mail.
	 */
	private void sendMail(String subject, String content) {
		StringBuffer body = new StringBuffer();
		try {
			// Make the body of the mail.
			body.append("Host: " + InetAddress.getLocalHost().getCanonicalHostName() + "\n");
			body.append("Date: " + new Date().toString() + "\n");
			body.append(content + "\n");
		} catch (Exception e) {
			throw new AlarmException("");
		}

		Properties props = new Properties();
		try {
			props.put(MAIL_FROM_PROPERTY_KEY, messageSender);
			props.put(MAIL_HOST_PROPERTY_KEY, mailServer);
		} catch (Exception e) {
			throw new AlarmException("");
		}

		Session session = Session.getDefaultInstance(props);
		Message msg = new MimeMessage(session);

		// to might contain more than one e-mail address
		for (String toAddressS : messageReceiver.split(",")) {
			try {
				InternetAddress toAddress
				= new InternetAddress(toAddressS.trim());
				msg.addRecipient(Message.RecipientType.TO, toAddress);
			} catch (Exception e) {
				throw new AlarmException("To address '" + toAddressS
						+ "' is not a valid email "
						+ "address", e);
			}
		}
		try {
			if (msg.getAllRecipients().length == 0) {
				throw new AlarmException("No valid recipients in '" + messageReceiver + "'");
			}
		} catch (MessagingException e) {
			throw new AlarmException("Cannot handle recipients of the message '" + msg + "'", e);
		}

		try {
			InternetAddress fromAddress = null;
			fromAddress = new InternetAddress(messageSender);
			msg.setFrom(fromAddress);
		} catch (Exception e) {
			throw new AlarmException("Cannot add the messageSender '" + messageSender + "' to the mail.", e);
		}

		try {
			msg.setSubject(subject);
			msg.setContent(body, MIMETYPE);
			msg.setSentDate(new Date());
			Transport.send(msg);
		} catch (Exception e) {
			throw new AlarmException("Could not send email with subject '" + subject +  "' from '" + messageSender 
					+ "' to '" + messageReceiver + "'. Body:\n" + body, e);
		}
	}
}
