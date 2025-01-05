package com.porfirio.orariprocida2011.utils;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class Analytics {

    private final Tracker tracker;

    public Analytics(AnalyticsApplication analyticsApplication) {
        this.tracker = analyticsApplication.getDefaultTracker();
        this.tracker.enableAdvertisingIdCollection(true);
    }

    public void send(String category, String action) {
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .build());
    }

}
