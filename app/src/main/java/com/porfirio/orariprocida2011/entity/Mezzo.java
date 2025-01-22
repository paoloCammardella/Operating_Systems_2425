package com.porfirio.orariprocida2011.entity;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Objects;

public class Mezzo {
    //gestite nel dettaglio segnalazioni per tipologia e motivi
    public final String nave;

    public final String portoPartenza;
    public final String portoArrivo;


    public int conferme = 0;
    public int tot = 0;
    public boolean conc = true;
    private boolean giornoSeguente;
    private int orderInList;

    private double costoIntero;
    private double costoResidente;
    private boolean circaIntero = false;
    private boolean circaResidente = false;

    private final LocalTime departureTime, arrivalTime;
    private final LocalDate exclusionStart, exclusionEnd;
    private final byte activeDays;
    private final float fullPrice, reducedPrice;

    private final int[] reports = new int[32];

    public Mezzo(String transport, String departureLocation, LocalTime departureTime, String arrivalLocation, LocalTime arrivalTime, LocalDate exclusionStart, LocalDate exclusionEnd, byte activeDays, float fullPrice, float reducedPrice) {
        this.nave = Objects.requireNonNull(transport);
        this.portoPartenza = Objects.requireNonNull(departureLocation);
        this.portoArrivo = Objects.requireNonNull(arrivalLocation);
        this.departureTime = Objects.requireNonNull(departureTime);
        this.arrivalTime = Objects.requireNonNull(arrivalTime);
        this.exclusionStart = exclusionStart;
        this.exclusionEnd = exclusionEnd;
        this.activeDays = activeDays;
        this.fullPrice = fullPrice;
        this.reducedPrice = reducedPrice;

        Arrays.fill(reports, 0);

        if (departureLocation.contentEquals("Procida"))
            calcolaCosto(transport, arrivalLocation);
        else
            calcolaCosto(transport, departureLocation);
    }

    public int segnalazionePiuComune() {
        int max = 0;
        int spc = -1;
        for (int i = 0; i < reports.length; i++) {
            if (reports[i] > max) {
                max = reports[i];
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

    public double getFullPrice() {
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

    public void addReason(int reason) {
        if (reason == 99) {
            conferme++;
        } else {
            if (tot > reports[reason])
                conc = false;
            reports[reason]++;
            tot++;
        }
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public boolean isActiveOnDay(LocalDate date) {
        return isActiveOnDay(date.getDayOfWeek());
    }

    public boolean isActiveOnDay(DayOfWeek day) {
        // The n-th bit represents whether or not the route is active in that day, where the first day (monday) is the the second bit

        // e.g.
        // A route active on monday, tuesday and friday is represented as 00100110
        // tuesday = 2, so 00100110 & 00000100 = 00001000 != 0 so it is active on tuesday

        return (activeDays & (1 << day.getValue())) != 0;
    }

    public boolean isDateInExclusion(LocalDate date) {
        return (exclusionStart != null && exclusionStart.isAfter(date)) || (exclusionEnd != null && exclusionEnd.isBefore(date));
    }

}
