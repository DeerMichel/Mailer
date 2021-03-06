/*
Mailer - A simple mail distribution tool (e.g. for newsletters)
Copyright (C) 2015 Micha Hanselmann

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package de.mh.mailer;

import javax.mail.*;
import javax.mail.internet.*;
import javax.swing.JFrame;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;


/** Mailer
 * - A simple mail distribution tool (e.g. for newsletters)
 * @author Micha Hanselmann
 * Copyright (c) 2015 Micha Hanselmann
 * Licensed under GNU GPL v2.
 */
public class Mailer {
	
	public List<String> recipients;
	public int index = 0;
	public String message;
	public String host;
	public String user;
	public String password;
	public String from;
	public String subject;
	public File logfile;
	//public List<File> attachments = new ArrayList<>();
	

	public static void main(String[] args) {
		new Mailer();
	}
	
	public Mailer() {
		
		// init Localizer
		Localizer.init();
		
		// create logfile
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		logfile = new File("mailer" + sdf.format(new Date()) + ".log");
		
		// create and show window
		JFrame window = new Window(this);
		window.setVisible(true);
		
	}
	
	public void log(String text) {
		
		try {
			
			// read log
			String log = "";
			if (logfile.exists()) log = new String(Files.readAllBytes(logfile.toPath()), StandardCharsets.UTF_8);
					
			// log
	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	        log = sdf.format(new Date()) + " " + text + "\n" + log;
					
			// save log
			PrintWriter writer = new PrintWriter(logfile);
			writer.print(log);
			writer.close();
			
		} catch(Exception e) { 
			System.out.println(e.toString());
		}
		
	}
	
	public boolean send() throws Exception {
		
		// log
		log("Send mail to " + recipients.get(index) + " (index: " + index + ")");
		
		// get properties
		Properties props = System.getProperties();
		
		// setup server
		props.setProperty("mail.smtp.host", host);
		props.setProperty("mail.smtp.starttls.enable", "true");
		props.setProperty("mail.smtp.auth", "true");
		props.setProperty("mail.smtp.port", "587");
		// props.setProperty("mail.debug", "true");
		
		// get session
		Session session = Session.getDefaultInstance(props, 
				new Authenticator() {

					@Override
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(user, password);
					}
			
		});
			
		// create message
		MimeMessage msg = new MimeMessage(session);

		// setup mail header
		msg.setFrom(new InternetAddress(from));
		msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recipients.get(index).trim()));
		msg.setSubject(subject);
		
		// create main part
		BodyPart part = new MimeBodyPart();
		part.setContent(message, "text/html");
		
		// setup multipart
		Multipart multipart = new MimeMultipart();
		multipart.addBodyPart(part);
		
		/* add attachments
		for (File file : attachments) {
			part = new MimeBodyPart();
			part.setDataHandler(new DataHandler(new FileDataSource(file)));
			part.setHeader("Content-ID", "<" + file.getName() + ">");
			multipart.addBodyPart(part);
		}*/

		// set content
		msg.setContent(multipart);

		// send
		Transport.send(msg);
		
		return true;
	}

}
