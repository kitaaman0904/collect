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

import android.os.AsyncTask;
import androidx.annotation.Nullable;

import org.odk.kitaaman.android.listeners.FormListDownloaderListener;
import org.odk.kitaaman.android.logic.FormDetails;
import org.odk.kitaaman.android.utilities.FormListDownloader;

import java.util.HashMap;

/**
 * Background task for downloading forms from urls or a formlist from a url. We overload this task
 * a bit so that we don't have to keep track of two separate downloading tasks and it simplifies
 * interfaces. If LIST_URL is passed to doInBackground(), we fetch a form list. If a hashmap
 * containing form/url pairs is passed, we download those forms.
 *
 * @author carlhartung
 */
public class DownloadFormListTask extends AsyncTask<Void, String, HashMap<String, FormDetails>> {

    private final FormListDownloader formListDownloader;

    private FormListDownloaderListener stateListener;
    private String url;
    private String username;
    private String password;

    public DownloadFormListTask(FormListDownloader formListDownloader) {
        this.formListDownloader = formListDownloader;
    }

    @Override
    protected HashMap<String, FormDetails> doInBackground(Void... values) {
        return formListDownloader.downloadFormList(url, username, password, false);
    }

    @Override
    protected void onPostExecute(HashMap<String, FormDetails> value) {
        synchronized (this) {
            if (stateListener != null) {
                stateListener.formListDownloadingComplete(value);
            }
        }
    }

    public void setDownloaderListener(FormListDownloaderListener sl) {
        synchronized (this) {
            stateListener = sl;
        }
    }

    public void setAlternateCredentials(@Nullable String url, @Nullable String username, @Nullable String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

}
