/*
 * SPDX-FileCopyrightText: (C) Copyright 2023 Regione Piemonte
 *
 * SPDX-License-Identifier: EUPL-1.2 */

package it.csi.mdpjob.dao;

import it.csi.mdpjob.dao.sm.GenericObjectArrayStatementMapper;
import it.csi.mdpjob.util.connection.ConnectionManager;
import it.csi.mdpjob.util.connection.ConnectionManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public abstract class BaseDAO<T> implements BaseDAOI<T> {

	private static final Logger log = LoggerFactory.getLogger ( BaseDAO.class );

	protected ResultSetExtractor<T> resultSetExtractor = null;

	protected StatementMapper statementMapper = new EmptyStatementMapper ();

	public abstract String componiQuery ();

	public void setResultSetExtractor ( ResultSetExtractor<T> resultSetExtractor ) {
		this.resultSetExtractor = resultSetExtractor;
	}

	public void setResultSetExtractor ( T o ) {
		this.setResultSetExtractor ( new ObjectResultSetExtractor<> ( o ) );
	}

	public void setStatementMapper ( StatementMapper statementMapper ) {
		this.statementMapper = statementMapper;
	}

	public void setStatementParams ( Object... params ) {
		this.statementMapper = new GenericObjectArrayStatementMapper ( params );
	}

	public T executeQuery () throws Exception {
		final var METHOD_NAME = "executeQuery";

		T returnObject;
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			conn = ConnectionManagerFactory.getInstance ().getConnection ();
			var sql = componiQuery ();
			//noinspection SqlSourceToSinkFlow
			stmt = conn.prepareStatement ( sql );
			log.debug ( METHOD_NAME + " - map statement params..." );
			statementMapper.mapStatementParameters ( stmt );

			log.info ( METHOD_NAME + " - start execute query: {}", sql );
			rs = stmt.executeQuery ();
			log.debug ( METHOD_NAME + " - extract data..." );
			returnObject = resultSetExtractor.extractData ( rs );
			log.debug ( METHOD_NAME + " - returning object." );
			return returnObject;

		} catch ( SQLException e ) {
			log.error ( METHOD_NAME + " - SQLException", e );
			throw e;
		} catch ( NamingException e ) {
			log.error ( METHOD_NAME + " - NamingException", e );
			throw e;
		} catch ( Exception e ) {
			log.error ( METHOD_NAME + " - Exception", e );
			throw e;
		} finally {
			ConnectionManager.closeConnection ( conn, stmt, rs );
		}
	}

}
