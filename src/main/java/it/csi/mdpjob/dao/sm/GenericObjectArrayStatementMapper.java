/*
 * SPDX-FileCopyrightText: (C) Copyright 2023 Regione Piemonte
 *
 * SPDX-License-Identifier: EUPL-1.2 */

package it.csi.mdpjob.dao.sm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;


public class GenericObjectArrayStatementMapper extends StatementMapperUtils {

	private static final Logger log = LoggerFactory.getLogger ( GenericObjectArrayStatementMapper.class );

	private final Object[] objs;

	public GenericObjectArrayStatementMapper ( Object... objs ) {
		this.objs = objs;
	}

	@Override
	public void mapStatementParameters ( PreparedStatement stmt ) throws SQLException {
		var parametri = new StringBuilder ();

		for ( var o : objs ) {
			setObj ( stmt, o );
			parametri.append ( o ).append ( "," );
		}
		log.debug ( "debug: {}", parametri );
	}

	private void setObj ( PreparedStatement stmt, Object o ) throws SQLException {
		if ( o instanceof Integer ) {
			setInt ( stmt, (Integer) o );
		} else if ( o instanceof Double ) {
			setDouble ( stmt, (Double) o );
		} else if ( o instanceof String ) {
			setString ( stmt, (String) o );
		} else if ( o instanceof java.sql.Timestamp ) {
			setTimestamp ( stmt, (Date) o );
		} else if ( o instanceof Date ) {
			setDate ( stmt, (Date) o );
		} else if ( o instanceof BigDecimal ) {
			setBigDecimal ( stmt, (BigDecimal) o );
		} else if ( o instanceof byte[] ) {
			setBinaryStream ( stmt, new ByteArrayInputStream ( (byte[]) o ), ( (byte[]) o ).length );
		} else if ( o instanceof Boolean ) {
			setBoolean ( stmt, (Boolean) o );
		} else if ( o instanceof Long ) {
			setLong ( stmt, (Long) o );
		} else if ( o == null ) {
			setNull ( stmt );
		} else {
			throw new IllegalArgumentException ( "GenericObjectArrayStatementMapper: Tipo oggetto non supportato [" + o.getClass () + "]" );
		}
	}
}
