package org.odk.kitaaman.android.formentry.saving;

import android.net.Uri;

import org.odk.kitaaman.android.analytics.Analytics;
import org.odk.kitaaman.android.javarosawrapper.FormController;
import org.odk.kitaaman.android.tasks.SaveToDiskResult;

public interface FormSaver {
    SaveToDiskResult save(Uri instanceContentURI, FormController formController, boolean shouldFinalize, boolean exitAfter, String updatedSaveName, ProgressListener progressListener, Analytics analytics);

    interface ProgressListener {
        void onProgressUpdate(String message);
    }
}
