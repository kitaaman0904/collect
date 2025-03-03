package org.odk.kitaaman.android.widgets;

import android.view.View;
import android.widget.CheckBox;

import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.kitaaman.android.formentry.questions.QuestionDetails;
import org.odk.kitaaman.android.listeners.WidgetValueChangedListener;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verify;
import static org.odk.kitaaman.android.widgets.support.QuestionWidgetHelpers.mockValueChangedListener;
import static org.odk.kitaaman.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer;
import static org.odk.kitaaman.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnly;
import static org.odk.kitaaman.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;

@RunWith(RobolectricTestRunner.class)
public class TriggerWidgetTest {

    @Test
    public void getAnswer_whenPromptAnswerDoesNotHaveAnswer_returnsNull() {
        assertThat(createWidget(promptWithAnswer(null)).getAnswer(), nullValue());
    }

    @Test
    public void getAnswer_whenPromptHasAnswer_returnsAnswer() {
        TriggerWidget widget = createWidget(promptWithAnswer(new StringData("OK")));
        assertThat(widget.getAnswer().getDisplayText(), equalTo("OK"));
    }

    @Test
    public void clearAnswer_clearsWidgetAnswer() {
        TriggerWidget widget = createWidget(promptWithAnswer(new StringData("OK")));

        widget.clearAnswer();
        assertThat(widget.getAnswer(), nullValue());
    }

    @Test
    public void clearAnswer_callsValueChangeListeners() {
        TriggerWidget widget = createWidget(promptWithAnswer(null));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);

        widget.clearAnswer();
        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void usingReadOnlyOption_makesAllClickableElementsDisabled() {
        TriggerWidget widget = createWidget(promptWithReadOnly());
        assertThat(widget.getCheckBox().getVisibility(), equalTo(View.VISIBLE));
        assertThat(widget.getCheckBox().isEnabled(), equalTo(Boolean.FALSE));
    }

    @Test
    public void whenPromptAnswerDoesNotHaveAnswer_checkboxIsUnchecked() {
        TriggerWidget widget = createWidget(promptWithAnswer(null));
        assertThat(widget.getCheckBox().isChecked(), equalTo(false));
    }

    @Test
    public void whenPromptHasAnswer_checkboxIsChecked() {
        TriggerWidget widget = createWidget(promptWithAnswer(new StringData("OK")));
        assertThat(widget.getCheckBox().isChecked(), equalTo(true));
    }

    @Test
    public void checkingCheckbox_setsAnswer() {
        TriggerWidget widget = createWidget(promptWithAnswer(null));
        CheckBox triggerButton = widget.getCheckBox();

        triggerButton.setChecked(true);
        assertThat(widget.getAnswer().getDisplayText(), equalTo("OK"));

        triggerButton.setChecked(false);
        assertThat(widget.getAnswer(), nullValue());
    }

    @Test
    public void checkingCheckbox_callsValueChangeListeners() {
        TriggerWidget widget = createWidget(promptWithAnswer(null));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        CheckBox triggerButton = widget.getCheckBox();

        triggerButton.setChecked(true);
        verify(valueChangedListener).widgetValueChanged(widget);
    }

    private TriggerWidget createWidget(FormEntryPrompt prompt) {
        return new TriggerWidget(widgetTestActivity(), new QuestionDetails(prompt, "formAnalyticsID"));
    }
}
