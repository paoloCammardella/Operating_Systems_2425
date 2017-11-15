package com.porfirio.orariprocida2011;

import java.util.ArrayList;

public class Compagnia {
	public String nome;
	public ArrayList<String> nomeNumeroTelefono;
	public ArrayList<String> numeroTelefono;
	
	
	public Compagnia(String n){
		nome=n;
		nomeNumeroTelefono=new ArrayList<String>();
		numeroTelefono=new ArrayList<String>();
	}
	
	public void addTelefono(String nome,String numero){
		nomeNumeroTelefono.add(nome);
		numeroTelefono.add(numero);
	}
	
}
