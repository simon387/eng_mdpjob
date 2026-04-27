/*
 * SPDX-FileCopyrightText: (C) Copyright 2023 Regione Piemonte
 *
 * SPDX-License-Identifier: EUPL-1.2 */

package it.csi.mdpjob.util.connection;

public class ConnectionManagerFactory {

	static ConnectionManager connManagerInstance = null;

	private ConnectionManagerFactory () {

	}

	public static ConnectionManager getInstance () throws Exception {
		if ( connManagerInstance == null ) {
			connManagerInstance = new ConnectionJdbc ();
		}
		return connManagerInstance;
	}
}

