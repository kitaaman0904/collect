package org.odk.kitaaman.android.feature.externalintents;

import androidx.test.filters.Suppress;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.kitaaman.android.activities.FormEntryActivity;

import java.io.IOException;

import static org.odk.kitaaman.android.feature.externalintents.ExportedActivitiesUtils.testDirectories;

@Suppress
// Frequent failures: https://github.com/opendatakit/collect/issues/796
@RunWith(AndroidJUnit4.class)
public class FormEntryActivityTest {

    @Rule
    public ActivityTestRule<FormEntryActivity> formEntryActivityRule =
            new ExportedActivityTestRule<>(FormEntryActivity.class);

    @Test
    public void formEntryActivityMakesDirsTest() throws IOException {
        testDirectories();
    }

}
