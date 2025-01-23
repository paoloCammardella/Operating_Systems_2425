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

    public float getFullPrice() {
        return fullPrice;
    }

    public float getCostoResidente() {
        return reducedPrice;
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
