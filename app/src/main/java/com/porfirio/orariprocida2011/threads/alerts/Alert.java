package com.porfirio.orariprocida2011.threads.alerts;

import java.time.LocalDate;

/**
 * Object representing an user-sent alert regarding one of the routes.
 */
public class Alert {

    /**
     * Code representing a confirmation.
     */
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

    /**
     * Returns the id of the alert.
     *
     * @return id of the alert
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the id of the route this alert refers to.
     *
     * @return id of the alert route
     */
    public String getRouteId() {
        return routeId;
    }

    /**
     * Returns the reason of the alert.
     *
     * @return reason of the alert
     */
    public int getReason() {
        return reason;
    }

    /**
     * Returns the additional details of the alert, if present.
     *
     * @return additional details of the alert
     */
    public String getDetails() {
        return details;
    }

    /**
     * Returns the date of the route this alert refers to.
     *
     * @return date of the alert route
     */
    public LocalDate getTransportDate() {
        return transportDate;
    }

}
