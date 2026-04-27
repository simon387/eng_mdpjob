/*
 * SPDX-FileCopyrightText: (C) Copyright 2023 Regione Piemonte
 *
 * SPDX-License-Identifier: EUPL-1.2 */

package it.csi.mdpjob.dao;

import java.sql.ResultSet;


public interface ResultSetExtractor<T> {

	T extractData ( ResultSet rs ) throws Exception;

}
