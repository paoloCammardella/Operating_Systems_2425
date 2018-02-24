package com.porfirio.orariprocida2011.entity;

public class Taxi {
	private String porto;
	private String compagnia;
	private String numero;
	
	public Taxi(String p, String c, String n){
		setPorto(p);
		setCompagnia(c);
		setNumero(n);
	}

	public String getPorto() {
		return porto;
	}

    private void setPorto(String porto) {
        this.porto = porto;
	}

	public String getCompagnia() {
		return compagnia;
	}

    private void setCompagnia(String compagnia) {
        this.compagnia = compagnia;
	}

	public String getNumero() {
		return numero;
	}

    private void setNumero(String numero) {
        this.numero = numero;
	}
	
}
