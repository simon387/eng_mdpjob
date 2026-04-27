/*
 * SPDX-FileCopyrightText: (C) Copyright 2023 Regione Piemonte
 *
 * SPDX-License-Identifier: EUPL-1.2 */

package it.csi.mdpjob.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;


public interface StatementMapper {

	/**
	 * In questo metodo vanno mappate le bind variable della query
	 * tramite il PreparedStatement passato.
	 *
	 */
	void mapStatementParameters ( PreparedStatement stmt ) throws SQLException;

}
