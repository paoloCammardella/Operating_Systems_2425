package com.porfirio.orariprocida2011.entity;

import java.util.ArrayList;

public class Compagnia {

    public final String nome;
    public final ArrayList<String> nomeNumeroTelefono;
    public final ArrayList<String> numeroTelefono;


    public Compagnia(String n) {
        nome = n;
        nomeNumeroTelefono = new ArrayList<>();
        numeroTelefono = new ArrayList<>();
    }

    public void addTelefono(String nome, String numero) {
        nomeNumeroTelefono.add(nome);
        numeroTelefono.add(numero);
    }

}
