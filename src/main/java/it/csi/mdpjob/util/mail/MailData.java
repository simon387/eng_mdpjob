/*
 * SPDX-FileCopyrightText: (C) Copyright 2023 Regione Piemonte
 *
 * SPDX-License-Identifier: EUPL-1.2 */

package it.csi.mdpjob.util.mail;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;


public class MailData {

	private String from;

	private String alias;

	private Set<InternetAddress> to;

	private Set<InternetAddress> cc;

	private Set<InternetAddress> bcc;

	private String subject;

	private String text;

	private String contentType;

	private Set<Attached> attachedFiles;

	public String getFrom () {
		return from;
	}

	public void setFrom ( String from ) {
		this.from = from;
	}

	public String getAlias () {
		return alias;
	}

	public void setAlias ( String alias ) {
		this.alias = alias;
	}

	public InternetAddress getFromAndAlias () throws UnsupportedEncodingException {
		return new InternetAddress ( from, alias );
	}

	public InternetAddress[] getTo () {
		if ( to == null ) {
			return null;
		}
		var ia = new InternetAddress[to.size ()];
		ia = to.toArray ( ia );
		return ia;
	}

	public boolean IsEmptyTo () {
		if ( to == null ) {
			return true;
		}
		return to.isEmpty ();
	}

	public void setTo ( String... tos ) throws AddressException {
		if ( tos == null ) {
			return;
		}
		if ( this.to == null ) {
			this.to = new HashSet<> ();
		}
		for ( String to : tos ) {
			this.to.add ( new InternetAddress ( to ) );
		}
	}

	public InternetAddress[] getCc () {
		if ( cc == null ) {
			return null;
		}
		var ia = new InternetAddress[cc.size ()];
		ia = cc.toArray ( ia );
		return ia;
	}

	public void setCc ( String cc ) throws AddressException {
		if ( this.cc == null ) {
			this.cc = new HashSet<> ();
		}
		this.cc.add ( new InternetAddress ( cc ) );
	}

	public boolean IsEmptyCc () {
		if ( cc == null )
			return true;
		return cc.isEmpty ();
	}

	public InternetAddress[] getBcc () {
		if ( bcc == null ) {
			return null;
		}
		var ia = new InternetAddress[bcc.size ()];
		ia = bcc.toArray ( ia );
		return ia;
	}

	public boolean IsEmptyBcc () {
		if ( bcc == null )
			return true;
		return bcc.isEmpty ();
	}

	public void setBcc ( String bcc ) throws AddressException {
		if ( this.bcc == null ) {
			this.bcc = new HashSet<> ();
		}
		this.bcc.add ( new InternetAddress ( bcc ) );
	}

	public String getSubject () {
		return subject;
	}

	public void setSubject ( String subject ) {
		this.subject = subject;
	}

	public String getText () {
		return text;
	}

	public void setText ( String text ) {
		this.text = text;
	}

	public String getContentType () {
		return contentType;
	}

	public void setContentType ( String contentType ) {
		this.contentType = contentType;
	}

	public Set<Attached> getAttachedFiles () {
		if ( attachedFiles == null ) {
			attachedFiles = new HashSet<> ();
		}
		return attachedFiles;
	}

	public void setAttachedFiles ( Set<Attached> attachedFiles ) {
		this.attachedFiles = attachedFiles;
	}

	public boolean hasAttachedFiles () {
		return attachedFiles != null && !attachedFiles.isEmpty ();
	}
}
