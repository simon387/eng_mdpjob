/*
 * SPDX-FileCopyrightText: (C) Copyright 2023 Regione Piemonte
 *
 * SPDX-License-Identifier: EUPL-1.2 */

package it.csi.mdpjob.enumeration;

public enum SubscriptionConfigCodeEnum {
	FDR_ORG ( "FDR-ORG" ),
	NODO_AUTH_FLUSSI ( "NODO_AUTH_FLUSSI" );

	private final String codice;

	SubscriptionConfigCodeEnum ( String codice ) {
		this.codice = codice;
	}

	public String getCodice () {
		return codice;
	}
}