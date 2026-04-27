/*
 * SPDX-FileCopyrightText: (C) Copyright 2023 Regione Piemonte
 *
 * SPDX-License-Identifier: EUPL-1.2 */

package it.csi.mdpjob.util.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


public class ConnectionJdbc extends ConnectionManager {

	private static final Logger log = LoggerFactory.getLogger ( ConnectionJdbc.class );

	String url;

	String driver;

	String user;

	String pass;

	long incrementalRetryMills;

	int maxRetryAttemps;

	long retryAttemps = 0;

	protected ConnectionJdbc () throws IOException, ClassNotFoundException {
		var is = ClassLoader.getSystemResourceAsStream ( "db.properties" );
		var props = new Properties ();
		props.load ( is );

		driver = props.getProperty ( "driver" );
		url = props.getProperty ( "url" );
		user = props.getProperty ( "user" );
		pass = props.getProperty ( "pass" );

		try {
			incrementalRetryMills = Long.parseLong ( props.getProperty ( "incrementalRetryMills" ) );
		} catch ( NumberFormatException nfe ) {
			incrementalRetryMills = 1000;
		}
		try {
			maxRetryAttemps = Integer.parseInt ( props.getProperty ( "maxRetryAttemps" ) );
		} catch ( NumberFormatException nfe ) {
			maxRetryAttemps = 10;
		}

		log.info ( "driver: {}", driver );
		log.info ( "url: {}", url );
		log.info ( "user: {}", user );
		log.info ( "pass: {}", pass );

		log.info ( "incrementalRetryMills: {}", incrementalRetryMills );
		log.info ( "maxRetryAttemps: {}", maxRetryAttemps );

		Class.forName ( driver );
	}

	@Override
	public synchronized Connection getConnection () throws SQLException {
		try {
			var conn = DriverManager.getConnection ( url, user, pass );
			retryAttemps = 0;
			return conn;
		} catch ( SQLException e ) {
			if ( e.getMessage ().contains ( "The Network Adapter could not establish the connection" ) && retryAttemps < maxRetryAttemps ) {
				log.error ( "getConnection - The Network Adapter could not establish the connection. Retry attemp: {}", retryAttemps + 1 );
				incrementalThreadSleep ();
				return getConnection ();
			}
			throw e;
		}
	}

	private void incrementalThreadSleep () {
		try {
			log.debug ( "sleepping for {}", retryAttemps * incrementalRetryMills );
			Thread.sleep ( retryAttemps * incrementalRetryMills );
		} catch ( InterruptedException e ) {
			log.error ( String.valueOf ( e ), e );
		}
		retryAttemps++;
	}

	public static void main ( String[] args ) {
		try {
			var conn = new ConnectionJdbc ();
			for ( var i = 0; i < 1; i++ ) {
				var c = conn.getConnection ();
				c.close ();
			}
		} catch ( Exception e ) {
			log.error ( String.valueOf ( e ), e );
		}
	}
}
