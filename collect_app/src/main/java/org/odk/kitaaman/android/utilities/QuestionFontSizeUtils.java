package org.odk.kitaaman.android.utilities;

import org.odk.kitaaman.android.preferences.GeneralSharedPreferences;

import static org.odk.kitaaman.android.preferences.GeneralKeys.KEY_FONT_SIZE;

public class QuestionFontSizeUtils {
    public static final int DEFAULT_FONT_SIZE = 21;

    private QuestionFontSizeUtils() {

    }

    public static int getQuestionFontSize() {
        try {
            return Integer.parseInt(String.valueOf(GeneralSharedPreferences.getInstance().get(KEY_FONT_SIZE)));
        } catch (Exception | Error e) {
            return DEFAULT_FONT_SIZE;
        }
    }
}
