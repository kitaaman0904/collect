package org.odk.kitaaman.android.utilities;

import org.odk.kitaaman.android.preferences.AdminSharedPreferences;

import static org.odk.kitaaman.android.preferences.AdminKeys.KEY_ADMIN_PW;

public class AdminPasswordProvider {
    private final AdminSharedPreferences adminSharedPreferences;

    public AdminPasswordProvider(AdminSharedPreferences adminSharedPreferences) {
        this.adminSharedPreferences = adminSharedPreferences;
    }

    public boolean isAdminPasswordSet() {
        String adminPassword = getAdminPassword();
        return adminPassword != null && !adminPassword.isEmpty();
    }

    public String getAdminPassword() {
        return (String) adminSharedPreferences.get(KEY_ADMIN_PW);
    }
}
