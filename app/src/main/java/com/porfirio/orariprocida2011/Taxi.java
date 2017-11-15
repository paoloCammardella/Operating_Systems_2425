package com.porfirio.orariprocida2011;

public class Taxi {
	private String porto;
	private String compagnia;
	private String numero;
	
	public Taxi(String p, String c, String n){
		setPorto(p);
		setCompagnia(c);
		setNumero(n);
	}

	void setPorto(String porto) {
		this.porto = porto;
	}

	String getPorto() {
		return porto;
	}

	void setCompagnia(String compagnia) {
		this.compagnia = compagnia;
	}

	String getCompagnia() {
		return compagnia;
	}

	void setNumero(String numero) {
		this.numero = numero;
	}

	String getNumero() {
		return numero;
	}
	
}
