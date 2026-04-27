/*
 * SPDX-FileCopyrightText: (C) Copyright 2023 Regione Piemonte
 *
 * SPDX-License-Identifier: EUPL-1.2 */

package it.csi.mdpjob.dao;

import it.csi.mdpjob.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;


/**
 * Questa classe e' in fase sperimentale!!! supporta solo le PersistenceField annotation dichiarate fino alla prima superclasse.
 *
 * @param <T>
 * @author 71551
 */
public class ObjectResultSetExtractor<T> implements ResultSetExtractor<T> {

	private static final Logger log = LoggerFactory.getLogger ( ObjectResultSetExtractor.class );

	private final T obj;

	public ObjectResultSetExtractor ( T obj ) {
		this.obj = obj;
	}

	public T extractData ( ResultSet rs ) throws Exception {
		final var methodName = "extractData";

		if ( !rs.next () ) {
			log.info ( methodName + " - no data" );
			return null;
		}
		if ( obj == null ) {
			log.info ( methodName + " - no object" );
			return null;
		}

		var fields = obj.getClass ().getDeclaredFields ();

		var fieldsSuperclass = obj.getClass ().getSuperclass ().getSuperclass ().getDeclaredFields ();

		var allFields = new Field[fields.length + fieldsSuperclass.length];
		System.arraycopy ( fields, 0, allFields, 0, fields.length );
		System.arraycopy ( fieldsSuperclass, 0, allFields, fields.length, fieldsSuperclass.length );

		for ( var f : allFields ) {
			var a = f.getAnnotation ( PersistenceField.class );
			if ( a != null ) {

				var columnName = a.column ();
				var columnIdx = rs.findColumn ( columnName );
				var columnType = rs.getMetaData ().getColumnType ( columnIdx );

				var value = getValue ( rs, columnIdx, columnType );
				log.info ( methodName + " - columnName:{} value:{}", columnName, value );

				try {
					Method accessorSet = null;
					var c = obj.getClass ();
					while ( c != null ) {
						try {
							accessorSet = c.getDeclaredMethod ( "set" + StringUtils.capitalize ( f.getName () ), f.getType () );
							c = null;
						} catch ( NoSuchMethodException e ) {
							var superclass = c.getSuperclass ();
							if ( superclass == null ) {
								var msg = "Impossibile trovare il setter method corrispondente alla colonna: " + columnName;
								log.error ( methodName + " - {}", msg );
								throw new Exception ( msg, e );
							} else {
								c = superclass;
							}
						}
					}

					accessorSet.invoke ( obj, value /*rs.getObject(a.column())*/ );

				} catch ( SecurityException e ) {
					var msg = "Impossibile accedere al setter method corrispondente alla colonna: " + columnName;
					log.error ( methodName + " - {}", msg, e );
					throw new Exception ( msg, e );
				} catch ( NoSuchMethodException e ) {
					var msg = "Impossibile trovare il setter method corrispondente alla colonna: " + columnName;
					log.error ( methodName + " - {}", msg, e );
					throw new Exception ( msg, e );
				} catch ( IllegalArgumentException e ) {
					var msg = value.getClass ().getName ();
					log.error ( methodName + " - {}", msg, e );
					throw new Exception ( msg, e );
				} catch ( IllegalAccessException e ) {
					var msg = "Accesso non valido per settare il valore corrispondente alla colonna: " + columnName;
					log.error ( methodName + " - {}", msg, e );
					throw new Exception ( msg, e );
				} catch ( InvocationTargetException e ) {
					var msg = "Impossibile settare il valore corrispondente alla colonna: " + columnName;
					log.error ( methodName + " - {}", msg, e );
					throw new Exception ( msg, e );
				}
			}
		}

		return obj;
	}

	private Object getValue ( ResultSet rs, int columnIdx, int type )
					throws SQLException {

		Object val;

		if ( type == Types.DATE ) {
			val = rs.getDate ( columnIdx );
		} else if ( type == Types.TIME || type == Types.TIMESTAMP ) {
			val = rs.getTimestamp ( columnIdx );
		} else if ( type == Types.VARCHAR || type == Types.CHAR || type == Types.LONGVARCHAR ) {
			val = rs.getString ( columnIdx );
		} else if ( type == Types.INTEGER || type == Types.SMALLINT || type == Types.TINYINT ) {
			val = rs.getInt ( columnIdx );
		} else if ( type == Types.DOUBLE || type == Types.FLOAT ) {
			val = new BigDecimal ( rs.getDouble ( columnIdx ) );
		} else if ( type == Types.DECIMAL || type == Types.NUMERIC ) {
			val = rs.getBigDecimal ( columnIdx );
		} else {
			throw new IllegalArgumentException ( "Tipo colonna non supportato: " + type );
		}
		return val;
	}

}
