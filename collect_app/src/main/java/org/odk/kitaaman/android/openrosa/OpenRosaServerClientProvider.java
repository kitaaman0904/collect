package org.odk.kitaaman.android.openrosa;

import androidx.annotation.Nullable;

public interface OpenRosaServerClientProvider {

    OpenRosaServerClient get(String schema, String userAgent, @Nullable HttpCredentialsInterface credentialsInterface);
}
