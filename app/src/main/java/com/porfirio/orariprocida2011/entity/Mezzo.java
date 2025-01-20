package com.porfirio.orariprocida2011.entity;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.time.LocalDate;

public class Mezzo {
    public final String nave;
    public final LocalTime oraPartenza;
    public final LocalTime oraArrivo;
    public final String portoPartenza;
    public final String portoArrivo;
    public final LocalDate inizioEsclusione;
    public final LocalDate fineEsclusione;
    public final String giorniSettimana;
    private final int[] segnalazioni = new int[100];
    public int conferme = 0;
    public int tot = 0;
    public boolean conc = true;
    private boolean giornoSeguente;
    private boolean esclusione;
    private int orderInList;
    private double costoIntero;
    private double costoResidente;
    private boolean circaIntero = false;
    private boolean circaResidente = false;

    public Mezzo(String n, String partenzaIso, String arrivoIso, String esclusioneInizioIso, String esclusioneFineIso, String pp, String pa, String gs) {
        nave = n;
        Arrays.fill(segnalazioni, 0);

        DateTimeFormatter isoDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
        DateTimeFormatter isoTimeFormatter = DateTimeFormatter.ISO_LOCAL_TIME;

        oraPartenza = LocalTime.parse(partenzaIso, isoTimeFormatter);
        oraArrivo = LocalTime.parse(arrivoIso, isoTimeFormatter);

        portoPartenza = pp;
        portoArrivo = pa;

        esclusione = esclusioneInizioIso != null && esclusioneFineIso != null;
        if (esclusione) {
            inizioEsclusione = LocalDate.parse(esclusioneInizioIso, isoDateFormatter);
            fineEsclusione = LocalDate.parse(esclusioneFineIso, isoDateFormatter);
        } else {
            inizioEsclusione = null;
            fineEsclusione = null;
        }

        giorniSettimana = gs;

        if (pp.equals("Procida")) {
            calcolaCosto(n, pa);
        } else {
            calcolaCosto(n, pp);
        }
    }

    public int segnalazionePiuComune() {
        int max = 0;
        int spc = -1;
        for (int i = 0; i < segnalazioni.length; i++) {
            if (segnalazioni[i] > max) {
                max = segnalazioni[i];
                spc = i;
            }
        }
        if (spc >= 0)
            return spc;
        else
            return -1;
    }

    private void calcolaCosto(String n, String p) {
        //TODO Anche questi dati andrebbero messi in un file esterno o su una risorsa web tipo Firebase
        if (n.contentEquals("Traghetto Caremar") && p.contentEquals("Pozzuoli")) {
            costoIntero = 7.90;
            setCircaIntero(true);
            costoResidente = 2.70;
            setCircaResidente(true);
            return;
        }
        if (n.contentEquals("Medmar") && p.contentEquals("Pozzuoli")) {
            costoIntero = 7.90;
            setCircaIntero(true);
            costoResidente = 2.70;
            setCircaResidente(true);
            return;
        }
//		if (n.contentEquals("Procida Lines") && p.contentEquals("Pozzuoli")){
//			costoIntero=5;
//			costoResidente=2.5;
//			setCircaIntero(true);
//			setCircaResidente(true);
//			return;
//		}
        if ((n.contentEquals("Motonave Gestur") || n.contentEquals("Traghetto Gestur")) && p.contentEquals("Pozzuoli")) {
            costoIntero = 7.90;
            setCircaIntero(true);
            costoResidente = 2.70;
            setCircaResidente(true);
            return;
        }
        if (n.contentEquals("Traghetto Caremar") && p.contentEquals("Napoli Porta di Massa")) {
            costoIntero = 10.60;
            costoResidente = 3.10;
            setCircaIntero(true);
            setCircaResidente(true);
            return;
        }
        if (n.contentEquals("Aliscafo Caremar") && p.contentEquals("Napoli Beverello")) {
            costoIntero = 14.40;
            costoResidente = 4.90;
            setCircaIntero(true);
            setCircaResidente(true);
            return;
        }
        if (n.contentEquals("Aliscafo SNAV") && p.contentEquals("Napoli Beverello")) {
            costoIntero = 19.20;
            costoResidente = 5.70;
            setCircaIntero(true);
            setCircaResidente(true);
            return;
        }
        if (n.contentEquals("Traghetto Caremar") && p.contentEquals("Ischia Porto")) {
            costoIntero = 7.80;
            costoResidente = 2.50;
            setCircaIntero(true);
            setCircaResidente(true);
            return;
        }
        if (n.contentEquals("Aliscafo Caremar") && p.contentEquals("Ischia Porto")) {
            costoIntero = 8.70;
            costoResidente = 2.70;
            setCircaIntero(true);
            setCircaResidente(true);
            return;
        }
        if (n.contentEquals("Aliscafo SNAV") && p.contentEquals("Casamicciola")) {
            costoIntero = 9.30;
            costoResidente = 2.60;
            setCircaIntero(true);
            setCircaResidente(true);
            return;
        }
        if (n.contentEquals("Medmar") && p.contentEquals("Ischia Porto")) {
            costoIntero = 7.80;
            costoResidente = 2.70;
            setCircaIntero(true);
            setCircaResidente(true);
            return;
        }
        if (n.contentEquals("Ippocampo")) { //TODO Da verificare
            costoIntero = 7.90;
            setCircaIntero(true);
            costoResidente = 2.50;
            setCircaResidente(true);
            return;
        }
        if (n.contentEquals("Scotto Line")) { //TODO Da verificare
            costoIntero = 7.90;
            setCircaIntero(true);
            costoResidente = 2.50;
            setCircaResidente(true);
            return;
        }
//		if (n.contentEquals("Aladino") ){ //TODO Da verificare
//			costoIntero=0.00;
//			costoResidente=0.00;
//			setCircaIntero(true);
//			setCircaResidente(true);
//			return;
//		}

        if (n.contentEquals("LazioMar")) { //TODO Da verificare
            costoIntero = 7.90;
            setCircaIntero(true);
            costoResidente = 2.60;
            setCircaResidente(true);
        }

    }

    public boolean getGiornoSeguente() {
        return giornoSeguente;
    }

    public void setGiornoSeguente(boolean b) {
        giornoSeguente = b;
    }

    public int getOrderInList() {
        return orderInList;
    }

    public void setOrderInList(int orderInList) {
        this.orderInList = orderInList;
    }

    public double getCostoIntero() {
        return costoIntero;
    }

    public double getCostoResidente() {
        return costoResidente;
    }

    public boolean isCircaIntero() {
        return circaIntero;
    }

    private void setCircaIntero(boolean circaIntero) {
        this.circaIntero = circaIntero;
    }

    public boolean isCircaResidente() {
        return circaResidente;
    }

    private void setCircaResidente(boolean circaResidente) {
        this.circaResidente = circaResidente;
    }

    public void addReason(String rigaMotivo) {
        int motivo = Integer.parseInt(rigaMotivo);
        addReason(motivo);
    }

    public void addReason(int motivo) {
        if (motivo == 99) {
            conferme++;
        } else {
            if (tot > segnalazioni[motivo]) {
                conc = false;
            }
            segnalazioni[motivo]++;
            tot++;
        }
    }

    public LocalTime getDepartureTime() {
        return oraPartenza;
    }

    public LocalTime getArrivalTime() {
        return oraArrivo;
    }

}
