package com.porfirio.orariprocida2011.threads.alerts;

import java.time.LocalDate;
import java.time.LocalTime;

public class Alert {

    public static final int REASON_NO_PROBLEM = 99;

    private final String id;
    private final String routeId;
    private final String transport;
    private final int reason;
    private final String details;
    private final String departureLocation;
    private final LocalTime departureTime;
    private final String arrivalLocation;
    private final LocalTime arrivalTime;
    private final LocalDate transportDate;

    public Alert(String routeId, String transport, int reason, String details, String departureLocation, LocalTime departureTime, String arrivalLocation, LocalTime arrivalTime, LocalDate transportDate) {
        this(null, routeId, transport, reason, details, departureLocation, departureTime, arrivalLocation, arrivalTime, transportDate);
    }

    public Alert(String id, String routeId, String transport, int reason, String details, String departureLocation, LocalTime departureTime, String arrivalLocation, LocalTime arrivalTime, LocalDate transportDate) {
        this.id = id;
        this.routeId = routeId;
        this.transport = transport;
        this.reason = reason;
        this.details = details;
        this.departureTime = departureTime;
        this.departureLocation = departureLocation;
        this.arrivalTime = arrivalTime;
        this.arrivalLocation = arrivalLocation;
        this.transportDate = transportDate;
    }

    public String getId() {
        return id;
    }

    public String getRouteId() {
        return routeId;
    }

    public String getTransport() {
        return transport;
    }

    public int getReason() {
        return reason;
    }

    public String getDetails() {
        return details;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public String getDepartureLocation() {
        return departureLocation;
    }

    public String getArrivalLocation() {
        return arrivalLocation;
    }

    public LocalDate getTransportDate() {
        return transportDate;
    }

}
