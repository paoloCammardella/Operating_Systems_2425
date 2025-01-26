package com.porfirio.orariprocida2011.threads.alerts;

import java.time.LocalDate;

public class Alert {

    public static final int REASON_NO_PROBLEM = 99;

    private final String id;
    private final String routeId;
    private final int reason;
    private final String details;
    private final LocalDate transportDate;

    public Alert(String routeId, int reason, String details, LocalDate transportDate) {
        this(null, routeId, reason, details, transportDate);
    }

    public Alert(String id, String routeId, int reason, String details, LocalDate transportDate) {
        this.id = id;
        this.routeId = routeId;
        this.reason = reason;
        this.details = details;
        this.transportDate = transportDate;
    }

    public String getId() {
        return id;
    }

    public String getRouteId() {
        return routeId;
    }

    public int getReason() {
        return reason;
    }

    public String getDetails() {
        return details;
    }

    public LocalDate getTransportDate() {
        return transportDate;
    }

}
