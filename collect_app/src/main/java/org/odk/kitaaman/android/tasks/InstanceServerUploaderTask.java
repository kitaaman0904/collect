/*
 * Copyright (C) 2009 University of Washington
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

import org.odk.kitaaman.android.R;
import org.odk.kitaaman.android.analytics.Analytics;
import org.odk.kitaaman.android.application.Collect;
import org.odk.kitaaman.android.instances.Instance;
import org.odk.kitaaman.android.openrosa.OpenRosaHttpInterface;
import org.odk.kitaaman.android.logic.PropertyManager;
import org.odk.kitaaman.android.upload.InstanceServerUploader;
import org.odk.kitaaman.android.upload.UploadAuthRequestedException;
import org.odk.kitaaman.android.upload.UploadException;
import org.odk.kitaaman.android.utilities.WebCredentialsUtils;

import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import static org.odk.kitaaman.android.analytics.AnalyticsEvents.SUBMISSION;

/**
 * Background task for uploading completed forms.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class InstanceServerUploaderTask extends InstanceUploaderTask {
    @Inject
    OpenRosaHttpInterface httpInterface;

    @Inject
    WebCredentialsUtils webCredentialsUtils;

    @Inject
    Analytics analytics;

    // Custom submission URL, username and password that can be sent via intent extras by external
    // applications
    private String completeDestinationUrl;
    private String customUsername;
    private String customPassword;

    public InstanceServerUploaderTask() {
        Collect.getInstance().getComponent().inject(this);
    }

    @Override
    public Outcome doInBackground(Long... instanceIdsToUpload) {
        Outcome outcome = new Outcome();

        InstanceServerUploader uploader = new InstanceServerUploader(httpInterface, webCredentialsUtils, new HashMap<>());
        List<Instance> instancesToUpload = uploader.getInstancesFromIds(instanceIdsToUpload);

        String deviceId = new PropertyManager(Collect.getInstance().getApplicationContext())
                    .getSingularProperty(PropertyManager.withUri(PropertyManager.PROPMGR_DEVICE_ID));

        for (int i = 0; i < instancesToUpload.size(); i++) {
            if (isCancelled()) {
                return outcome;
            }
            Instance instance = instancesToUpload.get(i);

            publishProgress(i + 1, instancesToUpload.size());

            try {
                String destinationUrl = uploader.getUrlToSubmitTo(instance, deviceId, completeDestinationUrl);
                String customMessage = uploader.uploadOneSubmission(instance, destinationUrl);
                outcome.messagesByInstanceId.put(instance.getDatabaseId().toString(),
                        customMessage != null ? customMessage : Collect.getInstance().getString(R.string.success));

                analytics.logEvent(SUBMISSION, "HTTP", Collect.getFormIdentifierHash(instance.getJrFormId(), instance.getJrVersion()));
            } catch (UploadAuthRequestedException e) {
                outcome.authRequestingServer = e.getAuthRequestingServer();
                // Don't add the instance that caused an auth request to the map because we want to
                // retry. Items present in the map are considered already attempted and won't be
                // retried.
            } catch (UploadException e) {
                outcome.messagesByInstanceId.put(instance.getDatabaseId().toString(),
                        e.getDisplayMessage());
            }
        }
        
        return outcome;
    }

    @Override
    protected void onPostExecute(Outcome outcome) {
        super.onPostExecute(outcome);

        // Clear temp credentials
        clearTemporaryCredentials();
    }

    @Override
    protected void onCancelled() {
        clearTemporaryCredentials();
    }

    public void setCompleteDestinationUrl(String completeDestinationUrl) {
        setCompleteDestinationUrl(completeDestinationUrl, true);
    }

    public void setCompleteDestinationUrl(String completeDestinationUrl, boolean clearPreviousConfig) {
        this.completeDestinationUrl = completeDestinationUrl;
        if (clearPreviousConfig) {
            setTemporaryCredentials();
        }
    }

    public void setCustomUsername(String customUsername) {
        this.customUsername = customUsername;
        setTemporaryCredentials();
    }

    public void setCustomPassword(String customPassword) {
        this.customPassword = customPassword;
        setTemporaryCredentials();
    }

    private void setTemporaryCredentials() {
        if (customUsername != null && customPassword != null) {
            webCredentialsUtils.saveCredentials(completeDestinationUrl, customUsername, customPassword);
        } else {
            // In the case for anonymous logins, clear the previous credentials for that host
            webCredentialsUtils.clearCredentials(completeDestinationUrl);
        }
    }

    private void clearTemporaryCredentials() {
        if (customUsername != null && customPassword != null) {
            webCredentialsUtils.clearCredentials(completeDestinationUrl);
        }
    }
}