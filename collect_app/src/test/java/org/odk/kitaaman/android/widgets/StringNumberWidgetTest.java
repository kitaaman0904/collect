package org.odk.kitaaman.android.widgets;

import androidx.annotation.NonNull;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.data.StringData;
import org.odk.kitaaman.android.formentry.questions.QuestionDetails;
import org.junit.Test;
import org.odk.kitaaman.android.widgets.base.GeneralStringWidgetTest;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.when;
import static org.odk.kitaaman.android.utilities.WidgetAppearanceUtils.THOUSANDS_SEP;

/**
 * @author James Knight
 */
public class StringNumberWidgetTest extends GeneralStringWidgetTest<StringNumberWidget, StringData> {

    @NonNull
    @Override
    public StringNumberWidget createWidget() {
        return new StringNumberWidget(activity, new QuestionDetails(formEntryPrompt, "formAnalyticsID"), false);
    }

    @NonNull
    @Override
    public StringData getNextAnswer() {
        return new StringData(RandomString.make());
    }

    @Test
    public void digitsNumberShouldNotBeLimited() {
        getWidget().answerText.setText("123456789123456789123456789123456789");
        assertEquals("123456789123456789123456789123456789", getWidget().getAnswerText());
    }

    @Test
    public void separatorsShouldBeAddedWhenEnabled() {
        when(formEntryPrompt.getAppearanceHint()).thenReturn(THOUSANDS_SEP);
        getWidget().answerText.setText("123456789123456789123456789123456789");
        assertEquals("123,456,789,123,456,789,123,456,789,123,456,789", getWidget().answerText.getText().toString());
    }
}
