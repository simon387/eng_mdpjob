/*
 * SPDX-FileCopyrightText: (C) Copyright 2023 Regione Piemonte
 *
 * SPDX-License-Identifier: EUPL-1.2 */

package it.csi.mdpjob.util.mail;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;


public class MailUtil {

	private static final Logger log = LoggerFactory.getLogger ( MailUtil.class );

	private static final String MAIL_PROPERTIES = "mail.properties";

	private static final String TEST_FLAG = "mail.test.flag";

	private static final String TEST_DESTINATARIO = "mail.test.destinatario";

	private static final String DEFAULT_DESTINATARIO = "mail.default.destinatario";

	private static final String MITTENTE_INDIRIZZO = "mail.mittente.indirizzo";

	private static final String MITTENTE_ALIAS = "mail.mittente.alias";

	private static final String DESTINATARIO_NOTIFICA_FALLITO_INVIO_RT_FRUITORE = "mail.destinatario.notifica.fallito.invio.rt.fruitore";

	private static final Properties properties = new Properties ();

	static {
		final var methodName = "initialization";

		try ( var inputStream = ClassLoader.getSystemResourceAsStream ( MAIL_PROPERTIES ) ) {
			properties.load ( inputStream );
		} catch ( IOException e ) {
			log.error ( methodName );
			log.error ( e.getMessage () );
		}

		for ( var property : properties.entrySet () ) {
			var message = "Property " + property.getKey () + " values '" + property.getValue () + "'";
			log.info ( methodName + " - {}", message );
		}
	}

	public static void inviaMail ( MailData mail ) throws MessagingException, UnsupportedEncodingException {
		final var methodName = "inviaMail";

		if ( StringUtils.isBlank ( mail.getFrom () ) ) {
			mail.setFrom ( properties.getProperty ( MITTENTE_INDIRIZZO ) );
		}
		if ( StringUtils.isBlank ( mail.getAlias () ) ) {
			mail.setAlias ( properties.getProperty ( MITTENTE_ALIAS ) );
		}

		var message = new MimeMessage ( Session.getInstance ( properties ) );
		message.setSentDate ( new Date () );
		message.setFrom ( mail.getFromAndAlias () );

		if ( Boolean.parseBoolean ( properties.getProperty ( TEST_FLAG ) ) ) {
			log.info ( methodName + " - mail di test" );
			InternetAddress[] ia = { new InternetAddress ( properties.getProperty ( TEST_DESTINATARIO ) ) };
			message.setRecipients ( Message.RecipientType.TO, ia );
		} else {
			if ( !mail.IsEmptyTo () ) {
				message.setRecipients ( Message.RecipientType.TO, mail.getTo () );
			} else {
				log.info ( methodName + " - destinatario di default" );
				InternetAddress[] ia = { new InternetAddress ( properties.getProperty ( DEFAULT_DESTINATARIO ) ) };
				message.setRecipients ( Message.RecipientType.TO, ia );
			}
			if ( !mail.IsEmptyCc () ) {
				message.setRecipients ( Message.RecipientType.CC, mail.getCc () );
			}
			if ( !mail.IsEmptyBcc () ) {
				message.setRecipients ( Message.RecipientType.BCC, mail.getBcc () );
			}
		}

		if ( Boolean.parseBoolean ( properties.getProperty ( TEST_FLAG ) ) ) {
			message.setSubject ( "TEST - " + mail.getSubject () );
		} else {
			message.setSubject ( mail.getSubject () );
		}

		if ( mail.hasAttachedFiles () ) {
			var multipart = new MimeMultipart ();

			/*testo*/
			var messageBodyPart = new MimeBodyPart ();
			messageBodyPart.setText ( mail.getText () );
			multipart.addBodyPart ( messageBodyPart );

			/* attachments */
			for ( var attached : mail.getAttachedFiles () ) {
				messageBodyPart = new MimeBodyPart ();
				messageBodyPart.setFileName ( attached.getName () );
				var source = new ByteArrayDataSource ( attached.getData (), attached.getType () );
				messageBodyPart.setDataHandler ( new DataHandler ( source ) );
				multipart.addBodyPart ( messageBodyPart );
			}

			message.setContent ( multipart );
		} else {
			message.setText ( mail.getText () );
		}
		Transport.send ( message );
	}

	public static void main ( String[] args ) {
		var properties = new Properties ();
		properties.setProperty ( "mail.smtp.host", "mailfarm.csi.it" );
		properties.setProperty ( "mail.smtp.port", "25" );
		properties.setProperty ( MITTENTE_INDIRIZZO, "servizio.mdp@csi.it" );
		properties.setProperty ( MITTENTE_ALIAS, "servizio.mdp" );
		properties.setProperty ( TEST_FLAG, "true" );
		properties.setProperty ( TEST_DESTINATARIO, "massimo.venesia@csi.it" );

		var mail = new MailData ();
		mail.setSubject ( "mail di prova" );
		mail.setText ( "mail per testare la classe che compone il messaggio" );

		try {
			MailUtil.inviaMail ( mail );
			log.info ( "fine invio   " );
		} catch ( Exception e ) {
			log.error ( "errore   {}", e, e );
		}
		log.debug ( "debug: {}", properties );
	}

	public static String getDestinatarioNotificaFallitoInvioRTFruitore () {
		return properties.getProperty ( DESTINATARIO_NOTIFICA_FALLITO_INVIO_RT_FRUITORE );
	}

}
