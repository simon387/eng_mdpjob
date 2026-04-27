/*
 * SPDX-FileCopyrightText: (C) Copyright 2023 Regione Piemonte
 *
 * SPDX-License-Identifier: EUPL-1.2 */

package it.csi.mdpjob.dto;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Timestamp;


public class SubscriptionConfig implements Serializable {

	@Serial
	private static final long serialVersionUID = -3726560037565831717L;

	private long id;

	private String codice;

	private String url;

	private String descrizione;

	private String keyPrimaria;

	private String keySecondaria;

	private Timestamp dataInizioValidita;

	private Timestamp dataFineValidita;

	private String utenteInserimento;

	private String utenteModifica;

	private Timestamp dataInserimento;

	private Timestamp dataModifica;

	public long getId () {
		return id;
	}

	public void setId ( long id ) {
		this.id = id;
	}

	public String getCodice () {
		return codice;
	}

	public void setCodice ( String codice ) {
		this.codice = codice;
	}

	public String getUrl () {
		return url;
	}

	public void setUrl ( String url ) {
		this.url = url;
	}

	public String getDescrizione () {
		return descrizione;
	}

	public void setDescrizione ( String descrizione ) {
		this.descrizione = descrizione;
	}

	public String getKeyPrimaria () {
		return keyPrimaria;
	}

	public void setKeyPrimaria ( String keyPrimaria ) {
		this.keyPrimaria = keyPrimaria;
	}

	public String getKeySecondaria () {
		return keySecondaria;
	}

	public void setKeySecondaria ( String keySecondaria ) {
		this.keySecondaria = keySecondaria;
	}

	public Timestamp getDataInizioValidita () {
		return dataInizioValidita;
	}

	public void setDataInizioValidita ( Timestamp dataInizioValidita ) {
		this.dataInizioValidita = dataInizioValidita;
	}

	public Timestamp getDataFineValidita () {
		return dataFineValidita;
	}

	public void setDataFineValidita ( Timestamp dataFineValidita ) {
		this.dataFineValidita = dataFineValidita;
	}

	public String getUtenteInserimento () {
		return utenteInserimento;
	}

	public void setUtenteInserimento ( String utenteInserimento ) {
		this.utenteInserimento = utenteInserimento;
	}

	public String getUtenteModifica () {
		return utenteModifica;
	}

	public void setUtenteModifica ( String utenteModifica ) {
		this.utenteModifica = utenteModifica;
	}

	public Timestamp getDataInserimento () {
		return dataInserimento;
	}

	public void setDataInserimento ( Timestamp dataInserimento ) {
		this.dataInserimento = dataInserimento;
	}

	public Timestamp getDataModifica () {
		return dataModifica;
	}

	public void setDataModifica ( Timestamp dataModifica ) {
		this.dataModifica = dataModifica;
	}
}
