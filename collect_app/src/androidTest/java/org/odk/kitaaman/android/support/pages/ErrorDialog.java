package org.odk.kitaaman.android.support.pages;

import androidx.test.rule.ActivityTestRule;

import org.odk.kitaaman.android.R;

public class ErrorDialog extends OkDialog {
    ErrorDialog(ActivityTestRule rule) {
        super(rule);
    }

    @Override
    public ErrorDialog assertOnPage() {
        super.assertOnPage();
        checkIsStringDisplayed(R.string.error_occured);
        return this;
    }
}
