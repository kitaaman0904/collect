/*
 * Copyright 2019 Nafundi
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

package org.odk.kitaaman.android.support;

import android.content.ContentValues;

import androidx.test.espresso.intent.rule.IntentsTestRule;

import org.javarosa.core.reference.ReferenceManager;
import org.odk.kitaaman.android.activities.FormEntryActivity;
import org.odk.kitaaman.android.dao.FormsDao;
import org.odk.kitaaman.android.provider.FormsProviderAPI.FormsColumns;
import org.odk.kitaaman.android.storage.StorageInitializer;
import org.odk.kitaaman.android.storage.StoragePathProvider;
import org.odk.kitaaman.android.storage.StorageSubdirectory;
import org.odk.kitaaman.android.tasks.FormLoaderTask;
import org.odk.kitaaman.android.utilities.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.odk.kitaaman.android.forms.FormUtils.setupReferenceManagerForForm;
import static org.odk.kitaaman.android.support.FileUtils.copyFileFromAssets;

public class FormLoadingUtils {

    public static final String ALL_WIDGETS_FORM = "all-widgets.xml";

    private FormLoadingUtils() {

    }

    /**
     * Copies a form with the given file name and given associated media to the SD Card where it
     * will be loaded by {@link FormLoaderTask}.
     */
    public static void copyFormToStorage(String formFilename, List<String> mediaFilePaths, boolean copyToDatabase) throws IOException {
        new StorageInitializer().createOdkDirsOnStorage();
        ReferenceManager.instance().reset();

        String pathname = copyForm(formFilename);
        if (mediaFilePaths != null) {
            copyFormMediaFiles(formFilename, mediaFilePaths);
        }

        if (copyToDatabase) {
            setupReferenceManagerForForm(ReferenceManager.instance(), FileUtils.getFormMediaDir(new File(pathname)));
            saveFormToDatabase(new File(pathname));
        }
    }

    /**
     * Copies a form with the given file name to the SD Card where it will be loaded by
     * {@link FormLoaderTask}.
     */
    public static void copyFormToStorage(String formFilename) throws IOException {
            copyFormToStorage(formFilename, null, false);
    }

    private static void saveFormToDatabase(File outFile) {
        Map<String, String> formInfo = FileUtils.getMetadataFromFormDefinition(outFile);
        final ContentValues v = new ContentValues();
        v.put(FormsColumns.FORM_FILE_PATH, new StoragePathProvider().getFormDbPath(outFile.getAbsolutePath()));
        v.put(FormsColumns.FORM_MEDIA_PATH, new StoragePathProvider().getFormDbPath(FileUtils.constructMediaPath(outFile.getAbsolutePath())));
        v.put(FormsColumns.DISPLAY_NAME, formInfo.get(FileUtils.TITLE));
        v.put(FormsColumns.JR_VERSION, formInfo.get(FileUtils.VERSION));
        v.put(FormsColumns.JR_FORM_ID, formInfo.get(FileUtils.FORMID));
        v.put(FormsColumns.SUBMISSION_URI, formInfo.get(FileUtils.SUBMISSIONURI));
        v.put(FormsColumns.BASE64_RSA_PUBLIC_KEY, formInfo.get(FileUtils.BASE64_RSA_PUBLIC_KEY));
        v.put(FormsColumns.AUTO_DELETE, formInfo.get(FileUtils.AUTO_DELETE));
        v.put(FormsColumns.AUTO_SEND, formInfo.get(FileUtils.AUTO_SEND));
        v.put(FormsColumns.GEOMETRY_XPATH, formInfo.get(FileUtils.GEOMETRY_XPATH));

        new FormsDao().saveForm(v);
    }

    public static IntentsTestRule<FormEntryActivity> getFormActivityTestRuleFor(String formFilename) {
        return new FormActivityTestRule(formFilename);
    }

    private static String copyForm(String formFilename) throws IOException {
        String pathname = new StoragePathProvider().getDirPath(StorageSubdirectory.FORMS) + "/" + formFilename;
        copyFileFromAssets("forms/" + formFilename, pathname);
        return pathname;
    }

    private static void copyFormMediaFiles(String formFilename, List<String> mediaFilePaths) throws IOException {
        String mediaPathName = new StoragePathProvider().getDirPath(StorageSubdirectory.FORMS) + "/" + formFilename.replace(".xml", "") + FileUtils.MEDIA_SUFFIX + "/";
        FileUtils.checkMediaPath(new File(mediaPathName));

        for (String mediaFilePath : mediaFilePaths) {
            copyFileFromAssets("media/" + mediaFilePath, mediaPathName + getMediaFileName(mediaFilePath));
        }
    }

    private static String getMediaFileName(String mediaFilePath) {
        return mediaFilePath.contains(File.separator)
                ? mediaFilePath.substring(mediaFilePath.indexOf(File.separator) + 1)
                : mediaFilePath;
    }
}
