/*
 * SPDX-FileCopyrightText: (C) Copyright 2023 Regione Piemonte
 *
 * SPDX-License-Identifier: EUPL-1.2 */

package it.csi.mdpjob.util.mail;

public class Attached {

	private String name;

	private byte[] data;

	private MimeType type;

	public String getName () {
		return name;
	}

	public void setName ( String name ) {
		this.name = name;
	}

	public byte[] getData () {
		return data;
	}

	public void setData ( byte[] data ) {
		this.data = data;
	}

	public String getType () {
		return type.getType ();
	}

	public void setType ( MimeType type ) {
		this.type = type;
	}
}
