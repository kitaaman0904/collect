package org.odk.kitaaman.android.analytics;

public interface Analytics {

    void logEvent(String category, String action);

    void logEvent(String category, String action, String label);

    void setAnalyticsCollectionEnabled(boolean isAnalyticsEnabled);
}