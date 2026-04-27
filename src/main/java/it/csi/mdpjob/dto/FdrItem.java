/*
 * SPDX-FileCopyrightText: (C) Copyright 2023 Regione Piemonte
 *
 * SPDX-License-Identifier: EUPL-1.2 */

package it.csi.mdpjob.dto;

public class FdrItem {

	private String fdr;

	private String pspId;

	private Integer revision;

	private String published;

	private String flowDate;

	public String getFdr () {
		return fdr;
	}

	public void setFdr ( String fdr ) {
		this.fdr = fdr;
	}

	public String getPspId () {
		return pspId;
	}

	public void setPspId ( String pspId ) {
		this.pspId = pspId;
	}

	public Integer getRevision () {
		return revision;
	}

	public void setRevision ( Integer revision ) {
		this.revision = revision;
	}

	public String getPublished () {
		return published;
	}

	public void setPublished ( String published ) {
		this.published = published;
	}

	public String getFlowDate () {
		return flowDate;
	}

	public void setFlowDate ( String flowDate ) {
		this.flowDate = flowDate;
	}
}
