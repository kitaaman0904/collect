package org.odk.kitaaman.android.formentry;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import org.odk.kitaaman.android.R;
import org.odk.kitaaman.android.formentry.backgroundlocation.BackgroundLocationViewModel;
import org.odk.kitaaman.android.formentry.questions.AnswersProvider;
import org.odk.kitaaman.android.formentry.saving.FormSaveViewModel;
import org.odk.kitaaman.android.javarosawrapper.FormController;
import org.odk.kitaaman.android.preferences.AdminKeys;
import org.odk.kitaaman.android.preferences.AdminSharedPreferences;
import org.odk.kitaaman.android.preferences.GeneralSharedPreferences;
import org.odk.kitaaman.android.preferences.PreferencesActivity;
import org.odk.kitaaman.android.utilities.MenuDelegate;
import org.odk.kitaaman.android.utilities.PlayServicesUtil;

import static org.odk.kitaaman.android.preferences.GeneralKeys.KEY_BACKGROUND_LOCATION;

public class FormEntryMenuDelegate implements MenuDelegate, RequiresFormController {

    private final AppCompatActivity activity;
    private final AnswersProvider answersProvider;
    private final FormIndexAnimationHandler formIndexAnimationHandler;

    @Nullable
    private FormController formController;

    public FormEntryMenuDelegate(AppCompatActivity activity, AnswersProvider answersProvider, FormIndexAnimationHandler formIndexAnimationHandler) {
        this.activity = activity;
        this.answersProvider = answersProvider;
        this.formIndexAnimationHandler = formIndexAnimationHandler;
    }

    @Override
    public void formLoaded(FormController formController) {
        this.formController = formController;
    }

    @Override
    public void onCreateOptionsMenu(MenuInflater menuInflater, Menu menu) {
        menuInflater.inflate(R.menu.form_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        boolean useability;

        useability = (boolean) AdminSharedPreferences.getInstance().get(AdminKeys.KEY_SAVE_MID);

        menu.findItem(R.id.menu_save).setVisible(useability).setEnabled(useability);

        useability = (boolean) AdminSharedPreferences.getInstance().get(AdminKeys.KEY_JUMP_TO);

        menu.findItem(R.id.menu_goto).setVisible(useability)
                .setEnabled(useability);

        useability = (boolean) AdminSharedPreferences.getInstance().get(AdminKeys.KEY_CHANGE_LANGUAGE)
                && (formController != null)
                && formController.getLanguages() != null
                && formController.getLanguages().length > 1;

        menu.findItem(R.id.menu_languages).setVisible(useability)
                .setEnabled(useability);

        useability = (boolean) AdminSharedPreferences.getInstance().get(AdminKeys.KEY_ACCESS_SETTINGS);

        menu.findItem(R.id.menu_preferences).setVisible(useability)
                .setEnabled(useability);

        if (formController != null && formController.currentFormCollectsBackgroundLocation()
                && PlayServicesUtil.isGooglePlayServicesAvailable(activity)) {
            MenuItem backgroundLocation = menu.findItem(R.id.track_location);
            backgroundLocation.setVisible(true);
            backgroundLocation.setChecked(GeneralSharedPreferences.getInstance().getBoolean(KEY_BACKGROUND_LOCATION, true));
        }

        menu.findItem(R.id.menu_add_repeat).setVisible(getFormEntryViewModel().canAddRepeat());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_repeat:
                getFormSaveViewModel().saveAnswersForScreen(answersProvider.getAnswers());
                getFormEntryViewModel().promptForNewRepeat();
                formIndexAnimationHandler.handle(getFormEntryViewModel().getCurrentIndex());
                return true;

            case R.id.menu_preferences:
                Intent pref = new Intent(activity, PreferencesActivity.class);
                activity.startActivity(pref);
                return true;

            case R.id.track_location:
                getBackgroundLocationViewModel().backgroundLocationPreferenceToggled();
                return true;
        }

        return false;
    }

    @Override
    public void invalidateOptionsMenu() {
        activity.invalidateOptionsMenu();
    }

    private FormEntryViewModel getFormEntryViewModel() {
        return ViewModelProviders.of(activity).get(FormEntryViewModel.class);
    }

    private FormSaveViewModel getFormSaveViewModel() {
        return ViewModelProviders.of(activity).get(FormSaveViewModel.class);
    }

    private BackgroundLocationViewModel getBackgroundLocationViewModel() {
        return ViewModelProviders.of(activity).get(BackgroundLocationViewModel.class);
    }
}
