/*
 * SPDX-FileCopyrightText: (C) Copyright 2023 Regione Piemonte
 *
 * SPDX-License-Identifier: EUPL-1.2 */

package it.csi.mdpjob.dao;

import java.sql.PreparedStatement;


public class EmptyStatementMapper implements StatementMapper {

	public void mapStatementParameters ( PreparedStatement stmt ) {
	}

}
