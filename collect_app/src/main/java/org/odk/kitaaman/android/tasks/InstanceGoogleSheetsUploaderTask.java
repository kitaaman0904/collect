/*
 * Copyright (C) 2018 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.kitaaman.android.tasks;

import android.database.Cursor;

import org.odk.kitaaman.android.R;
import org.odk.kitaaman.android.analytics.Analytics;
import org.odk.kitaaman.android.application.Collect;
import org.odk.kitaaman.android.dao.FormsDao;
import org.odk.kitaaman.android.forms.Form;
import org.odk.kitaaman.android.instances.Instance;
import org.odk.kitaaman.android.upload.InstanceGoogleSheetsUploader;
import org.odk.kitaaman.android.upload.UploadException;
import org.odk.kitaaman.android.utilities.InstanceUploaderUtils;
import org.odk.kitaaman.android.utilities.gdrive.GoogleAccountsManager;

import java.util.List;

import timber.log.Timber;

import static org.odk.kitaaman.android.analytics.AnalyticsEvents.SUBMISSION;
import static org.odk.kitaaman.android.utilities.InstanceUploaderUtils.DEFAULT_SUCCESSFUL_TEXT;
import static org.odk.kitaaman.android.utilities.InstanceUploaderUtils.SPREADSHEET_UPLOADED_TO_GOOGLE_DRIVE;

public class InstanceGoogleSheetsUploaderTask extends InstanceUploaderTask {
    private final GoogleAccountsManager accountsManager;
    private final Analytics analytics;

    public InstanceGoogleSheetsUploaderTask(GoogleAccountsManager accountsManager, Analytics analytics) {
        this.accountsManager = accountsManager;
        this.analytics = analytics;
    }

    @Override
    protected Outcome doInBackground(Long... instanceIdsToUpload) {
        InstanceGoogleSheetsUploader uploader = new InstanceGoogleSheetsUploader(accountsManager);
        final Outcome outcome = new Outcome();

        List<Instance> instancesToUpload = uploader.getInstancesFromIds(instanceIdsToUpload);

        for (int i = 0; i < instancesToUpload.size(); i++) {
            Instance instance = instancesToUpload.get(i);

            if (isCancelled()) {
                outcome.messagesByInstanceId.put(instance.getDatabaseId().toString(),
                        Collect.getInstance().getString(R.string.instance_upload_cancelled));
                return outcome;
            }

            publishProgress(i + 1, instancesToUpload.size());

            // Get corresponding blank form and verify there is exactly 1
            FormsDao dao = new FormsDao();
            Cursor formCursor = dao.getFormsCursor(instance.getJrFormId(), instance.getJrVersion());
            List<Form> forms = dao.getFormsFromCursor(formCursor);

            if (forms.size() != 1) {
                outcome.messagesByInstanceId.put(instance.getDatabaseId().toString(),
                        Collect.getInstance().getString(R.string.not_exactly_one_blank_form_for_this_form_id));
            } else {
                try {
                    String destinationUrl = uploader.getUrlToSubmitTo(instance, null, null);
                    if (InstanceUploaderUtils.doesUrlRefersToGoogleSheetsFile(destinationUrl)) {
                        uploader.uploadOneSubmission(instance, destinationUrl);
                        outcome.messagesByInstanceId.put(instance.getDatabaseId().toString(), DEFAULT_SUCCESSFUL_TEXT);

                        analytics.logEvent(SUBMISSION, "HTTP-Sheets", Collect.getFormIdentifierHash(instance.getJrFormId(), instance.getJrVersion()));
                    } else {
                        outcome.messagesByInstanceId.put(instance.getDatabaseId().toString(), SPREADSHEET_UPLOADED_TO_GOOGLE_DRIVE);
                    }
                } catch (UploadException e) {
                    Timber.d(e);
                    outcome.messagesByInstanceId.put(instance.getDatabaseId().toString(),
                            e.getDisplayMessage());
                }
            }
        }
        return outcome;
    }
}