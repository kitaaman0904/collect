package org.odk.kitaaman.android.support;

import androidx.test.rule.ActivityTestRule;

import org.odk.kitaaman.android.activities.MainMenuActivity;
import org.odk.kitaaman.android.support.pages.MainMenuPage;

public class FeatureTestRule extends ActivityTestRule<MainMenuActivity> {

    public FeatureTestRule() {
        super(MainMenuActivity.class);
    }

    public MainMenuPage mainMenu() {
        return new MainMenuPage(this).assertOnPage();
    }
}
