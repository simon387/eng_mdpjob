/*
 * SPDX-FileCopyrightText: (C) Copyright 2023 Regione Piemonte
 *
 * SPDX-License-Identifier: EUPL-1.2 */

package it.csi.mdpjob.util.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public abstract class ConnectionManager {

	private static final Logger log = LoggerFactory.getLogger ( ConnectionManager.class );

	public abstract Connection getConnection () throws SQLException;

	public static void closeConnection ( Connection conn, Statement stmt, ResultSet rs ) {
		closeResultSet ( rs );
		closeStatement ( stmt );
		closeConnection ( conn );
	}

	public static void closeConnection ( Connection conn, Statement stmt ) {
		closeStatement ( stmt );
		closeConnection ( conn );
	}

	public static void closeConnection ( Connection conn ) {
		if ( conn != null ) {
			try {
				conn.close ();
			} catch ( SQLException e ) {
				log.error ( String.valueOf ( e ), e );
			}
		}
	}

	public static void closeStatement ( Statement stmt ) {
		if ( stmt != null ) {
			try {
				stmt.close ();
			} catch ( SQLException e ) {
				log.error ( String.valueOf ( e ), e );
			}
		}
	}

	public static void closeResultSet ( ResultSet rs ) {
		if ( rs != null ) {
			try {
				rs.close ();
			} catch ( SQLException e ) {
				log.error ( String.valueOf ( e ), e );
			}
		}
	}
}
