package org.odk.kitaaman.android.preferences.utilities;

import android.app.Activity;

import org.odk.kitaaman.android.R;
import org.odk.kitaaman.android.activities.MainMenuActivity;
import org.odk.kitaaman.android.application.Collect;
import org.odk.kitaaman.android.listeners.ActionListener;
import org.odk.kitaaman.android.preferences.AdminSharedPreferences;
import org.odk.kitaaman.android.preferences.GeneralSharedPreferences;
import org.odk.kitaaman.android.preferences.PreferenceSaver;
import org.odk.kitaaman.android.utilities.LocaleHelper;
import org.odk.kitaaman.android.utilities.ToastUtils;

import timber.log.Timber;

import static org.odk.kitaaman.android.activities.ActivityUtils.startActivityAndCloseAllOthers;

public class SettingsUtils {

    private SettingsUtils() {
    }

    public static void applySettings(Activity activity, String content) {
        new PreferenceSaver(GeneralSharedPreferences.getInstance(), AdminSharedPreferences.getInstance()).fromJSON(content, new ActionListener() {
            @Override
            public void onSuccess() {
                Collect.getInstance().initializeJavaRosa();
                ToastUtils.showLongToast(Collect.getInstance().getString(R.string.successfully_imported_settings));
                final LocaleHelper localeHelper = new LocaleHelper();
                localeHelper.updateLocale(activity);
                startActivityAndCloseAllOthers(activity, MainMenuActivity.class);
            }

            @Override
            public void onFailure(Exception exception) {
                if (exception instanceof GeneralSharedPreferences.ValidationException) {
                    ToastUtils.showLongToast(Collect.getInstance().getString(R.string.invalid_qrcode));
                } else {
                    Timber.e(exception);
                }
            }
        });
    }
}
