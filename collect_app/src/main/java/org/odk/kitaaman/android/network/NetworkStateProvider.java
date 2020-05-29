package org.odk.kitaaman.android.network;

import android.net.NetworkInfo;

public interface NetworkStateProvider {
    boolean isDeviceOnline();

    NetworkInfo getNetworkInfo();
}
