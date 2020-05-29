package org.odk.kitaaman.android.regression;

import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.odk.kitaaman.android.activities.MainMenuActivity;

public class BaseRegressionTest {

    @Rule
    public ActivityTestRule<MainMenuActivity> rule = new ActivityTestRule<>(MainMenuActivity.class);
}