package org.odk.kitaaman.android.formentry.saving;

import android.net.Uri;

import org.odk.kitaaman.android.analytics.Analytics;
import org.odk.kitaaman.android.javarosawrapper.FormController;
import org.odk.kitaaman.android.tasks.SaveFormToDisk;
import org.odk.kitaaman.android.tasks.SaveToDiskResult;

public class DiskFormSaver implements FormSaver {

    @Override
    public SaveToDiskResult save(Uri instanceContentURI, FormController formController, boolean shouldFinalize, boolean exitAfter, String updatedSaveName, ProgressListener progressListener, Analytics analytics) {
        SaveFormToDisk saveFormToDisk = new SaveFormToDisk(formController, exitAfter, shouldFinalize, updatedSaveName, instanceContentURI, analytics);
        return saveFormToDisk.saveForm(progressListener);
    }
}
