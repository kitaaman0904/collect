/*
 * Copyright 2018 Shobhit Agarwal
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

package org.odk.kitaaman.android.instrumented.settings;

import android.graphics.Bitmap;
import androidx.test.runner.AndroidJUnit4;

import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.WriterException;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.kitaaman.android.preferences.AdminSharedPreferences;
import org.odk.kitaaman.android.preferences.GeneralSharedPreferences;
import org.odk.kitaaman.android.preferences.PreferenceSaver;
import org.odk.kitaaman.android.preferences.qr.ObservableQRCodeGenerator;
import org.odk.kitaaman.android.preferences.qr.QRCodeGenerator;
import org.odk.kitaaman.android.utilities.QRCodeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.DataFormatException;

import timber.log.Timber;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.odk.kitaaman.android.preferences.GeneralKeys.DEFAULTS;
import static org.odk.kitaaman.android.preferences.GeneralKeys.KEY_SELECTED_GOOGLE_ACCOUNT;
import static org.odk.kitaaman.android.preferences.GeneralKeys.KEY_SHOW_SPLASH;
import static org.odk.kitaaman.android.preferences.GeneralKeys.KEY_USERNAME;

@RunWith(AndroidJUnit4.class)
public class QrCodeTest {

    private final GeneralSharedPreferences preferences = GeneralSharedPreferences.getInstance();
    private final QRCodeGenerator qrCodeGenerator = new ObservableQRCodeGenerator();

    @Test
    public void importSettingsFromQrCode() throws JSONException, IOException, WriterException, DataFormatException, ChecksumException, NotFoundException, FormatException {
        // reset preferences
        preferences.loadDefaultPreferences();

        // verify that the following preferences actually have default values
        String[] keys = {KEY_USERNAME, KEY_SELECTED_GOOGLE_ACCOUNT, KEY_SHOW_SPLASH};
        assertPreferenceHaveDefaultValue(keys, true);

        // updating the preferences
        preferences
                .save(KEY_USERNAME, "test_username")
                .save(KEY_SELECTED_GOOGLE_ACCOUNT, "test@email.com")
                .save(KEY_SHOW_SPLASH, true);

        // verify that preferences values have been modified
        assertPreferenceHaveDefaultValue(keys, false);

        // generate QrCode
        final AtomicReference<Bitmap> generatedBitmap = new AtomicReference<>();
        qrCodeGenerator.generateQRCode(new ArrayList<>())
                .subscribe(generatedBitmap::set, Timber::e);

        assertNotNull(generatedBitmap.get());

        // reset preferences
        preferences.loadDefaultPreferences();

        // verify again that preferences are actually reset to default
        assertPreferenceHaveDefaultValue(keys, true);

        // decode the generated bitmap
        String result = QRCodeUtils.decodeFromBitmap(generatedBitmap.get());
        assertNotNull(result);

        String resultIfAllSharedPreferencesAreDefault = "{\"general\":{},\"admin\":{}}";
        assertNotEquals(resultIfAllSharedPreferencesAreDefault, result);

        // update shared preferences using the QrCode
        new PreferenceSaver(GeneralSharedPreferences.getInstance(), AdminSharedPreferences.getInstance()).fromJSON(result, null);

        // assert that values have updated properly
        assertPreferenceHaveDefaultValue(keys, false);
        assertEquals("test_username", preferences.get(keys[0]));
        assertEquals("test@email.com", preferences.get(keys[1]));
        assertTrue((Boolean) preferences.get(keys[2]));
    }

    private void assertPreferenceHaveDefaultValue(String[] keys, boolean shouldBeDefault) {
        for (String key : keys) {
            if (shouldBeDefault) {
                assertEquals(DEFAULTS.get(key), preferences.get(key));
            } else {
                assertNotEquals(DEFAULTS.get(key), preferences.get(key));
            }
        }
    }
}
