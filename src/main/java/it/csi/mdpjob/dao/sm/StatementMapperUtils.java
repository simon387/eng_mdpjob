/*
 * SPDX-FileCopyrightText: (C) Copyright 2023 Regione Piemonte
 *
 * SPDX-License-Identifier: EUPL-1.2 */

package it.csi.mdpjob.dao.sm;

import it.csi.mdpjob.dao.StatementMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public abstract class StatementMapperUtils implements StatementMapper {

	private static final Logger log = LoggerFactory.getLogger ( StatementMapperUtils.class );

	private int n = 1;

	public void setNull ( PreparedStatement stmt ) throws SQLException {
		stmt.setNull ( n, java.sql.Types.NULL );
		log.debug ( "setNull - {}. null", n );
		n = n + 1;
	}

	public void setBoolean ( PreparedStatement stmt, Boolean o ) throws SQLException {
		stmt.setBoolean ( n, o );
		log.debug ( "setBoolean - {}. null", n );
		n = n + 1;
	}

	public void setInt ( PreparedStatement stmt, Integer value ) throws SQLException {
		setInt ( stmt, value, false );
	}

	public void setInt ( PreparedStatement stmt, Integer value, boolean obbligatorio ) throws SQLException {
		if ( value != null ) {
			stmt.setInt ( n, value );
		} else {
			if ( obbligatorio ) {
				throw new SQLException ( "Paramentro obbligatorio mancante" );
			} else {
				stmt.setNull ( n, java.sql.Types.INTEGER );
			}
		}
		log.debug ( "debug 1 setInt - {}. {}", n, value );
		n = n + 1;
	}

	public void setLong ( PreparedStatement stmt, Long value ) throws SQLException {
		setLong ( stmt, value, false );
	}

	public void setLong ( PreparedStatement stmt, Long value, boolean obbligatorio ) throws SQLException {
		if ( value != null ) {
			stmt.setLong ( n, value );
		} else {
			if ( obbligatorio ) {
				throw new SQLException ( "Paramentro obbligatorio mancante" );
			} else {
				stmt.setNull ( n, java.sql.Types.BIGINT );
			}
		}
		log.debug ( "setLong - {}. {}", n, value );
		n = n + 1;
	}

	public void setDouble ( PreparedStatement stmt, Double value ) throws SQLException {
		setDouble ( stmt, value, false );
	}

	public void setDouble ( PreparedStatement stmt, Double value, boolean obbligatorio ) throws SQLException {
		if ( value != null ) {
			stmt.setDouble ( n, value );
		} else {
			if ( obbligatorio ) {
				throw new SQLException ( "Paramentro obbligatorio mancante" );
			} else {
				stmt.setNull ( n, java.sql.Types.DOUBLE );
			}
		}
		log.debug ( "setDouble - {}. {}", n, value );
		n = n + 1;
	}

	public void setBigDecimal ( PreparedStatement stmt, BigDecimal value ) throws SQLException {
		setBigDecimal ( stmt, value, false );
	}

	public void setBigDecimal ( PreparedStatement stmt, BigDecimal value, boolean obbligatorio ) throws SQLException {
		if ( value != null ) {
			stmt.setBigDecimal ( n, value );
		} else {
			if ( obbligatorio ) {
				throw new SQLException ( "Paramentro obbligatorio mancante" );
			} else {
				stmt.setNull ( n, java.sql.Types.DOUBLE );
			}
		}
		log.debug ( "setBigDecimal - {}. {}", n, value );
		n = n + 1;
	}

	public void setBinaryStream ( PreparedStatement stmt, ByteArrayInputStream value, int length ) throws SQLException {
		setBinaryStreamHelper ( stmt, value, length );
	}

	private void setBinaryStreamHelper ( PreparedStatement stmt, ByteArrayInputStream value, int length ) throws SQLException {
		if ( value != null ) {
			stmt.setBinaryStream ( n, value, length );
		} else {
			stmt.setNull ( n, java.sql.Types.BLOB );
		}
		log.debug ( "setBinaryStream - {}. {}", n, value );
		n = n + 1;
	}

	public void setString ( PreparedStatement stmt, String value ) throws SQLException {
		setString ( stmt, value, false );
	}

	public void setString ( PreparedStatement stmt, String value, boolean obbligatorio ) throws SQLException {
		if ( value != null ) {
			stmt.setString ( n, value );
		} else {
			if ( obbligatorio ) {
				throw new SQLException ( "Paramentro obbligatorio mancante" );
			} else {
				stmt.setNull ( n, java.sql.Types.VARCHAR );
			}
		}
		log.debug ( "setString - {}. {}", n, value );
		n = n + 1;
	}

	public void setDate ( PreparedStatement stmt, java.util.Date value ) throws SQLException {
		setDate ( stmt, value, false );
	}

	public void setDate ( PreparedStatement stmt, java.util.Date value, boolean obbligatorio ) throws SQLException {
		if ( value != null ) {
			stmt.setDate ( n, new java.sql.Date ( value.getTime () ) );
		} else {
			if ( obbligatorio ) {
				throw new SQLException ( "Paramentro obbligatorio mancante" );
			} else {
				stmt.setNull ( n, java.sql.Types.DATE );
			}
		}
		log.debug ( "setDate - {}. {}", n, value );
		n = n + 1;
	}

	public void setTimestamp ( PreparedStatement stmt, java.util.Date value ) throws SQLException {
		setTimestamp ( stmt, value, false );
	}

	public void setTimestamp ( PreparedStatement stmt, java.util.Date value, boolean obbligatorio ) throws SQLException {
		if ( value != null ) {
			stmt.setTimestamp ( n, new java.sql.Timestamp ( value.getTime () ) );
		} else {
			if ( obbligatorio ) {
				throw new SQLException ( "Paramentro obbligatorio mancante" );
			} else {
				stmt.setNull ( n, java.sql.Types.TIMESTAMP );
			}
		}
		log.debug ( "setTimestamp - {}. {}", n, value );
		n = n + 1;
	}

}

