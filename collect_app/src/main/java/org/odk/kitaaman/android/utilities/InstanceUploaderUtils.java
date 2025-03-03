/*
 * Copyright 2018 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.kitaaman.android.utilities;

import android.content.Context;
import android.database.Cursor;

import org.odk.kitaaman.android.R;
import org.odk.kitaaman.android.application.Collect;
import org.odk.kitaaman.android.dao.InstancesDao;
import org.odk.kitaaman.android.provider.InstanceProviderAPI.InstanceColumns;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class InstanceUploaderUtils {

    public static final String DEFAULT_SUCCESSFUL_TEXT = "full submission upload was successful!";
    public static final String SPREADSHEET_UPLOADED_TO_GOOGLE_DRIVE = "Failed. Records can only be submitted to spreadsheets created in Google Sheets. The submission spreadsheet specified was uploaded to Google Drive.";

    private InstanceUploaderUtils() {
    }

    /**
     * Returns a formatted message including submission results for all the filled forms accessible
     * through instancesProcessed in the following structure:
     *
     * Form name 1 - result
     *
     * Form name 2 - result
     */
    public static String getUploadResultMessage(Context context, Map<String, String> result) {
        Set<String> keys = result.keySet();
        Iterator<String> it = keys.iterator();

        StringBuilder message = new StringBuilder();
        int count = keys.size();
        while (count > 0) {
            String[] selectionArgs;

            if (count > ApplicationConstants.SQLITE_MAX_VARIABLE_NUMBER) {
                selectionArgs = new String[ApplicationConstants.SQLITE_MAX_VARIABLE_NUMBER];
            } else {
                selectionArgs = new String[count];
            }

            StringBuilder selection = new StringBuilder();
            selection.append(InstanceColumns._ID + " IN (");

            int i = 0;
            while (it.hasNext() && i < selectionArgs.length) {
                selectionArgs[i] = it.next();
                selection.append('?');

                if (i != selectionArgs.length - 1) {
                    selection.append(',');
                }
                i++;
            }

            selection.append(')');
            count -= selectionArgs.length;

            message.append(InstanceUploaderUtils
                    .getUploadResultMessageForInstances(new InstancesDao().getInstancesCursor(selection.toString(), selectionArgs), result));
        }

        if (message.length() == 0) {
            message = new StringBuilder(context.getString(R.string.no_forms_uploaded));
        }

        return message.toString().trim();
    }

    private static String getUploadResultMessageForInstances(Cursor instancesProcessed,
                                                             Map<String, String> resultMessagesByInstanceId) {
        StringBuilder queryMessage = new StringBuilder();
        try {
            if (instancesProcessed != null && instancesProcessed.getCount() > 0) {
                instancesProcessed.moveToPosition(-1);
                while (instancesProcessed.moveToNext()) {
                    String name =
                            instancesProcessed.getString(
                                    instancesProcessed.getColumnIndex(InstanceColumns.DISPLAY_NAME));
                    String id = instancesProcessed.getString(instancesProcessed.getColumnIndex(InstanceColumns._ID));
                    String text = localizeDefaultAggregateSuccessfulText(resultMessagesByInstanceId.get(id));
                    queryMessage
                            .append(name)
                            .append(" - ")
                            .append(text)
                            .append("\n\n");
                }
            }
        } finally {
            if (instancesProcessed != null) {
                instancesProcessed.close();
            }
        }
        return String.valueOf(queryMessage);
    }

    private static String localizeDefaultAggregateSuccessfulText(String text) {
        if (text != null && text.equals(DEFAULT_SUCCESSFUL_TEXT)) {
            text = Collect.getInstance().getString(R.string.success);
        }
        return text;
    }

    // If a spreadsheet is created using Excel (or a similar tool) and uploaded to GD it contains:
    // drive.google.com/file/d/ instead of docs.google.com/spreadsheets/d/
    // Such a file can't be used. We can write data only to documents generated via Google Sheets
    // https://forum.getodk.org/t/error-400-bad-request-failed-precondition-on-collect-to-google-sheets/19801/5?u=grzesiek2010
    public static boolean doesUrlRefersToGoogleSheetsFile(String url) {
        return !url.contains("drive.google.com/file/d/");
    }
}
