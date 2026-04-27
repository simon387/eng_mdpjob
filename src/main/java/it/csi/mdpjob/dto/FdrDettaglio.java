/*
 * SPDX-FileCopyrightText: (C) Copyright 2023 Regione Piemonte
 *
 * SPDX-License-Identifier: EUPL-1.2 */

package it.csi.mdpjob.dto;

import com.fasterxml.jackson.databind.JsonNode;


public class FdrDettaglio {

	private String status;

	private Integer revision;

	private String fdr;

	private Integer totPayments;

	private Double sumPayments;

	private Integer computedTotPayments;

	private Double computedSumPayments;

	private JsonNode rawJson; // intera response grezza per costruire jsonflusso

	public String getStatus () {
		return status;
	}

	public void setStatus ( String status ) {
		this.status = status;
	}

	public Integer getRevision () {
		return revision;
	}

	public void setRevision ( Integer revision ) {
		this.revision = revision;
	}

	public String getFdr () {
		return fdr;
	}

	public void setFdr ( String fdr ) {
		this.fdr = fdr;
	}

	public Integer getTotPayments () {
		return totPayments;
	}

	public void setTotPayments ( Integer totPayments ) {
		this.totPayments = totPayments;
	}

	public Double getSumPayments () {
		return sumPayments;
	}

	public void setSumPayments ( Double sumPayments ) {
		this.sumPayments = sumPayments;
	}

	public Integer getComputedTotPayments () {
		return computedTotPayments;
	}

	public void setComputedTotPayments ( Integer computedTotPayments ) {
		this.computedTotPayments = computedTotPayments;
	}

	public Double getComputedSumPayments () {
		return computedSumPayments;
	}

	public void setComputedSumPayments ( Double computedSumPayments ) {
		this.computedSumPayments = computedSumPayments;
	}

	public JsonNode getRawJson () {
		return rawJson;
	}

	public void setRawJson ( JsonNode rawJson ) {
		this.rawJson = rawJson;
	}
}
