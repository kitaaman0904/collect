package org.odk.kitaaman.android.feature.formentry.backgroundlocation;

import android.Manifest;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.rule.GrantPermissionRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.kitaaman.android.R;
import org.odk.kitaaman.android.activities.FormEntryActivity;
import org.odk.kitaaman.android.preferences.GeneralSharedPreferences;
import org.odk.kitaaman.android.support.CopyFormRule;
import org.odk.kitaaman.android.support.ResetStateRule;
import org.odk.kitaaman.android.support.FormLoadingUtils;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class SetGeopointActionTest {
    private static final String SETGEOPOINT_ACTION_FORM = "setgeopoint-action.xml";

    @Rule
    public IntentsTestRule<FormEntryActivity> activityTestRule = FormLoadingUtils.getFormActivityTestRuleFor(SETGEOPOINT_ACTION_FORM);

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_FINE_LOCATION)
            )
            .around(new ResetStateRule())
            .around(new CopyFormRule(SETGEOPOINT_ACTION_FORM, true));

    @Before
    @After
    public void resetGeneralPreferences() {
        GeneralSharedPreferences.getInstance().loadDefaultPreferences();
    }

    @Test
    public void locationCollectionSnackbar_ShouldBeDisplayedAtFormLaunch() {
        onView(withId(com.google.android.material.R.id.snackbar_text))
                .check(matches(withText(String.format(ApplicationProvider.getApplicationContext().getString(R.string.background_location_enabled), "⋮"))));
    }

    @Test
    public void locationCollectionToggle_ShouldBeAvailable() {
        openActionBarOverflowOrOptionsMenu(activityTestRule.getActivity());
        onView(withText(R.string.track_location)).check(matches(isDisplayed()));
    }
}
