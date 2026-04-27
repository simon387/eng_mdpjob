/*
 * SPDX-FileCopyrightText: (C) Copyright 2023 Regione Piemonte
 *
 * SPDX-License-Identifier: EUPL-1.2 */

package it.csi.mdpjob.dto;

public class ConfigParam {

	private String organizationId;  // organizationID.flusso110

	private String pspId;           // pspID.flusso110

	private String publishedGt;     // publishedGt.flusso110

	private String flowDate;        // flowDate.flusso110

	private Integer numGiorniPublished; // numGiorniPublished.flusso110

	public String getOrganizationId () {
		return organizationId;
	}

	public void setOrganizationId ( String organizationId ) {
		this.organizationId = organizationId;
	}

	public String getPspId () {
		return pspId;
	}

	public void setPspId ( String pspId ) {
		this.pspId = pspId;
	}

	public String getPublishedGt () {
		return publishedGt;
	}

	public void setPublishedGt ( String publishedGt ) {
		this.publishedGt = publishedGt;
	}

	public String getFlowDate () {
		return flowDate;
	}

	public void setFlowDate ( String flowDate ) {
		this.flowDate = flowDate;
	}

	public Integer getNumGiorniPublished () {
		return numGiorniPublished;
	}

	public void setNumGiorniPublished ( Integer numGiorniPublished ) {
		this.numGiorniPublished = numGiorniPublished;
	}
}