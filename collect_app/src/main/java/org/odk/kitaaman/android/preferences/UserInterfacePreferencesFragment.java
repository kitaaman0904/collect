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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.fragment.app.FragmentActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import org.odk.kitaaman.android.R;
import org.odk.kitaaman.android.activities.CollectAbstractActivity;
import org.odk.kitaaman.android.activities.MainMenuActivity;
import org.odk.kitaaman.android.injection.DaggerUtils;
import org.odk.kitaaman.android.utilities.LocaleHelper;
import org.odk.kitaaman.android.utilities.MediaUtils;
import org.odk.kitaaman.android.version.VersionInformation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;

import javax.inject.Inject;

import timber.log.Timber;

import static android.app.Activity.RESULT_CANCELED;
import static org.odk.kitaaman.android.activities.ActivityUtils.startActivityAndCloseAllOthers;
import static org.odk.kitaaman.android.preferences.GeneralKeys.KEY_APP_LANGUAGE;
import static org.odk.kitaaman.android.preferences.GeneralKeys.KEY_APP_THEME;
import static org.odk.kitaaman.android.preferences.GeneralKeys.KEY_FONT_SIZE;
import static org.odk.kitaaman.android.preferences.GeneralKeys.KEY_NAVIGATION;
import static org.odk.kitaaman.android.preferences.GeneralKeys.KEY_SPLASH_PATH;
import static org.odk.kitaaman.android.preferences.PreferencesActivity.INTENT_KEY_ADMIN_MODE;

public class UserInterfacePreferencesFragment extends PreferenceFragmentCompat {

    protected static final int IMAGE_CHOOSER = 0;

    @Inject
    VersionInformation versionInformation;

    public static UserInterfacePreferencesFragment newInstance(boolean adminMode) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(INTENT_KEY_ADMIN_MODE, adminMode);

        UserInterfacePreferencesFragment userInterfacePreferencesFragment = new UserInterfacePreferencesFragment();
        userInterfacePreferencesFragment.setArguments(bundle);

        return userInterfacePreferencesFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        DaggerUtils.getComponent(context).inject(this);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.user_interface_preferences, rootKey);

        FragmentActivity activity = getActivity();
        if (activity instanceof CollectAbstractActivity) {
            ((CollectAbstractActivity) activity).initToolbar(getPreferenceScreen().getTitle());
        }

        initThemePrefs();
        initNavigationPrefs();
        initFontSizePref();
        initLanguagePrefs();
        initSplashPrefs();
    }

    private void initThemePrefs() {
        final ListPreference pref = findPreference(KEY_APP_THEME);

        if (pref != null) {
            if (versionInformation.isRelease()) {
                hideExperimentalThemes(pref);
            }

            pref.setSummary(pref.getEntry());
            pref.setOnPreferenceChangeListener((preference, newValue) -> {
                int index = ((ListPreference) preference).findIndexOfValue(newValue.toString());
                String entry = (String) ((ListPreference) preference).getEntries()[index];
                if (pref.getEntry() == null || !pref.getEntry().equals(entry)) {
                    preference.setSummary(entry);
                    startActivityAndCloseAllOthers(getActivity(), MainMenuActivity.class);
                }
                return true;
            });
        }
    }

    private void hideExperimentalThemes(ListPreference pref) {
        CharSequence[] entries = pref.getEntries();
        pref.setEntries(Arrays.copyOfRange(entries, 0, entries.length - 1));

        CharSequence[] entryValues = pref.getEntryValues();
        pref.setEntryValues(Arrays.copyOfRange(entryValues, 0, entryValues.length - 1));
    }

    private void initNavigationPrefs() {
        final ListPreference pref = findPreference(KEY_NAVIGATION);

        if (pref != null) {
            pref.setSummary(pref.getEntry());
            pref.setOnPreferenceChangeListener((preference, newValue) -> {
                int index = ((ListPreference) preference).findIndexOfValue(newValue.toString());
                String entry = (String) ((ListPreference) preference).getEntries()[index];
                preference.setSummary(entry);
                return true;
            });
        }
    }

    private void initFontSizePref() {
        final ListPreference pref = findPreference(KEY_FONT_SIZE);

        if (pref != null) {
            pref.setSummary(pref.getEntry());
            pref.setOnPreferenceChangeListener((preference, newValue) -> {
                int index = ((ListPreference) preference).findIndexOfValue(newValue.toString());
                CharSequence entry = ((ListPreference) preference).getEntries()[index];
                preference.setSummary(entry);
                return true;
            });
        }
    }

    private void initLanguagePrefs() {
        final ListPreference pref = findPreference(KEY_APP_LANGUAGE);

        if (pref != null) {
            final LocaleHelper localeHelper = new LocaleHelper();
            TreeMap<String, String> languageList = localeHelper.getEntryListValues();
            int length = languageList.size() + 1;
            ArrayList<String> entryValues = new ArrayList<>();
            entryValues.add(0, "");
            entryValues.addAll(languageList.values());
            pref.setEntryValues(entryValues.toArray(new String[length]));
            ArrayList<String> entries = new ArrayList<>();
            entries.add(0, getActivity().getResources()
                    .getString(R.string.use_device_language));
            entries.addAll(languageList.keySet());
            pref.setEntries(entries.toArray(new String[length]));
            if (pref.getValue() == null) {
                //set Default value to "Use phone locale"
                pref.setValueIndex(0);
            }
            pref.setSummary(pref.getEntry());
            pref.setOnPreferenceChangeListener((preference, newValue) -> {
                int index = ((ListPreference) preference).findIndexOfValue(newValue.toString());
                String entry = (String) ((ListPreference) preference).getEntries()[index];
                preference.setSummary(entry);

                SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                edit.putString(KEY_APP_LANGUAGE, newValue.toString());
                edit.apply();

                localeHelper.updateLocale(getActivity());
                startActivityAndCloseAllOthers(getActivity(), MainMenuActivity.class);
                return true;
            });
        }
    }

    private void initSplashPrefs() {
        final Preference pref = findPreference(KEY_SPLASH_PATH);

        if (pref != null) {
            pref.setOnPreferenceClickListener(new SplashClickListener(this, pref));
            pref.setSummary((String) GeneralSharedPreferences.getInstance().get(KEY_SPLASH_PATH));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Timber.d("onActivityResult %d %d", requestCode, resultCode);
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_CANCELED) {
            // request was canceled, so do nothing
            return;
        }

        switch (requestCode) {
            case IMAGE_CHOOSER:
                // get gp of chosen file
                Uri selectedMedia = intent.getData();
                String sourceMediaPath = MediaUtils.getPathFromUri(getActivity(), selectedMedia,
                        MediaStore.Images.Media.DATA);

                // setting image path
                setSplashPath(sourceMediaPath);

                break;
        }
    }

    void setSplashPath(String path) {
        GeneralSharedPreferences.getInstance().save(KEY_SPLASH_PATH, path);
        Preference splashPathPreference = findPreference(KEY_SPLASH_PATH);
        splashPathPreference.setSummary(path);
    }
}
