package org.odk.kitaaman.android.support.pages;

import androidx.test.rule.ActivityTestRule;

import org.odk.kitaaman.android.R;

public class FormMapPage extends Page<FormMapPage> {
    FormMapPage(ActivityTestRule rule) {
        super(rule);
    }

    @Override
    public FormMapPage assertOnPage() {
        return checkIsIdDisplayed(R.id.geometry_status);
    }

    public FormEntryPage clickFillBlankFormButton(String formName) {
        clickOnId(R.id.new_instance);
        return new FormEntryPage(formName, rule).assertOnPage();
    }
}
