/*
 * Copyright (C) 2017 Shobhit
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.kitaaman.android.preferences;

import android.os.Bundle;
import android.preference.CheckBoxPreference;

import org.odk.kitaaman.android.R;
import org.odk.kitaaman.android.analytics.Analytics;
import org.odk.kitaaman.android.application.Collect;
import org.odk.kitaaman.android.utilities.MultiClickGuard;

import javax.inject.Inject;

import static org.odk.kitaaman.android.preferences.GeneralKeys.KEY_ANALYTICS;
import static org.odk.kitaaman.android.preferences.PreferencesActivity.INTENT_KEY_ADMIN_MODE;

public class IdentityPreferences extends BasePreferenceFragment {

    @Inject
    Analytics analytics;

    public static IdentityPreferences newInstance(boolean adminMode) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(INTENT_KEY_ADMIN_MODE, adminMode);

        IdentityPreferences identityPreferences = new IdentityPreferences();
        identityPreferences.setArguments(bundle);

        return identityPreferences;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.identity_preferences);
        Collect.getInstance().getComponent().inject(this);

        findPreference("form_metadata").setOnPreferenceClickListener(preference -> {
            if (MultiClickGuard.allowClick(getClass().getName())) {
                AndroidXPreferencesActivity.start(getActivity(), FormMetadataFragment.class);
                return true;
            }
            return false;
        });

        initAnalyticsPref();
    }

    private void initAnalyticsPref() {
        final CheckBoxPreference analyticsPreference = (CheckBoxPreference) findPreference(KEY_ANALYTICS);

        if (analyticsPreference != null) {
            analyticsPreference.setOnPreferenceClickListener(preference -> {
                analytics.setAnalyticsCollectionEnabled(analyticsPreference.isChecked());
                return true;
            });
        }
    }
}
